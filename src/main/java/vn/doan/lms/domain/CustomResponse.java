package vn.doan.lms.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponse<T> {
    private int statusCode;
    private String error;
    // message co the la: String or ArrayList
    private Object message;
    private T data;
}
