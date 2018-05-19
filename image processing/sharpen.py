#!/usr/bin/env python2
# -*- coding: utf-8 -*-
"""
Created on Thu May 17 17:19:35 2018

@author: mike
"""

import cv2
import numpy as np


from PIL import Image
import os
import shutil
import glob

path = '/Users/mike/Desktop/Dataset/Pterygium'
i=0
for filename in glob.glob(os.path.join(path, '*.png')):
    image  = cv2.imread(filename)
    name = "pterygium" + str(i) + ".png"
    # Create our shapening kernel, it must equal to one eventually
    kernel_sharpening = np.array([[-1,-1,-1], 
                              [-1, 9,-1],
                              [-1,-1,-1]])
    # applying the sharpening kernel to the input image & displaying it.
    sharpened = cv2.filter2D(image, -1, kernel_sharpening)

       # im = Image.fromarray(eye)
       # im.save(name)
    print(i)
    i += 1
    cv2.imwrite(name,sharpened)