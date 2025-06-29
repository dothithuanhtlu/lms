package vn.doan.lms.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Autowired
    private Tokenizer tokenizer;

    @Bean
    @Primary
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> TokenWindowChatMemory.builder()
                .id(memoryId)
                .maxTokens(8000, tokenizer) // Tăng token limit để cho phép câu trả lời dài hơn
                // .tokenizer(tokenizer)
                .build();
    }
}
