package com.v1rtual.vvv_backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1rtual.vvv_backend.entity.Blog;
import com.v1rtual.vvv_backend.entity.Gif;
import com.v1rtual.vvv_backend.entity.HomeConfig;
import com.v1rtual.vvv_backend.entity.Music;
import com.v1rtual.vvv_backend.entity.Photo;
import com.v1rtual.vvv_backend.entity.User;
import com.v1rtual.vvv_backend.entity.Video;
import com.v1rtual.vvv_backend.mapper.BlogMapper;
import com.v1rtual.vvv_backend.mapper.GifMapper;
import com.v1rtual.vvv_backend.mapper.HomeConfigMapper;
import com.v1rtual.vvv_backend.mapper.MusicMapper;
import com.v1rtual.vvv_backend.mapper.PhotoMapper;
import com.v1rtual.vvv_backend.mapper.VideoMapper;
import com.v1rtual.vvv_backend.service.UserService;
import com.v1rtual.vvv_backend.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

  private final HomeConfigMapper homeConfigMapper;
  private final BlogMapper blogMapper;
  private final ObjectMapper objectMapper;
  private final VideoMapper videoMapper;
  private final GifMapper gifMapper;
  private final PhotoMapper photoMapper;
  private final UserService userService;
  private final MusicMapper musicMapper;

  /**
   * 获取 Home 页面完整配置（供前端 Home.vue 调用）
   * 返回结构：
   * {
   * "main": {type, src, title, desc, alt, random},
   * "galleryItems": [{type, src, alt}, ...],
   * "latestBlogs": [Blog1, Blog2, Blog3],
   * "pinnedBlog": Blog or null
   * }
   */
  @GetMapping("/config")
  public Result<Map<String, Object>> getHomeConfig() {
    // 固定取 id=1 的配置（单条记录模式）
    HomeConfig config = homeConfigMapper.getHomeConfig();
    if (config == null) {
      // 如果表里没数据，返回默认配置
      config = new HomeConfig();
      config.setMainType("video");
      config.setMainSrc("https://example.com/default-video.mp4");
      config.setMainTitle("V1rtual 的月光时刻");
      config.setMainDesc("欢迎来到我的想象世界～");
      config.setMainAlt("月光温柔洒落");
      config.setMainRandom(0);
      config.setGalleryJson("[]");
    }

    Map<String, Object> result = new HashMap<>();
    String mainType = StringUtils.defaultString(config.getMainType(), "video");
    String mainSrc = StringUtils.defaultString(config.getMainSrc(), "https://example.com/default.mp4");
    String mainTitle = StringUtils.defaultString(config.getMainTitle(), "V1rtual 的月光时刻");
    String mainDesc = StringUtils.defaultString(config.getMainDesc(), "欢迎来到我的想象世界～");
    String mainAlt = StringUtils.defaultString(config.getMainAlt(), "月光温柔洒落");
    boolean random = config.getMainRandom() != null && config.getMainRandom() == 1;

    List<String> availableFiles = new ArrayList<>();

    if (random) {
      // 根据类型从数据库随机取一条
      switch (mainType.toLowerCase()) {
        case "video":
          Video v = videoMapper.selectRandomOne();
          if (v != null) {
            mainSrc = v.getSrc();
            availableFiles = videoMapper.selectAllSrcs();
          }
          break;
        case "gif":
          Gif g = gifMapper.selectRandomOne();
          if (g != null) {
            mainSrc = g.getSrc();
            availableFiles = gifMapper.selectAllSrcs();
          }
          break;
        case "image", "photo":
          Photo p = photoMapper.selectRandomOne();
          if (p != null) {
            mainSrc = p.getSrc();
            availableFiles = photoMapper.selectAllSrcs();
          }
          break;
        default:
          // 默认用 imgs
          Photo ph = photoMapper.selectRandomOne();
          if (ph != null) {
            mainSrc = ph.getSrc();
            availableFiles = photoMapper.selectAllSrcs();
          }
      }
    }

    Map<String, Object> main = Map.of(
        "type", mainType,
        "src", mainSrc,
        "title", mainTitle,
        "desc", mainDesc,
        "alt", mainAlt,
        "random", random);

    result.put("main", main);
    result.put("availableFiles", availableFiles); // 回显该表所有 src
    // Gallery 拼图解析 JSON
    List<Map<String, Object>> galleryItems = new ArrayList<>();
    if (StringUtils.isNotBlank(config.getGalleryJson())) {
      try {
        galleryItems = objectMapper.readValue(config.getGalleryJson(), new TypeReference<>() {
        });
      } catch (Exception e) {
        log.error("Gallery JSON 解析失败", e);
      }
    }
    result.put("galleryItems", galleryItems);

    // 最新 Blog（前 3 条）
    List<Blog> latestBlogs = blogMapper.selectLatest3(); // 见下面 Mapper 定义
    result.put("latestBlogs", latestBlogs);

    // 置顶 Blog（右半部分）
    Blog pinnedBlog = null;
    if (config.getPinnedBlogId() != null) {
      pinnedBlog = blogMapper.selectById(config.getPinnedBlogId());
    }
    result.put("pinnedBlog", pinnedBlog);

    return Result.success(result, "Home 配置加载成功～✞");
  }

  @GetMapping("/random")
  public Result<Map<String, Object>> getRandomMain(@RequestParam String type) {
    Map<String, Object> data = new HashMap<>();
    String randomSrc = null;
    String uploaderUsername = "V1rtual";
    String uploaderAvatar = "/default-avatar.gif";
    String uploadTime = "刚刚上传";

    // 1. 根据 type 从对应表随机取一条记录
    switch (type.toLowerCase()) {
      case "video":
        Video v = videoMapper.selectRandomOne();
        if (v != null) {
          randomSrc = v.getSrc();
          uploaderUsername = v.getUploaderUsername();
          uploadTime = v.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
          // 2. 用 uploader_id 查 user 表获取头像
          if (v.getUploaderId() != null && v.getUploaderId() > 0) {
            User user = userService.findById(v.getUploaderId());
            if (user != null && StringUtils.isNotBlank(user.getAvatar())) {
              uploaderAvatar = user.getAvatar();
            }
          }
        }
        break;

      case "gif":
        Gif g = gifMapper.selectRandomOne();
        if (g != null) {
          randomSrc = g.getSrc();
          uploaderUsername = g.getUploaderUsername();
          uploadTime = g.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
          if (g.getUploaderId() != null && g.getUploaderId() > 0) {
            User user = userService.findById(g.getUploaderId());
            if (user != null && StringUtils.isNotBlank(user.getAvatar())) {
              uploaderAvatar = user.getAvatar();
            }
          }
        }
        break;

      case "image":
      case "photo":
        Photo p = photoMapper.selectRandomOne();
        if (p != null) {
          randomSrc = p.getSrc();
          uploaderUsername = p.getUploaderUsername();
          uploadTime = p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
          if (p.getUploaderId() != null && p.getUploaderId() > 0) {
            User user = userService.findById(p.getUploaderId());
            if (user != null && StringUtils.isNotBlank(user.getAvatar())) {
              uploaderAvatar = user.getAvatar();
            }
          }
        }
        break;

      default:
        return Result.error("不支持的类型");
    }

    if (randomSrc == null) {
      return Result.error("暂无可用资源");
    }

    data.put("src", randomSrc);
    data.put("title", "随机月光时刻"); // 可从记录取或固定
    data.put("description", "新的惊喜～");
    data.put("alt", "随机资源");
    data.put("uploaderAvatar", uploaderAvatar);
    data.put("uploaderUsername", uploaderUsername);
    data.put("uploadTime", uploadTime);

    return Result.success(data, "随机资源加载成功～✞");
  }

  @GetMapping("/full-item")
  public Result<Map<String, Object>> getFullMainItem(
      @RequestParam String src,
      @RequestParam String type) {
    Map<String, Object> data = new HashMap<>();

    Object record = null;
    Long uploaderId = null;
    String uploaderUsername = "V1rtual";
    String uploaderAvatar = "/default-avatar.gif";
    String uploadTime = "未知时间";

    switch (type.toLowerCase()) {
      case "video":
        record = videoMapper.selectBySrc(src);
        if (record instanceof Video v) {
          uploaderId = v.getUploaderId();
          uploaderUsername = StringUtils.defaultString(v.getUploaderUsername(), "V1rtual");
          uploadTime = v.getCreatedAt() != null
              ? v.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
              : "未知时间";
        }
        break;

      case "gif":
        record = gifMapper.selectBySrc(src);
        if (record instanceof Gif g) {
          uploaderId = g.getUploaderId();
          uploaderUsername = StringUtils.defaultString(g.getUploaderUsername(), "V1rtual");
          uploadTime = g.getCreatedAt() != null
              ? g.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
              : "未知时间";
        }
        break;

      case "image":
      case "photo":
        record = photoMapper.selectBySrc(src);
        if (record instanceof Photo p) {
          uploaderId = p.getUploaderId();
          uploaderUsername = StringUtils.defaultString(p.getUploaderUsername(), "V1rtual");
          uploadTime = p.getCreatedAt() != null
              ? p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
              : "未知时间";
        }
        break;

      default:
        return Result.error("不支持的类型");
    }
    if (record == null) {
      return Result.error("未找到该资源");
    }

    // 用 uploaderId 查询 user 表的头像（如果有）
    if (uploaderId != null && uploaderId >= 0) {
      User user = userService.findById(uploaderId);
      if (user != null && StringUtils.isNotBlank(user.getAvatar())) {
        uploaderAvatar = user.getAvatar();
      }
    }

    // 填充返回数据
    data.put("src", src);
    data.put("title", "随机资源"); // 可根据需要从 record 取真实 title
    data.put("description", "新的惊喜～");
    data.put("alt", "随机资源");
    data.put("uploaderAvatar", uploaderAvatar);
    data.put("uploaderUsername", uploaderUsername);
    data.put("uploadTime", uploadTime);

    return Result.success(data, "完整资源加载成功～✞");
  }
}