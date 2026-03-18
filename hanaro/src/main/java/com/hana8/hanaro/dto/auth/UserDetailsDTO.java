package com.hana8.hanaro.dto.auth;

import com.hana8.hanaro.common.enums.Role;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = false)
public class UserDetailsDTO extends User {

	private final Long id;
	private final String email;
	private final String name;
	private final Role role;

	public UserDetailsDTO(Long id, String email, String passwd, String name, Role role) {
		super(email, passwd, List.of(new SimpleGrantedAuthority(role.name())));
		this.id = id;
		this.email = email;
		this.name = name;
		this.role = role;
	}

	public Map<String, Object> getClaims() {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("email", email);
		map.put("name", name);
		map.put("role", role.name());
		return map;
	}
}
