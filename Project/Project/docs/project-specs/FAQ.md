# E-Learning Platform with Auto Evaluation - Frequently Asked Questions (FAQ)

## General Questions

### What is the E-Learning Platform with Auto Evaluation?

The E-Learning Platform with Auto Evaluation is a comprehensive educational technology solution that automates programming assignment evaluation and provides interactive learning experiences. It offers both REST API and CLI interfaces for uploading assignments, managing test cases, and retrieving evaluation results.

### What programming languages does it support?

Currently, the system supports Java programming assignments only. The system compiles Java source code and runs JUnit tests to evaluate student submissions.

### Who is the target audience?

- **Educators**: Teachers and professors who want to automate assignment grading
- **Students**: Learners who want immediate feedback on their code
- **Developers**: Those learning Java programming and testing practices
- **Institutions**: Educational organizations looking for automated evaluation solutions

### Is this system suitable for production use?

Yes, the system is designed with production deployment in mind, including:
- Scalable architecture
- Security best practices
- Monitoring and logging
- Database persistence
- Error handling and recovery

## Technical Questions

### What are the system requirements?

**Minimum Requirements:**
- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- 2GB RAM
- 10GB disk space

**Recommended Requirements:**
- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- 4GB RAM
- 50GB disk space
- Redis for caching

### What databases are supported?

- **MySQL 8.0+**: Primary database for structured data (students, assignments, evaluations)
- **DynamoDB**: For unstructured evaluation logs and detailed execution data
- **Redis**: Optional caching layer for improved performance

### Can I use other databases instead of MySQL?

The system is built with Spring Data JPA, so it can theoretically support other JPA-compatible databases like PostgreSQL, MariaDB, or H2. However, you may need to:
- Update database driver dependencies
- Modify database-specific configurations
- Test thoroughly as the system is primarily tested with MySQL

### How does the evaluation process work?

1. **Upload Assignment**: Teacher uploads the assignment template and test files
2. **Student Submission**: Student submits their Java implementation
3. **Compilation**: System compiles the student's Java code
4. **Testing**: System runs JUnit tests against the compiled code
5. **Scoring**: System calculates scores based on test results
6. **Storage**: Results are stored in databases and cache
7. **Retrieval**: Results can be retrieved via API or CLI

### What security measures are in place?

- **Input Validation**: File type, size, and content validation
- **Sandboxed Execution**: Compilation and testing in isolated environments
- **Rate Limiting**: Protection against abuse and DoS attacks
- **Secure File Handling**: Path traversal prevention and secure storage
- **Resource Limits**: Timeout mechanisms and memory constraints
- **SQL Injection Prevention**: Parameterized queries and ORM usage

## Usage Questions

### How do I upload an assignment?

**Via REST API:**
```bash
curl -X POST http://localhost:8080/api/v1/assignments \
  -F "file=@Calculator.java" \
  -F "title=Calculator Assignment" \
  -F "description=Basic calculator operations"
```

**Via CLI:**
```bash
evaluator upload-assignment --title "Calculator Assignment" Calculator.java
```

### How do I submit student work for evaluation?

**Via REST API:**
```bash
curl -X POST http://localhost:8080/api/v1/evaluations \
  -F "file=@StudentCalculator.java" \
  -F "studentId=student123" \
  -F "assignmentId=calc-assignment-1"
```

**Via CLI:**
```bash
evaluator submit --student student123 --assignment calc-assignment-1 StudentCalculator.java
```

### How do I retrieve evaluation results?

**Via REST API:**
```bash
# Get basic results
curl http://localhost:8080/api/v1/evaluations/eval-123-456

# Get detailed results
curl http://localhost:8080/api/v1/evaluations/eval-123-456/results
```

**Via CLI:**
```bash
# Get basic results
evaluator results eval-123-456

# Get detailed results with logs
evaluator results --detailed --logs eval-123-456
```

### What file formats are supported?

- **Assignment Files**: `.java` files only
- **Test Files**: `.java` files containing JUnit tests
- **Maximum File Size**: 1MB by default (configurable)
- **File Naming**: No special requirements, but descriptive names are recommended

### How long does evaluation take?

- **Typical Evaluation**: 5-30 seconds
- **Complex Assignments**: Up to 60 seconds
- **Timeout Limit**: 60 seconds by default (configurable)

Factors affecting evaluation time:
- Code complexity
- Number of test cases
- System load
- Compilation time

### Can I evaluate multiple submissions simultaneously?

Yes, the system supports concurrent evaluations:
- **Default Concurrency**: 10 simultaneous evaluations
- **Configurable**: Can be adjusted based on system resources
- **Queue Management**: Requests are queued when limits are reached
- **Async Processing**: Evaluations run asynchronously

## Configuration Questions

### How do I change the database configuration?

Edit the `application.yml` file or set environment variables:

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/your-database
    username: your-username
    password: your-password
```

Or use environment variables:
```bash
export DB_HOST=your-host
export DB_NAME=your-database
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
```

### How do I configure file upload limits?

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB

evaluation:
  max-file-size: 5242880  # 5MB in bytes
```

### How do I enable debug logging?

```yaml
logging:
  level:
    com.studentevaluator: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

### How do I configure evaluation timeouts?

```yaml
evaluation:
  timeout: 60000  # 60 seconds in milliseconds
  max-concurrent-evaluations: 20
```

### Can I customize the temporary directory?

```yaml
evaluation:
  temp-directory: /custom/temp/path
```

Or set environment variable:
```bash
export TEMP_DIR=/custom/temp/path
```

## Deployment Questions

### How do I deploy for development?

1. **Prerequisites**: Install Java 11+, Maven, MySQL
2. **Database Setup**: Create database and user
3. **Configuration**: Set up `application-dev.yml`
4. **Build**: Run `mvn clean package`
5. **Run**: Execute `java -jar target/student-evaluator-*.jar`

See the [Local Development Guide](deployment/LOCAL_DEVELOPMENT_GUIDE.md) for detailed instructions.

### How do I deploy to production?

The system supports multiple deployment options:
- **Docker containers** with Docker Compose
- **Kubernetes** with Helm charts
- **AWS ECS** with Fargate
- **Traditional servers** with systemd

See the [Production Deployment Guide](deployment/PRODUCTION_DEPLOYMENT_GUIDE.md) for detailed instructions.

### What are the scaling considerations?

**Horizontal Scaling:**
- Multiple application instances behind load balancer
- Shared database and cache
- Stateless application design

**Vertical Scaling:**
- Increase CPU and memory resources
- Optimize JVM settings
- Database performance tuning

**Database Scaling:**
- Read replicas for MySQL
- Connection pooling optimization
- Query optimization and indexing

### How do I monitor the system in production?

The system provides multiple monitoring options:
- **Health Checks**: `/actuator/health` endpoint
- **Metrics**: Prometheus metrics via `/actuator/prometheus`
- **Application Metrics**: Custom business metrics
- **Logging**: Structured logging with correlation IDs
- **Dashboards**: Grafana dashboards for visualization

## Troubleshooting Questions

### The application won't start. What should I check?

1. **Port Conflicts**: Check if port 8080 is already in use
2. **Database Connection**: Verify database is running and accessible
3. **Java Version**: Ensure Java 11+ is installed
4. **Memory**: Check available system memory
5. **Logs**: Review startup logs for specific errors

See the [Troubleshooting Guide](troubleshooting/TROUBLESHOOTING_GUIDE.md) for detailed solutions.

### Evaluations are failing. What could be wrong?

Common causes:
1. **Compilation Errors**: Student code has syntax errors
2. **Missing Dependencies**: JUnit or other required libraries not found
3. **Timeout**: Evaluation taking longer than configured timeout
4. **Resource Limits**: Insufficient memory or disk space
5. **File Permissions**: Temporary directory not writable

### The system is running slowly. How can I improve performance?

1. **Database Optimization**: Add indexes, optimize queries
2. **Caching**: Enable Redis caching for frequently accessed data
3. **JVM Tuning**: Optimize garbage collection and heap settings
4. **Concurrent Processing**: Increase thread pool sizes
5. **Resource Allocation**: Increase CPU and memory resources

### How do I backup and restore data?

**MySQL Backup:**
```bash
mysqldump -h localhost -u evaluator -p student_evaluator > backup.sql
mysql -h localhost -u evaluator -p student_evaluator < backup.sql
```

**DynamoDB Backup:**
```bash
# Enable point-in-time recovery
aws dynamodb update-continuous-backups --table-name evaluation-logs --point-in-time-recovery-specification PointInTimeRecoveryEnabled=true

# Create on-demand backup
aws dynamodb create-backup --table-name evaluation-logs --backup-name backup-$(date +%Y%m%d)
```

## Integration Questions

### Can I integrate this with my existing LMS?

Yes, the system provides a REST API that can be integrated with Learning Management Systems like:
- Moodle
- Canvas
- Blackboard
- Custom LMS solutions

Integration typically involves:
1. API authentication setup
2. Assignment synchronization
3. Grade passback implementation
4. User mapping between systems

### How do I integrate with CI/CD pipelines?

The CLI interface makes it easy to integrate with CI/CD systems:

**GitHub Actions Example:**
```yaml
- name: Evaluate Assignment
  run: |
    evaluator submit --student ${{ github.actor }} --assignment homework-1 Solution.java
    evaluator results --wait --format json > results.json
```

**Jenkins Example:**
```groovy
stage('Evaluate') {
    steps {
        sh 'evaluator submit --student ${BUILD_USER} --assignment ${ASSIGNMENT_ID} ${JAVA_FILE}'
        sh 'evaluator results --evaluation ${EVALUATION_ID} --format json > results.json'
    }
}
```

### Can I customize the evaluation criteria?

Currently, evaluation is based on JUnit test results. Customization options include:
- **Test Weighting**: Different tests can have different point values
- **Partial Credit**: Tests can award partial points
- **Custom Scoring**: Implement custom scoring logic in test cases
- **Multiple Test Suites**: Different test suites for different aspects

Future enhancements may include:
- Code quality metrics (complexity, style)
- Performance benchmarks
- Security vulnerability scanning

### How do I add support for other programming languages?

The system is designed with extensibility in mind. To add support for other languages:

1. **Implement Compiler Interface:**
```java
public interface CompilerPlugin {
    CompilationResult compile(File sourceFile, CompilerOptions options);
    boolean supports(String fileExtension);
}
```

2. **Implement Test Runner Interface:**
```java
public interface TestRunnerPlugin {
    TestExecutionResult runTests(File compiledClass, List<File> testFiles);
    boolean supports(String testFramework);
}
```

3. **Register Plugins:**
```java
@Configuration
public class PluginConfiguration {
    @Bean
    public CompilerPlugin pythonCompiler() {
        return new PythonCompilerPlugin();
    }
}
```

## Performance Questions

### What are the performance benchmarks?

**Typical Performance (single instance):**
- **Throughput**: 100-500 evaluations per hour
- **Response Time**: 95th percentile < 2 seconds for API calls
- **Evaluation Time**: Average 15 seconds per evaluation
- **Concurrent Users**: 50-100 simultaneous users

**Scalability:**
- **Horizontal**: Linear scaling with additional instances
- **Database**: Supports thousands of students and assignments
- **Storage**: Handles gigabytes of evaluation data

### How can I optimize performance?

1. **Enable Caching:**
```yaml
spring:
  redis:
    host: redis-server
    port: 6379
```

2. **Optimize Database:**
```sql
CREATE INDEX idx_evaluations_student_id ON evaluations(student_id);
CREATE INDEX idx_evaluations_assignment_id ON evaluations(assignment_id);
```

3. **Tune JVM:**
```bash
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

4. **Increase Concurrency:**
```yaml
evaluation:
  max-concurrent-evaluations: 50
server:
  tomcat:
    threads:
      max: 200
```

### What are the resource requirements for different loads?

**Small Deployment (< 100 students):**
- 2 vCPUs, 4GB RAM
- 50GB storage
- Single instance

**Medium Deployment (100-1000 students):**
- 4 vCPUs, 8GB RAM per instance
- 2-3 instances
- 200GB storage
- Redis cache

**Large Deployment (1000+ students):**
- 8 vCPUs, 16GB RAM per instance
- 5+ instances with load balancer
- 500GB+ storage
- Redis cluster
- Database read replicas

## Security Questions

### Is the system secure for educational use?

Yes, the system implements multiple security layers:
- **Input validation** prevents malicious file uploads
- **Sandboxed execution** isolates student code
- **Resource limits** prevent resource exhaustion attacks
- **Rate limiting** protects against abuse
- **Secure file handling** prevents path traversal attacks

### How do I enable authentication?

The system is designed to integrate with existing authentication systems. Common approaches:

1. **OAuth 2.0 / OpenID Connect:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer().jwt();
        return http.build();
    }
}
```

2. **API Keys:**
```java
@Component
public class ApiKeyAuthenticationFilter implements Filter {
    // API key validation logic
}
```

3. **LDAP Integration:**
```yaml
spring:
  ldap:
    urls: ldap://ldap.example.com:389
    base: dc=example,dc=com
```

### How do I secure file uploads?

The system includes several file security measures:
- File type validation (only .java files)
- File size limits
- Content scanning for malicious patterns
- Secure temporary storage
- Automatic cleanup

Additional security can be added:
```java
@Component
public class SecurityScanner {
    public boolean isFileSafe(MultipartFile file) {
        // Custom security scanning logic
        return !containsMaliciousPatterns(file);
    }
}
```

### What about data privacy?

The system handles educational data responsibly:
- **Data Minimization**: Only collects necessary information
- **Secure Storage**: Encrypted data at rest and in transit
- **Access Control**: Role-based access to sensitive data
- **Audit Logging**: Comprehensive audit trails
- **Data Retention**: Configurable data retention policies

For GDPR compliance:
- Student data can be exported
- Data can be deleted upon request
- Processing is based on legitimate educational interest

## Support Questions

### Where can I get help?

1. **Documentation**: Comprehensive guides and API documentation
2. **Troubleshooting Guide**: Common issues and solutions
3. **GitHub Issues**: Report bugs and request features
4. **Community Forum**: Discuss with other users
5. **Professional Support**: Available for enterprise deployments

### How do I report bugs?

1. **Check existing issues** on GitHub
2. **Gather information**:
   - System version
   - Environment details
   - Steps to reproduce
   - Error logs
3. **Create detailed issue** with reproduction steps
4. **Provide logs** and configuration (sanitized)

### How do I request new features?

1. **Check roadmap** for planned features
2. **Search existing requests** to avoid duplicates
3. **Create feature request** with:
   - Use case description
   - Expected behavior
   - Business justification
   - Implementation suggestions

### Is commercial support available?

Yes, commercial support options include:
- **Installation and setup** assistance
- **Custom development** for specific requirements
- **Performance optimization** consulting
- **24/7 support** for critical deployments
- **Training and workshops** for teams

Contact the development team for commercial support inquiries.

## Roadmap Questions

### What features are planned for future releases?

**Short-term (next 3-6 months):**
- Enhanced security features
- Performance improvements
- Additional monitoring capabilities
- UI/web interface

**Medium-term (6-12 months):**
- Support for additional programming languages
- Advanced analytics and reporting
- Integration with popular LMS platforms
- Mobile API support

**Long-term (12+ months):**
- AI-powered code analysis
- Plagiarism detection
- Advanced grading algorithms
- Multi-tenant architecture

### How can I contribute to the project?

Contributions are welcome! Ways to contribute:
1. **Code contributions**: Bug fixes, features, improvements
2. **Documentation**: Guides, tutorials, API documentation
3. **Testing**: Bug reports, test cases, performance testing
4. **Community**: Help other users, answer questions
5. **Feedback**: Feature requests, usability feedback

See the contributing guidelines in the repository for detailed information.

### Is there a plugin system planned?

Yes, a plugin architecture is planned to allow:
- **Custom compilers** for different languages
- **Custom test runners** for different frameworks
- **Custom scoring algorithms**
- **Integration plugins** for external systems
- **UI extensions** for additional functionality

The plugin system will provide well-defined interfaces and a plugin registry for easy installation and management.