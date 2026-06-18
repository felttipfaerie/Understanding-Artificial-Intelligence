import tensorflow as tf

a = tf.ones((2, 2))
b = tf.square(a)                                                     #1
c = tf.sqrt(a)                                                       #2
d = b + c                                                            #3
e = tf.matmul(a, b)                                                  #4
f = tf.concat((a, b), axis=0)   

print(f)