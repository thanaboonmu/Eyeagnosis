import cv2
import argparse
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", required=True,help="path to input image")
args = vars(ap.parse_args())
image = cv2.imread(args["image"])
num = cv2.Laplacian(image, cv2.CV_64F).var()
cv2.putText(image, "{:.2f}".format(num), (10, 30),cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 3)
cv2.imshow("a",image)
cv2.waitKey(0)
