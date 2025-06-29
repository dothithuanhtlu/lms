package vn.doan.lms.service;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class Tooling {

    @Tool("Get Current Time and Date")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return "Thời gian hiện tại: " + now.format(formatter);
    }

    @Tool("Calculate basic math operations")
    public String calculate(String expression) {
        try {
            // Simple calculator for basic operations
            expression = expression.replaceAll("\\s+", "");

            // Handle basic operations: +, -, *, /
            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");
                double result = Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
                return String.format("Kết quả: %.2f", result);
            } else if (expression.contains("-")) {
                String[] parts = expression.split("-");
                double result = Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
                return String.format("Kết quả: %.2f", result);
            } else if (expression.contains("*")) {
                String[] parts = expression.split("\\*");
                double result = Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
                return String.format("Kết quả: %.2f", result);
            } else if (expression.contains("/")) {
                String[] parts = expression.split("/");
                if (Double.parseDouble(parts[1]) == 0) {
                    return "Lỗi: Không thể chia cho 0";
                }
                double result = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                return String.format("Kết quả: %.2f", result);
            }

            return "Không thể tính toán biểu thức này. Vui lòng sử dụng các phép toán cơ bản: +, -, *, /";
        } catch (Exception e) {
            return "Lỗi: Biểu thức không hợp lệ";
        }
    }

    @Tool("Generate random number")
    public String generateRandomNumber(int min, int max) {
        if (min > max) {
            return "Lỗi: Giá trị min phải nhỏ hơn hoặc bằng max";
        }
        Random random = new Random();
        int result = random.nextInt(max - min + 1) + min;
        return String.format("Số ngẫu nhiên từ %d đến %d: %d", min, max, result);
    }

    @Tool("Convert temperature between Celsius and Fahrenheit")
    public String convertTemperature(double value, String fromUnit) {
        fromUnit = fromUnit.toLowerCase();
        if (fromUnit.equals("c") || fromUnit.equals("celsius")) {
            double fahrenheit = (value * 9 / 5) + 32;
            return String.format("%.1f°C = %.1f°F", value, fahrenheit);
        } else if (fromUnit.equals("f") || fromUnit.equals("fahrenheit")) {
            double celsius = (value - 32) * 5 / 9;
            return String.format("%.1f°F = %.1f°C", value, celsius);
        } else {
            return "Đơn vị không hợp lệ. Sử dụng 'C' hoặc 'F'";
        }
    }
}
