import cv2
import numpy
import argparse
import os
from PIL import Image
from resizeimage import resizeimage
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True,
	help="path to input image")
args = vars(ap.parse_args())
face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
eye_cascade = cv2.CascadeClassifier('haarcascade_eye.xml')
# cap = cv2.VideoCapture(0)
img = cv2.imread(args["image"])
while True:
	# ret, img = cap.read()
	gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
	# faces = face_cascade.detectMultiScale(gray,1.3,5)
	# for (x,y,w,h) in faces:
	    # cv2.rectangle(img,(x,y),(x+w,y+h),(255,0,0),2)
	# rol_gray = gray[y:y+h,x:x+w]
	# rol_color = img[y:y+h,x:x+w]
	    #FaceFileName = "face_" + str(y) + ".jpg"
	eyes = eye_cascade.detectMultiScale(gray,1.3,5)
	for (ex,ey,ew,eh) in eyes:
	    cv2.rectangle(gray,(ex,ey),(ex+ew,ey+eh),(0,255,0),2)
	    # eye1 = img[ey:ey+ew,ex:ex+eh]
	    # cv2.imshow('aa', eye1)
	# gray = resizeimage.resize_contain(gray, [200, 100])
	cv2.imwrite('FaceFileName.jpg', img)
	cv2.imshow('img',img)
	cv2.waitKey(0)
# k = cv2.waitKey(0) % 0xFF
# if k == 27:
#     cap.release()
#     cv2.destroyAllWindows()
