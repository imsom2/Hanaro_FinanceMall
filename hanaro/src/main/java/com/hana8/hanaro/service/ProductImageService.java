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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {

	private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
	private static final long MAX_TOTAL_SIZE = 10L * 1024 * 1024;
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg", "image/png", "image/gif", "image/webp"
	);

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
		List<String> uploadedFileNames = new ArrayList<>();

		try {
			for (MultipartFile file : files) {
				String saveName = fileService.upload(file, todayPath);
				uploadedFileNames.add(saveName);

				ProductImage image = ProductImage.builder()
					.product(product)
					.orgName(file.getOriginalFilename())
					.saveName(saveName)
					.saveDir(todayPath)
					.build();

				savedImages.add(productImageRepository.save(image));
			}
		} catch (Exception e) {
			for (String saveName : uploadedFileNames) {
				fileService.delete(saveName, todayPath);
			}
			throw e;
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

		String saveName = image.getSaveName();
		String saveDir = image.getSaveDir();

		// ✅ DB 먼저 삭제 후, 트랜잭션 커밋 이후에 파일 삭제 (정합성 보장)
		productImageRepository.delete(image);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				fileService.delete(saveName, saveDir);
			}
		});
	}

	private void validateProductOwnership(Long productId, ProductImage image) {
		if (!image.getProduct().getId().equals(productId)) {
			throw new IllegalArgumentException("해당 상품의 이미지가 아닙니다.");
		}
		if (image.getProduct().isDeleted()) {
			throw new IllegalArgumentException("삭제된 상품의 이미지에는 접근할 수 없습니다.");
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

			String contentType = file.getContentType();
			if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
				throw new IllegalArgumentException(
					"지원하지 않는 파일 형식입니다. (jpeg, png, gif, webp만 허용): " + file.getOriginalFilename());
			}

			try {
				BufferedImage img = ImageIO.read(file.getInputStream());
				if (img == null) {
					throw new IllegalArgumentException(
						"유효하지 않은 이미지 파일입니다: " + file.getOriginalFilename());
				}
			} catch (IOException e) {
				throw new IllegalArgumentException(
					"이미지 파일을 읽을 수 없습니다: " + file.getOriginalFilename());
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
