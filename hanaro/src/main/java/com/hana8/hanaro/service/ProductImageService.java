package com.hana8.hanaro.service;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;
import com.hana8.hanaro.mapper.ProductMapper;
import com.hana8.hanaro.repository.ProductImageRepository;
import com.hana8.hanaro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
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
		int fileCount = files == null ? 0 : files.size();
		log.info("이미지 업로드 시작: productId={}, fileCount={}", productId, fileCount);

		Product product = productRepository.findByIdAndDeletedFalse(productId)
			.orElseThrow(() -> {
				log.warn("상품을 찾을 수 없음: productId={}", productId);
				return new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품이 존재하지 않습니다. id=" + productId);
			});
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
				log.info("파일 저장 완료: orgName={}, saveName={}", file.getOriginalFilename(), saveName);
			}
		} catch (Exception e) {
			log.error("이미지 업로드 중 오류 발생, 롤백 시작: productId={}", productId, e);
			for (String saveName : uploadedFileNames) {
				fileService.delete(saveName, todayPath);
			}
			throw e;
		}
		log.info("모든 이미지 업로드 완료: productId={}, count={}", productId, savedImages.size());
		return productMapper.toImageDTOList(savedImages);
	}

	public ResponseEntity<Resource> downloadImage(Long productId, Long imageId, boolean inline) {
		log.info("이미지 다운로드 요청: productId={}, imageId={}", productId, imageId);

		ProductImage image = productImageRepository.findByIdAndDeletedFalse(imageId)
			.orElseThrow(() -> {
				log.warn("이미지 없음 또는 삭제됨: imageId={}", imageId);
				return new BusinessException(ErrorCode.IMAGE_NOT_FOUND, "이미지를 찾을 수 없거나 삭제된 상태입니다. id=" + imageId);
			});

		validateImageOwnership(productId, image);
		if (image.getProduct().isDeleted()) {
			log.warn("연결된 상품이 삭제됨: productId={}", productId);
			throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
		}

		return fileService.download(image.getSaveDir(), image.getSaveName(), inline);
	}

	@Transactional
	public void deleteImage(Long productId, Long imageId) {
		log.info("이미지 삭제 시도: productId={}, imageId={}", productId, imageId);

		ProductImage image = productImageRepository.findById(imageId)
			.orElseThrow(() -> {
				log.warn("이미지 찾을 수 없음: imageId={}", imageId);
				return new BusinessException(ErrorCode.IMAGE_NOT_FOUND, "이미지를 찾을 수 없거나 삭제된 상태입니다. id=" + imageId);
			});
		validateImageOwnership(productId, image);
		image.setDeleted(true);
		log.info("이미지 삭제(Soft Delete) 완료: imageId={}", imageId);
	}

	private void validateImageOwnership(Long productId, ProductImage image) {
		if (!image.getProduct().getId().equals(productId)) {
			throw new BusinessException(ErrorCode.IMAGE_NOT_OWNED);
		}
	}

	private void validateFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			throw new BusinessException(ErrorCode.IMAGE_EMPTY);
		}

		long totalSize = 0L;

		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				throw new BusinessException(ErrorCode.IMAGE_EMPTY);
			}

			long fileSize = file.getSize();
			if (fileSize > MAX_FILE_SIZE) {
				throw new BusinessException(ErrorCode.IMAGE_TOO_LARGE);
			}
			totalSize += fileSize;
			if (totalSize > MAX_TOTAL_SIZE) {
				throw new BusinessException(ErrorCode.IMAGE_TOTAL_TOO_LARGE);
			}

			String contentType = file.getContentType();
			if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
				throw new BusinessException(ErrorCode.IMAGE_INVALID_TYPE,
					ErrorCode.IMAGE_INVALID_TYPE.getMessage() + ": " + file.getOriginalFilename());
			}

			try {
				byte[] bytes = file.getBytes();
				BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
				if (img == null) {
					throw new BusinessException(ErrorCode.INVALID_FILE,
						"유효하지 않은 이미지 파일입니다: " + file.getOriginalFilename());
				}
			} catch (IOException e) {
				throw new BusinessException(ErrorCode.INVALID_FILE,
					"이미지 파일을 읽을 수 없습니다: " + file.getOriginalFilename());
			}
		}
	}

	private String getTodayPath() {
		return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	}
}
