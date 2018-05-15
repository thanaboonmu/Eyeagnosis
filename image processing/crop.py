# -*- coding: utf-8 -*-
"""
Spyder Editor
Crop image into ideal ratio(800*600)
This is a temporary script file.
"""

from PIL import Image
import os
import shutil
import glob

path = '/Users/mike/Desktop/opencv-haar-classifier-training-master/negative_images'
i=0
for filename in glob.glob(os.path.join(path, '*.png')):
    

    image  = Image.open(filename)
    width  = image.size[0]
    height = image.size[1]

    aspect = width / float(height)
 
    ideal_width = 800
    ideal_height = 600
 
    ideal_aspect = ideal_width / float(ideal_height)
 
    if aspect > ideal_aspect:
        # Then crop the left and right edges:
        new_width = int(ideal_aspect * height)
        offset = (width - new_width) / 2
        resize = (offset, 0, width - offset, height)
    else:
        # ... crop the top and bottom:
        new_height = int(width / ideal_aspect)
        offset = (height - new_height) / 2
        resize = (0, offset, width, height - offset)
    print('before'+filename)
    thumb = image.crop(resize).resize((ideal_width, ideal_height), Image.ANTIALIAS)
    thumb.save('negative_crop'+str(i)+'.png')
    print('after'+filename)
    i=i+1

