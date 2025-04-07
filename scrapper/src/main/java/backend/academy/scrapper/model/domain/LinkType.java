package backend.academy.scrapper.model.domain;

public enum LinkType {
    GITHUB,
    STACKOVERFLOW;

    public static LinkType getLinkType(String url) {
        if (url.contains("github.com")) {
            return GITHUB;
        } else if (url.contains("stackoverflow.com")) {
            return STACKOVERFLOW;
        } else {
            throw new IllegalArgumentException("Неизвестная ссылка");
        }
    }
}
