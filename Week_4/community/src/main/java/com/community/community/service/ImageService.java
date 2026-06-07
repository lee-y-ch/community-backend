package com.community.community.service;

import com.community.community.dto.ImageUploadResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    private final Path uploadRootPath;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public ImageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadRootPath = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public ImageUploadResponseDTO uploadImage(MultipartFile image, String type) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("invalid_image_upload_request");
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("invalid_image_upload_request");
        }

        if (!type.equals("profile") && !type.equals("post")) {
            throw new IllegalArgumentException("invalid_image_upload_request");
        }

        String contentType = image.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalStateException("unsupported_image_type");
        }

        try {
            Path uploadPath = uploadRootPath.resolve(type);
            Files.createDirectories(uploadPath);

            String originalFilename = image.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;

            // MultipartFile을 실제 저장 경로에 복사하고, 이후 접근 가능한 이미지 URL만 응답으로 반환한다.
            Path filePath = uploadPath.resolve(storedFilename).normalize();

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 현재는 로컬 개발 환경 기준 URL을 반환하고, 추후 S3/CDN으로 저장소를 바꿀 수 있다.
            String imageUrl = "http://localhost:8080/images/" + type + "/" + storedFilename;

            return new ImageUploadResponseDTO(imageUrl);

        } catch (IOException e) {
            throw new RuntimeException("image_upload_failed");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}