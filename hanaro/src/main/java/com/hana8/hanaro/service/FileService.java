package com.hana8.hanaro.service;

import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

	@Value("${upload.path}")
	private String uploadPath;

	public String upload(MultipartFile file, String saveDir) {
		if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
			throw new BusinessException(ErrorCode.IMAGE_EMPTY);
		}

		String originalFilename = file.getOriginalFilename();
		String ext = extractExtension(originalFilename);
		String savedFilename = UUID.randomUUID() + ext;

		Path basePath = Paths.get(uploadPath).normalize();
		Path savePath = basePath.resolve(Paths.get(saveDir, savedFilename)).normalize();
		Path thumbPath = basePath.resolve(Paths.get(saveDir, "thumb_" + savedFilename)).normalize();

		if (!savePath.startsWith(basePath) || !thumbPath.startsWith(basePath)) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "잘못된 파일 경로입니다.");
		}

		try {
			Files.createDirectories(savePath.getParent());
			file.transferTo(savePath);

			String contentType = file.getContentType();
			if (contentType != null && contentType.startsWith("image/")) {
				Thumbnails.of(savePath.toFile())
					.size(200, 200)
					.crop(Positions.CENTER)
					.outputQuality(0.8)
					.toFile(thumbPath.toFile());
			}
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 실패: " + e.getMessage());
		}

		return savedFilename;
	}

	public ResponseEntity<Resource> download(String saveDir, String saveName, boolean inline) {
		Path basePath = Paths.get(uploadPath).normalize();
		Path filePath = basePath.resolve(Paths.get(saveDir, saveName)).normalize();

		if (!filePath.startsWith(basePath)) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "잘못된 파일 경로입니다.");
		}

		Resource resource = new FileSystemResource(filePath);
		if (!resource.exists()) {
			throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일을 찾을 수 없습니다: " + saveName);
		}

		String contentType = "application/octet-stream";
		try {
			String probed = Files.probeContentType(filePath);
			if (probed != null && !probed.isBlank()) {
				contentType = probed;
			}
		} catch (IOException ignored) {
		}

		String disposition = (inline ? "inline" : "attachment")
			+ "; filename=\"" + filePath.getFileName().toString() + "\"";

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
			.body(resource);
	}

	public void delete(String saveName, String saveDir) {
		Path basePath = Paths.get(uploadPath).normalize();
		Path filePath = basePath.resolve(Paths.get(saveDir, saveName)).normalize();

		if (!filePath.startsWith(basePath)) {
			throw new BusinessException(ErrorCode.INVALID_FILE, "잘못된 파일 경로입니다.");
		}

		if (!Files.exists(filePath)) {
			throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일을 찾을 수 없습니다: " + saveName);
		}

		try {
			Files.delete(filePath);

			Path thumbPath = basePath.resolve(Paths.get(saveDir, "thumb_" + saveName)).normalize();
			if (Files.exists(thumbPath)) {
				Files.delete(thumbPath);
			}
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 삭제 실패: " + e.getMessage());
		}
	}

	private String extractExtension(String originalFilename) {
		int index = originalFilename.lastIndexOf(".");
		if (index == -1) return "";
		return originalFilename.substring(index);
	}
}
