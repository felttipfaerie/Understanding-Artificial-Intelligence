import csv
import os
from datetime import date

TODAY = date.today().isoformat()
CSV_FILE = f"orders_{TODAY}.csv"


class Tbl:
    def __init__(self, number, customers):
        self.number = number
        self.customers = customers


class Order:
    def __init__(self, customer_name, hoofdgerecht, extra, price):
        self.customer_name = customer_name
        self.hoofdgerecht = hoofdgerecht
        self.extra = extra
        self.price = price


class Menuitem:
    def __init__(self, item, price):
        self.item = item
        self.price = price


# Menu items
hg1 = Menuitem("Biefstuk", 25)
hg2 = Menuitem("Mosselen", 20)
hg3 = Menuitem("Scampis", 15)
hg4 = Menuitem("Quinoa met wintergroenten", 10)

ss1 = Menuitem("Geen", 0)
ss2 = Menuitem("Béarnaise", 3)
ss3 = Menuitem("Pepersaus", 2)
ss4 = Menuitem("Champignonroomsaus", 5)

sk1 = Menuitem("Natuur", 0)
sk2 = Menuitem("Look", 0)
sk3 = Menuitem("Pikant", 0)


# Create a daily CSV file once, and append to it on later runs.
if not os.path.exists(CSV_FILE):
    with open(CSV_FILE, "w", encoding="utf-8", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(["Date", TODAY])
        writer.writerow(["Table", "Customer", "Main dish", "Extra", "Price"])


def write_order_to_csv(table_number, order):
    with open(CSV_FILE, "a", encoding="utf-8", newline="") as file:
        writer = csv.writer(file)
        writer.writerow([
            table_number,
            order.customer_name,
            order.hoofdgerecht,
            order.extra,
            order.price
        ])


def get_int(prompt, valid_options):
    while True:
        try:
            choice = int(input(prompt))

            if choice in valid_options:
                return choice
            else:
                print(f"Please choose one of: {valid_options}")

        except ValueError:
            print("Must be a whole number!")


def askorder(customer_name):
    # Select main dish
    hoofdgerecht_choice = get_int(
        f"What would customer{customer_name} like to eat?\n"
        f"1: {hg1.item}\n"
        f"2: {hg2.item}\n"
        f"3: {hg3.item}\n"
        f"4: {hg4.item}\n"
        f"Choice: ",
        [1, 2, 3, 4]
    )

    if hoofdgerecht_choice == 1:
        hoofdgerecht = hg1.item
        price = hg1.price
    elif hoofdgerecht_choice == 2:
        hoofdgerecht = hg2.item
        price = hg2.price
    elif hoofdgerecht_choice == 3:
        hoofdgerecht = hg3.item
        price = hg3.price
    else:
        hoofdgerecht = hg4.item
        price = hg4.price

    # Beef sauce
    if hoofdgerecht_choice == 1:
        saus_choice = get_int(
            f"What sauce would {customer_name} like?\n"
            f"1: {ss1.item}\n"
            f"2: {ss2.item}\n"
            f"3: {ss3.item}\n"
            f"4: {ss4.item}\n"
            f"Choice: ",
            [1, 2, 3, 4]
        )

        if saus_choice == 1:
            extra = ss1.item
            price += ss1.price
        elif saus_choice == 2:
            extra = ss2.item
            price += ss2.price
        elif saus_choice == 3:
            extra = ss3.item
            price += ss3.price
        else:
            extra = ss4.item
            price += ss4.price

    # Mussels flavour
    elif hoofdgerecht_choice == 2:
        smaak_choice = get_int(
            f"What flavour would {customer_name} like?\n"
            f"1: {sk1.item}\n"
            f"2: {sk2.item}\n"
            f"3: {sk3.item}\n"
            f"Choice: ",
            [1, 2, 3]
        )

        if smaak_choice == 1:
            extra = sk1.item
        elif smaak_choice == 2:
            extra = sk2.item
        else:
            extra = sk3.item

    else:
        extra = "Geen"

    order = Order(customer_name, hoofdgerecht, extra, price)

    print("\nOrder:")
    if extra != "Geen":
        print(f"{order.customer_name}: {order.hoofdgerecht} met {order.extra}")
    else:
        print(f"{order.customer_name}: {order.hoofdgerecht}")

    print(f"Cost: €{order.price}\n")

    return order


def tableorder():
    while True:        
        try:
            table_number = int(input("What is the table number?: "))
            if table_number > 0:
                break
        except ValueError:
            print("Must be a whole number!")   

    while True:
        try:
            customers = int(input("How many customers?: "))
            if customers > 0:
                break
            else:
                print("Number of customers must be more than 0.")
        except ValueError:
            print("Must be a whole number!")

    tbl = Tbl(table_number, customers)

    total = 0

    print(f"\nOrder for table {tbl.number}\n")

    for customer_no in range(1, tbl.customers + 1):
        customer_name = customer_no

        order = askorder(customer_name)
        write_order_to_csv(tbl.number, order)

        total += order.price

    print(f"Total cost for table {tbl.number}: €{total}\n")

    # Optional: write table total row to CSV
    with open(CSV_FILE, "a", encoding="utf-8", newline="") as file:
        writer = csv.writer(file)
        writer.writerow([tbl.number, "", "", "", total])

    return total


def run_restaurant():
    grand_total = 0

    while True:
        grand_total += tableorder()

        while True:
            try:
                again = input("Do you want to enter another table? y/n: ").lower()

                if again in ["y", "n"]:
                    break

                print("Please enter 'y' or 'n'.")

            except ValueError:
                print("Invalid input. Please enter 'y' or 'n'.")

        if again != "y":
            break

    with open(CSV_FILE, "a", encoding="utf-8", newline="") as file:
        writer = csv.writer(file)
        writer.writerow([])
        writer.writerow(["", "", "", "ABSOLUTE TOTAL", grand_total])

    print(f"Orders exported to {CSV_FILE}")

    try:
        os.startfile(CSV_FILE)
    except AttributeError:
        print("CSV file created. Open output.csv manually.")


run_restaurant()