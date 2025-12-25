package com.v1rtual.vvv_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Photo {
  private Long id;
  private String title;
  private String description;
  private String src;
  private String alt;
  private String tags;
  private String category;
  private Integer isPinned = 0;
  private Long likes = 0L;
  private Long viewCount = 0L;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long uploaderId;
  private String uploaderUsername;
}