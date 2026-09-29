package com.sarvasvanaturals.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud.name:}") String cloudName,
                             @Value("${cloudinary.api.key:}") String apiKey,
                             @Value("${cloudinary.api.secret:}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    private synchronized Cloudinary client() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Cloudinary is not configured. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET.");
        }
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true));
        }
        return cloudinary;
    }

    /** Uploads one product image to Cloudinary and returns its https URL. */
    public String uploadProductImage(MultipartFile file) throws IOException {
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new IllegalArgumentException(file.getOriginalFilename() + " is not an image file");
        }
        Map<?, ?> result = client().uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "sarvasva-naturals/products", "resource_type", "image"));
        return (String) result.get("secure_url");
    }
}
