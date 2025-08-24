package com.bakeryshop.service.impl;

import com.bakeryshop.dto.CartItemDTO;
import com.bakeryshop.entity.Cart;
import com.bakeryshop.entity.CartItem;
import com.bakeryshop.entity.Product;
import com.bakeryshop.entity.User;
import com.bakeryshop.repository.CartRepository;
import com.bakeryshop.repository.ProductRepository;
import com.bakeryshop.repository.UserRepository;
import com.bakeryshop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void addToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Số lượng sản phẩm trong kho không đủ");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Số lượng sản phẩm trong kho không đủ");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.updateSubTotal();
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.updateSubTotal();
            cart.addItem(cartItem);
        }

        cart.updateTotalAmount();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = getCart(userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("Số lượng sản phẩm trong kho không đủ");
        }

        cartItem.setQuantity(quantity);
        cartItem.updateSubTotal();
        cart.updateTotalAmount();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getCart(userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));
        
        cart.removeItem(cartItem);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.clearItems();
        cartRepository.save(cart);
    }

    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        Cart cart = getCart(userId);
        return cart.getItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getCartItemCount(Long userId) {
        Cart cart = getCart(userId);
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    public Double calculateTotal(Long userId) {
        Cart cart = getCart(userId);
        return cart.getItems().stream()
                .mapToDouble(item -> item.getSubTotal().doubleValue())
                .sum();
    }

    private Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
    }

    private Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductImage(cartItem.getProduct().getImageUrl());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getProduct().getPrice());
        dto.setSubTotal(cartItem.getSubTotal());
        return dto;
    }
} 