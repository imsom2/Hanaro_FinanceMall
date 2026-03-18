package com.hana8.hanaro.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;

@Component
public class AccountUtil {

	private final String key;
	public AccountUtil(@Value("${account.key}") String key) {
		this.key = key;
	}


	public String encrypt(String accountNum) {
		if (accountNum == null) return null;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			byte[] iv = new byte[12];
			new SecureRandom().nextBytes(iv);
			GCMParameterSpec spec = new GCMParameterSpec(128, iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
			byte[] encrypted = cipher.doFinal(accountNum.getBytes(StandardCharsets.UTF_8));
			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			throw new RuntimeException("계좌번호 암호화 중 오류 발생", e);
		}
	}

	public String decrypt(String encryptedAccountNum) {
		if (encryptedAccountNum == null) return null;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			byte[] decoded = Base64.getDecoder().decode(encryptedAccountNum);
			byte[] iv = new byte[12];
			System.arraycopy(decoded, 0, iv, 0, 12);
			GCMParameterSpec spec = new GCMParameterSpec(128, iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
			return new String(cipher.doFinal(decoded, 12, decoded.length - 12), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("계좌번호 복호화 중 오류 발생", e);
		}
	}

	public String format(String rawAccountNum) {
		if (rawAccountNum == null || rawAccountNum.length() != 11) {
			return rawAccountNum;
		}
		return rawAccountNum.substring(0, 3) + "-" +
			rawAccountNum.substring(3, 9) + "-" +
			rawAccountNum.substring(9);
	}

	public String mask(String rawAccountNum) {
		if (rawAccountNum == null || rawAccountNum.length() < 8) return rawAccountNum;
		return rawAccountNum.substring(0, 4) + "****" + rawAccountNum.substring(8);
	}
}
