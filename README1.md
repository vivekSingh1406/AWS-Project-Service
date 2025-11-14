#  Project flow
![Architecture!](https://github.com/vivekSingh1406/AWS-Service/blob/main/Example-S3-Lambda-RDS-Service/vivek-singh.png)


### Full Spring Boot application:

- Receives data via POST request (e.g., using Postman),
- Uploads that data to an S3 bucket in AWS,
- Triggers a Lambda function when new data is uploaded,
- The Lambda function then stores the data into an RDS (Relational Database Service) instance,
- And CloudWatch is used for logging


### Spring Boot Project (Java)

- REST Controller to receive data
- AWS SDK integration to upload to S3

### AWS Resources

- S3 Bucket
- Lambda Function
- IAM roles/policies
- RDS instance (e.g., MySQL/PostgreSQL)
- CloudWatch (automatic with Lambda)
