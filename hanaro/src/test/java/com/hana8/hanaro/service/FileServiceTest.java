package com.hana8.hanaro.service;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class FileServiceTest {

	private FileService fileService;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		fileService = new FileService();
		ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
	}

	@Test
	void upload_success_nonImageFile() {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"test.txt",
			"text/plain",
			"hello".getBytes()
		);

		String saveName = fileService.upload(file, "20260319");

		assertThat(saveName).endsWith(".txt");
		assertThat(Files.exists(tempDir.resolve("20260319").resolve(saveName))).isTrue();
	}

	@Test
	void upload_fail_whenFileIsNull() {
		assertThatThrownBy(() -> fileService.upload(null, "20260319"))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IMAGE_EMPTY);
			});
	}

	@Test
	void upload_fail_whenFileIsEmpty() {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"empty.txt",
			"text/plain",
			new byte[0]
		);

		assertThatThrownBy(() -> fileService.upload(file, "20260319"))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IMAGE_EMPTY);
			});
	}

	@Test
	void upload_fail_whenInvalidPath() {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"test.txt",
			"text/plain",
			"hello".getBytes()
		);

		assertThatThrownBy(() -> fileService.upload(file, "../hack"))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE);
			});
	}

	@Test
	void download_success_attachment() throws IOException {
		Path dir = tempDir.resolve("20260319");
		Files.createDirectories(dir);
		Path filePath = dir.resolve("sample.txt");
		Files.writeString(filePath, "hello");

		var response = fileService.download("20260319", "sample.txt", false);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getHeaders().getFirst("Content-Disposition"))
			.contains("attachment")
			.contains("sample.txt");
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().exists()).isTrue();
	}

	@Test
	void download_success_inline() throws IOException {
		Path dir = tempDir.resolve("20260319");
		Files.createDirectories(dir);
		Path filePath = dir.resolve("sample.txt");
		Files.writeString(filePath, "hello");

		var response = fileService.download("20260319", "sample.txt", true);

		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getHeaders().getFirst("Content-Disposition"))
			.contains("inline")
			.contains("sample.txt");
	}

	@Test
	void download_fail_whenInvalidPath() {
		assertThatThrownBy(() -> fileService.download("../hack", "sample.txt", false))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE);
			});
	}

	@Test
	void download_fail_whenFileNotFound() {
		assertThatThrownBy(() -> fileService.download("20260319", "notfound.txt", false))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FILE_NOT_FOUND);
			});
	}

	@Test
	void delete_success() throws IOException {
		Path dir = tempDir.resolve("20260319");
		Files.createDirectories(dir);

		Path filePath = dir.resolve("sample.txt");
		Path thumbPath = dir.resolve("thumb_sample.txt");

		Files.writeString(filePath, "hello");
		Files.writeString(thumbPath, "thumb");

		fileService.delete("sample.txt", "20260319");

		assertThat(Files.exists(filePath)).isFalse();
		assertThat(Files.exists(thumbPath)).isFalse();
	}

	@Test
	void delete_success_whenThumbDoesNotExist() throws IOException {
		Path dir = tempDir.resolve("20260319");
		Files.createDirectories(dir);

		Path filePath = dir.resolve("sample.txt");
		Files.writeString(filePath, "hello");

		fileService.delete("sample.txt", "20260319");

		assertThat(Files.exists(filePath)).isFalse();
	}

	@Test
	void delete_fail_whenInvalidPath() {
		assertThatThrownBy(() -> fileService.delete("sample.txt", "../hack"))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE);
			});
	}

	@Test
	void delete_fail_whenFileNotFound() {
		assertThatThrownBy(() -> fileService.delete("notfound.txt", "20260319"))
			.isInstanceOf(BusinessException.class)
			.satisfies(e -> {
				BusinessException ex = (BusinessException) e;
				assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FILE_NOT_FOUND);
			});
	}
}
