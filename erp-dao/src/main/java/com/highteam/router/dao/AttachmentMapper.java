package com.highteam.router.dao;

import com.highteam.router.model.Attachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttachmentMapper {
    int deleteByPrimaryKey(Integer attachmentId);

    int insert(Attachment record);

    int insertSelective(Attachment record);

    Attachment selectByPrimaryKey(Integer attachmentId);

    int updateByPrimaryKeySelective(Attachment record);

    int updateByPrimaryKey(Attachment record);

    int batchInsert(List<Attachment> list);

    int deleteByBusinessTypeAndBusinessId(@Param("businessType") String businessType, @Param("businessId") Integer businessId);

    int deleteByBusinessTypeAndBusinessIds(@Param("businessType") String businessType, @Param("businessIds") String businessIds);

    List<Attachment> selectListByParam(Attachment param);

    List<Attachment> selectByBusinessTypeAndBusinessIds(@Param("businessType") String businessType, @Param("businessIds") String businessIds);
}