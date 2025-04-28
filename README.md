# [Re-Route](https://re-route.ca) Vehicle Routing Solution

A Java-based solution for vehicle routing problems (VRP) leveraging GraphHopper for distance calculations, a genetic algorithm (GA) for single-driver TSP, and OR-Tools for multi-driver routing.

## Features

- **Distance Calculation**: GraphHopper computes shortest-path distances between nodes.
- **Single-Driver Routing**: Customizable GA implementation for TSP.
- **Multi-Driver Routing**: Google OR-Tools distributes stops across multiple drivers.
- **Routing Profiles**: Eight GraphHopper profiles across three modes:
  - Standard
  - Avoid Highways & Ferries
  - Avoid Tolls
- **Unit Testing**: JUnit tests for GA operators, fitness functions, and convergence checks.
- **Legacy Code**: Some files in `src/` are deprecated or unused after refactoring; included for reference.
- **Initial Build Script**: `setupBuildFile.txt` provides instructions to generate the GraphHopper graph-cache (isolated due to dependency conflicts).

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/re-route-vrp.git
   cd re-route-vrp
   ```
2. Build a fat JAR with the Gradle Shadow plugin:
   ```bash
   ./gradlew clean shadowJar
   ```

## Graph-Cache Initialization

Before running, generate the GraphHopper graph-cache by following the steps in `setupBuildFile.txt`:

```bash
bash setupBuildFile.txt
```

## Configuration

Edit settings in the source or appropriate properties:

- Path to OSM data for GraphHopper
- GA parameters (population size, mutation/crossover rates, generation count)
- OR-Tools vehicle and demand definitions

Rebuild the JAR after making configuration changes.

## Usage

- **Single-Driver GA**:
  ```bash
  java -jar build/libs/re-route-vrp-all.jar ga --input routes.json --output solution.json
  ```

- **Multi-Driver OR-Tools**:
  ```bash
  java -jar build/libs/re-route-vrp-all.jar ortools --input routes.json --output solution.json --drivers 5
  ```

## Project Structure

```
├── src/main/java/.../         # VRP implementation code
├── src/test/java/.../         # JUnit tests for GA components
├── setupBuildFile.txt         # GraphHopper cache build instructions
├── build.gradle.kts           # Gradle build script (with Shadow plugin)
└── LICENSE                    # Apache 2.0 license file
```

## License

Apache 2.0 License. See the LICENSE file for details.

