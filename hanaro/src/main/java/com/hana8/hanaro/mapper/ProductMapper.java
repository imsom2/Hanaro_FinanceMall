package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.ProductDetailDTO;
import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.dto.ProductListDTO;
import com.hana8.hanaro.dto.ProductRequestDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
	ProductDetailDTO toDTO(ProductRequestDTO requestDto);

	ProductDetailDTO toDTO(Product product);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "images", ignore = true)
	Product toEntity(ProductDetailDTO dto);

	@Mapping(target = "thumbImage", ignore = true)
	ProductListDTO toListDTO(Product product);

	ProductImageDTO toImageDTO(ProductImage image);

	List<ProductImageDTO> toImageDTOList(List<ProductImage> images);
}
