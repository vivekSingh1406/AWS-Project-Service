# AWS Deployment — Spring Boot & Serverless Projects

1. [Project 1: Deploy Spring Boot on EC2](#project-1-deploy-spring-boot-on-ec2)
2. [Project 2: S3-Lambda-RDS Service](#project-2-s3-lambda-rds-service)
3. [Project 3: S3-Lambda-CloudFront-DynamoDB Service](#project-3-s3-lambda-cloudfront-dynamodb-service)

---

## Project 1: Deploy Spring Boot on EC2

Manual deployment of a Spring Boot JAR onto a single EC2 instance — good for learning or small/dev workloads.

### Step 1 — Launch an EC2 Instance
1. Go to **EC2 Console → Launch Instance**.
2. Choose an AMI: **Amazon Linux 2** or **Ubuntu 22.04 LTS** (both covered below).
3. Choose an instance type — `t2.micro` / `t3.micro` (free-tier eligible) is enough for testing.
4. Select or create a **key pair** (`.pem` file) — required for SSH access. Store it securely; you can't re-download it later.
5. Configure the **Security Group**:
   - Allow **SSH (22)** from your IP (not `0.0.0.0/0`, for security).
   - Allow **Custom TCP (8080)** from `0.0.0.0/0` (or your IP, if restricting access) — this is covered again in Step 7, but it's easiest to add it up front.
6. Launch the instance and wait for it to reach the **Running** state.

### Step 2 — Connect via SSH
```bash
chmod 400 /path/to/your-key.pem
ssh -i /path/to/your-key.pem ubuntu@<public-ip-address>        # Ubuntu
```

### Step 3 — Install Git, Java, and Maven

**Ubuntu:**
```bash
sudo apt update -y
sudo apt install git -y
sudo apt install openjdk-17-jdk -y
sudo apt install maven -y
```

Verify installs:
```bash
java -version
mvn -version
git --version
```

### Step 4 — Clone the Repository
```bash
git clone <your-https-repo-url>
cd your-repository-name
```

### Step 5 — Build the JAR
```bash
mvn clean package -DskipTests
```
This produces a JAR under `target/`, typically `target/your-project-name-0.0.1-SNAPSHOT.jar`.

### Step 6 — Run the Application

**Quick test (foreground):**
```bash
java -jar target/your-project-name-0.0.1-SNAPSHOT.jar
```

### Step 7 — Open the App Port in the Security Group
1. **EC2 Console → Instances → select instance → Security tab → Security Group**.
2. **Edit inbound rules → Add rule**:
   - Type: `Custom TCP`
   - Port: `8080` (or your app's `server.port`)
   - Source: `0.0.0.0/0` (public) or your IP range (restricted)
3. Save rules.

### Step 8 — Access the Application
```
http://<ec2-public-ip>:8080
```

### Notes & Best Practices
- Use an **Elastic IP** if you don't want the public IP to change on instance restart.
- For production, put the app behind an **Application Load Balancer** + **ACM (HTTPS)** rather than exposing 8080 directly.
- Consider externalizing config via environment variables or AWS **Systems Manager Parameter Store** instead of hardcoding secrets.

---

## Project 2: S3-Lambda-RDS Service

**Repo:** [`Example-S3-Lambda-RDS-Service`](https://github.com/vivekSingh1406/AWS-Project-Service/blob/main/Example-S3-Lambda-RDS-Service/vivek-singh.png)

A full Spring Boot application that ingests data via REST, stores it in S3, and pipes it into RDS through an event-driven Lambda — with CloudWatch handling observability.

### Flow
1. Client sends a **POST** request (e.g., via Postman) to the Spring Boot REST API.
2. The Spring Boot app uploads the payload to an **S3 bucket** (via AWS SDK).
3. The S3 upload event **triggers a Lambda function**.
4. The Lambda function parses the data and writes it into an **RDS instance** (MySQL/PostgreSQL).
5. **CloudWatch** automatically captures Lambda execution logs and metrics.

### Components

| Layer | Technology |
|---|---|
| API | Spring Boot REST Controller |
| Storage integration | AWS SDK for Java (S3 client) |
| Event processing | AWS Lambda (triggered on S3 `ObjectCreated`) |
| Database | Amazon RDS (MySQL/PostgreSQL) |
| Access control | IAM roles/policies (S3 → Lambda → RDS) |
| Monitoring | Amazon CloudWatch (auto-enabled for Lambda) |

### Setup Checklist
- [ ] Spring Boot REST controller accepts POST payloads and uses the AWS SDK to `putObject` into S3.
- [ ] S3 bucket configured with an event notification on `s3:ObjectCreated:*` targeting the Lambda function.
- [ ] Lambda execution role has permissions for `s3:GetObject` and RDS network/DB access.
- [ ] Lambda deployed inside the same VPC as RDS (or with a properly configured security group / VPC peering) so it can reach the database.
- [ ] RDS security group allows inbound traffic from the Lambda's security group on the DB port.
- [ ] CloudWatch log group created automatically for the Lambda function — verify logs after a test upload.

---

## Project 3: S3-Lambda-CloudFront-DynamoDB Service

**Repo:** [`Example-S3-Lambda-CloudFront-DynamoDB-Service`](https://github.com/vivekSingh1406/AWS-Project-Service/blob/main/Example-S3-Lambda-CloudFront-DynamoDB-Service/project.png)

A fully serverless web application — static frontend delivered globally via CloudFront, backed by API Gateway, Lambda, and DynamoDB.

### Flow
```
User → CloudFront → S3 (static frontend)
User's browser → API Gateway → Lambda (GET/POST) → DynamoDB
```
1. User accesses the frontend (HTML/React/Angular) through **CloudFront**.
2. The frontend is hosted in an **S3 bucket** and distributed globally via CloudFront for low-latency delivery.
3. The frontend calls **API Gateway**, the single entry point for backend requests.
4. API Gateway routes requests to **Lambda functions**:
   - **GET Lambda** → reads from DynamoDB.
   - **POST Lambda** → writes to DynamoDB.
5. **DynamoDB** serves as the fully managed NoSQL backend.
6. **CloudWatch** (always active, not shown in the architecture diagram) monitors logs and metrics for both Lambda and API Gateway.

### Components

| Layer | Technology |
|---|---|
| Frontend | Static HTML/JS |
| CDN | Amazon CloudFront |
| Static hosting | Amazon S3 |
| API layer | Amazon API Gateway |
| Compute | AWS Lambda (separate GET/POST functions) |
| Database | Amazon DynamoDB |
| Monitoring | Amazon CloudWatch |

### Setup Checklist
- [ ] S3 bucket configured for static website hosting (or as a CloudFront origin with Origin Access Control).
- [ ] CloudFront distribution created, pointing to the S3 bucket, with HTTPS enforced.
- [ ] API Gateway REST/HTTP API created with `GET` and `POST` routes.
- [ ] Two Lambda functions (or one with routing logic) with an IAM role granting `dynamodb:GetItem`/`Query` and `dynamodb:PutItem`.
- [ ] DynamoDB table created with an appropriate partition key (and sort key if needed).
- [ ] CORS enabled on API Gateway so the CloudFront-served frontend can call it.
- [ ] CloudWatch log groups verified for both Lambda functions and API Gateway execution logs.

---
