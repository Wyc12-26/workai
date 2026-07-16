package com.workai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workai.entity.Conversation;
import com.workai.entity.Message;
import com.workai.mapper.ConversationMapper;
import com.workai.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public ConversationService(ConversationMapper conversationMapper, MessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * 创建对话
     */
    @Transactional
    public Conversation createConversation(String title) {
        Conversation conversation = new Conversation();
        conversation.setTitle(title);
        conversation.setUserId("default"); // TODO: 从认证上下文获取用户ID
        conversation.setDeleted(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return conversation;
    }

    /**
     * 获取用户所有对话
     */
    public List<Conversation> getUserConversations(String userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getDeleted, 0)
                .orderByDesc(Conversation::getUpdatedAt);
        return conversationMapper.selectList(wrapper);
    }

    /**
     * 删除对话（软删除）
     */
    @Transactional
    public void deleteConversation(Long conversationId, String userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getId, conversationId)
                .eq(Conversation::getUserId, userId);
        Conversation conversation = conversationMapper.selectOne(wrapper);
        if (conversation != null) {
            conversation.setDeleted(1);
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationMapper.updateById(conversation);

            // 同时删除该对话的所有消息
            LambdaQueryWrapper<Message> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(Message::getConversationId, conversationId);
            messageMapper.delete(msgWrapper);
        }
    }

    /**
     * 获取对话详情
     */
    public Conversation getConversation(Long conversationId, String userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getId, conversationId)
                .eq(Conversation::getUserId, userId)
                .eq(Conversation::getDeleted, 0);
        return conversationMapper.selectOne(wrapper);
    }

    /**
     * 保存消息
     */
    @Transactional
    public void saveMessage(Message message) {
        messageMapper.insert(message);
    }

    /**
     * 获取对话消息列表
     */
    public List<Message> getMessages(Long conversationId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt);
        return messageMapper.selectList(wrapper);
    }

    /**
     * 获取对话最近的消息（用于上下文）
     */
    public List<Message> getRecentMessages(Long conversationId, int limit) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt)
                .last("LIMIT " + limit);
        return messageMapper.selectList(wrapper);
    }
}
