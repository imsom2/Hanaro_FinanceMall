package com.hana8.hanaro.service;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.ProductDetailDTO;
import com.hana8.hanaro.dto.ProductListDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.mapper.ProductMapper;
import com.hana8.hanaro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository repository;
	private final ProductMapper mapper;

	public List<ProductListDTO> getProducts() {
		return repository.findByDeletedFalse()
			.stream()
			.map(product -> {
				ProductListDTO dto = mapper.toListDTO(product);
				product.getImages().stream()
					.filter(img -> !img.isDeleted())
					.findFirst()
					.ifPresent(img -> dto.setThumbImage(mapper.toImageDTO(img)));
				return dto;
			})
			.toList();
	}

	public ProductDetailDTO getProduct(Long id) {
		Product product = repository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품이 존재하지 않습니다. id=" + id));

		ProductDetailDTO dto = mapper.toDTO(product);
		dto.setImages(
			product.getImages().stream()
				.filter(img -> !img.isDeleted())
				.map(mapper::toImageDTO)
				.toList()
		);

		return dto;
	}

	@Transactional
	public ProductDetailDTO createProduct(ProductDetailDTO dto) {
		log.info("새로운 상품 등록 시도: name={}", dto.getProductName());
		Product product = repository.save(mapper.toEntity(dto));
		log.info("상품 등록 완료: ID={}, name={}", product.getId(), product.getProductName());
		return mapper.toDTO(product);
	}

	@Transactional
	public ProductDetailDTO updateProduct(Long id, ProductDetailDTO dto) {
		log.info("상품 수정 시도: ID={}, targetName={}", id, dto.getProductName());
		Product product = repository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> {
				log.warn("수정 대상 상품 없음: ID={}", id);
				return new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품이 존재하지 않습니다. id=" + id);
			});
		product.setProductName(dto.getProductName());
		product.setProductType(dto.getProductType());
		product.setMin(dto.getMin());
		product.setMax(dto.getMax());
		product.setPeriod(dto.getPeriod());
		product.setMaturityYield(dto.getMaturityYield());
		product.setCancelYield(dto.getCancelYield());
		product.setDescription(dto.getDescription());

		log.info("상품 정보 수정 완료: ID={}", id);
		return mapper.toDTO(product);
	}

	@Transactional
	public void deleteProduct(Long id) {
		log.info("상품 삭제 시도: ID={}", id);
		Product product = repository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> {
				log.warn("삭제 대상 상품 없음: ID={}", id);
				return new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "상품이 존재하지 않습니다. id=" + id);
			});
		product.setDeleted(true);
		product.getImages().forEach(image -> image.setDeleted(true));
		log.info("상품 및 관련 이미지 삭제 처리 완료: ID={}", id);
	}
}
