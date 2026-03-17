package com.hana8.hanaro.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Component
public class AccountUtil {

	private static String algorithm;
	private static String key;

	@Value("${account.algorithm}")
	public void setAlgorithm(String value) {
		algorithm = value;
	}

	@Value("${account.key}")
	public void setKey(String value) {
		key = value;
	}

	public static String encrypt(String accountNum) {
		if (accountNum == null) return null;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encrypted = cipher.doFinal(accountNum.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			throw new RuntimeException("계좌번호 암호화 중 오류 발생", e);
		}
	}

	public static String decrypt(String encryptedAccountNum) {
		if (encryptedAccountNum == null) return null;
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decoded = Base64.getDecoder().decode(encryptedAccountNum);
			return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("계좌번호 복호화 중 오류 발생", e);
		}
	}

	public static String format(String rawAccountNum) {
		if (rawAccountNum == null || rawAccountNum.length() != 11) {
			return rawAccountNum;
		}
		return rawAccountNum.substring(0, 3) + "-" +
			rawAccountNum.substring(3, 9) + "-" +
			rawAccountNum.substring(9);
	}

	public static String decryptAndFormat(String encryptedAccountNum) {
		String decrypted = decrypt(encryptedAccountNum);
		return format(decrypted);
	}
}
