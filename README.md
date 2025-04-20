Below is a step‑by‑step README for your GitHub repository. It explains how to clone the repo, compile the Java code, and test the functionality using both a local JSON file and the API mode. You can copy and paste this README into your repository's README.md.

---

# Log Index Analyzer

This project is a Java command‑line tool to analyze your Elasticsearch logging strategy. It reads JSON data from either a local file or an API endpoint, aggregates statistics, and writes the analysis output to a text file.

The analysis includes:
- **Top 5 largest indexes by storage size** (displayed in GB, using base‑10 conversion rounded to 2 decimal places)
- **Top 5 largest indexes by shard count**
- **Top 5 least balanced indexes** (based on the ratio of GB per shard) along with a recommended shard count using the rule: **1 shard per 30 GB of data**

## Prerequisites

- **Java 8 or higher** (tested with Java 17)
- **Jackson JSON libraries**:
  - `jackson-core-2.17.0.jar`
  - `jackson-annotations-2.17.0.jar`
  - `jackson-databind-2.17.0.jar`

Make sure you have the Jackson JARs either in your local Maven repository or placed in a local folder.

## Project Structure

```
log-index-analyzer-java/
├── src/
│   └── org/
│       └── example/
│           └── LogAnalyzer.java
├── example-in.json          # Sample input file (JSON)
└── README.md                # This file
```

## Step‑by‑Step Instructions

### 1. Clone the Repository

Clone the repository from GitHub (replace the URL with your repository URL):

```bash
git clone https://github.com/yourusername/log-index-analyzer-java.git
cd log-index-analyzer-java
```

### 2. Download the Jackson Dependencies

If you do not already have these libraries, you can:
- Download the JARs from the [Maven Central Repository](https://search.maven.org/), or
- Use your Maven/Gradle build configuration.

For manual compilation, note the paths to your Jackson JAR files. For example:

- `~/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.17.0/jackson-core-2.17.0.jar`
- `~/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar`
- `~/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.17.0/jackson-databind-2.17.0.jar`

### 3. Compile the Code

Open a terminal in the project root folder. Set up your classpath with the Jackson JARs. For example, on macOS/Linux:

```bash
export JARS=~/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.17.0/jackson-core-2.17.0.jar:\
~/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.17.0/jackson-annotations-2.17.0.jar:\
~/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.17.0/jackson-databind-2.17.0.jar

javac -cp "$JARS" src/org/example/LogAnalyzer.java
```

> **Note on Windows:** Replace the colon (`:`) with a semicolon (`;`) in the classpath.

### 4. Run the Code in Debug Mode (Local File Input)

In your `src/org/example/LogAnalyzer.java`, the code is set to run in debug mode (using a local file) when `debug` is true. Make sure the path to your sample JSON file (`example-in.json`) is correct.

Run the program with:

```bash
java -cp "src:$JARS" org.example.LogAnalyzer
```

After the run completes, you should see a message:  
`Analysis written to analysis-output.txt`  
Open the generated `analysis-output.txt` file to view the results.

### 5. Run the Code in Live Mode (API Input)

To use the API mode, do the following:
- In `src/org/example/LogAnalyzer.java`, set `debug` to `false`.
- Set the `endpoint` variable to your live Elasticsearch endpoint.
- Adjust the `days` variable if necessary.

Recompile and run:

```bash
javac -cp "$JARS" src/org/example/LogAnalyzer.java
java -cp "src:$JARS" org.example.LogAnalyzer
```

The output will again be written to `analysis-output.txt`.

### 6. Verify the Output

Check `analysis-output.txt` to ensure it matches the expected output. The file should contain three sections:
1. **Largest indexes by storage size**
2. **Largest indexes by shard count**
3. **Least balanced indexes** (including each index’s recommended shard count)

## Troubleshooting

- Ensure the Jackson JAR paths are correct.
- Verify the path to `example-in.json` is accurate.
- Use `debug = true` when testing locally.
- If you encounter any errors, check the console output for troubleshooting hints.

## Contributing

Feel free to fork this project and submit pull requests with improvements or fixes.

## License

This project is provided "AS-IS" without any warranty. Use it at your own risk.

---

This README gives clear, step‑by‑step instructions for building and testing the project from GitHub. Feel free to modify any sections to match your project’s exact configuration and repository URL.
