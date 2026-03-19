package com.hana8.hanaro.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.service.ProductImageService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductImageAdminControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProductImageService productImageService;

	@Test
	@DisplayName("상품 이미지 업로드")
	void uploadImagesTest() throws Exception {

		Long productId = 1L;

		MockMultipartFile file1 = new MockMultipartFile(
			"files",
			"test1.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"image1".getBytes()
		);

		MockMultipartFile file2 = new MockMultipartFile(
			"files",
			"test2.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"image2".getBytes()
		);

		ProductImageDTO dto1 = new ProductImageDTO();
		dto1.setId(1L);
		dto1.setOrgName("test1.jpg");
		dto1.setSaveName("save1.jpg");
		dto1.setSaveDir("/images");

		ProductImageDTO dto2 = new ProductImageDTO();
		dto2.setId(2L);
		dto2.setOrgName("test2.jpg");
		dto2.setSaveName("save2.jpg");
		dto2.setSaveDir("/images");

		given(productImageService.uploadImages(eq(productId), anyList()))
			.willReturn(List.of(dto1, dto2));

		mvc.perform(multipart("/api/admin/products/{productId}/images", productId)
				.file(file1)
				.file(file2))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].id").value(1))
			.andExpect(jsonPath("$.data[1].id").value(2));
	}

	@Test
	@DisplayName("상품 이미지 삭제")
	void deleteImageTest() throws Exception {

		Long productId = 1L;
		Long imageId = 10L;

		mvc.perform(delete("/api/admin/products/{productId}/images/{imageId}", productId, imageId))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}
}
