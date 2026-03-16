package com.hana8.hanaro.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ProductImageRequestDTO {
	Long memberId;

	@NotEmpty
	List<MultipartFile> files;
}
