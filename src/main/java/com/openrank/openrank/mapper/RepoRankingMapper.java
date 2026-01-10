package com.openrank.openrank.mapper;

import com.openrank.openrank.model.RepoRanking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RepoRankingMapper {

    String findLatestPeriod(@Param("periodType") String periodType);

    List<RepoRanking> listByPeriod(@Param("periodType") String periodType,
                                   @Param("period") String period,
                                   @Param("limit") int limit);

    List<RepoRanking> listByPeriodOrdered(@Param("periodType") String periodType,
                                          @Param("period") String period,
                                          @Param("orderColumn") String orderColumn,
                                          @Param("limit") int limit);
}
