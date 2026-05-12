# Version 1.02 (Alpha)
- ID now shows in LowStockReport page properly
- Subtotals for orders now display correctly


# Project Overview
This application is a robust Grocery/Product Management System designed 
to handle inventory tracking, supplier management, and automated stock reporting. It provides a JavaFX-based graphical interface for warehouse managers 
to monitor stock levels, manage perishable batches, and generate low stock reports to streamline the reordering process. 

# HOW TO RUN


## Setup
1. Clone the repo
2. Open IntelliJ IDEA and select Open
3. Navigate to the project folder and open OR File > New > Project from Version Control and paste this into "Repository URL": 
https://github.com/Grocery-Management-System/Grocery-Management and create a new folder to locate the project
4. Go to File > Project Structure > Project
5. Ensure the SDK is set to 25 and Language Level is set to 21
6. If the project fails to compile, ensure the JavaFX library is added to your Global Libraries
7. Check that the module-info.java correctly exports the US packages

## Database Configuration: 
This project requires a local MySQL instance. Follow these steps to connect the application to your database: 


1. Install MySQL
2. Create username + password in MySQL
3. Open Run > Edit Configurations
4. Select Add new... Application
5. Ensure Java 25 is selected
6. In main class, paste this:
grocery.system.model.Main

7. For security, the application retrieves database credentials via environment variables. You must set these on your system for the connection to succeed:

- DB_USER: Your MySQL username
- DB_PASSWORD: Your MySQL password

Example: DB_USER=Joolian;DB_PASSWORD=Simmons

No spaces or quotes are allowed

## Run the Application

Once the database is live and environment variables are set: 

1. Locate the Main.java file in the src directory. 
2. Right-click and select Run 'Main.main()' or press the green arrow on the top bar to run Main.main()
3. The Home screen should appear if the database connection is successful. 


## Requirements & Dependencies:
To run this project, ensure your environment meets the following specifications: 

Project SDK: openjdk-25

Language Level: 21 (Utilizes Record patterns and pattern matching for switches)

Framework: JavaFX

Database: MySQL Server 8.0+

IDE: IntelliJ IDEA (Recommended)

### Important: If project doesn't run, make sure to check JavaFX is properly configured in your IDE
