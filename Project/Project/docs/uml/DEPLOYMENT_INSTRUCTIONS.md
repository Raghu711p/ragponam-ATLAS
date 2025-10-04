# Student Evaluator System - Deployment Guide

This guide provides comprehensive instructions for deploying the Student Evaluator System locally without containerization, using local storage as requested.

## Overview

The deployment system includes:
- **Local build automation** - Scripts to build and package the application
- **Local deployment** - Scripts to deploy and manage the application locally
- **Test automation** - Comprehensive test execution with reporting
- **Code quality checks** - Static analysis and quality validation
- **Environment setup** - Automated environment configuration

## Prerequisites

### Required Software
- **Java 17 or later** - Runtime environment
- **Maven 3.6.0 or later** - Build tool
- **Git** (recommended) - Version control

### System Requirements
- **OS**: Windows, Linux, or macOS
- **RAM**: Minimum 2GB, recommended 4GB
- **Disk**: Minimum 1GB free space
- **Network**: Internet connection for dependency downloads

## Quick Start

### 1. Environment Setup

**Windows:**
```cmd
scripts\setup-environment.bat
```

**Linux/macOS:**
```bash
./scripts/setup-environment.sh
```

### 2. Build Application

**Windows:**
```cmd
scripts\build.bat
```

**Linux/macOS:**
```bash
./scripts/build.sh
```

### 3. Run Tests

**Windows:**
```cmd
scripts\run-tests.bat
```

**Linux/macOS:**
```bash
./scripts/run-tests.sh
```

### 4. Deploy Locally

**Windows:**
```cmd
scripts\deploy-local.bat
```

**Linux/macOS:**
```bash
./scripts/deploy-local.sh
```

### 5. Start Application

**Windows:**
```cmd
%USERPROFILE%\student-evaluator-deploy\start.bat
```

**Linux/macOS:**
```bash
~/student-evaluator-deploy/start.sh
```

## Detailed Deployment Process

### Master Deployment Script

For a complete automated deployment:

**Linux/macOS:**
```bash
./scripts/deploy.sh [OPTIONS]
```

**Options:**
- `--type TYPE` - Deployment type (local|production)
- `--skip-tests` - Skip running tests
- `--skip-build` - Skip building application
- `--auto-start` - Automatically start application after deployment
- `--help` - Show help message

**Examples:**
```bash
# Full deployment with tests
./scripts/deploy.sh

# Quick deployment without tests
./scripts/deploy.sh --skip-tests --auto-start

# Production deployment (when available)
./scripts/deploy.sh --type production
```

## Directory Structure

After deployment, the following structure is created:

```
~/student-evaluator-deploy/          # Main deployment directory
├── student-evaluator-system-1.0.0.jar  # Application JAR
├── start.sh/.bat                    # Start script
├── stop.sh/.bat                     # Stop script
├── status.sh/.bat                   # Status check script
├── config/                          # Configuration files
│   └── application-local.yml        # Local configuration
├── logs/                           # Application logs
│   ├── application.log             # Main application log
│   └── startup.log                 # Startup log
├── data/                           # Local data storage
│   ├── assignments/                # Assignment files
│   ├── submissions/                # Student submissions
│   ├── test-files/                 # JUnit test files
│   └── studentevaluator.mv.db      # H2 database file
└── backups/                        # Backup files (if enabled)
```

## Application Management

### Starting the Application

**Windows:**
```cmd
%USERPROFILE%\student-evaluator-deploy\start.bat
```

**Linux/macOS:**
```bash
~/student-evaluator-deploy/start.sh
```

The application will start on port 8080 by default.

### Stopping the Application

**Windows:**
```cmd
%USERPROFILE%\student-evaluator-deploy\stop.bat
```

**Linux/macOS:**
```bash
~/student-evaluator-deploy/stop.sh
```

### Checking Status

**Windows:**
```cmd
%USERPROFILE%\student-evaluator-deploy\status.bat
```

**Linux/macOS:**
```bash
~/student-evaluator-deploy/status.sh
```

## Application URLs

Once deployed and started, the application is available at:

- **Main Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Application Info**: http://localhost:8080/actuator/info
- **H2 Database Console**: http://localhost:8080/h2-console

### H2 Database Connection

When accessing the H2 console:
- **JDBC URL**: `jdbc:h2:file:~/student-evaluator-deploy/data/studentevaluator`
- **User Name**: `sa`
- **Password**: (leave empty)

## Configuration

### Local Configuration

The application uses `application-local.yml` for local deployment configuration:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/studentevaluator
    username: sa
    password: 

logging:
  level:
    com.studentevaluator: INFO
  file:
    name: logs/application.log

app:
  storage:
    base-path: ./data
  evaluation:
    timeout-seconds: 30
  security:
    max-file-size: 1048576
```

### Customizing Configuration

1. Edit the configuration file:
   - **Windows**: `%USERPROFILE%\student-evaluator-deploy\config\application-local.yml`
   - **Linux/macOS**: `~/student-evaluator-deploy/config/application-local.yml`

2. Restart the application for changes to take effect

## Testing

### Running Different Test Types

**Unit Tests Only:**
```bash
./scripts/run-tests.sh --type unit
```

**Integration Tests Only:**
```bash
./scripts/run-tests.sh --type integration
```

**BDD Tests Only:**
```bash
./scripts/run-tests.sh --type bdd
```

**Specific Test Pattern:**
```bash
./scripts/run-tests.sh --test "*Service*"
```

### Test Reports

Test reports are generated in:
- **Surefire Reports**: `target/surefire-reports/`
- **BDD Reports**: `target/cucumber-reports/`
- **Archived Reports**: `target/test-reports/`

## Code Quality

### Running Quality Checks

**Linux/macOS:**
```bash
./scripts/code-quality.sh
```

This script checks for:
- TODO/FIXME comments
- System.out.println usage
- Empty catch blocks
- Static analysis (if configured)
- Test coverage (if configured)

### Quality Reports

Quality reports are generated in:
- **Quality Reports**: `target/quality-reports/`
- **Coverage Reports**: `target/site/jacoco/` (if JaCoCo is configured)

## Storage and Data

### Local Storage

All data is stored locally in the deployment directory:

- **Database**: H2 file-based database
- **File Storage**: Local filesystem
- **Logs**: Local log files
- **Configuration**: Local YAML files

### Data Backup

The system can be configured for automatic backups:

1. Enable backup in configuration:
   ```yaml
   backup:
     enabled: true
     directory: "backups"
     retention-days: 30
   ```

2. Manual backup:
   ```bash
   cp -r ~/student-evaluator-deploy/data ~/student-evaluator-deploy/backups/backup-$(date +%Y%m%d)
   ```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Change port in `application-local.yml`
   - Or stop the conflicting service

2. **Java Version Issues**
   - Ensure Java 17+ is installed
   - Check `java -version`

3. **Maven Build Failures**
   - Run `mvn clean` first
   - Check internet connection for dependencies

4. **Application Won't Start**
   - Check logs in `logs/application.log`
   - Verify configuration syntax
   - Ensure database directory is writable

### Log Files

Check these log files for troubleshooting:
- **Application Log**: `~/student-evaluator-deploy/logs/application.log`
- **Startup Log**: `~/student-evaluator-deploy/logs/startup.log`
- **Build Logs**: `target/maven.log` (if exists)

### Health Checks

Monitor application health:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Security Considerations

### Local Deployment Security

- H2 console is enabled for development (disable in production)
- File uploads are restricted to .java files
- File size limits are enforced
- No external network exposure by default

### Production Considerations

For production deployment:
1. Disable H2 console
2. Use external database
3. Configure proper logging levels
4. Set up monitoring and alerting
5. Implement backup strategies

## Performance Tuning

### JVM Options

Default JVM settings:
```bash
-Xms512m -Xmx1024m
```

For better performance:
```bash
-Xms1024m -Xmx2048m -XX:+UseG1GC
```

### Application Settings

Tune these settings in configuration:
```yaml
app:
  evaluation:
    max-concurrent-evaluations: 5
    timeout-seconds: 30
  performance:
    thread-pool-size: 10
    cache-size: 1000
```

## Maintenance

### Regular Maintenance Tasks

1. **Log Rotation**: Logs are rotated automatically
2. **Database Cleanup**: Remove old evaluation data periodically
3. **Backup Verification**: Test backup restoration regularly
4. **Dependency Updates**: Update Maven dependencies regularly
5. **Security Updates**: Keep Java and system updated

### Monitoring

Monitor these metrics:
- Application health endpoint
- Log file sizes
- Database file size
- Available disk space
- Memory usage

## Support

### Getting Help

1. Check this deployment guide
2. Review application logs
3. Check the troubleshooting section
4. Verify system requirements
5. Test with minimal configuration

### Reporting Issues

When reporting issues, include:
- Operating system and version
- Java version
- Maven version
- Error messages from logs
- Steps to reproduce
- Configuration files (sanitized)

---

This deployment guide provides comprehensive instructions for local deployment without containerization, using local storage as requested. The system is designed to be simple to deploy and manage while providing robust functionality for the Student Evaluator System.