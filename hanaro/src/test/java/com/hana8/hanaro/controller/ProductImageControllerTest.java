package com.hana8.hanaro.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.hana8.hanaro.service.ProductImageService;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductImageControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProductImageService productImageService;

	@Test
	@DisplayName("상품 이미지 다운로드 - attachment")
	void downloadImageTest() throws Exception {

		Long productId = 1L;
		Long imageId = 10L;

		Resource resource = new ByteArrayResource("test-image".getBytes());

		ResponseEntity<Resource> response = ResponseEntity.ok()
			.contentType(MediaType.IMAGE_JPEG)
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.jpg")
			.body(resource);

		given(productImageService.downloadImage(productId, imageId, false))
			.willReturn(response);

		mvc.perform(get("/api/products/{productId}/images/{imageId}", productId, imageId))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.jpg"))
			.andExpect(content().contentType(MediaType.IMAGE_JPEG));
	}

	@Test
	@DisplayName("상품 이미지 다운로드 - inline")
	void downloadImageInlineTest() throws Exception {

		Long productId = 1L;
		Long imageId = 10L;

		Resource resource = new ByteArrayResource("test-image".getBytes());

		ResponseEntity<Resource> response = ResponseEntity.ok()
			.contentType(MediaType.IMAGE_JPEG)
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=test.jpg")
			.body(resource);

		given(productImageService.downloadImage(productId, imageId, true))
			.willReturn(response);

		mvc.perform(get("/api/products/{productId}/images/{imageId}", productId, imageId)
				.param("inline", "true"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=test.jpg"))
			.andExpect(content().contentType(MediaType.IMAGE_JPEG));
	}
}
