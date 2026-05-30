w = 0.2
a = 0.9
x = 0.1
lr = 10

for i in range(100):
    p = w * x
    l = (a - p) ** 2
    g = -2 * x * (a - p)
    w = w - lr * g
    print(f"iteration={i + 1}, loss = {l}, predicted = {p}, actual = {a}, gradient = {g}, weight = {w}")