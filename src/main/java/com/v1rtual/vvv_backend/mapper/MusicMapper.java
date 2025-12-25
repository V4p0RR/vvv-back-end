package com.v1rtual.vvv_backend.mapper;

import com.v1rtual.vvv_backend.entity.Music;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MusicMapper {

  @Insert({
      "INSERT INTO music (title, description, src, cover_image, duration, artist, album, tags, ",
      "is_pinned, view_count, created_at, updated_at, ",
      "uploader_id, uploader_username) ",
      "VALUES (#{title}, #{description}, #{src}, #{coverImage}, #{duration}, #{artist}, #{album}, #{tags}, ",
      "#{isPinned}, #{viewCount}, #{createdAt}, #{updatedAt}, ",
      "#{uploaderId}, #{uploaderUsername})"
  })
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Music music);

  @Insert({
      "<script>",
      "INSERT INTO music (title, description, src, cover_image, duration, artist, album, tags, ",
      "is_pinned, view_count, created_at, updated_at, ",
      "uploader_id, uploader_username) VALUES ",
      "<foreach collection='list' item='item' separator=','>",
      "(#{item.title}, #{item.description}, #{item.src}, #{item.coverImage}, #{item.duration}, ",
      "#{item.artist}, #{item.album}, #{item.tags}, #{item.isPinned}, #{item.viewCount}, ",
      "#{item.createdAt}, #{item.updatedAt}, #{item.uploaderId}, #{item.uploaderUsername})",
      "</foreach>",
      "</script>"
  })
  int insertBatch(@Param("list") List<Music> list);

  @Select({
      "<script>",
      "SELECT src FROM music WHERE src IN ",
      "<foreach collection='srcList' item='src' open='(' separator=',' close=')'>",
      "#{src}",
      "</foreach>",
      "</script>"
  })
  List<String> selectExistSrcs(@Param("srcList") List<String> srcList);
}