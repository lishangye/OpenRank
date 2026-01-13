package com.openrank.openrank.mapper;

import com.openrank.openrank.model.Repo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RepoMapper {

    Repo findByOwnerAndRepo(@Param("owner") String owner, @Param("repo") String repo);

    Repo findByFullName(@Param("fullName") String fullName);

    int upsert(Repo repo);
}
