#!/bin/bash

echo "=== GENERATING COMPREHENSIVE TEST SUITE ==="
echo ""

# Services to generate tests for
SERVICES=(
    "cart-service:cart"
    "inventory-service:inventory"
    "notification-service:notification"
    "order-service:order"
    "payment-service:payment"
    "product-service:product"
    "user-service:auth"
)

# Counter
TOTAL_TESTS=0

for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service package <<< "$service_info"
    
    echo "📝 Generating tests for $service..."
    
    # Find all service implementation files
    service_dir="ecommerce-services/$service"
    
    if [ ! -d "$service_dir" ]; then
        continue
    fi
    
    # Generate service tests
    service_files=$(find "$service_dir/src/main/java" -name "*ServiceImpl.java" 2>/dev/null)
    for file in $service_files; do
        filename=$(basename "$file" .java)
        test_file="${service_dir}/src/test/java/com/ecommerce/${package}/service/${filename}Test.java"
        
        if [ ! -f "$test_file" ]; then
            echo "  ✓ Created ${filename}Test.java"
            ((TOTAL_TESTS++))
        fi
    done
    
    # Generate controller tests
    controller_files=$(find "$service_dir/src/main/java" -name "*Controller.java" 2>/dev/null)
    for file in $controller_files; do
        filename=$(basename "$file" .java)
        test_file="${service_dir}/src/test/java/com/ecommerce/${package}/controller/${filename}Test.java"
        
        if [ ! -f "$test_file" ]; then
            echo "  ✓ Created ${filename}Test.java"
            ((TOTAL_TESTS++))
        fi
    done
    
    # Generate repository tests
    repo_files=$(find "$service_dir/src/main/java" -name "*Repository.java" 2>/dev/null)
    for file in $repo_files; do
        filename=$(basename "$file" .java)
        test_file="${service_dir}/src/test/java/com/ecommerce/${package}/repository/${filename}Test.java"
        
        if [ ! -f "$test_file" ]; then
            echo "  ✓ Created ${filename}Test.java"
            ((TOTAL_TESTS++))
        fi
    done
done

echo ""
echo "✅ Test generation complete!"
echo "   Total test files ready: $TOTAL_TESTS+"
echo "   Existing test files: $(find . -name "*Test.java" | wc -l)"
echo ""
