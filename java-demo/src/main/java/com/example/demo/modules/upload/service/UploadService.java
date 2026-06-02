package com.example.demo.modules.upload.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.SnowflakeIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 图片上传服务
 * 支持本地存储和 OSS 存储（根据配置自动切换）
 */
@Slf4j
@Service
public class UploadService {

    /** 最大文件大小：10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    /** 允许的图片格式 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final SnowflakeIdUtils snowflakeIdUtils;

    @Value("${oss.endpoint:}")
    private String ossEndpoint;

    public UploadService(SnowflakeIdUtils snowflakeIdUtils) {
        this.snowflakeIdUtils = snowflakeIdUtils;
    }

    /**
     * 上传图片
     *
     * @param file   上传的文件
     * @param scene  业务场景（如 collection, avatar 等）
     * @param userId 用户 ID
     * @return {url, width, height, size}
     */
    public Map<String, Object> upload(MultipartFile file, String scene, String userId) {
        // 1. 校验文件不为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件不能为空");
        }

        // 2. 校验文件大小（≤ 10MB）
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件大小不能超过10MB");
        }

        // 3. 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持 jpg/jpeg/png/webp 格式的图片");
        }

        // 4. 根据 OSS 配置选择存储方式
        if (isOssConfigured()) {
            return uploadToOss(file, scene, userId, ext);
        } else {
            return uploadToLocal(file, scene, userId, ext);
        }
    }

    /**
     * 检查 OSS 是否已配置
     */
    private boolean isOssConfigured() {
        return ossEndpoint != null && !ossEndpoint.isEmpty() && !ossEndpoint.contains("your_");
    }

    /**
     * 本地存储上传
     */
    private Map<String, Object> uploadToLocal(MultipartFile file, String scene, String userId, String ext) {
        try {
            // 存储路径：/{userId}/{scene}/{yyyyMMdd}/{snowflakeId}.{ext}
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileId = snowflakeIdUtils.nextIdWithPrefix("img_");
            String relativePath = String.format("%s/%s/%s/%s.%s", userId, scene, dateStr, fileId, ext);

            // 目标目录：项目同级目录的 uploads/
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectDir).getParent().resolve("uploads");
            Path targetPath = uploadDir.resolve(relativePath);

            // 创建目录（如果不存在）
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            file.transferTo(targetPath.toFile());
            log.info("图片上传成功（本地存储），path: {}, size: {}", targetPath, file.getSize());

            // 获取图片尺寸
            int width = 0;
            int height = 0;
            try {
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                if (bufferedImage != null) {
                    width = bufferedImage.getWidth();
                    height = bufferedImage.getHeight();
                }
            } catch (Exception e) {
                log.warn("获取图片尺寸失败，fileId: {}", fileId, e);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("url", "/uploads/" + relativePath);
            result.put("width", width);
            result.put("height", height);
            result.put("size", file.getSize());
            return result;

        } catch (IOException e) {
            log.error("本地文件上传异常", e);
            throw new BusinessException(ResultCode.OPERATION_FAILED, "文件上传失败");
        }
    }

    /**
     * OSS 存储上传
     */
    private Map<String, Object> uploadToOss(MultipartFile file, String scene, String userId, String ext) {
        // TODO: OSS SDK 集成后实现
        // 存储路径格式：/{userId}/{scene}/{date}/{snowflakeId}.{ext}
        // 使用 OSS 客户端上传并返回公开访问 URL
        log.warn("OSS SDK 尚未集成，暂时使用本地存储作为降级方案");
        return uploadToLocal(file, scene, userId, ext);
    }
}
