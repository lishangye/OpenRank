package com.openrank.openrank.mapper;

import com.openrank.openrank.model.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    List<Favorite> findByUserId(@Param("userId") Long userId);

    int insertFavorite(Favorite favorite);

    int deleteFavorite(@Param("userId") Long userId, @Param("repo") String repo);

    Favorite findOne(@Param("userId") Long userId, @Param("repo") String repo);
}
