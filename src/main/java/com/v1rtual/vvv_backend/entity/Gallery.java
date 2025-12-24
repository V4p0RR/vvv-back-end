package com.v1rtual.vvv_backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Gallery {
  private Long id;
  private String title;
  private String description;
  private String imageUrl;
  private Long userId;
  private Long likes;
  private LocalDateTime createdAt;
}