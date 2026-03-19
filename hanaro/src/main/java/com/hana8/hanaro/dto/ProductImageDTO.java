package com.hana8.hanaro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 이미지 응답 DTO")
public class ProductImageDTO {
	@Schema(description = "이미지 ID", example = "10")
	private Long id;

	@Schema(description = "원본 파일명", example = "banner.png")
	private String orgName;

	@Schema(description = "서버 저장 파일명", example = "uuid_banner.png")
	private String saveName;

	@Schema(description = "서버 저장 경로", example = "20211218")
	private String saveDir;
}
