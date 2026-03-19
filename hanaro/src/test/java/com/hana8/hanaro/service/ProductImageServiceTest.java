package com.hana8.hanaro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;
import com.hana8.hanaro.mapper.ProductMapper;
import com.hana8.hanaro.repository.ProductImageRepository;
import com.hana8.hanaro.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

	@InjectMocks
	private ProductImageService productImageService;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductImageRepository productImageRepository;

	@Mock
	private ProductMapper productMapper;

	@Mock
	private FileService fileService;

	private static final Long PRODUCT_ID = 1L;
	private static final Long IMAGE_ID = 10L;

	private byte[] validPngBytes;
	private Product product;

	@BeforeEach
	void setUp() throws IOException {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		validPngBytes = baos.toByteArray();

		product = new Product();
		product.setId(PRODUCT_ID);
		product.setProductName("테스트 상품");
		product.setProductType(ProductType.SAVINGS);
	}

	// ── uploadImages ─────────────────────────────────────────────────────

	@Test
	void uploadImages_success() throws Exception {
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getSize()).willReturn(100L);
		given(file.getContentType()).willReturn("image/png");
		given(file.getOriginalFilename()).willReturn("test.png");
		given(file.getBytes()).willReturn(validPngBytes);

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));
		given(fileService.upload(any(), any())).willReturn("saved_uuid.png");

		ProductImage savedImage = ProductImage.builder()
			.orgName("test.png").saveName("saved_uuid.png").saveDir("20260319").product(product).build();
		given(productImageRepository.save(any())).willReturn(savedImage);
		given(productMapper.toImageDTOList(any())).willReturn(List.of(new ProductImageDTO()));

		List<ProductImageDTO> result = productImageService.uploadImages(PRODUCT_ID, List.of(file));

		assertThat(result).hasSize(1);
	}

	@Test
	void uploadImages_productNotFound_throws() {
		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(mock(MultipartFile.class))))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_filesNull_throws() {
		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, null))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_emptyFilesList_throws() {
		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of()))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_fileTooLarge_throws() {
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getSize()).willReturn(3 * 1024 * 1024L); // 3MB > 2MB limit

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(file)))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_invalidContentType_throws() {
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getSize()).willReturn(100L);
		given(file.getContentType()).willReturn("text/plain");

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(file)))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_nullContentType_throws() {
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getSize()).willReturn(100L);
		given(file.getContentType()).willReturn(null);

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(file)))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_invalidImageBytes_throws() throws Exception {
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getSize()).willReturn(100L);
		given(file.getContentType()).willReturn("image/png");
		given(file.getOriginalFilename()).willReturn("fake.png");
		given(file.getBytes()).willReturn(new byte[]{1, 2, 3}); // 유효하지 않은 이미지

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(file)))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_ioException_throws() throws Exception {
		MultipartFile file = mock(MultipartFile.class);
		given(file.isEmpty()).willReturn(false);
		given(file.getSize()).willReturn(100L);
		given(file.getContentType()).willReturn("image/png");
		given(file.getOriginalFilename()).willReturn("test.png");
		given(file.getBytes()).willThrow(new IOException("read failed"));

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(file)))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void uploadImages_totalSizeExceedsLimit_throws() throws Exception {
		// 파일 6개 각 2MB(허용 한도) → 합계 12MB > 10MB(총합 한도 초과)
		// 6번째 파일에서 총합 초과 체크가 걸려야 하므로 6번째는 contentType 등 불필요
		// validateFiles 는 반복하면서 totalSize 누적 검사를 하므로
		// 5번째까지(각 2MB)는 통과, 6번째에서 12MB > 10MB 로 예외 발생
		long size = 2 * 1024 * 1024L; // 정확히 2MB (파일당 MAX_FILE_SIZE)

		MultipartFile f1 = makeMockFile(size);
		MultipartFile f2 = makeMockFile(size);
		MultipartFile f3 = makeMockFile(size);
		MultipartFile f4 = makeMockFile(size);
		MultipartFile f5 = makeMockFile(size);
		// 6번째 파일: totalSize = 12MB > 10MB 에서 예외 발생(getContentType, getBytes 호출 전)
		MultipartFile f6 = mock(MultipartFile.class);
		given(f6.isEmpty()).willReturn(false);
		given(f6.getSize()).willReturn(size);

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));

		assertThatThrownBy(() ->
			productImageService.uploadImages(PRODUCT_ID, List.of(f1, f2, f3, f4, f5, f6)))
			.isInstanceOf(BusinessException.class);
	}

	private MultipartFile makeMockFile(long size) throws IOException {
		MultipartFile f = mock(MultipartFile.class);
		given(f.isEmpty()).willReturn(false);
		given(f.getSize()).willReturn(size);
		given(f.getContentType()).willReturn("image/png");
		given(f.getBytes()).willReturn(validPngBytes);
		return f;
	}

	@Test
	void uploadImages_uploadFails_rollsBack() throws Exception {
		MultipartFile file1 = mock(MultipartFile.class);
		given(file1.isEmpty()).willReturn(false);
		given(file1.getSize()).willReturn(100L);
		given(file1.getContentType()).willReturn("image/png");
		given(file1.getOriginalFilename()).willReturn("test1.png");
		given(file1.getBytes()).willReturn(validPngBytes);

		MultipartFile file2 = mock(MultipartFile.class);
		given(file2.isEmpty()).willReturn(false);
		given(file2.getSize()).willReturn(100L);
		given(file2.getContentType()).willReturn("image/png");
		given(file2.getBytes()).willReturn(validPngBytes);

		given(productRepository.findByIdAndDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));
		given(fileService.upload(eq(file1), any())).willReturn("file1.png");
		given(fileService.upload(eq(file2), any())).willThrow(new RuntimeException("disk full"));
		given(productImageRepository.save(any())).willReturn(
			ProductImage.builder().orgName("test1.png").saveName("file1.png").saveDir("test").product(product).build());

		assertThatThrownBy(() -> productImageService.uploadImages(PRODUCT_ID, List.of(file1, file2)))
			.isInstanceOf(RuntimeException.class);

		verify(fileService).delete(eq("file1.png"), any(String.class));
	}

	// ── downloadImage ─────────────────────────────────────────────────────

	@Test
	@SuppressWarnings("unchecked")
	void downloadImage_success() {
		ProductImage image = ProductImage.builder()
			.orgName("test.png").saveName("saved.png").saveDir("/upload")
			.deleted(false).product(product).build();
		image.setId(IMAGE_ID);

		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.of(image));
		given(fileService.download(any(), any(), anyBoolean()))
			.willReturn((ResponseEntity) ResponseEntity.ok().build());

		ResponseEntity<Resource> result = productImageService.downloadImage(PRODUCT_ID, IMAGE_ID, true);

		assertThat(result.getStatusCode().value()).isEqualTo(200);
	}

	@Test
	void downloadImage_imageNotFound_throws() {
		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> productImageService.downloadImage(PRODUCT_ID, IMAGE_ID, false))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void downloadImage_productDeleted_throws() {
		product.setDeleted(true);
		ProductImage image = ProductImage.builder()
			.orgName("test.png").saveName("saved.png").saveDir("/upload")
			.deleted(false).product(product).build();
		image.setId(IMAGE_ID);

		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.of(image));

		assertThatThrownBy(() -> productImageService.downloadImage(PRODUCT_ID, IMAGE_ID, false))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void downloadImage_notOwned_throws() {
		Product otherProduct = new Product();
		otherProduct.setId(999L);
		ProductImage image = ProductImage.builder()
			.orgName("test.png").saveName("saved.png").saveDir("/upload")
			.deleted(false).product(otherProduct).build();
		image.setId(IMAGE_ID);

		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.of(image));

		assertThatThrownBy(() -> productImageService.downloadImage(PRODUCT_ID, IMAGE_ID, false))
			.isInstanceOf(BusinessException.class);
	}

	// ── deleteImage ───────────────────────────────────────────────────────

	@Test
	void deleteImage_success() {
		ProductImage image = ProductImage.builder()
			.orgName("test.png").saveName("saved.png").saveDir("/upload")
			.deleted(false).product(product).build();
		image.setId(IMAGE_ID);

		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.of(image));

		productImageService.deleteImage(PRODUCT_ID, IMAGE_ID);

		assertThat(image.isDeleted()).isTrue();
	}

	@Test
	void deleteImage_imageNotFound_throws() {
		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> productImageService.deleteImage(PRODUCT_ID, IMAGE_ID))
			.isInstanceOf(BusinessException.class);
	}

	@Test
	void deleteImage_notOwned_throws() {
		Product otherProduct = new Product();
		otherProduct.setId(999L);
		ProductImage image = ProductImage.builder()
			.orgName("test.png").saveName("saved.png").saveDir("/upload")
			.deleted(false).product(otherProduct).build();
		image.setId(IMAGE_ID);

		given(productImageRepository.findByIdAndDeletedFalse(IMAGE_ID)).willReturn(Optional.of(image));

		assertThatThrownBy(() -> productImageService.deleteImage(PRODUCT_ID, IMAGE_ID))
			.isInstanceOf(BusinessException.class);
	}
}
