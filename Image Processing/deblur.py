import numpy as np
#import matplotlib.pyplot as plt
from scipy.misc import imfilter, imread
from skimage import color, data, restoration
from scipy.signal import convolve2d as conv2
import matplotlib.pyplot as plt

image = imread("result.jpg")
#plt.imshow(arr, cmap='gray')#plt.show()
#blurred_arr = imfilter(arr, "blur")
# image = image.sum(-1)
gray = color.rgb2gray(image)
# image.show()
# m,n = 5,5
# matrix = [ [ 0 for i in range(n) ] for j in range(m) ]
# matrix.append(1)
psf = np.ones((1, 1))/1
# print (matrix) # image = conv2(image, psf, 'same')
# image += 0.1 * image.std() * np.random.standard_normal(image.shape)
deconvolved = restoration.wiener(gray, psf,1,clip = False)
# deconvolved = restoration.richardson_luc(gray, psf)
#print deconvolved
fig = plt.subplot()
plt.gray()
fig.imshow(deconvolved)
# ax.axis('off')
plt.show()
