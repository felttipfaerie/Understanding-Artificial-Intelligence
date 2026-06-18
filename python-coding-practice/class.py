class XYZ:
    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z


Alfa = XYZ(1, 2, 3)
print(Alfa.x, Alfa.y, Alfa.z)
Beta = XYZ(4, 5, 6)
print(Beta.x, Beta.y, Beta.z)



class Child:
    def __init__(self, name, age, mother, father):
        self.name = name
        self.age = age
        self.mother = mother
        self.father = father

Child1 = Child("Alice", 10, "Mary", "John")
Child2 = Child("Bob", 12, "Susan", "Michael")
Child3 = Child("Charlie", 8, "Linda", "David")
print(Child1.name, Child1.age, Child1.mother, Child1.father)
print(Child2.name, Child2.age, Child2.mother, Child2.father)
print(Child3.name, Child3.age, Child3.mother, Child3.father)
print(Child1.__dict__)  