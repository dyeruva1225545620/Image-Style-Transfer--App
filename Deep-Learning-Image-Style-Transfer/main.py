import torch
import torch.nn as nn
import torch.optim as optim
import torchvision.transforms as transforms
from torchvision.models import vgg19
from PIL import Image
import io
import base64
from flask import Flask, request, jsonify

app = Flask(__name__)

# Load pre-trained VGG19 model
vgg = vgg19(pretrained=True).features.eval()

# Define content and style layers
content_layers = ['conv_4']
style_layers = ['conv_1', 'conv_2', 'conv_3', 'conv_4', 'conv_5']

def image_to_tensor(image):
    transform = transforms.Compose([
        transforms.Resize((512, 512)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    return transform(image).unsqueeze(0)

def tensor_to_image(tensor):
    tensor = tensor.squeeze(0)
    tensor = tensor * torch.tensor([0.229, 0.224, 0.225]).view(3, 1, 1) + torch.tensor([0.485, 0.456, 0.406]).view(3, 1, 1)
    tensor = tensor.clamp(0, 1)
    return transforms.ToPILImage()(tensor)

def style_transfer(content_img, style_img, num_steps=300):
    content_tensor = image_to_tensor(content_img)
    style_tensor = image_to_tensor(style_img)
    input_tensor = content_tensor.clone()

    optimizer = optim.LBFGS([input_tensor.requires_grad_()])

    for step in range(num_steps):
        def closure():
            optimizer.zero_grad()
            loss = calculate_loss(input_tensor, content_tensor, style_tensor)
            loss.backward()
            return loss

        optimizer.step(closure)

    return tensor_to_image(input_tensor)

def calculate_loss(input_tensor, content_tensor, style_tensor):
    return nn.MSELoss()(input_tensor, content_tensor) + nn.MSELoss()(input_tensor, style_tensor)

@app.route('/stylize', methods=['POST'])
def stylize_image():
    content_image = request.files['content_image']
    style_image = request.files['style_image']

    content_img = Image.open(io.BytesIO(content_image.read()))
    style_img = Image.open(io.BytesIO(style_image.read()))

    stylized_image = style_transfer(content_img, style_img)

    buffered = io.BytesIO()
    stylized_image.save(buffered, format="PNG")
    stylized_image_base64 = base64.b64encode(buffered.getvalue()).decode('utf-8')

    return jsonify({'stylized_image': stylized_image_base64})

@app.route('/')
def home():
    return 'Style Transfer API is running!'

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
