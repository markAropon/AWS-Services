# AWS Services {Textraction and Rekognition}

Spring Boot activity that extracts structured data from receipt images using Amazon Textract OCR service and Rekognition.

## Demo for textraction

[View Video Demo For Textraction](https://screenrec.com/share/aTUEz4nySR)

## Demo for Rekognition

[View Video Demo For Rekognition](https://screenrec.com/share/zK9bSfr63D)

## Features

- Extract receipt data from uploaded images
- Parse company name, branch, manager, cashier information
- Extract itemized products with quantities and prices
- Calculate subtotal, cash, and change amounts
- Store receipt data in MySQL database
- RESTful API with Swagger documentation
- Global exception Handlin

- Rekognition Service used to recognize famous celebrities in the world

## Technologies

- Java 21
- Spring Boot
- Spring Data JPA
- MySQL Database
- AWS Textract SDK
- AWS Rekognition SDK
- Lombok
- Springdoc OpenAPI (Swagger)
- Maven

## Setup

1. Clone the repository
2. Configure MySQL database:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost/textract
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. Configure AWS credentials in your environment or AWS credentials file

4. Run the application:

   ```bash
   mvn spring-boot:run
   ```

5. Access Swagger UI: http://localhost:8082/swagger-ui.html

## API Endpoints

### Extract Receipt Data

```
POST /api/v1/textract/extract
Content-Type: multipart/form-data
Parameter: file (image file)
```

### Get Receipt by ID

```
GET /api/v1/textract/receipt/{id}
```

### Post Recognize Celebrities

````
POST api/v1/rekognition/celebrities
Content-Type: multipart/form-data
Parameter: file (image file)


## Sample Response for textraction

```json
{
  "httpStatus": "200 OK",
  "success": true,
  "message": "Receipt data extracted and saved successfully",
  "data": {
    "id": 1,
    "companyName": "SM HYPERMARKET",
    "branch": "Quezon City",
    "managerName": "Eric Steer",
    "cashierNumber": "#3",
    "items": [
      {
        "id": 1,
        "productName": "Apple",
        "quantity": 1,
        "price": 9.2
      },
      {
        "id": 2,
        "productName": "Gardenia",
        "quantity": 1,
        "price": 19.2
      }
    ],
    "subTotal": 107.6,
    "cash": 200.0,
    "change": 92.4
  }
}
````

## Sample Response for Rekognition

```json
{
  "celebrities": [
    {
      "name": "Park Shin-hye",
      "id": "3JN9ir7l",
      "confidence": 99.99869,
      "urls": ["www.wikidata.org/wiki/Q497785", "www.imdb.com/name/nm2339975"],
      "boundingBox": {
        "width": 0.42160946,
        "height": 0.27741024,
        "left": 0.24580704,
        "top": 0.27954635
      }
    }
  ],
  "unrecognizedFaces": 1
}
```

### Receipts Table

- id (Long, Primary Key)
- company_name (String)
- branch (String)
- manager_name (String)
- cashier_number (String)
- sub_total (Double)
- cash (Double)
- change_amount (Double)

### Receipt Items Table

- id (Long, Primary Key)
- product_name (String)
- quantity (Integer)
- price (Double)
- receipt_id (Foreign Key)

## Configuration

Application runs on port 8082 by default. Database tables are created automatically on startup.
