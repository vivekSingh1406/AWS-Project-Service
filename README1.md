# 1 - Project:- Example-S3-Lambda-RDS-Service
![Architecture!](https://github.com/vivekSingh1406/AWS-Project/blob/main/Example-S3-Lambda-RDS-Service/vivek-singh.png)


### Full Spring Boot application

- Receives data via POST request (e.g., using Postman),
- Uploads that data to an S3 bucket in AWS,
- Triggers a Lambda function when new data is uploaded,
- The Lambda function then stores the data into an RDS (Relational Database Service) instance,
- And CloudWatch is used for logging


### Spring Boot Project

- REST Controller to receive data
- AWS SDK integration to upload to S3

### AWS Resources

- S3 Bucket
- Lambda Function
- IAM roles/policies
- RDS instance (e.g., MySQL/PostgreSQL)
- CloudWatch (automatic with Lambda)

# 2 - Project:- Example-S3-Lambda-CloudFront-DynamoDB-Service
![Architecture!](https://github.com/vivekSingh1406/AWS-Project/blob/main/Example-S3-Lambda-CloudFront-DynamoDB-Service/project.png)

### Full Serverless Web Application

- User → CloudFront → S3 → API Gateway → Lambda (GET/POST) → DynamoDB
- A user accesses a frontend web app (HTML/React/Angular) through CloudFront.
- The frontend is hosted in an S3 bucket and delivered globally via CloudFront.
- The frontend makes API calls to API Gateway, which acts as the entry point for backend requests.
- API Gateway invokes AWS Lambda functions:
    - GET Lambda → fetches data from DynamoDB.
    - POST Lambda → stores new data into DynamoDB.
- DynamoDB is used as the backend database (NoSQL, fully managed).
- CloudWatch (not shown in diagram but always active) monitors logs and metrics for Lambda/API Gateway.

### Frontend (Static Website)

- Technology: Plain HTML/JS.
- Hosting: Uploaded to an S3 bucket and distributed through CloudFront.
- What it does: Displays UI, makes API calls (GET/POST) to backend via API Gateway.
