import pygame
from numpy import argmax
from keras.preprocessing.image import img_to_array
from keras.models import load_model
from torchvision import transforms
from PIL import Image
import matplotlib.pyplot as plt

from pygame.locals import (
    QUIT, 
    MOUSEBUTTONUP,
    MOUSEBUTTONDOWN,
    MOUSEMOTION,
)

model = load_model('C:\Dani\ML_Projects\Mnist_Dataset\CNN\\final_model.h5')
background_color = (0, 0, 0)

def predict(img):
    image = Image.fromarray(img)
    image = image.transpose(Image.ROTATE_270)
    image = image.transpose(Image.FLIP_LEFT_RIGHT)
    transform = transforms.Compose([
        transforms.Grayscale(),
        transforms.Resize((28, 28))
    ])

    transformed_img = transform(image)

    # convert img to array
    prep_img = img_to_array(transformed_img)
    prep_img = prep_img.reshape(1, 28, 28, 1)

    # prepare pixel data
    prep_img = prep_img.astype('float32') / 255.0

    # make prediction
    predict_value = model.predict(prep_img)
    digit = argmax(predict_value)
    print('Prediction: ', digit)

pygame.init()

screen = pygame.display.set_mode((150, 150))
screen.fill(background_color)

running = True
drawing = False
while running:

    for event in pygame.event.get():
        if event.type == QUIT:
            running = False

        # right mouse btn clears screen
        if event.type == MOUSEBUTTONDOWN and event.button == 3:
            screen.fill(background_color)

        if event.type == MOUSEBUTTONDOWN and event.button == 1:
            drawing = True

        if event.type == MOUSEBUTTONUP and event.button == 1:
            drawing = False 

            # pass drawing to model to predict
            img = pygame.surfarray.array3d(screen)
            predict(img)

        if event.type == MOUSEMOTION:
            if drawing:
                x, y = pygame.mouse.get_pos()

                for i in range(3):
                    screen.set_at((x, y), (255, 255, 255))
                    screen.set_at((x - i, y - i), (255, 255, 255))
                    screen.set_at((x + i, y + i), (255, 255, 255))

    pygame.display.flip()