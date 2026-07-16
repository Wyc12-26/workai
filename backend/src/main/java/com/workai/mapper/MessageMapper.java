package com.workai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workai.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    List<Message> findByConversationId(Long conversationId);
}
