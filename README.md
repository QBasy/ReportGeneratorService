# Report Generator 
## Made By Sayat Adilkhanov

---

### Written on Java 
### Dependencies:
The project uses the following dependencies

```groovy
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-web-services'

    implementation 'net.sf.jasperreports:jasperreports:6.20.0'
    implementation 'com.lowagie:itext:2.1.7'

    runtimeOnly 'org.postgresql:postgresql'
```

---

## Main Endpoints

### The service exposes the following endpoints under the /api/xml-generator base path:
```
    /sendXML [POST]
```

Generates a .jrxml file and sends it as an attachment.

Parameters:
    Query Parameters (optional):
        tableName: The name of the database table to generate the report for.
        columns: A comma-separated list of column names to include in the report.
    Body (optional):
        A custom SQL query to generate the .jrxml file.
    
### Example Usage:

```bash
# Generate a JRXML file for a specific table and columns:
curl -X POST "http://localhost:8080/api/xml-generator/sendXML?tableName=users&columns=id,name" --output report.jrxml

# Generate a JRXML file from a custom SQL query:
curl -X POST -H "Content-Type: text/plain" -d "SELECT id, name FROM users" http://localhost:8080/api/xml-generator/sendXML --output report.jrxml
```

```
    /generate [POST]
```
Generates a PDF report by creating and compiling a .jrxml file into PDF.

Parameters:
    Query Parameters (optional):
        tableName: The name of the database table to generate the report for.
        columns: A comma-separated list of column names to include in the report.
    Body (optional):
        A custom SQL query to generate the report.

---

## Project Structure:
### controller: 
        Contains the REST controller handling the endpoints.
### service: 
        Includes business logic for generating JRXML and PDF files.
### repository: 
        Manages database interactions using JPA.
### config: 
        Contains configuration files for PostgreSQL and application settings.

---

## How to Run:
1. Clone the repository and navigate to the project directory:
```bash
    git clone <repository_url>
    cd report-generator
```

2. Configure the application.properties file to point to your PostgreSQL database:
```properties
    spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
    spring.datasource.username=<username>
    spring.datasource.password=<password>
```

3. Build and run the application:
```bash
./gradlew bootRun
```

4. Access the service at ```http://localhost:8080/api/xml-generator.```
