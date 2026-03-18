package com.hana8.hanaro.controller;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.service.ProductImageService;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "상품", description = "예적금 상품에 관련한 전체 API입니다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

	private final ProductImageService productImageService;

	@Tag(name = "관리자 - 상품 운영", description = "상품을 관리하는 관리자용 API입니다")
	@Operation(summary = "상품 이미지 업로드 ( 관리자 )", description = "한 개 이상의 상품 이미지를 한 번에 업로드합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
		@ApiResponse(responseCode = "400", description = "파일 형식 또는 크기 오류", content = @Content),
		@ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음", content = @Content)
	})
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponseDTO<List<ProductImageDTO>> uploadImages(
		@PathVariable Long productId,
		@Parameter(description = "업로드할 이미지 파일들")
		@RequestPart("files") List<MultipartFile> files
	) {
		List<ProductImageDTO> data = productImageService.uploadImages(productId, files);
		return SuccessResponseDTO.of(SuccessCode.IMAGE_UPLOADED, data);
	}

	@Operation(summary = "상품 이미지 다운로드", description = "이미지 파일을 다운로드하거나 브라우저에서 표시합니다.")
	@GetMapping("/{imageId}")
	public ResponseEntity<Resource> download(
		@PathVariable Long productId,
		@PathVariable Long imageId,
		@RequestParam(defaultValue = "false") boolean inline
	) {
		return productImageService.downloadImage(productId, imageId, inline);
	}

	@Tag(name = "관리자 - 상품 운영", description = "상품을 관리하는 관리자용 API입니다")
	@Operation(summary = "상품 이미지 삭제 ( 관리자 )", description = "상품 이미지를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content)
	})
	@DeleteMapping("/{imageId}")
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponseDTO<Void> deleteImage(
		@PathVariable Long productId,
		@PathVariable Long imageId
	) {
		productImageService.deleteImage(productId, imageId);
		return SuccessResponseDTO.of(SuccessCode.IMAGE_DELETED);
	}
}
