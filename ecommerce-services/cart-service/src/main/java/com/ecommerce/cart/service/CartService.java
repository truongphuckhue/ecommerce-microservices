package com.ecommerce.cart.service;
import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.entity.*;
import com.ecommerce.cart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    
    @Transactional
    public CartResponse getOrCreateCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> cartRepository.save(
                Cart.builder().userId(userId).build()
            ));
        return toCartResponse(cart);
    }
    
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> cartRepository.save(
                Cart.builder().userId(userId).build()
            ));
        
        CartItem existingItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), request.getProductId())
            .orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .productId(request.getProductId())
                .productName(request.getProductName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .build();
            cart.addItem(newItem);
            cartRepository.save(cart);
        }
        
        return toCartResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        
        return toCartResponse(cart);
    }
    
    @Transactional
    public CartResponse removeFromCart(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        cart.removeItem(item);
        cartItemRepository.delete(item);
        
        return toCartResponse(cart);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.clearItems();
        cartRepository.save(cart);
    }
    
    private CartResponse toCartResponse(Cart cart) {
        return CartResponse.builder()
            .id(cart.getId())
            .userId(cart.getUserId())
            .items(cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList()))
            .totalPrice(cart.getTotalPrice())
            .totalItems(cart.getTotalItems())
            .build();
    }
    
    private CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .price(item.getPrice())
            .quantity(item.getQuantity())
            .imageUrl(item.getImageUrl())
            .subtotal(item.getSubtotal())
            .build();
    }
}
