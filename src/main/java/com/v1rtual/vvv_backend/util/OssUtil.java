package com.v1rtual.vvv_backend.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

/**
 * OSS工具类
 * 支持四类目录：静态图片、音乐、GIF、视频
 */
@Component
@Slf4j
public class OssUtil {

  private final OSS ossClient;

  @Value("${aliyun.oss.bucket-name}")
  private String bucketName;

  @Value("${aliyun.oss.endpoint}")
  private String endpoint;

  public OssUtil(OSS ossClient) {
    this.ossClient = ossClient;
  }

  /**
   * 文件类型枚举
   */
  public enum FileType {
    IMGS("imgs/"), // 静态图片的静谧殿堂
    MUSIC("music/"), // 音乐的低吟回廊
    GIF("gif/"), // GIF的跳动心跳室
    VIDEO("video/"); // 视频的永恒故事厅

    private final String path;

    FileType(String path) {
      this.path = path;
    }

    public String getPath() {
      return path;
    }
  }

  /**
   * 上传文件～温柔流式直传，根据类型自动选择专属目录
   * 
   * @param file     前端的文件
   * @param fileType 文件类型（自动分配目录）
   * @return 公开访问URL
   */
  public String upload(MultipartFile file, FileType fileType) throws IOException {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isEmpty()) {
      throw new IllegalArgumentException("文件名不能为空哦～");
    }

    // 提取后缀，生成唯一文件名
    String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    String fileName = fileType.getPath() + UUID.randomUUID() + suffix;

    // 元数据～让文件更懂自己
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    // 流式上传～不落地磁盘，轻柔省内存
    try (InputStream inputStream = file.getInputStream()) {
      ossClient.putObject(bucketName, fileName, inputStream, metadata);
    }

    log.info("宝贝的{}已安全抵达{}宝库～路径：{}", suffix, fileType.name(), fileName);
    return getPublicUrl(fileName);
  }

  /**
   * 获取公开URL～永恒可访问
   */
  public String getPublicUrl(String fileName) {
    return "https://" + bucketName + "." + endpoint.replace("https://", "").replace("http://", "") + "/" + fileName;
  }

  /**
   * 生成签名临时URL～更私密的安全通道
   * 
   * @param fileName      文件完整路径
   * @param expireSeconds 过期秒数（如3600=1小时）
   */
  public String getSignedUrl(String fileName, long expireSeconds) {
    Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName,
        com.aliyun.oss.HttpMethod.GET);
    request.setExpiration(expiration);
    return ossClient.generatePresignedUrl(request).toString();
  }

  /**
   * 删除文件～温柔告别
   */
  public void delete(String fileName) {
    ossClient.deleteObject(bucketName, fileName);
    log.info("文件已轻轻离去～路径：{}", fileName);
  }

  /**
   * 检查文件是否存在～像轻轻叩门
   */
  public boolean exists(String fileName) {
    return ossClient.doesObjectExist(bucketName, fileName);
  }
}