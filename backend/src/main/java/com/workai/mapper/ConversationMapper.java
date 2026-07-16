package com.workai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workai.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    List<Conversation> findAllByUserId(String userId);
}
