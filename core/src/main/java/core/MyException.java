package core;

public class MyException extends RuntimeException{
    MyException(Long errorCode, String message) {
        super("Error " + errorCode + ": " + message);
    }
}
