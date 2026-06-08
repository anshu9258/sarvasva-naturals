package com.sarvasvanaturals.service;

import com.sarvasvanaturals.model.Category;
import com.sarvasvanaturals.model.Product;
import com.sarvasvanaturals.repository.CategoryRepository;
import com.sarvasvanaturals.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<Product> getAllActiveProducts(int page, int size, String sort) {
        Sort sortObj = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> getProductsByCategory(String categorySlug, int page, int size, String sort) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categorySlug));
        Sort sortObj = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return productRepository.findByCategoryAndActiveTrue(category, pageable);
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    public Page<Product> getFeaturedProducts(int page, int size) {
        return productRepository.findByFeaturedTrueAndActiveTrue(PageRequest.of(page, size));
    }

    public List<Product> getTopSellers(int limit) {
        return productRepository.findTopSellers(PageRequest.of(0, limit));
    }

    public Page<Product> searchProducts(String query, int page, int size) {
        return productRepository.searchProducts(query, PageRequest.of(page, size));
    }

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional
    public Product save(Product product) {
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(slugify(product.getName()));
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    private Sort resolveSort(String sort) {
        return switch (sort != null ? sort : "default") {
            case "price_asc"  -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating"     -> Sort.by("averageRating").descending();
            case "newest"     -> Sort.by("createdAt").descending();
            case "bestseller" -> Sort.by("soldCount").descending();
            default           -> Sort.by("featured").descending().and(Sort.by("soldCount").descending());
        };
    }

    private String slugify(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
