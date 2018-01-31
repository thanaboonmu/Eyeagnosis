import sys
import cv2

response = {}
response["imgUrl"] = sys.argv[1]
response["disease"] = "Retinoblastoma"
response["possibility"] = 0.9
response["severity"] = 4

print(response)
