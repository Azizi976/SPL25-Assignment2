# SPL Assignment 2: Linear Algebra Engine (LAE)

## Project Overview
[cite_start]This project was developed as part of Assignment 2 in the Systems Programming (SPL) course at Ben-Gurion University of the Negev[cite: 1]. [cite_start]The system is a high-performance Linear Algebra Engine (LAE) implemented in Java, designed to execute complex matrix computations (Addition, Multiplication, Transpose, and Negation)[cite: 7, 9, 81, 85]. [cite_start]The core focus of this project is multi-threading, advanced synchronization techniques, and the implementation of a custom thread-pool architecture[cite: 7, 8].

**Developers:** Tal Azizi & Ifat Nahmani

## Key Features & Technical Mechanisms
[cite_start]The engine processes mathematical expressions represented as a JSON computation tree and parallelizes the tasks to maximize CPU utilization[cite: 71, 173, 174, 188].

### 1. Custom Thread Pool (TiredExecutor)
[cite_start]Unlike standard Java executors, this system utilizes a custom-built `TiredExecutor`[cite: 181, 314]. 
- [cite_start]**Monitor Primitives:** The thread pool and worker scheduling are implemented strictly using Java's built-in monitor primitives: `synchronized` blocks, `wait()`, and `notifyAll()`[cite: 316].
- [cite_start]**Fatigue-Based Scheduling:** To ensure fairness, tasks are dynamically assigned to the "least-fatigued" worker threads based on their accumulated CPU time and a random fatigue factor[cite: 319, 320, 331, 380].

### 2. Shared Memory & Fine-Grained Locking
[cite_start]The LAE manages data through `SharedMatrix` and `SharedVector` classes[cite: 345, 346]:
- [cite_start]**Fine-Grained Synchronization:** Instead of locking entire matrices, the system uses `ReentrantReadWriteLock` on individual `SharedVector` instances[cite: 352, 356].
- [cite_start]**Concurrency:** This allows multiple threads to read vectors simultaneously while ensuring exclusive access for in-place write operations[cite: 354, 355, 358].

### 3. Computation Logic
- [cite_start]**Orchestration:** The engine iteratively identifies resolvable nodes (where operands are concrete matrices) and decomposes them into `Runnable` tasks[cite: 177, 178, 180, 181].
- [cite_start]**Error Handling:** Dimensions are validated before execution; illegal operations trigger a detailed error report in the output JSON[cite: 194, 195, 383, 385].

## Architecture Overview

```text
├── src/main/java/
│   ├── memory/             # SharedMatrix and SharedVector (Locking logic)
│   ├── parser/             # JSON parsing and ComputationTree structures
│   ├── scheduling/         # TiredExecutor and TiredThread (Thread pool logic)
│   └── spl/lae/            # Core Engine and Main entry point
├── src/test/java/          # Comprehensive Unit Tests
├── pom.xml                 # Maven configuration and dependencies
└── README.md               # Project documentation
