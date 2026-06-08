package com.sarvasvanaturals.config;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        try {
            if (userRepository.count() == 0) seedAdmin();
        } catch (Exception e) { log.error("Failed to seed admin: {}", e.getMessage()); }
        try {
            if (categoryRepository.count() == 0) seedCategories();
        } catch (Exception e) { log.error("Failed to seed categories: {}", e.getMessage()); }
        try {
            if (productRepository.count() == 0) seedProducts();
        } catch (Exception e) { log.error("Failed to seed products: {}", e.getMessage()); }
        log.info("✅ Sarvasva Naturals data initialized");
    }

    private void seedAdmin() {
        User admin = User.builder()
                .firstName("Admin")
                .lastName("Sarvasva")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(User.Role.ADMIN)
                .emailVerified(true)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.info("✅ Admin user created: {}", adminEmail);
    }

    private void seedCategories() {
        List<Category> categories = List.of(
            Category.builder().name("Turmeric").slug("turmeric")
                .description("Premium HPLC-verified turmeric varieties")
                .icon("🌿").displayOrder(1).build(),
            Category.builder().name("Cumin & Seeds").slug("cumin-seeds")
                .description("Aromatic seeds from heritage farms")
                .icon("🌱").displayOrder(2).build(),
            Category.builder().name("Pepper").slug("pepper")
                .description("Malabar & tellicherry varieties")
                .icon("⚫").displayOrder(3).build(),
            Category.builder().name("Coriander").slug("coriander")
                .description("Farm-to-table coriander")
                .icon("🌾").displayOrder(4).build(),
            Category.builder().name("Ayurvedic Blends").slug("ayurvedic-blends")
                .description("Traditional medicinal formulations")
                .icon("🍃").displayOrder(5).build(),
            Category.builder().name("Cardamom").slug("cardamom")
                .description("Green & black cardamom from the hills")
                .icon("💚").displayOrder(6).build()
        );
        categoryRepository.saveAll(categories);
        log.info("✅ {} categories created", categories.size());
    }

    private void seedProducts() {
        Category turmeric = categoryRepository.findBySlug("turmeric").orElseThrow();
        Category cumin = categoryRepository.findBySlug("cumin-seeds").orElseThrow();
        Category pepper = categoryRepository.findBySlug("pepper").orElseThrow();
        Category coriander = categoryRepository.findBySlug("coriander").orElseThrow();
        Category ayurveda = categoryRepository.findBySlug("ayurvedic-blends").orElseThrow();
        Category cardamom = categoryRepository.findBySlug("cardamom").orElseThrow();

        List<Product> products = List.of(
            createProduct("Organic Rajapuri Curcumin Turmeric", "organic-rajapuri-curcumin-turmeric",
                "Our flagship Rajapuri turmeric boasts a curcumin content of 9.5%+ — consistently 30% higher than market standard. Sourced from a single heritage farm in Maharashtra and HPLC-tested for every batch.",
                "HPLC-verified, 9.5% curcumin content",
                new BigDecimal("18.00"), new BigDecimal("24.00"),
                turmeric, "Maharashtra, India", "Rajapuri", "100g,200g,500g",
                "turmeric,organic,hplc,curcumin,anti-inflammatory",
                "https://images.unsplash.com/photo-1615485500704-8e990f9900f7?w=600&q=80",
                true, true, 150),

            createProduct("Naguari Whole Cumin Seeds", "naguari-whole-cumin-seeds",
                "Harvested from the Nagaur district of Rajasthan — the undisputed cumin capital of India. These seeds carry an essential oil content of 3.2%, releasing an intensely aromatic fragrance when tempered.",
                "High essential oil content, Nagaur district",
                new BigDecimal("14.00"), new BigDecimal("19.00"),
                cumin, "Nagaur, Rajasthan", "Naguari", "100g,250g,500g,1kg",
                "cumin,seeds,organic,rajasthan",
                "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&q=80",
                true, true, 200),

            createProduct("Kumbhraj Coriander Seeds", "kumbhraj-coriander-seeds",
                "Sourced from Kumbhraj, Madhya Pradesh — known for producing India's finest coriander. Split-tested for volatile oil content above 0.5ml/100g for maximum flavour release.",
                "Aromatic split coriander from MP",
                new BigDecimal("12.00"), new BigDecimal("16.00"),
                coriander, "Kumbhraj, MP", "Bold Split", "100g,250g,500g",
                "coriander,seeds,organic,madhya-pradesh",
                "https://images.unsplash.com/photo-1599789197514-47270cd526b4?w=600&q=80",
                true, false, 180),

            createProduct("Malabar Grade Black Pepper", "malabar-grade-black-pepper",
                "True Malabar pepper from Wayanad, Kerala. Piperine content of 5.8%+ — the bioactive compound responsible for black pepper's therapeutic and flavour properties. ICP-MS tested for heavy metals.",
                "5.8% piperine, Wayanad origin",
                new BigDecimal("16.00"), new BigDecimal("22.00"),
                pepper, "Wayanad, Kerala", "Malabar Grade", "50g,100g,250g,500g",
                "pepper,black-pepper,malabar,piperine,kerala",
                "https://images.unsplash.com/photo-1618662822390-6b1b4b8f9ddd?w=600&q=80",
                true, true, 120),

            createProduct("Green Cardamom Elettaria", "green-cardamom-elettaria",
                "Idukki district's finest green cardamom — shade-grown at 1500m altitude. Volatile oil content exceeds 6%, offering intense floral notes used in both culinary and Ayurvedic contexts.",
                "6%+ volatile oil, Idukki highlands",
                new BigDecimal("28.00"), new BigDecimal("36.00"),
                cardamom, "Idukki, Kerala", "Elettaria", "50g,100g,250g",
                "cardamom,green,idukki,kerala,aromatic",
                "https://images.unsplash.com/photo-1622558355486-c4a69c4e7d68?w=600&q=80",
                true, false, 90),

            createProduct("Trikatu Ayurvedic Blend", "trikatu-ayurvedic-blend",
                "Classic Ayurvedic formulation of three pungent herbs: Long Pepper (Pippali), Black Pepper (Maricha), and Ginger (Shunti). Lab-verified ratios. Supports digestion, bioavailability, and metabolic function.",
                "Classical 3-herb Ayurvedic formula",
                new BigDecimal("22.00"), new BigDecimal("28.00"),
                ayurveda, "Multiple origins", "Classical", "100g,200g",
                "ayurvedic,trikatu,digestive,blend,therapeutic",
                "https://images.unsplash.com/photo-1611073615830-9b4f28e69ee5?w=600&q=80",
                true, false, 75),

            createProduct("Lakadong Turmeric (High Curcumin)", "lakadong-turmeric-high-curcumin",
                "The rarest variety in our collection. Lakadong turmeric from Meghalaya delivers 7-12% curcumin — the world's highest recorded concentration. Each batch digitally certified.",
                "World's highest curcumin: 7-12%",
                new BigDecimal("32.00"), null,
                turmeric, "Meghalaya", "Lakadong", "50g,100g,250g",
                "turmeric,lakadong,premium,meghalaya,high-curcumin",
                "https://images.unsplash.com/photo-1615485500704-8e990f9900f7?w=600&q=80",
                true, true, 60),

            createProduct("Tellicherry Black Pepper Extra Bold", "tellicherry-black-pepper-extra-bold",
                "TGSEB (Tellicherry Garbled Special Extra Bold) grade — peppercorns sized above 4.75mm. The premier specification demanded by Michelin-starred kitchens worldwide.",
                "TGSEB grade, above 4.75mm",
                new BigDecimal("24.00"), new BigDecimal("30.00"),
                pepper, "Tellicherry, Kerala", "TGSEB", "50g,100g,250g",
                "pepper,tellicherry,bold,premium,kerala",
                "https://images.unsplash.com/photo-1618662822390-6b1b4b8f9ddd?w=600&q=80",
                false, true, 80)
        );

        productRepository.saveAll(products);
        log.info("✅ {} products created", products.size());
    }

    private Product createProduct(String name, String slug, String description, String shortDesc,
                                   BigDecimal price, BigDecimal originalPrice, Category category,
                                   String origin, String variety, String weightOptions,
                                   String tags, String imageUrl, boolean featured, boolean bestSeller,
                                   int stock) {
        Product product = Product.builder()
                .name(name).slug(slug).description(description).shortDescription(shortDesc)
                .price(price).originalPrice(originalPrice).category(category)
                .origin(origin).variety(variety).weightOptions(weightOptions)
                .defaultWeight(weightOptions.split(",")[1])
                .tags(tags).featured(featured).bestSeller(bestSeller)
                .stockQuantity(stock).active(true)
                .hplcVerified("true")
                .build();

        ProductImage img = ProductImage.builder()
                .imageUrl(imageUrl).mainImage(true).product(product).build();
        product.getImages().add(img);
        return product;
    }
}
