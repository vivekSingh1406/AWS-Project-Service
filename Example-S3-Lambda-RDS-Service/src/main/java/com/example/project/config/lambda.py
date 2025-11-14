```
{
  "id": 101,
  "name": "Vivek Singh",
  "email": "vivek.singh@example.com",
  "description": "i'm full stack developer",
  "age": 28
}

```

```
import json
import boto3
import pymysql
import os

# Database configuration via environment variables
rds_host = os.environ['RDS_HOST']
rds_user = os.environ['RDS_USER']
rds_password = os.environ['RDS_PASSWORD']
rds_db = os.environ['RDS_DB']

# MySQL connection setup
def get_connection():
    return pymysql.connect(
        host=rds_host,
        user=rds_user,
        password=rds_password,
        database=rds_db,
        cursorclass=pymysql.cursors.DictCursor
    )

def lambda_handler(event, context):
    s3 = boto3.client('s3')

    # Process each S3 event record
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = record['s3']['object']['key']

        try:
            # Fetch the file from S3
            response = s3.get_object(Bucket=bucket, Key=key)
            content = response['Body'].read().decode('utf-8')
            data = json.loads(content)

            # Extract fields
            user_id = data.get('id')
            name = data.get('name')
            email = data.get('email')
            description = data.get('description')
            age = data.get('age')

            # Insert into RDS
            conn = get_connection()
            with conn.cursor() as cursor:
                sql = """
                    INSERT INTO users (id, name, email, description, age)
                    VALUES (%s, %s, %s, %s, %s)
                """
                cursor.execute(sql, (user_id, name, email, description, age))
                conn.commit()
                print(f"Inserted user {user_id} into database")

        except Exception as e:
            print(f"Error processing file {key} from bucket {bucket}: {e}")
            raise e

    return {
        'statusCode': 200,
        'body': json.dumps('Successfully processed all records.')
    }

```
