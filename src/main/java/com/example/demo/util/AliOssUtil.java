package com.example.demo.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AliOssUtil {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    @Value("${aliyun.oss.domain:}")
    private String domain;

    /**
     * 上传图片到OSS
     * @param imageUrl 图片URL
     * @return OSS访问地址
     */
    public String uploadImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        OSS ossClient = null;
        try {
            // 下载图片
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            byte[] imageBytes = IOUtils.toByteArray(inputStream);

            // 生成OSS文件名（避免重复）
            String fileName = "travel/images/" + UUID.randomUUID() + ".jpg";

            // 创建OSS客户端并上传
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, new ByteArrayInputStream(imageBytes));
            ossClient.putObject(request);

            // 拼接OSS访问地址
            String ossUrl = StringUtils.hasText(domain) ? domain + "/" + fileName
                    : "https://" + bucketName + "." + endpoint + "/" + fileName;
            log.info("图片上传OSS成功：{}", ossUrl);
            return ossUrl;
        } catch (Exception e) {
            log.error("上传图片到OSS失败", e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 批量上传图片到OSS
     * @param imageUrls 图片URL列表
     * @return OSS访问地址列表
     */
    public List<String> batchUploadImages(List<String> imageUrls) {
        List<String> ossUrls = new ArrayList<>();
        if (imageUrls == null || imageUrls.isEmpty()) {
            return ossUrls;
        }
        for (String imageUrl : imageUrls) {
            String ossUrl = uploadImage(imageUrl);
            if (StringUtils.hasText(ossUrl)) {
                ossUrls.add(ossUrl);
            }
        }
        return ossUrls;
    }
    /**
     * 上传头像到 OSS（接收 MultipartFile）
     * @param file 头像文件
     * @return OSS 访问地址
     */
    // 在 AliOssUtil.java 中新增：
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        OSS ossClient = null;
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "travel/avatars/" + UUID.randomUUID() + suffix;

            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file.getInputStream());
            ossClient.putObject(request);

            String ossUrl = StringUtils.hasText(domain) ? domain + "/" + fileName
                    : "https://" + bucketName + "." + endpoint + "/" + fileName;
            log.info("头像上传OSS成功：{}", ossUrl);
            return ossUrl;
        } catch (Exception e) {
            log.error("头像上传OSS失败", e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
