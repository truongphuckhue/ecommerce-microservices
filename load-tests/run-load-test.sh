#!/bin/bash

echo "=== E-COMMERCE PLATFORM LOAD TEST ==="
echo ""

# Check if JMeter is installed
if ! command -v jmeter &> /dev/null; then
    echo "❌ JMeter is not installed!"
    echo "   Install: brew install jmeter (macOS) or download from https://jmeter.apache.org"
    exit 1
fi

echo "✓ JMeter found: $(jmeter --version | head -1)"
echo ""

# Create results directory
mkdir -p results
rm -f results/*.jtl results/*.log

echo "🚀 Starting load test..."
echo "   Test plan: jmeter/ecommerce-load-test.jmx"
echo "   Results: results/"
echo ""

# Run JMeter in non-GUI mode
jmeter -n \
    -t jmeter/ecommerce-load-test.jmx \
    -l results/results.jtl \
    -j results/jmeter.log \
    -e \
    -o results/html-report

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✅ Load test completed successfully!"
    echo ""
    echo "📊 Results:"
    echo "   - JTL file: results/results.jtl"
    echo "   - Log file: results/jmeter.log"
    echo "   - HTML Report: results/html-report/index.html"
    echo ""
    echo "Open HTML report:"
    echo "   open results/html-report/index.html"
else
    echo ""
    echo "❌ Load test failed with exit code $EXIT_CODE"
    echo "   Check results/jmeter.log for details"
fi

exit $EXIT_CODE
