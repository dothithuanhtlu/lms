package vn.doan.lms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.doan.lms.config.Assistant;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow CORS for frontend
@Slf4j
public class ChatBotController {
    private final Assistant assistant;

    @GetMapping("/{message}")
    @Transactional
    public ResponseEntity<String> getTeacher(@PathVariable("message") String message) {
        try {
            log.info("Received message: {}", message);

            // Generate a unique session ID based on request or use a default one
            long sessionId = Math.abs(message.hashCode()) % 1000000;

            String response = assistant.lmsAssistantHelp(message, sessionId);
            log.info("Generated response for session {}: {}", sessionId, response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
            return ResponseEntity.internalServerError()
                    .body("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.");
        }
    }

    @PostMapping("/chat")
    @Transactional(readOnly = true)
    public ResponseEntity<String> chatWithAssistant(
            @RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request.getMessage());

            long sessionId = request.getSessionId() != null ? request.getSessionId()
                    : Math.abs(request.getMessage().hashCode()) % 1000000;

            String response = assistant.lmsAssistantHelp(request.getMessage(), sessionId);
            log.info("Generated response for session {}: {}", sessionId, response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat request: {}", request.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.");
        }
    }

    public static class ChatRequest {
        private String message;
        private Long sessionId;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }
    }
}
