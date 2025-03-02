package backend.academy.bot.util;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Validator {
    private static final String GITHUB_REGEX = "^(https?://)?(www.)?github.com/[^/]+/[^/]+/?$";
    private static final String STACKOVERFLOW_REGEX = "^(https?://)?(www.)?stackoverflow.com/questions/\\d+/.*$";

    private static final Pattern GITHUB_PATTERN = Pattern.compile(GITHUB_REGEX);
    private static final Pattern STACKOVERFLOW_PATTERN = Pattern.compile(STACKOVERFLOW_REGEX);

    public static boolean isGitHubRepo(String url) {
        return GITHUB_PATTERN.matcher(url).matches();
    }

    public static boolean isStackOverflowQuestion(String url) {
        return STACKOVERFLOW_PATTERN.matcher(url).matches();
    }
}
