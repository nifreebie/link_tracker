package backend.academy.bot.exceptions;

public class IsAlreadyRegisteredException extends RuntimeException {
    public IsAlreadyRegisteredException(String message) {
        super(message);
    }
}
