package com.hana8.hanaro.common.converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;

@Component
public class AccountUtil {

	private static byte[] keyBytes;
	public AccountUtil(@Value("${account.key}") String key) {
		keyBytes = key.getBytes(StandardCharsets.UTF_8);
	}

	// 암호화
	public static String encrypt(String accountNum) {
		if (accountNum == null) return null;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(new byte[16]));

			byte[] encrypted = cipher.doFinal(accountNum.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			throw new RuntimeException("암호화 오류", e);
		}
	}

	// 복호화
	public static String decrypt(String encrypted) {
		if (encrypted == null) return null;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(new byte[16]));

			byte[] decoded = Base64.getDecoder().decode(encrypted);
			return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return encrypted;
		}
	}

	public static String mask(String raw) {
		if (raw == null || raw.length() < 11) return raw;
		String clean = raw.replaceAll("[\\s-]", "");
		String masked = clean.substring(0, 3) + "****" + clean.substring(7);
		return masked.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
	}
}
