#!/usr/bin/env python2
# -*- coding: utf-8 -*-
"""
Created on Mon May 14 21:33:14 2018
Convert xxx into png
@author: mike
"""

from PIL import Image
import os
import glob
path = '/Users/mike/Desktop/opencv-haar-classifier-training-master/negative_images'
i=0
for filename in glob.glob(os.path.join(path, '*.tif')):
    print(filename)
    im = Image.open(filename)
    print('before '+filename)
    im=im.convert('RGB')
    im.save('3negative'+str(i)+'.png')
    print('after '+filename)
    i=i+1