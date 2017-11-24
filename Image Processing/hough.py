import cv2
import numpy as np
import os

cap = cv2.VideoCapture(0)
# img = cv2.imread(cap)
while True:
    ret, img = cap.read()
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    circles = cv2.HoughCircles(gray, cv2.HOUGH_GRADIENT, 1.2, 100)
    if circles is not None:
        circles = np.round(circles[0, :]).astype("int")
        for (x,y,r) in circles:
            cv2.circle(img, (x, y), r, (0, 255, 0), 4)
            cv2.rectangle(img, (x - 5, y - 5), (x + 5, y + 5), (0, 128, 255), -1)
    # faces = face_cascade.detectMultiScale(gray,1.3,5)
    # for (x,y,w,h) in faces:
    #     cv2.rectangle(img,(x,y),(x+w,y+h),(255,0,0),2)
    #     rol_gray = gray[y:y+h,x:x+w]
    #     rol_color = img[y:y+h,x:x+w]
    #     #FaceFileName = "face_" + str(y) + ".jpg"

    cv2.imwrite('FaceFileName.jpg', img)
    cv2.imshow('img',img)
    k = cv2.waitKey(30) % 0xFF
    if k == 27:
        break;
cap.release()
cv2.destroyAllWindows()
