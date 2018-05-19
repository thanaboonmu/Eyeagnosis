#!/usr/bin/env python2
# -*- coding: utf-8 -*-
"""
Created on Tue May 15 15:41:35 2018
flip image
@author: mike
"""

import cv2
import numpy as np


from PIL import Image
import os
import shutil
import glob

path = '/Users/mike/Desktop/Dataset/Sharpen/Pterygium'
i=0
for filename in glob.glob(os.path.join(path, '*.png')):
    image  = cv2.imread(filename)
    
    horizontal_img = image.copy()
    vertical_img = image.copy()
    both_img = image.copy()
    
    horizontal_img = cv2.flip(image, 0)
    vertical_img = cv2.flip(image, 1)
    both_img = cv2.flip(image, -1)

       # im = Image.fromarray(eye)
       # im.save(name)
    print(i)
    i += 1
    name = "2pterygium" + str(i) + ".png"
    cv2.imwrite(name,horizontal_img)
    i += 1
    name = "2pterygium" + str(i) + ".png"
    cv2.imwrite(name,vertical_img)
    i += 1
    name = "2pterygium" + str(i) + ".png"
    cv2.imwrite(name,both_img)