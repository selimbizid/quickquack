<h3>University Project</h3><br>
<h4>Webtech-2 Module</h4><br>
<h4>TU Dortmund</h4><br>

How to deploy:<br>

Prerequisite:<br>
	1. MySQL Server<br>
	2. WildFly 10 reachable on address http://localhost:8080<br>
	3. NodeJS (npm)<br>
	4. Angular CLI version >= 1.0.1<br>
	5. TypeScript version >= 2.2.0<br>


Installation steps:<br>
	1. Execute SQL Script on database: quickquack-sql/create_db_and_tables.sql<br>
	2. Merge quickquack-sql/wildfly/modules with your local WildFly installation directory to install MySQL driver<br>
	3. Copy the lines 151-164 from quickquack-sql/wildfly/standalone/configuration/standalone.xml to your local standalone.xml file under the <datasources> section<br>
	4. Update the copied <user-name> and <password> sections with your MySQL database credentials<br>
	5. Copy the lines 169-171 from quickquack-sql/wildfly/standalone/configuration/standalone.xml to your local standalone.xml file under the <drivers> section<br>


Building and starting the server:<br>
	1. Run the command 'npm install' inside the folder quickquack-web<br>
	2. Run the command 'mvn install' inside the root folder (quickquack)<br>
	3. Run the command 'mvn wildfly:deploy' inside the folder quickquack-rest, or manually copy quickquack-rest/quickquack.war into standalone/deployments of your WildFly folder.<br>
	4. Launch the website: http://localhost:8080/quickquack<br>
	5. Login with 'superadmin@quickquack.com' as eMail and 'superadmin' as password.<br>
