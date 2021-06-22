package api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorResponseExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(ErrorResponseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String errorResponseExceptionHandler(ErrorResponseException e) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(e.getError());
        } catch (JsonProcessingException ex) {
            return ex.getMessage();
        }
    }
}
