package com.workai.controller;

import com.workai.dto.ChatRequest;
import com.workai.entity.Conversation;
import com.workai.entity.Message;
import com.workai.service.ChatService;
import com.workai.service.ConversationService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ConversationService conversationService;

    public ChatController(ChatService chatService, ConversationService conversationService) {
        this.chatService = chatService;
        this.conversationService = conversationService;
    }

    @PostMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);

        new Thread(() -> {
            try {
                final Long[] convIdHolder = new Long[1];
                if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
                    try {
                        convIdHolder[0] = Long.parseLong(request.getConversationId());
                    } catch (NumberFormatException e) {
                    }
                }

                if (convIdHolder[0] == null) {
                    Conversation conv = conversationService.createConversation(
                        request.getContent().length() > 20 
                            ? request.getContent().substring(0, 20) + "..." 
                            : request.getContent()
                    );
                    convIdHolder[0] = conv.getId();
                }

                Long finalConvId = convIdHolder[0];

                Message userMsg = new Message();
                userMsg.setConversationId(finalConvId);
                userMsg.setRole("user");
                userMsg.setContent(request.getContent());
                userMsg.setFileId(request.getFileId());
                userMsg.setFunctionName(request.getFunctionName());
                userMsg.setCreatedAt(LocalDateTime.now());
                conversationService.saveMessage(userMsg);

                List<org.springframework.ai.chat.messages.Message> historyMessages = new ArrayList<>();
                List<Message> dbMessages = conversationService.getRecentMessages(finalConvId, 10);
                for (Message msg : dbMessages) {
                    if ("user".equals(msg.getRole())) {
                        historyMessages.add(new UserMessage(msg.getContent()));
                    } else if ("assistant".equals(msg.getRole())) {
                        historyMessages.add(new AssistantMessage(msg.getContent()));
                    }
                }

                StringBuilder aiContent = new StringBuilder();

                chatService.generateStream(request, historyMessages)
                        .subscribe(
                                data -> {
                                    try {
                                        aiContent.append(data);
                                        emitter.send(SseEmitter.event().data(data));
                                    } catch (IOException e) {
                                        emitter.completeWithError(e);
                                    }
                                },
                                error -> {
                                    try {
                                        emitter.send(SseEmitter.event().data("生成回复时出现错误"));
                                    } catch (IOException e) {
                                        emitter.completeWithError(e);
                                    }
                                    emitter.completeWithError(error);
                                },
                                () -> {
                                    Message aiMsg = new Message();
                                    aiMsg.setConversationId(finalConvId);
                                    aiMsg.setRole("assistant");
                                    aiMsg.setContent(aiContent.toString());
                                    aiMsg.setCreatedAt(LocalDateTime.now());
                                    conversationService.saveMessage(aiMsg);
                                    emitter.complete();
                                }
                        );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    @PostMapping("/conversations")
    public Map<String, Object> createConversation(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "新对话");
        Conversation conversation = conversationService.createConversation(title);
        return Map.of(
            "success", true,
            "data", Map.of(
                "id", String.valueOf(conversation.getId()),
                "title", conversation.getTitle(),
                "createdAt", conversation.getCreatedAt().toString()
            )
        );
    }

    @GetMapping("/conversations")
    public Map<String, Object> getUserConversations(@RequestParam(defaultValue = "default") String userId) {
        List<Conversation> conversations = conversationService.getUserConversations(userId);
        return Map.of(
            "success", true,
            "data", conversations.stream().map(c -> Map.of(
                "id", String.valueOf(c.getId()),
                "title", c.getTitle(),
                "createdAt", c.getCreatedAt().toString(),
                "updatedAt", c.getUpdatedAt().toString()
            )).toList()
        );
    }

    @DeleteMapping("/conversations/{id}")
    public Map<String, Object> deleteConversation(
            @PathVariable String id,
            @RequestParam(defaultValue = "default") String userId) {
        try {
            Long convId = Long.parseLong(id);
            conversationService.deleteConversation(convId, userId);
            return Map.of("success", true, "message", "对话已删除");
        } catch (NumberFormatException e) {
            return Map.of("success", false, "message", "无效的对话ID");
        }
    }

    @GetMapping("/conversations/{id}/messages")
    public Map<String, Object> getMessages(@PathVariable String id) {
        try {
            Long convId = Long.parseLong(id);
            List<Message> messages = conversationService.getMessages(convId);
            return Map.of(
                "success", true,
                "data", messages.stream().map(m -> Map.of(
                    "id", String.valueOf(m.getId()),
                    "role", m.getRole(),
                    "content", m.getContent(),
                    "fileId", m.getFileId(),
                    "fileName", m.getFileName(),
                    "functionName", m.getFunctionName(),
                    "timestamp", m.getCreatedAt().toString()
                )).toList()
            );
        } catch (NumberFormatException e) {
            return Map.of("success", false, "message", "无效的对话ID");
        }
    }
}
