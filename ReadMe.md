# SPL Assignment 2: Linear Algebra Engine (LAE)

## Project Overview
This project was developed for the Systems Programming course at Ben-Gurion University of the Negev. It is a highly concurrent Linear Algebra Engine implemented in Java. The system processes complex mathematical expressions (such as matrix addition, multiplication, transposition, and negation) by parallelizing tasks across multiple threads to maximize CPU utilization.

**Developers:** Tal Azizi & Nahmani

## Core Architecture and Features

### 1. Custom Thread Pool (TiredExecutor)
Instead of relying on standard Java concurrency frameworks, this engine implements a custom thread pool from scratch.
- **Monitor-Based Synchronization:** The scheduling logic strictly utilizes fundamental Java monitor primitives (`synchronized`, `wait()`, `notifyAll()`).
- **Fatigue-Aware Task Allocation:** Tasks are dynamically assigned based on a custom fairness metric. Each worker thread calculates its own "fatigue" (balancing active CPU time and idle time), ensuring that the least-fatigued threads receive new tasks first.

### 2. Shared Memory Management
To handle concurrent data access efficiently, the system employs fine-grained locking:
- **SharedMatrix and SharedVector:** Matrices are represented as collections of vectors. Read/Write locks (`ReentrantReadWriteLock`) are applied exclusively at the vector level.
- **High Throughput:** Multiple threads can read from the same vector simultaneously, while write operations are safely isolated, preventing deadlocks and data corruption without locking the entire matrix.

### 3. Execution Flow
- **JSON Parsing:** The engine reads a JSON configuration file to construct a computation tree.
- **Task Decomposition:** Resolvable operations are broken down into discrete, row-by-row or column-by-column tasks.
- **Output Generation:** Upon completion, the final matrix (or an appropriate error message regarding illegal dimensions) is formatted and written back to a target JSON file.

## Build and Execution

### Requirements
- Java 21 or higher
- Maven

### Compiling the Project
Open your terminal in the project's root directory and use Maven to build the application:

```bash
# Compile the project files
mvn compile

# Execute unit tests
mvn test

# Package the application into an executable JAR
mvn package
