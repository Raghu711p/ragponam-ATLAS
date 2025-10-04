# Student Evaluator System - Complete Beginner's Guide

## Table of Contents
1. [What is This System?](#what-is-this-system)
2. [Who Can Use This?](#who-can-use-this)
3. [What You Need to Get Started](#what-you-need-to-get-started)
4. [Quick Start Guide](#quick-start-guide)
5. [Understanding the System](#understanding-the-system)
6. [Step-by-Step Setup](#step-by-step-setup)
7. [How to Use the System](#how-to-use-the-system)
8. [Common Problems and Solutions](#common-problems-and-solutions)
9. [Advanced Usage](#advanced-usage)
10. [Getting Help](#getting-help)

---

## What is This System?

The **Student Evaluator System** is like having an automatic grading assistant for Java programming assignments. Think of it as a robot teacher that can:

- ✅ Check if student code compiles (works without errors)
- ✅ Run tests to see if the code does what it's supposed to do
- ✅ Give scores based on how many tests pass
- ✅ Provide detailed feedback about what went wrong
- ✅ Handle multiple students at the same time

### Real-World Example
Imagine you're a teacher with 100 students who submit Java homework. Instead of manually checking each submission (which could take days), this system can evaluate all 100 submissions in minutes and give you detailed reports.

---

## Who Can Use This?

### 👩‍🏫 **Teachers/Professors**
- Automatically grade Java programming assignments
- Get detailed reports on student performance
- Save hours of manual grading time
- Provide instant feedback to students

### 👨‍🎓 **Students**
- Get immediate feedback on your Java code
- See which tests pass or fail
- Learn from detailed error messages
- Practice without waiting for teacher feedback

### 🏫 **Educational Institutions**
- Deploy for multiple classes and teachers
- Handle large numbers of students
- Integrate with existing learning management systems

### 💻 **Anyone Learning Java**
- Practice Java programming with instant feedback
- Learn proper testing practices
- Understand code quality through automated evaluation

---

## What You Need to Get Started

Don't worry if you don't have everything - we'll help you install what's missing!

### Essential Requirements (Must Have)
1. **A Computer** - Windows, Mac, or Linux
2. **Internet Connection** - For downloading software and updates
3. **Basic Computer Skills** - Ability to download, install software, and use command line

### Software We'll Install Together
1. **Java** - The programming language (free)
2. **Maven** - Helps build the project (free)
3. **MySQL** - Main database for storing data (free)
4. **DynamoDB Local** - For logging and detailed data (free)
5. **Git** - Downloads the project (free)

### Optional but Helpful
1. **Code Editor** - Like Visual Studio Code (free)
2. **MySQL Workbench** - Visual database management (free)
3. **NoSQL Workbench** - For DynamoDB management (free)

---

## Quick Start Guide

### Complete Setup (45 minutes)
We'll set up everything properly using local databases and tools. This gives you full control and understanding of the system.

Follow the [Step-by-Step Setup](#step-by-step-setup) section below.

---

## Understanding the System

### How It Works (Simple Explanation)

```
1. Teacher uploads assignment template (Calculator.java)
2. Teacher uploads test files (CalculatorTest.java)
3. Student submits their solution (MyCalculator.java)
4. System compiles student's code
5. System runs tests against the code
6. System calculates score and provides feedback
7. Results are stored and can be viewed anytime
```

### System Components (What Each Part Does)

#### 🌐 **Web API**
- Receives file uploads and requests
- Like a receptionist that handles all incoming requests
- You can interact with it through web browsers or command line

#### 🧠 **Evaluation Engine**
- The "brain" that compiles and tests Java code
- Runs in a secure environment so bad code can't harm your computer
- Calculates scores based on test results

#### 💾 **Database**
- Stores student information, assignments, and results
- Like a filing cabinet that remembers everything
- Two types: MySQL (structured data) and DynamoDB (logs)

#### 📊 **Monitoring System**
- Keeps track of system health and performance
- Alerts you if something goes wrong
- Shows statistics and usage reports

---

## Step-by-Step Setup

### Step 1: Install Java

#### Windows:
1. Go to https://adoptopenjdk.net/
2. Download "OpenJDK 11 (LTS)" for Windows
3. Run the installer and follow the prompts
4. Open Command Prompt and type: `java -version`
5. You should see something like "openjdk version 11..."

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install openjdk-11-jdk
java -version
```

### Step 2: Install Maven

#### Windows:
1. Go to https://maven.apache.org/download.cgi
2. Download "Binary zip archive"
3. Extract to C:\Program Files\Maven
4. Add to PATH environment variable
5. Test: Open Command Prompt and type `mvn -version`

#### Linux:
```bash
sudo apt install maven
mvn -version
```

### Step 3: Install Git

#### Windows:
1. Go to https://git-scm.com/download/win
2. Download and install
3. Test: `git --version`

#### Linux:
```bash
sudo apt install git
git --version
```

### Step 4: Install MySQL Database

#### Windows:
1. Go to https://dev.mysql.com/downloads/mysql/
2. Download "MySQL Community Server" for Windows
3. Run the installer and choose "Developer Default" setup
4. Set root password (remember this!)
5. Complete the installation
6. Test: Open Command Prompt and type `mysql --version`

#### Linux:
```bash
# Install MySQL Server
sudo apt update
sudo apt install mysql-server

# Secure the installation
sudo mysql_secure_installation

# Test installation
mysql --version
```

### Step 5: Install MySQL Workbench (Optional but Recommended)

This gives you a visual interface to manage your database.

#### Windows:
1. Go to https://dev.mysql.com/downloads/workbench/
2. Download MySQL Workbench for Windows
3. Install and launch it
4. Connect to your local MySQL server (localhost:3306)

#### Linux:
```bash
sudo apt install mysql-workbench-community
```

### Step 6: Install DynamoDB Local

#### Windows:
1. Go to https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html
2. Download DynamoDB Local
3. Extract to a folder like `C:\DynamoDB`
4. You'll need Java to run it (which we already installed)

#### Linux:
```bash
# Create directory for DynamoDB
mkdir ~/dynamodb-local
cd ~/dynamodb-local

# Download DynamoDB Local
wget https://s3.us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz
tar -xzf dynamodb_local_latest.tar.gz
```

### Step 7: Install NoSQL Workbench (Optional)

This provides a visual interface for DynamoDB.

1. Go to https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.html
2. Download NoSQL Workbench for your operating system
3. Install and launch it

### Step 8: Download the Project

```bash
# Create a folder for your projects
mkdir my-projects
cd my-projects

# Download the Student Evaluator System
git clone https://github.com/your-org/student-evaluator-system.git
cd student-evaluator-system

# Look at what we downloaded
ls -la
```

### Step 9: Set Up the Databases

#### Set Up MySQL Database

1. **Connect to MySQL:**
   ```bash
   # Windows (if MySQL is in PATH)
   mysql -u root -p
   
   # Linux
   sudo mysql -u root -p
   ```

2. **Create the database and user:**
   ```sql
   -- Create the database
   CREATE DATABASE student_evaluator;
   
   -- Create a user for the application
   CREATE USER 'evaluator'@'localhost' IDENTIFIED BY 'your_password_here';
   
   -- Grant permissions
   GRANT ALL PRIVILEGES ON student_evaluator.* TO 'evaluator'@'localhost';
   
   -- Apply changes
   FLUSH PRIVILEGES;
   
   -- Exit MySQL
   EXIT;
   ```

3. **Test the connection:**
   ```bash
   mysql -u evaluator -p student_evaluator
   # Enter the password you set above
   # If successful, you'll see the MySQL prompt
   ```

#### Start DynamoDB Local

1. **Windows:**
   ```cmd
   # Navigate to DynamoDB folder
   cd C:\DynamoDB
   
   # Start DynamoDB Local
   java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port 8000
   ```

2. **Linux:**
   ```bash
   # Navigate to DynamoDB folder
   cd ~/dynamodb-local
   
   # Start DynamoDB Local
   java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port 8000
   ```

3. **Keep this terminal open** - DynamoDB Local needs to keep running

#### Create DynamoDB Table

Open a new terminal and run:

```bash
# Install AWS CLI if you don't have it
# Windows: Download from https://aws.amazon.com/cli/
# Linux: sudo apt install awscli

# Configure AWS CLI for local use (use dummy values)
aws configure
# AWS Access Key ID: dummy
# AWS Secret Access Key: dummy
# Default region name: us-east-1
# Default output format: json

# Create the evaluation logs table
aws dynamodb create-table \
    --table-name evaluation_logs \
    --attribute-definitions \
        AttributeName=evaluation_id,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=evaluation_id,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000
```

### Step 10: Configure the Application

Before building, we need to configure the application to use our local databases.

1. **Edit the configuration file:**
   ```bash
   # Navigate to the project directory
   cd student-evaluator-system
   
   # Edit the application configuration
   # Windows: Use Notepad or any text editor
   # Linux: Use nano, vim, or any text editor
   nano src/main/resources/application.yml
   ```

2. **Update the database settings:**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/student_evaluator
       username: evaluator
       password: your_password_here  # Use the password you set for MySQL
       driver-class-name: com.mysql.cj.jdbc.Driver
     
     jpa:
       hibernate:
         ddl-auto: create-drop  # This will create tables automatically
       show-sql: true  # Shows SQL queries in logs (helpful for debugging)

   aws:
     region: us-east-1
     dynamodb:
       endpoint: http://localhost:8000  # Points to local DynamoDB
       table-name: evaluation_logs

   # Application settings
   server:
     port: 8080

   logging:
     level:
       com.studentevaluator: DEBUG  # More detailed logs
   ```

### Step 11: Build and Start the Application

```bash
# Build the project (this might take a few minutes the first time)
mvn clean package

# Start the application
java -jar target/student-evaluator-*.jar
```

Wait for the application to start. You'll see messages like:
```
Started StudentEvaluatorApplication in 45.123 seconds
```

### Step 12: Verify Everything Works

Open a new terminal/command prompt and test:

```bash
# Check if the application is healthy
curl http://localhost:8080/actuator/health
```

You should see:
```json
{"status":"UP"}
```

**Congratulations! 🎉 Your system is now running!**

### Step 13: Explore Your Databases (Optional)

Now that everything is working, you can use the visual tools to see your data:

#### Using MySQL Workbench
1. Open MySQL Workbench
2. Create a new connection:
   - Connection Name: Student Evaluator Local
   - Hostname: localhost
   - Port: 3306
   - Username: evaluator
   - Password: (the password you set)
3. Connect and explore the `student_evaluator` database
4. You'll see tables like `students`, `assignments`, `evaluations` after you start using the system

#### Using NoSQL Workbench for DynamoDB
1. Open NoSQL Workbench
2. Go to "Operation builder"
3. Add connection:
   - Connection name: Local DynamoDB
   - Endpoint: http://localhost:8000
   - Region: us-east-1
   - Access key: dummy
   - Secret key: dummy
4. Connect and explore the `evaluation_logs` table

---

## How to Use the System

**Important Note about Placeholders:**
Throughout this guide, you'll see placeholders like `YOUR_ASSIGNMENT_ID`, `YOUR_EVALUATION_ID`, etc. These are NOT actual values - you need to replace them with the real IDs you get from the system responses. For example:
- `YOUR_ASSIGNMENT_ID` might become `calc-assignment-1`
- `YOUR_EVALUATION_ID` might become `eval-123-456`

### Method 1: Using Command Line (Easiest)

#### Upload an Assignment
```bash
# Create a simple calculator assignment
echo 'public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
}' > Calculator.java

# Upload it to the system
curl -X POST http://localhost:8080/api/v1/assignments \
  -F "file=@Calculator.java" \
  -F "title=Basic Calculator" \
  -F "description=Simple calculator with add and subtract"
```

#### Upload Test Files
```bash
# Create test file
echo 'import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {
    @Test
    void testAdd() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3));
    }
    
    @Test
    void testSubtract() {
        Calculator calc = new Calculator();
        assertEquals(1, calc.subtract(3, 2));
    }
}' > CalculatorTest.java

# Upload test file (replace YOUR_ASSIGNMENT_ID with the actual ID from previous step)
curl -X POST http://localhost:8080/api/v1/assignments/YOUR_ASSIGNMENT_ID/tests \
  -F "files=@CalculatorTest.java"
```

#### Submit Student Work
```bash
# Create a student submission (with a small bug)
echo 'public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b + 1; // Oops! Bug here
    }
}' > StudentCalculator.java

# Submit for evaluation (replace YOUR_ASSIGNMENT_ID with the actual assignment ID)
curl -X POST http://localhost:8080/api/v1/evaluations \
  -F "file=@StudentCalculator.java" \
  -F "studentId=john_doe" \
  -F "assignmentId=YOUR_ASSIGNMENT_ID"
```

#### Get Results
```bash
# Check evaluation status (replace YOUR_EVALUATION_ID with the actual evaluation ID from the previous response)
curl http://localhost:8080/api/v1/evaluations/YOUR_EVALUATION_ID

# Get detailed results
curl http://localhost:8080/api/v1/evaluations/YOUR_EVALUATION_ID/results
```

**Note:** Replace `YOUR_EVALUATION_ID` with the actual evaluation ID you received when submitting the student work. It will look something like `eval-123-456`.

### Method 2: Using a Web Interface Tool

#### Using curl with a Web Interface (No Installation Required)
1. Go to https://reqbin.com/curl
2. Paste the curl commands from above
3. Click "Send" to execute

#### Using a REST Client Extension (VS Code)
If you're using Visual Studio Code:
1. Install the "REST Client" extension
2. Create a file called `api-test.http`
3. Add your API calls in a simple format:
   ```http
   ### Upload Assignment
   POST http://localhost:8080/api/v1/assignments
   Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

   ------WebKitFormBoundary7MA4YWxkTrZu0gW
   Content-Disposition: form-data; name="title"

   Basic Calculator
   ------WebKitFormBoundary7MA4YWxkTrZu0gW
   Content-Disposition: form-data; name="file"; filename="Calculator.java"
   Content-Type: text/plain

   public class Calculator {
       public int add(int a, int b) {
           return a + b;
       }
   }
   ------WebKitFormBoundary7MA4YWxkTrZu0gW--
   ```
4. Click "Send Request" above each request

### Method 3: Build a Simple Web Page

Create a file called `test.html`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Student Evaluator Test</title>
</head>
<body>
    <h1>Upload Assignment</h1>
    <form id="uploadForm">
        <input type="file" id="fileInput" accept=".java" required>
        <input type="text" id="titleInput" placeholder="Assignment Title" required>
        <input type="text" id="descInput" placeholder="Description">
        <button type="submit">Upload Assignment</button>
    </form>
    
    <div id="result"></div>
    
    <script>
        document.getElementById('uploadForm').onsubmit = async function(e) {
            e.preventDefault();
            
            const formData = new FormData();
            formData.append('file', document.getElementById('fileInput').files[0]);
            formData.append('title', document.getElementById('titleInput').value);
            formData.append('description', document.getElementById('descInput').value);
            
            try {
                const response = await fetch('http://localhost:8080/api/v1/assignments', {
                    method: 'POST',
                    body: formData
                });
                
                const result = await response.json();
                document.getElementById('result').innerHTML = 
                    '<pre>' + JSON.stringify(result, null, 2) + '</pre>';
            } catch (error) {
                document.getElementById('result').innerHTML = 'Error: ' + error.message;
            }
        };
    </script>
</body>
</html>
```

Open this file in your web browser to get a simple upload interface.

---

## Common Problems and Solutions

### Problem: "Port 8080 already in use"

**What it means:** Another program is using the same port.

**Solution:**
```bash
# Find what's using the port
lsof -i :8080  # Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or use a different port
java -jar target/student-evaluator-*.jar --server.port=8081
```

### Problem: "Cannot connect to database"

**What it means:** The database isn't running or configured correctly.

**Solution:**

1. **Check MySQL is running:**
   ```bash
   # Windows
   net start mysql80  # or mysql57, depending on version
   
   # Linux
   sudo systemctl status mysql
   sudo systemctl start mysql  # if not running
   ```

2. **Test MySQL connection:**
   ```bash
   mysql -u evaluator -p student_evaluator
   # If this fails, recreate the user and database
   ```

3. **Check DynamoDB Local is running:**
   ```bash
   # You should see DynamoDB Local running in a terminal
   # If not, restart it:
   # Windows: cd C:\DynamoDB && java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port 8000
   # Linux: cd ~/dynamodb-local && java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb -port 8000
   ```

4. **Verify DynamoDB table exists:**
   ```bash
   aws dynamodb list-tables --endpoint-url http://localhost:8000
   # Should show "evaluation_logs" in the list
   ```

### Problem: "Java not found"

**What it means:** Java isn't installed or not in PATH.

**Solution:**
```bash
# Check if Java is installed
java -version

# If not found, reinstall Java and make sure it's in your PATH
# Windows: Add Java bin directory to PATH environment variable
# Linux: Add to ~/.bashrc:
export JAVA_HOME=/path/to/java
export PATH=$JAVA_HOME/bin:$PATH
```

### Problem: "Maven not found"

**What it means:** Maven isn't installed or not in PATH.

**Solution:**
```bash
# Check Maven installation
mvn -version

# If not found, reinstall Maven and add to PATH
```

### Problem: "Out of memory"

**What it means:** The application needs more memory.

**Solution:**
```bash
# Give Java more memory
java -Xmx2g -jar target/student-evaluator-*.jar

# Or set environment variable
export JAVA_OPTS="-Xmx2g"
java -jar target/student-evaluator-*.jar
```

### Problem: "Compilation failed"

**What it means:** The Java code has syntax errors.

**Solution:**
1. Check the Java file for syntax errors
2. Make sure the class name matches the file name
3. Ensure all brackets and semicolons are correct
4. Check the error message in the evaluation results

### Problem: "Tests not found"

**What it means:** Test files weren't uploaded or have issues.

**Solution:**
1. Make sure test files are uploaded before submitting student work
2. Check that test files have proper JUnit annotations (@Test)
3. Verify test class names match the file names

---

## Advanced Usage

### Setting Up for Multiple Users

#### 1. Configure User Management
```yaml
# Add to application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-provider.com
```

#### 2. Set Up Load Balancing
```bash
# Run multiple instances
java -jar target/student-evaluator-*.jar --server.port=8080 &
java -jar target/student-evaluator-*.jar --server.port=8081 &
java -jar target/student-evaluator-*.jar --server.port=8082 &

# Use nginx or similar to distribute load
```

#### 3. Production Database Setup
```bash
# Use real MySQL instead of Docker for production
mysql -u root -p
CREATE DATABASE student_evaluator_prod;
CREATE USER 'evaluator'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON student_evaluator_prod.* TO 'evaluator'@'%';
```

### Integration with Learning Management Systems

#### Moodle Integration
```php
// Moodle plugin code example
function submit_to_evaluator($assignment_file, $student_id, $assignment_id) {
    $curl = curl_init();
    curl_setopt_array($curl, array(
        CURLOPT_URL => 'http://your-evaluator.com/api/v1/evaluations',
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => array(
            'file' => new CURLFile($assignment_file),
            'studentId' => $student_id,
            'assignmentId' => $assignment_id
        )
    ));
    return curl_exec($curl);
}
```

#### Canvas Integration
```javascript
// Canvas LTI integration
const evaluatorAPI = {
    baseURL: 'http://your-evaluator.com/api/v1',
    
    async submitAssignment(file, studentId, assignmentId) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('studentId', studentId);
        formData.append('assignmentId', assignmentId);
        
        const response = await fetch(`${this.baseURL}/evaluations`, {
            method: 'POST',
            body: formData
        });
        
        return response.json();
    }
};
```

### Monitoring and Maintenance

#### Set Up Monitoring
```bash
# Check system health
curl http://localhost:8080/actuator/health

# View metrics
curl http://localhost:8080/actuator/metrics

# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

#### Regular Maintenance Tasks
```bash
# Backup database daily
mysqldump -u evaluator -p student_evaluator > backup_$(date +%Y%m%d).sql

# Clean up old temporary files
find /tmp/evaluations -type f -mtime +7 -delete

# Restart application weekly
pkill -f student-evaluator
java -jar target/student-evaluator-*.jar &
```

#### Log Analysis
```bash
# View recent errors
tail -f logs/student-evaluator.log | grep ERROR

# Count evaluations per day
grep "Evaluation completed" logs/student-evaluator.log | grep $(date +%Y-%m-%d) | wc -l

# Find slow evaluations
grep "Evaluation took" logs/student-evaluator.log | awk '$4 > 30000'
```

---

## Getting Help

### Self-Help Resources

#### 1. Check the Logs
```bash
# Application logs
tail -f logs/student-evaluator.log

# MySQL logs
# Windows: Check MySQL error log in MySQL installation directory
# Linux: sudo tail -f /var/log/mysql/error.log

# DynamoDB Local logs
# Check the terminal where DynamoDB Local is running

# System logs
# Windows: Event Viewer
# Linux: /var/log/syslog
```

#### 2. Test Individual Components
```bash
# Test database connection
mysql -h localhost -u evaluator -p -e "SELECT 1"

# Test Java compilation
javac Calculator.java

# Test application health
curl http://localhost:8080/actuator/health
```

#### 3. Common Diagnostic Commands
```bash
# Check running processes
ps aux | grep java

# Check port usage
netstat -tulpn | grep 8080

# Check disk space
df -h

# Check memory usage
free -h  # Linux
top      # Linux
```

### Community Support

#### 1. Documentation
- **API Reference**: Check `docs/api/openapi.yaml`
- **Architecture Guide**: See `docs/developer/ARCHITECTURE_GUIDE.md`
- **FAQ**: Read `docs/FAQ.md`

#### 2. Online Resources
- **GitHub Issues**: Report bugs and ask questions
- **Stack Overflow**: Tag questions with `student-evaluator-system`
- **Discord/Slack**: Join the community chat (if available)

#### 3. Professional Support
For schools and institutions:
- **Setup Assistance**: Help with installation and configuration
- **Custom Development**: Modifications for specific needs
- **Training**: Workshops for teachers and administrators
- **24/7 Support**: Critical issue resolution

### Troubleshooting Checklist

When something goes wrong, check these in order:

1. **✅ Is the application running?**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **✅ Is the database accessible?**
   ```bash
   mysql -h localhost -u evaluator -p -e "SELECT 1"
   ```

3. **✅ Are there any error messages?**
   ```bash
   tail -20 logs/student-evaluator.log
   ```

4. **✅ Is there enough disk space?**
   ```bash
   df -h
   ```

5. **✅ Is there enough memory?**
   ```bash
   free -h
   ```

6. **✅ Are the ports available?**
   ```bash
   netstat -tulpn | grep 8080
   ```

7. **✅ Are the file permissions correct?**
   ```bash
   ls -la /tmp/evaluations
   ```

### Creating a Support Request

When asking for help, include:

1. **What you were trying to do**
   - "I was trying to upload an assignment file"

2. **What happened instead**
   - "I got an error message saying 'file too large'"

3. **Your environment**
   - Operating system (Windows 10, macOS Big Sur, Ubuntu 20.04)
   - Java version (`java -version`)
   - Application version

4. **Error messages**
   - Copy the exact error message
   - Include relevant log entries

5. **Steps to reproduce**
   - List the exact steps that led to the problem

**Example Support Request:**
```
Subject: File upload failing with "file too large" error

Environment:
- Windows 10
- Java 11.0.12
- Student Evaluator System v1.0.0

Problem:
I'm trying to upload a Calculator.java file (2KB) but getting an error.

Error Message:
{"error":{"code":"FILE_TOO_LARGE","message":"File size exceeds maximum limit"}}

Steps to Reproduce:
1. Open Command Prompt
2. Run: curl -X POST http://localhost:8080/api/v1/assignments -F "file=@Calculator.java" -F "title=Test"
3. Error occurs immediately

Additional Info:
- File size is only 2KB
- Same error happens with different files
- Application health check returns OK
```

---

## Conclusion

You now have everything you need to:
- ✅ Understand what the Student Evaluator System does
- ✅ Install and set up the system
- ✅ Use it to evaluate Java assignments
- ✅ Troubleshoot common problems
- ✅ Get help when needed

### Next Steps

1. **Start Small**: Try evaluating a simple assignment first
2. **Experiment**: Upload different types of Java code and tests
3. **Scale Up**: Once comfortable, set up for multiple users
4. **Integrate**: Connect with your existing tools and workflows
5. **Contribute**: Share your experience and help improve the system

### Remember

- **Don't be afraid to experiment** - you can always restart the system
- **Read error messages carefully** - they usually tell you what's wrong
- **Ask for help** - the community is here to support you
- **Start simple** - get basic functionality working before adding complexity

**Happy evaluating! 🚀**

---

*This guide is maintained by the Student Evaluator System community. If you find errors or have suggestions for improvement, please contribute back to help others.*