import numpy as np

import matplotlib.pyplot as plt

num_samples_per_class = 1000
negative_samples = np.random.multivariate_normal(
    mean=[0, 3], cov=[[1, 0.5], [0.5, 1]], size=num_samples_per_class    #1
)                                                                         #1
positive_samples = np.random.multivariate_normal(
    mean=[3, 0], cov=[[1, 0.5], [0.5, 1]], size=num_samples_per_class    #2
) 

# Listing 3.14 Stacking the two classes into an array with shape (2000, 2)

inputs = np.vstack((negative_samples, positive_samples)).astype(np.float32)

# Listing 3.15 Generating the corresponding targets (0 and 1)

targets = np.vstack(
    (
        np.zeros((num_samples_per_class, 1), dtype="float32"),
        np.ones((num_samples_per_class, 1), dtype="float32"),
    )
)

# Listing 3.16 Plotting the two point classes

plt.scatter(inputs[:, 0], inputs[:, 1], c=targets[:, 0])
plt.show()

# Listing 3.17 Creating the linear classifier variables
input_dim = 2                                                        #1
output_dim = 1                                                       #2
W = tf.Variable(initial_value=tf.random.uniform(shape=(input_dim, output_dim)))
b = tf.Variable(initial_value=tf.zeros(shape=(output_dim,)))

# Listing 3.18 The forward pass function

def model(inputs, W, b):
    return tf.matmul(inputs, W) + b


# Listing 3.19 The mean squared error loss function

def mean_squared_error(targets, predictions):
    per_sample_losses = tf.square(targets - predictions)                 #1
    return tf.reduce_mean(per_sample_losses)          

# Listing 3.20 The training-step function

learning_rate = 0.1

@tf.function(jit_compile=True)                                       #1
def training_step(inputs, targets, W, b):
    with tf.GradientTape() as tape:                                   #2
        predictions = model(inputs, W, b)                              #2
        loss = mean_squared_error(predictions, targets)                #2
    grad_loss_wrt_W, grad_loss_wrt_b = tape.gradient(loss, [W, b])    #3
    W.assign_sub(grad_loss_wrt_W * learning_rate)                     #4
    b.assign_sub(grad_loss_wrt_b * learning_rate)                      #4
    return loss

x = np.linspace(-1, 4, 100)           #1                                 
y = -W[0] / W[1] * x + (0.5 - b) / W[1]         #2                       
plt.plot(x, y, "-r")                                                    #3
plt.scatter(inputs[:, 0], inputs[:, 1], c=predictions[:, 0] > 0.5)     #4