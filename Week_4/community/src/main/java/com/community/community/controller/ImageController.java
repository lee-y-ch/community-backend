package com.community.community.controller;

import com.community.community.ApiResponse;
import com.community.community.dto.ImageUploadResponseDTO;
import com.community.community.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(
            @RequestPart("image") MultipartFile image,
            @RequestParam("type") String type
    ) {
        try {
            ImageUploadResponseDTO data = imageService.uploadImage(image, type);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>("image_upload_success", data)
            );

            // 이미지 파일이 없거나 type 값이 잘못된 경우: 400
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 지원하지 않는 이미지 형식인 경우: 415
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                    new ApiResponse<>(e.getMessage(), null)
            );

            // 파일 저장 중 서버 오류가 발생한 경우: 500
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("internal_server_error", null)
            );
        }
    }
}
