package backend.academy.bot.exceptions;

public class UnavailableCommandException extends RuntimeException {
    public UnavailableCommandException(String message) {
        super(message);
    }
}
