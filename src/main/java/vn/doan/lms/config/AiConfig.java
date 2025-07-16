package vn.doan.lms.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Lớp cấu hình cho LangChain4j AI Assistant.
 * 
 * Cung cấp bộ nhớ hội thoại (memory) với giới hạn token lớn để lưu toàn bộ lịch
 * sử hội thoại.
 */
@Configuration
public class AiConfig {

    // Inject tokenizer để tính toán số token khi lưu hội thoại
    @Autowired
    private Tokenizer tokenizer;

    /**
     * Bean cung cấp bộ nhớ hội thoại, sử dụng cơ chế Token Window (cửa sổ trượt)
     * để giữ lại các đoạn hội thoại gần nhất trong giới hạn token cho phép.
     *
     * @return ChatMemoryProvider tương ứng với từng session (chatId)
     */
    @Bean
    @Primary
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> TokenWindowChatMemory.builder()
                .id(memoryId) // ID đại diện cho từng người dùng/session
                .maxTokens(8000, tokenizer) // Tối đa 8000 token được ghi nhớ
                .build();
    }
}
