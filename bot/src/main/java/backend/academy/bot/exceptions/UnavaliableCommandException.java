package backend.academy.bot.exceptions;

public class UnavaliableCommandException extends RuntimeException {
    public UnavaliableCommandException(String message) {
        super(message);
    }
}
