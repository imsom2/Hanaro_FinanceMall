package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.auth.SignUpDTO;
import com.hana8.hanaro.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

	SignUpDTO toSignUpDTO(User user);
}
