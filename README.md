# 🖼️ FilterFlare

A high-resolution image filtering benchmark and visualization tool designed to compare the performance of different processing strategies.

## 🔍 What it Does

- Applies selected image filters (e.g., grayscale, warm tone, Van Gogh-style) on a set of images.
- Benchmarks and compares **three modes**:
  - **Sequential** processing
  - **Parallel** processing using threads
  - **ForkJoin** parallelism using the Fork/Join framework
- Visualizes:
  - **CPU usage over time**
  - **Execution time bar chart**
  - **Speedup of parallel modes**
  - **Memory overhead per strategy**
- Displays a **console progress bar** for real-time feedback.

## 📁 Folder Structure

```
project/
│
├── input/              # Place your input images here (JPEG/PNG)
├── output_sequential/  # Auto-generated filtered outputs
├── output_parallel/
├── output_forkjoin/
│
├── src/
│   ├── Main.java
│   ├── Benchmark.java
│   ├── Filters.java
│
├── lib/                # External libraries (e.g., JFreeChart)
├── bin/                # Compiled classes
└── README.md
└── concurrent          # report pdf
└── cp                  # powerpoint presentation

```

## 🧪 How to Run

1. Place your images inside the `input/` folder.
2. Compile the project:
   ```bash
   javac -cp "lib/*" -d bin src/*.java
   ```
3. Run the main program:
   ```bash
   java -cp "bin;lib/*" Main
   ```
4. Choose a filter when prompted (e.g., `gray`, `warm`, `vangogh`).
5. Watch the terminal and GUI for progress and benchmark results.

## 📊 Sample Output

- Terminal:
  ```
  Running mode: SEQUENTIAL
  [==========              ] 15/30
  sequential took 1450.23 ms
  ...
  Speedup of Parallel over Sequential: 2.31x
  Memory Usage Summary:
  Sequential: 104.5 MB
  Parallel:   178.3 MB
  Forkjoin:   189.2 MB
  ```
- GUI:
  - Real-time CPU usage line charts per mode
  - Bar chart comparing total execution times

## ⚙️ Technologies

- Java 24+
- Swing (GUI)
- JFreeChart (charting library)
- Fork/Join Framework
- Multithreading

