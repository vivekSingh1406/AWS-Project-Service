
## Deploy Spring boot application on EC2
- Launch EC2 Instance
- Launch a new EC2 instance (Amazon Linux 2 or Ubuntu preferred)
- Select a key pair (or create one) for SSH access.
- Connect to EC2 via SSH(Use the following SSH command to connect to your instance)
```
  ssh -i /path/to/your-key.pem ec2-user@<public-ip-address>
```
- Since the project is hosted on GitHub, install the Git client 
```
sudo yum install git -y
```
- Clone the GitHub repository
```
git clone ---https-url----
cd your-repository-name
```
- install maven and java(EC2 machine to build or run application)
```
sudo yum install maven -y
```
- mvc clean package (package project in jar file)
```
java -jar target/project-name
```
- Go to the Security Group associated with your EC2 instance.
- Edit Inbound Rules and allow Custom TCP on port 8080 (or whichever your app uses) from Anywhere (0.0.0.0/0).
- Obtain the public IP address of your EC2 instance and use it to access your application in a web browser

    
# Spring Boot AWS-S3 File Upload Project

This project demonstrates how to upload files directly to Amazon S3 using a Spring Boot application. The application allows users to upload files through a web interface, and those files are then stored in an S3 bucket.

## Features
- Upload files directly to AWS S3 from a web interface.
- Spring Boot integration with AWS SDK.
- No need to store files locally; files are streamed directly to S3.

## Prerequisites
- Java 17 (or higher)
- AWS S3 Account with appropriate permissions

## Project Setup
### 1. Set Up AWS S3
1. **Create an S3 Bucket**:
    - Log in to your [AWS Management Console](https://aws.amazon.com/console/).
    - Navigate to **S3** and create a new bucket.
    - Make sure to note down the **bucket name** and the **region** where the bucket is created.

2. **IAM User & Credentials**:
    - Create an IAM user with `AmazonS3FullAccess` permissions or a custom policy that allows uploading files to S3.
    - Obtain the **AWS Access Key ID** and **AWS Secret Access Key** for the IAM user.

### 2. Configure the Project
You need to provide your AWS credentials and bucket information to the application. You can do this by modifying the `application.properties` file.

#### 2.1 Open `src/main/resources/application.properties` and configure it as follows:

```properties
# AWS S3 Configuration
cloud.aws.credentials.access-key=your-access-key-id
cloud.aws.credentials.secret-key=your-secret-key
```

Replace the values with:
- `your-access-key-id`: Your AWS Access Key.
- `your-secret-key`: Your AWS Secret Key.
- `your-region`: The AWS region where your S3 bucket is located (e.g., `us-east-1`).

##### createBucket, deleteBucket, deleteFile, uploadFile
```properties
# Get(createBucket)
http://localhost:8080/s3bucket/add/fist-bucket-vivek
# Del(deleteBucket)
http://localhost:8080/s3bucket/delete/bucket/fist-bucket-vivek
# Del(deleteFile)
http://localhost:8080/s3bucket/delete/file/fist-bucket-vivek/vivek_singh_bais.pdf
# Post(uploadFile)
http://localhost:8080/s3bucket/upload/file/fist-bucket-vivek
```

# Spring Boot AWS-RDS database
##### Create a RDS MySQL instance
- Use Free Tier
- Username will be 'admin' and you can set password(you can't use special character)
- Keep the public access to true to access it from local or remote server
- Create a security group(and allow 3306 from everywhere
- After creating, you can find endpoint(hostname) to connect to this DB
  ```
  url: jdbc:mysql://<rds-endpoint>:3306/<database-name>
  username: <username>
  password: <password>
  driver-class-name: com.mysql.cj.jdbc.Driver
  ```

  
# Spring Boot SQS Integration: Message Publisher and Consumer

- I have created an SQS queue using the AWS Console by navigating to Amazon SQS and clicking "Create queue."
- I have developed two Spring Boot applications — one acts as a producer and the other as a consumer.
- In the producer application, I created an API that accepts key-value JSON data such as:
  ```
  {
    "type" : "deep",
    "description" : "i am java full stack developer"
  }
  ```
- We are using Spring Scheduler to poll messages from the AWS SQS queue.
- After pulling a message from the queue, we must handle it properly by acknowledging the message and sending a delete request using the message's receipt handle.
- If we do not delete the message after processing, it will remain in the queue and may be delivered again, which can lead to message duplication.
  ```properties
  
  app.config.message.queue.topic=my-sqs-queue
  app.config.aws.access_key_id=YOUR_AWS_ACCESS_KEY
  app.config.aws.secret_key_id=YOUR_AWS_SECRET_KEY
  ```

---
---
# Project-1 :- Example-S3-Lambda-RDS-Service
![Architecture!](https://github.com/vivekSingh1406/AWS-Project-Service/blob/main/Example-S3-Lambda-RDS-Service/vivek-singh.png)


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

# Project-2 :- Example-S3-Lambda-CloudFront-DynamoDB-Service
![Architecture!](https://github.com/vivekSingh1406/AWS-Project-Service/blob/main/Example-S3-Lambda-CloudFront-DynamoDB-Service/project.png)

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
