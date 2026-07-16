package com.workai.service;

import com.workai.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天服务 - 对接 Spring AI
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 流式生成回复
     */
    public Flux<String> generateStream(ChatRequest request, List<Message> historyMessages) {
        // 构建消息列表
        List<Message> messages = buildMessages(request, historyMessages);

        // 根据功能类型添加系统提示
        String systemPrompt = getSystemPrompt(request.getFunctionName());
        if (systemPrompt != null) {
            messages.add(0, new org.springframework.ai.chat.messages.SystemMessage(systemPrompt));
        }

        // 如果有附件文件，读取内容并添加到消息
        if (request.getFileId() != null) {
            String fileContent = readFileContent(request.getFileId());
            if (fileContent != null && !fileContent.isEmpty()) {
                messages.add(0, new org.springframework.ai.chat.messages.SystemMessage(
                    "以下是用户上传的文件内容，请基于此内容回答问题：\n" + fileContent
                ));
            }
        }

        // 使用 Spring AI 流式调用
        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content();
    }

    /**
     * 构建消息列表
     */
    private List<Message> buildMessages(ChatRequest request, List<Message> historyMessages) {
        List<Message> messages = new ArrayList<>();

        // 添加历史消息
        if (historyMessages != null) {
            messages.addAll(historyMessages);
        }

        // 添加当前用户消息
        messages.add(new UserMessage(request.getContent()));

        return messages;
    }

    /**
     * 根据功能类型获取系统提示
     */
    private String getSystemPrompt(String functionName) {
        String baseFormat = "【格式要求】\n" +
                "- 使用 Markdown 格式输出，确保段落清晰、层次分明\n" +
                "- 每个段落之间使用空行分隔\n" +
                "- 使用标题（#、##、###）区分不同层级\n" +
                "- 使用有序列表或无序列表列出要点\n" +
                "- 重要内容使用加粗 **文字** 强调\n" +
                "- 保持适当的换行和缩进，避免文字堆积在一起\n";
        
        if (functionName == null) {
            return "你是一个专业的企业办公 AI 助手。" + baseFormat;
        }
        switch (functionName) {
            case "summarize":
                return "你是一个专业的文档总结助手。请对用户提供的文档内容进行精炼总结，提取核心要点，保持客观准确。" + baseFormat;
            case "template":
                return "你是一个专业的文案生成助手。请根据用户的需求，生成高质量、结构清晰的文案内容。注意语气专业且友好，适合办公场景使用。" + baseFormat;
            case "search":
                return "你是一个联网检索助手。请基于搜索到的信息，为用户提供准确、有用的答案。如果找不到相关信息，请如实告知。" + baseFormat;
            default:
                return null;
        }
    }

    /**
     * 读取文件内容
     */
    private String readFileContent(String fileId) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File dir = new File(uploadDir);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(fileId + "_")) {
                        String extension = getFileExtension(file.getName());
                        if ("txt".equalsIgnoreCase(extension) || "md".equalsIgnoreCase(extension)) {
                            return new String(Files.readAllBytes(file.toPath()), "UTF-8");
                        } else if ("doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension)) {
                            return "[暂不支持直接读取 " + extension.toUpperCase() + " 文件内容，请转换为 TXT 或 MD 格式]";
                        } else if ("pdf".equalsIgnoreCase(extension)) {
                            return "[暂不支持直接读取 PDF 文件内容，请转换为 TXT 或 MD 格式]";
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("读取文件失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}
