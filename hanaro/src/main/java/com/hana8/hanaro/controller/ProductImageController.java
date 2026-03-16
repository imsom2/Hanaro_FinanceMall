package com.hana8.hanaro.controller;

import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

	private final ProductImageService productImageService;

	// 이미지 등록
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public List<ProductImageDTO> uploadImages(
		@PathVariable Long productId,
		@RequestPart("files") List<MultipartFile> files
	) {
		return productImageService.uploadImages(productId, files);
	}

	// 이미지 다운로드
	@GetMapping("/{imageId}")
	ResponseEntity<Resource> download(
		@PathVariable Long productId,
		@PathVariable Long imageId,
		@RequestParam(defaultValue = "false") boolean inline) {
		return productImageService.downloadImage(productId, imageId, inline);
	}

	// 이미지 삭제
	@DeleteMapping("/{imageId}")
	public ResponseEntity<Void> deleteImage(
		@PathVariable Long productId,
		@PathVariable Long imageId
	) {
		productImageService.deleteImage(productId, imageId);
		return ResponseEntity.ok().build();
	}
}
