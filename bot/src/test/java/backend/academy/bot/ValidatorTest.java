package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.bot.util.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.telegram-token=token"})
public class ValidatorTest {
    @Test
    void testValidGitHubUrls() {
        assertTrue(Validator.isGitHubRepo("https://github.com/user/repo"));
        assertTrue(Validator.isGitHubRepo("http://github.com/user/repo"));
        assertTrue(Validator.isGitHubRepo("https://www.github.com/user/repo/"));
    }

    @Test
    void testInvalidGitHubUrls() {
        assertFalse(Validator.isGitHubRepo("https://github.com/user"));
        assertFalse(Validator.isGitHubRepo("https://github.com/user/repo/extra"));
        assertFalse(Validator.isGitHubRepo("https://gitlab.com/user/repo"));
        assertFalse(Validator.isGitHubRepo("string"));
    }

    @Test
    void testValidStackOverflowUrls() {
        assertTrue(Validator.isStackOverflowQuestion("https://stackoverflow.com/questions/12345/example-question"));
        assertTrue(Validator.isStackOverflowQuestion("http://stackoverflow.com/questions/12345/example-question"));
        assertTrue(Validator.isStackOverflowQuestion("https://www.stackoverflow.com/questions/12345/example-question"));
    }

    @Test
    void testInvalidStackOverflowUrls() {
        assertFalse(Validator.isStackOverflowQuestion("https://stackoverflow.com/questions/"));
        assertFalse(Validator.isStackOverflowQuestion("https://stackoverflow.com/questions/example"));
        assertFalse(Validator.isStackOverflowQuestion("https://stackoverflow.com/q/12345"));
        assertFalse(Validator.isStackOverflowQuestion("string"));
    }
}
