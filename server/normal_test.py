import sys
import cv2

response = {}
response["imgUrl"] = sys.argv[1]
response["disease"] = "Healthy"
response["possibility"] = 0.92
response["recommendation"] = "Congratulations! Your eyes are fine for now"

print(response)

