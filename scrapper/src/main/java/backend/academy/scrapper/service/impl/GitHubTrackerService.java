package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.service.TrackerService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GitHubTrackerService implements TrackerService {
    private final GithubClient githubClient;

    @Autowired
    public GitHubTrackerService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    @Override
    public Mono<String> track(String url) {
        String[] credentials = extractOwnerAndRepo(url);
        String owner = credentials[0];
        String repo = credentials[1];
        return githubClient.getRepoLastUpdated(owner, repo);
    }

    private String[] extractOwnerAndRepo(String githubUrl) {
        Pattern pattern = Pattern.compile("https?://github.com/([^/]+)/([^/]+)");
        Matcher matcher = pattern.matcher(githubUrl);
        if (matcher.find()) {
            return new String[] {matcher.group(1), matcher.group(2)};
        }
        throw new IllegalArgumentException("Invalid GitHub URL format");
    }
}
