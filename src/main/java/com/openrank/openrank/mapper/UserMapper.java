package com.openrank.openrank.mapper;

import com.openrank.openrank.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    int insertUser(User user);

    User findById(@Param("id") Long id);
}
