# Student Evaluator System - UML Diagrams

This directory contains comprehensive UML diagrams for the Student Evaluator System using PlantUML syntax.

## 📁 Diagram Files

### Class Diagrams
- **`class-diagram-overview.puml`** - Complete system class overview with all major components
- **`class-diagram-detailed-services.puml`** - Detailed service layer classes with all methods
- **`class-diagram-models.puml`** - Domain models and data structures
- **`class-diagram-security.puml`** - Security components and validation classes

### Sequence Diagrams
- **`sequence-evaluation-flow.puml`** - Complete evaluation process flow
- **`sequence-assignment-upload.puml`** - Assignment upload workflow
- **`sequence-cli-interaction.puml`** - CLI to REST API interaction

### Other Diagrams
- **`activity-diagram-evaluation.puml`** - Evaluation process activity flow
- **`component-diagram-system.puml`** - System component architecture

## 🔧 How to View/Render These Diagrams

### Option 1: Online PlantUML Editor
1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy and paste the content of any `.puml` file
3. Click "Submit" to render the diagram

### Option 2: VS Code Extension
1. Install the "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Press `Alt+D` to preview the diagram

### Option 3: IntelliJ IDEA Plugin
1. Install the "PlantUML integration" plugin
2. Open any `.puml` file
3. The diagram will render automatically in the editor

### Option 4: Command Line Tool
```bash
# Install PlantUML
npm install -g node-plantuml

# Generate PNG from PUML file
puml generate class-diagram-overview.puml

# Generate SVG
puml generate class-diagram-overview.puml -f svg
```

### Option 5: Docker
```bash
# Generate all diagrams as PNG
docker run --rm -v $(pwd):/data plantuml/plantuml:latest -tpng /data/*.puml

# Generate as SVG
docker run --rm -v $(pwd):/data plantuml/plantuml:latest -tsvg /data/*.puml
```

## 📊 Diagram Descriptions

### Class Diagram Overview
Shows the complete system architecture with all major classes, their relationships, and key methods. This is the best starting point to understand the system structure.

### Detailed Service Classes
Focuses on the service layer with complete method signatures, private methods, and detailed class relationships. Essential for understanding business logic implementation.

### Model Classes
Comprehensive view of all domain models, DTOs, and data structures. Shows entity relationships and includes all getters, setters, and business methods.

### Security Classes
Detailed view of security components including validation, file handling, rate limiting, and authentication. Shows security-related enums and exceptions.

### Evaluation Flow Sequence
Step-by-step sequence showing how an evaluation request flows through the system, including error handling and async processing.

### Assignment Upload Sequence
Shows the complete workflow for uploading assignments and test files, including validation and storage processes.

### CLI Interaction Sequence
Demonstrates how CLI commands interact with the REST API, including polling for results and error handling.

### Evaluation Activity Diagram
Activity flow showing the evaluation process with decision points, parallel processing, and error paths.

### System Component Diagram
High-level component view showing how different system parts interact, including external interfaces, databases, and monitoring.

## 🎯 Usage Recommendations

### For Developers
1. Start with **class-diagram-overview.puml** to understand the system structure
2. Use **sequence-evaluation-flow.puml** to understand the main workflow
3. Refer to **class-diagram-detailed-services.puml** when implementing new features
4. Check **class-diagram-security.puml** for security-related implementations

### For Architects
1. Review **component-diagram-system.puml** for system architecture
2. Use **activity-diagram-evaluation.puml** for process optimization
3. Analyze **sequence diagrams** for performance bottlenecks

### For New Team Members
1. Begin with **class-diagram-overview.puml** for system understanding
2. Follow **sequence-evaluation-flow.puml** to trace a complete request
3. Study **class-diagram-models.puml** to understand data structures

### For Documentation
1. Include rendered diagrams in technical documentation
2. Use **component-diagram-system.puml** for system overview presentations
3. Reference **sequence diagrams** in API documentation

## 🔄 Keeping Diagrams Updated

When modifying the codebase:

1. **Add new classes**: Update the relevant class diagram
2. **Change method signatures**: Update detailed class diagrams
3. **Modify workflows**: Update sequence and activity diagrams
4. **Add new components**: Update the component diagram

## 📝 PlantUML Syntax Reference

### Basic Class Syntax
```plantuml
class ClassName {
    -privateField: Type
    +publicMethod(param: Type): ReturnType
    #protectedMethod(): void
    ~packageMethod(): void
}
```

### Relationships
```plantuml
ClassA --> ClassB : uses
ClassA --o ClassB : aggregation
ClassA --* ClassB : composition
ClassA --|> ClassB : inheritance
ClassA ..|> InterfaceB : implements
```

### Sequence Diagram Syntax
```plantuml
Actor -> Object : method()
activate Object
Object --> Actor : return
deactivate Object
```

## 🛠️ Customization

You can customize these diagrams by:

1. **Changing themes**: Add `!theme <theme_name>` at the top
2. **Modifying colors**: Use `skinparam` directives
3. **Adjusting layout**: Use layout hints and grouping
4. **Adding notes**: Use `note` elements for additional information

## 📚 Additional Resources

- [PlantUML Official Documentation](https://plantuml.com/)
- [PlantUML Class Diagram Guide](https://plantuml.com/class-diagram)
- [PlantUML Sequence Diagram Guide](https://plantuml.com/sequence-diagram)
- [PlantUML Activity Diagram Guide](https://plantuml.com/activity-diagram-beta)

---

*These UML diagrams are maintained alongside the codebase. Please update them when making significant architectural changes.*