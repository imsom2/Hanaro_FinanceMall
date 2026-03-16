package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.ProductDTO;
import com.hana8.hanaro.dto.ProductImageDTO;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mapping(target = "images", ignore = true)
	ProductDTO toDTO(Product product);

	@Mapping(target = "images", ignore = true)
	Product toEntity(ProductDTO dto);

	List<ProductDTO> toDTOList(List<Product> products);

	List<ProductImageDTO> toImageDTOList(List<ProductImage> images);

}
