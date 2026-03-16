package com.hana8.hanaro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageDTO {
	private Long id;
	@NotBlank
	private String orgName;
	private String saveName;
	private String saveDir;
	private Integer sortOrder;
}
