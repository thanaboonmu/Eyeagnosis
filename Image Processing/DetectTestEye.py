# USAGE
# python detect_face_parts.py --shape-predictor shape_predictor_68_face_landmarks.dat --image images/example_01.jpg

# import the necessary packages
from imutils import face_utils
import numpy as np
import argparse
import imutils
import dlib
import cv2
from PIL import Image
from PIL import ImageEnhance
# construct the argument parser and parse the arguments
# def testFunction:

ap = argparse.ArgumentParser()
ap.add_argument("-p", "--shape-predictor", required=True,
	help="path to facial landmark predictor")
ap.add_argument("-i", "--image", required=True,
	help="path to input image")
args = vars(ap.parse_args())

# initialize dlib's face detector (HOG-based) and then create
# the facial landmark predictor
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor(args["shape_predictor"])

# load the input image, resize it, and convert it to grayscale
image = cv2.imread(args["image"])
image = imutils.resize(image, width=500)
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# detect faces in the grayscale image
rects = detector(gray, 3)

# loop over the face detections
# for (i, rect) in enumerate(rects):
	# determine the facial landmarks for the face region, then
	# convert the landmark (x, y)-coordinates to a NumPy array
shape = predictor(gray, rects)
shape = face_utils.shape_to_np(shape)

	# loop over the face parts individually
for (name, (i, j)) in face_utils.FACIAL_LANDMARKS_IDXS.items():
	if name is not "right_eye":
		continue
	# clone the original image so we can draw on it, then
	# display the name of the face part on the image
	clone = image.copy()
	cv2.putText(clone, name, (10, 30), cv2.FONT_HERSHEY_SIMPLEX,
			0.7, (0, 0, 255), 2)
	# loop over the subset of facial landmarks, drawing the
	# specific face part
	for (x, y) in shape[i:j]:
		cv2.circle(clone, (x, y), 1, (0, 0, 255), 2)

	# extract the ROI of the face region as a separate image
		(x, y, w, h) = cv2.boundingRect(np.array([shape[i:j]]))
		roi = image[y:y + h, x:x + w]
		roi = imutils.resize(roi, width=250, inter=cv2.INTER_CUBIC)
		# Image.fromarray()
		# enhancer_object = ImageEnhance.Contrast(roi)
		# out = enhancer_object.enhance(0.7)

		# show the particular face part
	for (name, (i, j)) in face_utils.FACIAL_LANDMARKS_IDXS.items():
		if name is not "left_eye":
			continue
			# clone the original image so we can draw on it, then
			# display the name of the face part on the image
		clone2 = image.copy()
		cv2.putText(clone2, "left_eye and right_eye", (10, 30), cv2.FONT_HERSHEY_SIMPLEX,0.7, (0, 0, 255), 2)
		# loop over the subset of facial landmarks, drawing the
		# specific face part
		for (x, y) in shape[i:j]:
			cv2.circle(clone, (x, y), 1, (0, 0, 255), 2)
			# extract the ROI of the face region as a separate image
			(x, y, w, h) = cv2.boundingRect(np.array([shape[i:j]]))
			roi = image[y:y + h, x:x + w]
			roi = imutils.resize(roi, width=250, inter=cv2.INTER_CUBIC)

			# Image.fromarray()
			# enhancer_object = ImageEnhance.Contrast(roi)
			# out = enhancer_object.enhance(0.7)

			# show the particular face part
cv2.imshow("ROI", roi)
cv2.imwrite("~/Deskstop/result2.jpg",roi)
Image1 = Image.open("result.jpg")
enhancer_object = ImageEnhance.Contrast(Image1)
out = enhancer_object.enhance(0.7)
out.save("test.jpg")
# cv2.imshow("ROI after",out)
cv2.imshow("Image", clone)
cv2.imwrite("FinalToe.jpg",clone)
cv2.waitKey(0)
	# visualize all facial landmarks with a transparent overlay
output = face_utils.visualize_facial_landmarks(image, shape)
cv2.imshow("Image", output)
cv2.imwrite("~/Deskstop/result.jpg",output)
cv2.waitKey(0)
