package backend.academy.scrapper.exceptions;

public class IsAlreadyRegisteredException extends RuntimeException {
    public IsAlreadyRegisteredException(String message) {
        super(message);
    }
}
