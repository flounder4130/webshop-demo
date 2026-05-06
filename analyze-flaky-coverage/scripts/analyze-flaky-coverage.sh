#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------------------------
# Per-project configuration
# ---------------------------------------------------------------------------
TEST_CLASS=com.example.webshop.service.InvoiceServiceTest
INCLUDE_PATTERN="com.example.webshop.*"
TOTAL_RUNS="${1:-50}"

# ---------------------------------------------------------------------------
# Layout
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_DIR="$(cd "$SKILL_DIR/.." && pwd)"
TOOLS_DIR="$SKILL_DIR/tools"
TOOLS_BUILD_DIR="$SKILL_DIR/build"
WORK_DIR="$PROJECT_DIR/target/coverage-runs"
SUMMARY="$PROJECT_DIR/target/coverage-summary.txt"

# ---------------------------------------------------------------------------
# Resolve IntelliJ coverage agent + reporter from Maven Central (cached in ~/.m2)
# ---------------------------------------------------------------------------
COVERAGE_VERSION="${COVERAGE_VERSION:-1.0.766}"
AGENT_GAV="org.jetbrains.intellij.deps:intellij-coverage-agent:${COVERAGE_VERSION}"
REPORTER_GAV="org.jetbrains.intellij.deps:intellij-coverage-reporter:${COVERAGE_VERSION}"

MVN="${PROJECT_DIR}/mvnw"
[ -x "$MVN" ] || MVN="mvn"

resolve_jar() {
  local gav="$1"
  "$MVN" -q dependency:get -Dartifact="$gav" -Dtransitive=false >/dev/null
  local g="${gav%%:*}"; local rest="${gav#*:}"
  local a="${rest%%:*}"; local v="${rest##*:}"
  echo "$HOME/.m2/repository/${g//.//}/$a/$v/$a-$v.jar"
}

echo "Resolving coverage agent ${COVERAGE_VERSION} from Maven Central..."
AGENT_JAR="$(resolve_jar "$AGENT_GAV")"
REPORTER_JAR="$(resolve_jar "$REPORTER_GAV")"

# ---------------------------------------------------------------------------
# Compile bundled TextCoverageStatistics.java against the resolved jars
# ---------------------------------------------------------------------------
TOOLS_CP="$TOOLS_BUILD_DIR:$AGENT_JAR:$REPORTER_JAR"
if [ ! -f "$TOOLS_BUILD_DIR/com/intellij/rt/coverage/report/TextCoverageStatistics.class" ] \
   || [ "$TOOLS_DIR/TextCoverageStatistics.java" -nt "$TOOLS_BUILD_DIR/com/intellij/rt/coverage/report/TextCoverageStatistics.class" ]; then
  echo "Compiling TextCoverageStatistics..."
  mkdir -p "$TOOLS_BUILD_DIR"
  javac -d "$TOOLS_BUILD_DIR" -cp "$AGENT_JAR:$REPORTER_JAR" "$TOOLS_DIR/TextCoverageStatistics.java"
fi

# ---------------------------------------------------------------------------
# Run the test under coverage N times
# ---------------------------------------------------------------------------
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"

generate_report() {
  java -cp "$TOOLS_CP" \
    com.intellij.rt.coverage.report.TextCoverageStatistics \
    "$1" "$PROJECT_DIR/target/classes"
}

exec > >(tee "$SUMMARY") 2>&1

pass=0; fail=0
for i in $(seq 1 "$TOTAL_RUNS"); do
  IC_FILE="$WORK_DIR/run-${i}.ic"

  set +e
  cd "$PROJECT_DIR"
  "$MVN" -q surefire:test \
    -Dtest="$TEST_CLASS" \
    "-DargLine=-Didea.coverage.calculate.hits=true -javaagent:${AGENT_JAR}=${IC_FILE},true,false,false,false,${INCLUDE_PATTERN}" \
    > /dev/null 2>&1
  rc=$?
  set -e

  if [ $rc -eq 0 ]; then
    pass=$((pass + 1))
  else
    fail=$((fail + 1))
  fi

  generate_report "$IC_FILE" > "$WORK_DIR/report-${i}.txt"
  rm -f "$IC_FILE"
done

echo "Collected $TOTAL_RUNS runs: $pass pass, $fail fail"
echo

set +e

diff_line_nums="$WORK_DIR/varying-lines.txt"
> "$diff_line_nums"
for i in $(seq 2 "$TOTAL_RUNS"); do
  diff "$WORK_DIR/report-1.txt" "$WORK_DIR/report-${i}.txt" \
    | awk '/^[0-9]/ { split($1, a, /[acd,]/); for(j=a[1]; j<=a[2]+0 || j<=a[1]; j++) print j }' \
    >> "$diff_line_nums" 2>/dev/null
done
varying=$(sort -un "$diff_line_nums")

if [ -z "$varying" ]; then
  echo "All runs produced identical coverage."
else
  echo "Lines that vary across runs:"
  echo
  for ln in $varying; do
    cls=$(head -n "$ln" "$WORK_DIR/report-1.txt" | grep "^--- " | tail -1 | sed 's/^--- //;s/ ---$//')
    content=$(sed -n "${ln}p" "$WORK_DIR/report-1.txt" | sed 's/^  *//')
    src_line=$(echo "$content" | awk '{print $1}')

    [[ "$src_line" =~ ^[0-9]+$ ]] || continue

    label="${cls##*.}:${src_line}"

    hits_set=""
    branch_set=""
    for i in $(seq 1 "$TOTAL_RUNS"); do
      val=$(sed -n "${ln}p" "$WORK_DIR/report-${i}.txt" | sed 's/^  *//')
      h=$(echo "$val" | awk '{print $2}')
      b=$(echo "$val" | awk '{print $3}')
      hits_set="$hits_set $h"
      if [ -n "$b" ]; then
        branch_set="$branch_set $b"
      fi
    done

    hits_unique=$(echo "$hits_set" | tr ' ' '\n' | grep -v '^$' | sort -un | tr '\n' ',' | sed 's/,$//')
    branch_unique=$(echo "$branch_set" | tr ' ' '\n' | grep -v '^$' | sort -u | tr '\n' ',' | sed 's/,$//')

    if [ -n "$branch_unique" ]; then
      printf "  %-35s  Hits(%s)  Branch(%s)\n" "$label" "$hits_unique" "$branch_unique"
    else
      printf "  %-35s  Hits(%s)\n" "$label" "$hits_unique"
    fi
  done
fi

rm -rf "$WORK_DIR"
