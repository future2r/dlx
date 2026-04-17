# DLX Simulator

This project is about simulating that processor using Java and JavaFX.

## What You'll Find in This Project

This project is also intended as a programming tutorial. Here is what it demonstrates from a developer's perspective:

- **Modern Java** — Sealed types, records, pattern matching, and switch expressions
- **JavaFX & MVVM** — Property binding, FXML with factory-based dependency injection, and syntax highlighting via incubator rich text modules
- **Java Module System** — Single application module with selective package opening for JavaFX reflection
- **Theming** — Light, dark, and auto theme switching with CSS stylesheet swapping based on system preferences
- **Internationalization** — Resource bundles for English and German, integrated with FXML property references
- **Testing** — JUnit with parameterized tests, integration tests, and in-memory preference mocks
- **Build & Packaging** — Maven Wrapper, toolchains for JDK management, and profiles for jlink runtime images and jpackage native installers
- **CI/CD** — GitHub Actions workflow with branch-based triggers
- **Code Quality** — All compiler warnings as errors, strict Javadoc linting for accessibility, HTML, syntax, and references
- **Compiler Pipeline** — Three-stage assembler (lexer, parser, compiler) with algebraic data types for tokens, AST nodes, and operands
- **CPU Simulation** — 5-stage pipeline with hazard detection, data forwarding, and snapshot-then-commit execution model

## Development

### Prerequisites

- **Any JDK** installed and `JAVA_HOME` pointing to it. The Maven wrapper uses this JDK to bootstrap Maven itself.
- **JDK 26** (OpenJDK) registered in a [Maven toolchain](https://maven.apache.org/guides/mini/guide-using-toolchains.html). The build selects the correct JDK via the `maven-toolchains-plugin`, so your `JAVA_HOME` JDK does not need to be version 26. Add the following to your `~/.m2/toolchains.xml`:

```xml
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>26</version>
      <vendor>openjdk</vendor>
    </provides>
    <configuration>
      <jdkHome>C:\path\to\jdk-26</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### Build

The project uses the [Maven Wrapper](https://maven.apache.org/wrapper/), so you do **not** need Maven installed. The wrapper automatically downloads the required Maven version on first use.

```powershell
.\mvnw compile       # compile sources and generate Javadoc
.\mvnw test          # run unit tests
.\mvnw package       # build JAR in target/lib/
.\mvnw clean         # remove build artifacts
```

### Editing

The recommended editor is [Visual Studio Code](https://code.visualstudio.com/) with the Java extensions.

1. Open the workspace file `dlx.code-workspace` in VS Code.
2. Install the recommended extensions when prompted.
3. Use the pre-configured launch configuration to run the application.

### Using

1. Open a DLX source file from `assets/examples`.
2. Choose *Compile and Load*.
3. Choose *Run*.
4. Watch the magic happen! 😉
