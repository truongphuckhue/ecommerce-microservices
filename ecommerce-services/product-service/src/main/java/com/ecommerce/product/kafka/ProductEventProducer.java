package com.ecommerce.product.kafka;

import com.ecommerce.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private static final String PRODUCT_TOPIC = "product-events";
    
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public void sendProductCreatedEvent(Product product) {
        ProductEvent event = buildProductEvent(product, "CREATED");
        sendEvent(event);
    }

    public void sendProductUpdatedEvent(Product product) {
        ProductEvent event = buildProductEvent(product, "UPDATED");
        sendEvent(event);
    }

    public void sendProductDeletedEvent(Long productId) {
        ProductEvent event = ProductEvent.builder()
                .eventType("DELETED")
                .productId(productId)
                .timestamp(LocalDateTime.now())
                .build();
        sendEvent(event);
    }

    public void sendStockChangedEvent(Product product) {
        ProductEvent event = buildProductEvent(product, "STOCK_CHANGED");
        sendEvent(event);
    }

    private void sendEvent(ProductEvent event) {
        try {
            CompletableFuture<SendResult<String, ProductEvent>> future = 
                kafkaTemplate.send(PRODUCT_TOPIC, event.getProductId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Product event sent successfully: eventType={}, productId={}, offset={}", 
                            event.getEventType(), event.getProductId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send product event: eventType={}, productId={}", 
                            event.getEventType(), event.getProductId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending product event: eventType={}, productId={}", 
                    event.getEventType(), event.getProductId(), e);
        }
    }

    private ProductEvent buildProductEvent(Product product, String eventType) {
        return ProductEvent.builder()
                .eventType(eventType)
                .productId(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
