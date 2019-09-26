University Project
Webtech-2 Module
TU Dortmund

How to deploy:

Prerequisite:
	1. MySQL Server
	2. WildFly 10 reachable on address http://localhost:8080
	3. NodeJS (npm)
	4. Angular CLI version >= 1.0.1
	5. TypeScript version >= 2.2.0


Installation steps:
	1. Execute SQL Script on database: quickquack-sql/create_db_and_tables.sql
	2. Merge quickquack-sql/wildfly/modules with your local WildFly installation directory to install MySQL driver
	3. Copy the lines 151-164 from quickquack-sql/wildfly/standalone/configuration/standalone.xml to your local standalone.xml file under the <datasources> section
	4. Update the copied <user-name> and <password> sections with your MySQL database credentials
	5. Copy the lines 169-171 from quickquack-sql/wildfly/standalone/configuration/standalone.xml to your local standalone.xml file under the <drivers> section


Building and starting the server:
	1. Run the command 'npm install' inside the folder quickquack-web
	2. Run the command 'mvn install' inside the root folder (quickquack)
	3. Run the command 'mvn wildfly:deploy' inside the folder quickquack-rest, or manually copy quickquack-rest/quickquack.war into standalone/deployments of your WildFly folder.
	4. Launch the website: http://localhost:8080/quickquack
	5. Login with 'superadmin@quickquack.com' as eMail and 'superadmin' as password.
