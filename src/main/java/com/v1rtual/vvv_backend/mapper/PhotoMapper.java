package com.v1rtual.vvv_backend.mapper;

import com.v1rtual.vvv_backend.entity.Photo;
import com.v1rtual.vvv_backend.entity.Video;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PhotoMapper {

  @Insert({
      "INSERT INTO photo (title, description, src, alt, tags, category, ",
      "is_pinned, likes, view_count, created_at, updated_at, ",
      "uploader_id, uploader_username) ",
      "VALUES (#{title}, #{description}, #{src}, #{alt}, #{tags}, #{category}, ",
      "#{isPinned}, #{likes}, #{viewCount}, #{createdAt}, #{updatedAt}, ",
      "#{uploaderId}, #{uploaderUsername})"
  })
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Photo photo);

  @Insert({
      "<script>",
      "INSERT INTO photo (title, description, src, alt, tags, category, ",
      "is_pinned, likes, view_count, created_at, updated_at, ",
      "uploader_id, uploader_username) VALUES ",
      "<foreach collection='list' item='item' separator=','>",
      "(#{item.title}, #{item.description}, #{item.src}, #{item.alt}, #{item.tags}, ",
      "#{item.category}, #{item.isPinned}, #{item.likes}, #{item.viewCount}, ",
      "#{item.createdAt}, #{item.updatedAt}, #{item.uploaderId}, #{item.uploaderUsername})",
      "</foreach>",
      "</script>"
  })
  int insertBatch(@Param("list") List<Photo> list);

  @Select({
      "<script>",
      "SELECT src FROM photo WHERE src IN ",
      "<foreach collection='srcList' item='src' open='(' separator=',' close=')'>",
      "#{src}",
      "</foreach>",
      "</script>"
  })
  List<String> selectExistSrcs(@Param("srcList") List<String> srcList);

  // 随机取一条（status=1 或其他条件）
  @Select("SELECT * FROM photo ORDER BY RAND() LIMIT 1")
  Photo selectRandomOne();

  // 列出所有可用 src（可加条件）
  @Select("SELECT src FROM photo")
  List<String> selectAllSrcs();

  @Select("SELECT * FROM photo WHERE src = #{src} LIMIT 1")
  Video selectBySrc(String src);
}