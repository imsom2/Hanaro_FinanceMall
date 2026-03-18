package com.hana8.hanaro.controller.user;

import com.hana8.hanaro.service.ProductImageService;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/images")
@Tag(name = "상품", description = "예적금 상품 관련 일반 USER용 API입니다")
public class ProductImageController {

	private final ProductImageService productImageService;

	@Operation(summary = "상품 이미지 다운로드", description = "이미지 파일을 다운로드하거나 브라우저에서 표시합니다.")
	@GetMapping("/{imageId}")
	public ResponseEntity<Resource> download(
		@PathVariable Long productId,
		@PathVariable Long imageId,
		@RequestParam(defaultValue = "false") boolean inline
	) {
		return productImageService.downloadImage(productId, imageId, inline);
	}
}
