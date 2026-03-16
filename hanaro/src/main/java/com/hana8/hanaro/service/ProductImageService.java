package com.hana8.hanaro.service;

import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;
import com.hana8.hanaro.mapper.ProductMapper;
import com.hana8.hanaro.repository.ProductImageRepository;
import com.hana8.hanaro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {

	private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;   // 2MB
	private static final long MAX_TOTAL_SIZE = 10L * 1024 * 1024; // 10MB

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final ProductMapper productMapper;
	private final FileService fileService;

	@Transactional
	public List<ProductImageDTO> uploadImages(Long productId, List<MultipartFile> files) {
		Product product = productRepository.findByIdAndDeletedFalse(productId)
			.orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. id=" + productId));

		validateFiles(files);

		String todayPath = getTodayPath();
		List<ProductImage> savedImages = new ArrayList<>();

		int sortOrder = productImageRepository.countByProductId(productId) + 1;

		for (MultipartFile file : files) {
			String saveName = fileService.upload(file, todayPath);

			ProductImage image = ProductImage.builder()
				.product(product)
				.orgName(file.getOriginalFilename())
				.saveName(saveName)
				.saveDir(todayPath)
				.sortOrder(sortOrder++)
				.build();

			savedImages.add(productImageRepository.save(image));
		}

		return productMapper.toImageDTOList(savedImages);
	}

	public ResponseEntity<Resource> downloadImage(Long productId, Long imageId, boolean inline) {
		ProductImage image = productImageRepository.findById(imageId)
			.orElseThrow(() -> new IllegalArgumentException("상품 이미지를 찾을 수 없습니다. id=" + imageId));

		validateProductOwnership(productId, image);

		return fileService.download(image.getSaveDir(), image.getSaveName(), inline);
	}

	@Transactional
	public void deleteImage(Long productId, Long imageId) {
		ProductImage image = productImageRepository.findById(imageId)
			.orElseThrow(() -> new IllegalArgumentException("상품 이미지를 찾을 수 없습니다. id=" + imageId));

		validateProductOwnership(productId, image);

		fileService.delete(image.getSaveName(), image.getSaveDir());
		productImageRepository.delete(image);
	}

	private void validateProductOwnership(Long productId, ProductImage image) {
		if (!image.getProduct().getId().equals(productId)) {
			throw new IllegalArgumentException("해당 상품의 이미지가 아닙니다.");
		}
	}

	private void validateFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}

		long totalSize = 0L;

		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				throw new IllegalArgumentException("비어 있는 파일은 업로드할 수 없습니다.");
			}

			if (file.getSize() > MAX_FILE_SIZE) {
				throw new IllegalArgumentException("파일 1개당 최대 크기는 2MB입니다.");
			}

			totalSize += file.getSize();
		}

		if (totalSize > MAX_TOTAL_SIZE) {
			throw new IllegalArgumentException("전체 파일 크기 합은 10MB를 초과할 수 없습니다.");
		}
	}

	private String getTodayPath() {
		return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	}
}
