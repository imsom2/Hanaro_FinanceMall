package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.ProductDTO;
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
	ProductDTO toDTO(ProductRequestDTO requestDto);

	ProductDTO toDTO(Product product);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "images", ignore = true)
	Product toEntity(ProductDTO dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "images", ignore = true)
	Product toEntity(ProductRequestDTO dto);

	List<ProductDTO> toDTOList(List<Product> products);

	@Mapping(target = "thumbImage", ignore = true)
	ProductListDTO toListDTO(Product product);

	List<ProductListDTO> toListDTOList(List<Product> products);

	ProductImageDTO toImageDTO(ProductImage image);

	List<ProductImageDTO> toImageDTOList(List<ProductImage> images);
}
