---
name: analyze-flaky-coverage
description: >-
  Diagnose flaky tests by running them many times under the IntelliJ Coverage
  Agent, diffing per-line hit counts and branch coverage across runs to pinpoint
  the exact lines whose coverage varies. Use when a test passes and fails
  non-deterministically and you need to find the root cause (race conditions,
  thread-safety bugs, non-deterministic ordering).
license: Apache-2.0
compatibility: >-
  Requires Java 17+, Maven, and a built IntelliJ Coverage checkout
  ($COVERAGE_HOME or ~/IdeaProjects/intellij-coverage).
metadata:
  author: jetbrains
  version: "1.0"
allowed-tools: Bash(mvn:*) Bash(java:*) Bash(diff:*) Read
---

# Flaky-Test Root-Cause Analysis with Coverage Variation

Run a flaky test N times under the IntelliJ Coverage Agent, generate a text
report per run, then diff the reports. Lines whose hit counts or branch
coverage differ between runs are the source of non-determinism.

## Step-by-step instructions

### 1. Ensure prerequisites

- The target Maven project must be compiled (`mvn compile test-compile`).
- The [IntelliJ Coverage project](https://github.com/jetbrains/intellij-coverage) must be built (`./gradlew jar` from its root).
  The default location is `$COVERAGE_HOME` or `~/IdeaProjects/intellij-coverage`.

### 2. Configure the script

Edit the variables at the top of [scripts/analyze-flaky-coverage.sh](../../../analyze-flaky-coverage/scripts/analyze-flaky-coverage.sh):

| Variable | Purpose |
|----------|---------|
| `TEST_CLASS` | Fully-qualified test class name |
| `INCLUDE_PATTERN` | Coverage include pattern (e.g. `com.example.webshop.*`) |

### 3. Run the analysis

```bash
# From the Maven project root:
./analyze-flaky-coverage/scripts/analyze-flaky-coverage.sh 50
```

The argument is the number of iterations (default 50). The script will:

1. Run `mvn surefire:test` with the coverage agent attached for each iteration.
2. Record PASS/FAIL from the Maven exit code.
3. Convert each binary `.ic` report to text via `TextCoverageStatistics`.
4. Diff all reports against the first and collect varying line numbers.
5. Print the result in the format:

```
Class:LineNumber  Hits(v1, v2)  Branch(v1, v2)
```

Output is also saved to `target/coverage-summary.txt`.

### 4. Interpret the results

Any line that shows **multiple values** in `Hits(...)` or `Branch(...)` is a
candidate for the source of non-determinism.

**Example output** (50 runs, 30 pass / 20 fail):

```
InvoiceService:19    Hits(1,2)  Branch(1/2,2/2)
InvoiceService:20    Hits(1,2)
InvoiceService:22    Hits(1,2)
```

How to read this:

- `Hits(1,2)` — some runs hit the line once, others twice (concurrent threads).
- `Branch(1/2,2/2)` — in some runs only one branch of a conditional was taken;
  in others both were. This is the strongest signal for a race condition.

See [references/REFERENCE.md](../../../analyze-flaky-coverage/references/REFERENCE.md) for key coverage-agent
flags, the `TextCoverageStatistics` Java entry point, and a worked example with
diagnosis and fix options.

### 5. Adapt to another project

1. Copy the `analyze-flaky-coverage/` directory into the target Maven project.
2. Edit `TEST_CLASS` and `INCLUDE_PATTERN` in the script.
3. Pre-compile: `mvn compile test-compile`.
4. Run: `./analyze-flaky-coverage/scripts/analyze-flaky-coverage.sh 50`

## Edge cases

- If all runs produce identical coverage, the flakiness is not caused by
  differing code paths (look at external state, timing, or test ordering instead).
- If no FAIL runs appear after 3x the requested iterations, the test may not be
  flaky under the current environment/load. Try increasing iterations or adding
  artificial thread contention.
- The script uses `surefire:test` (skips compilation) for speed. If you change
  source code between runs, re-run `mvn compile test-compile` first.
