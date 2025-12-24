package com.v1rtual.vvv_backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.v1rtual.vvv_backend.entity.User;

@Mapper
public interface UserMapper {
  /**
   * 根据用户名查找用户
   * 
   * @param username
   * @return 用户实体
   */
  @Select("SELECT * FROM user WHERE username = #{username}")
  User findByUsername(String username);

  /**
   * 插入新用户
   * 
   * @param user
   * @return 影响的行数
   */
  @Insert("INSERT INTO user(username, password, created_at, status) " +
      "VALUES(#{username}, #{password}, #{createdAt}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(User user);

  /**
   * 更新用户信息
   * 
   * @param user
   * @return 影响的行数
   */
  @Update("UPDATE user SET password = #{password}, description = #{description}, " +
      "sex = #{sex}, avatar = #{avatar} WHERE id = #{id}")
  int update(User user);
}
