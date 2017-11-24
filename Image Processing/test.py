import cv2
import numpy as np
import argparse

# import pil
# cap = cv2.VideoCapture(0)
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True,
	help="path to input image")
args = vars(ap.parse_args())
img = cv2.imread(args["image"])

# img = cv2.imread("222.jpg")
# size = 7016, 4961
# # im = Image.open("my_image.png")
# im_resized = im.resize(size, Image.ANTIALIAS)
# im_resized.save("my_image_resized.png", "PNG")

# while True:
    # _, img = cap.read()
# img = img.resize()
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
cv2.imshow("gray",gray)
laplacian = cv2.Laplacian(gray, cv2.CV_64F)
sobelx = cv2.Sobel(gray,cv2.CV_64F,1,0,ksize=5)
sobely = cv2.Sobel(gray,cv2.CV_64F,0,1,ksize=5)
sobelz = cv2.Sobel(gray,cv2.CV_64F,1,1,ksize=5)
edge = cv2.Canny(gray,100,200)
# while True:
# cv2.imshow('original',img)
cv2.imshow('Laplacian',laplacian)
cv2.imshow('sobelx',sobelx)
cv2.imshow('sobely',sobely)
cv2.imshow('sobelz',sobelz)
cv2.imshow('edge',edge)
# if cv2.waitKey() & 0xFF == ord('q')
cv2.waitKey(0)
cv2.destroyAllWindows()
# for i in range (1,5):
# k = cv2.waitKey(5) & 0xFF
# cv2.destroyAllWindows()
# cap.release()
