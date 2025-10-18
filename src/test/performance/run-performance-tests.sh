#!/bin/bash

# Movies API Performance Tests Runner Script

echo "🚀 Starting Movies API Performance Tests..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven not found. Please install Maven to run performance tests.${NC}"
    exit 1
fi

# Check if JMeter is available
if ! command -v jmeter &> /dev/null; then
    echo -e "${YELLOW}⚠️  JMeter not found. JMeter tests will be skipped.${NC}"
    JMETER_AVAILABLE=false
else
    JMETER_AVAILABLE=true
    echo -e "${GREEN}✅ JMeter found. JMeter tests will be included.${NC}"
fi

# Create reports directory
mkdir -p target/performance-reports
REPORTS_DIR="target/performance-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${YELLOW}📊 Reports will be saved to: $REPORTS_DIR${NC}"

# 1. Run JMH Benchmarks
echo -e "${GREEN}🔄 Running JMH Benchmarks...${NC}"
mvn clean compile test-compile
java -jar target/benchmarks.jar -rf json -rff "$REPORTS_DIR/jmh-results-$TIMESTAMP.json" > "$REPORTS_DIR/jmh-output-$TIMESTAMP.txt" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ JMH Benchmarks completed successfully${NC}"
else
    echo -e "${RED}❌ JMH Benchmarks failed${NC}"
fi

# 2. Run JUnit Performance Tests
echo -e "${GREEN}🔄 Running JUnit Performance Tests...${NC}"
mvn test -Dtest="*PerformanceTest,*LoadTest" > "$REPORTS_DIR/junit-performance-$TIMESTAMP.txt" 2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ JUnit Performance Tests completed successfully${NC}"
else
    echo -e "${RED}❌ JUnit Performance Tests failed${NC}"
fi

# 3. Run JMeter Tests (if available)
if [ "$JMETER_AVAILABLE" = true ]; then
    echo -e "${GREEN}🔄 Running JMeter Load Tests...${NC}"
    
    # Start application in background for JMeter tests
    echo "Starting application for JMeter tests..."
    mvn spring-boot:run > "$REPORTS_DIR/app-startup-$TIMESTAMP.log" 2>&1 &
    APP_PID=$!
    
    # Wait for application to start
    echo "Waiting for application to start..."
    sleep 30
    
    # Run JMeter tests
    jmeter -n -t src/test/jmeter/movies-performance-test.jmx \
           -l "$REPORTS_DIR/jmeter-results-$TIMESTAMP.jtl" \
           -e -o "$REPORTS_DIR/jmeter-html-report-$TIMESTAMP"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ JMeter Tests completed successfully${NC}"
    else
        echo -e "${RED}❌ JMeter Tests failed${NC}"
    fi
    
    # Stop application
    echo "Stopping application..."
    kill $APP_PID 2>/dev/null || true
fi

# 4. Generate Summary Report
echo -e "${GREEN}📋 Generating Performance Test Summary...${NC}"

cat > "$REPORTS_DIR/performance-summary-$TIMESTAMP.md" << EOF
# Movies API Performance Test Summary

**Test Date:** $(date)
**Test Duration:** $(date -d @$((SECONDS)))

## Test Results

### JMH Benchmarks
- Results file: \`jmh-results-$TIMESTAMP.json\`
- Output log: \`jmh-output-$TIMESTAMP.txt\`

### JUnit Performance Tests
- Results log: \`junit-performance-$TIMESTAMP.txt\`

### JMeter Load Tests
EOF

if [ "$JMETER_AVAILABLE" = true ]; then
cat >> "$REPORTS_DIR/performance-summary-$TIMESTAMP.md" << EOF
- Results file: \`jmeter-results-$TIMESTAMP.jtl\`
- HTML Report: \`jmeter-html-report-$TIMESTAMP/index.html\`
EOF
else
cat >> "$REPORTS_DIR/performance-summary-$TIMESTAMP.md" << EOF
- Status: Skipped (JMeter not available)
EOF
fi

cat >> "$REPORTS_DIR/performance-summary-$TIMESTAMP.md" << EOF

## How to View Results

1. **JMH Results**: Open the JSON file in a JSON viewer or convert to CSV
2. **JUnit Results**: Check the output log for detailed test results
3. **JMeter Results**: Open \`jmeter-html-report-$TIMESTAMP/index.html\` in a web browser

## Performance Thresholds

- **Response Time**: < 1000ms for GET requests
- **Throughput**: > 100 requests/second
- **Error Rate**: < 1%
- **Memory Usage**: < 512MB for load tests

## Next Steps

1. Review all test results
2. Compare against performance thresholds
3. Optimize any bottlenecks found
4. Re-run tests after optimizations
EOF

echo -e "${GREEN}🎉 Performance tests completed!${NC}"
echo -e "${YELLOW}📁 Results are available in: $REPORTS_DIR${NC}"
echo -e "${YELLOW}📋 Summary: $REPORTS_DIR/performance-summary-$TIMESTAMP.md${NC}"

# Open results in default browser (if on macOS/Linux with GUI)
if command -v xdg-open &> /dev/null && [ -d "$REPORTS_DIR/jmeter-html-report-$TIMESTAMP" ]; then
    echo -e "${YELLOW}🌐 Opening JMeter HTML report in browser...${NC}"
    xdg-open "$REPORTS_DIR/jmeter-html-report-$TIMESTAMP/index.html" 2>/dev/null || true
fi

