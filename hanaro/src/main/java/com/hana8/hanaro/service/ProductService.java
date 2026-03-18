package com.hana8.hanaro.service;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.ProductDTO;
import com.hana8.hanaro.dto.ProductListDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.mapper.ProductMapper;
import com.hana8.hanaro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository repository;
	private final ProductMapper mapper;

	// 상품 목록 조회
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

	// 상품 상세 조회
	public ProductDTO getProduct(Long id) {
		Product product = repository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
				"상품이 존재하지 않습니다. id=" + id));
		ProductDTO dto = mapper.toDTO(product);
		if (dto.getImages() != null) {
			dto.setImages(dto.getImages().stream()
				.filter(img -> !img.isDeleted())
				.toList());
		}
		return dto;
	}

	// 상품 등록
	@Transactional
	public ProductDTO createProduct(ProductDTO dto) {
		return mapper.toDTO(repository.save(mapper.toEntity(dto)));
	}

	// 상품 수정
	@Transactional
	public ProductDTO updateProduct(Long id, ProductDTO dto) {
		Product product = repository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
				"상품이 존재하지 않습니다. id=" + id));

		product.setProductName(dto.getProductName());
		product.setProductType(dto.getProductType());
		product.setMin(dto.getMin());
		product.setMax(dto.getMax());
		product.setPeriod(dto.getPeriod());
		product.setMaturityYield(dto.getMaturityYield());
		product.setCancelYield(dto.getCancelYield());
		product.setDescription(dto.getDescription());

		return mapper.toDTO(product);
	}

	// 상품 삭제 (soft delete)
	@Transactional
	public void deleteProduct(Long id) {
		Product product = repository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
				"상품이 존재하지 않습니다. id=" + id));

		product.setDeleted(true);
		product.getImages().forEach(image -> image.setDeleted(true));
	}
}
