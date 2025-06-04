
# Project Overview
A Java-based high-performance IP counter designed to process extremely large text files (containing IP addresses) and determine the number of unique entries. The program reads files efficiently and utilizes optimized memory handling strategies to scale to files over 100 GB.

# Implementation Approaches
The application offers three different algorithms for counting unique IP addresses, all capable of handling very large files:

## LineReader with BitSet
- Reads the file line by line, converting each IP into an integer, and uses two BitSets (positive and negative) to track unique IPs.
- Average time on 106 GB file: ~33 minutes

## ByteBuffer with BitSet
- Reads the file in byte chunks (with adjustable buffer size), manually converts the bytes to IP addresses instead of relying on standard line-based parsing, and uses BitSets.
- Average time on 106 GB file: ~7–8 minutes

## ByteBuffer with Direct Memory
- Replaces BitSets with direct memory (ByteBuffer.allocateDirect) to track unique IPs using a bit-level approach.
- Performance notes: Depending on buffer size (1MB to 32MB), performance may vary, but no significant speed improvement was observed compared to the ByteBuffer with BitSet version.
  - 1MB: ~6m 5s
  - 2MB: ~6m 10s
  - 4MB: ~6m 32s
  - 8MB: ~6m 30s
  - 16MB: ~7m 43s
  - 32MB: ~7m 41s

## Parallel LineReader with BitSet
The `parallel` algorithm implements a two-phase processing model:
1. **DataReader** (single thread) reads the file line by line and pushes each line into a `BlockingQueue`.
2. **IPConverter** workers (multiple threads) consume the queue in parallel, parse valid IPv4 addresses, and mark them in two `BitSet` structures (for positive and negative integer representations).

**Key characteristics:**
- Synchronization is applied separately to the two `BitSet` instances to avoid collisions.
- Worker threads terminate automatically if the queue remains empty for a defined timeout (`2 seconds`).
- The number of processing threads is limited to `availableProcessors() - 1` to leave one core for the reader thread.
- Average time on 106 GB file: ~14 minutes

# Configuration Options
The program allows customization of several runtime parameters via the configuration file (`config.properties`) or command-line arguments:
- `buffer.size.mb`: Size of the byte buffer in megabytes for reading the file.
- `algorithm`: Choose between `linebitset`, `bytebitset`, `bytememory` and `parallel`.
- `filePath`: Specify the input file path.

# Usage Instructions
1. Clone the repository.
2. Build the project using your preferred Java build tool (e.g., Gradle or Maven).
3. Prepare a `config.properties` file or specify parameters via the command line.
4. Run the application with `java -jar ipcounter.jar --config=config.properties` or using the appropriate build command.
