package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.LinkService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LinkServiceTest {

    @Autowired
    private LinkService linkService;

    private AddLinkRequest request;

    @BeforeEach
    void setUp() {
        request = new AddLinkRequest("https://github.com/user/repo", List.of("tag"), List.of("filter"));
    }

    @Test
    void testCorrectSavingLink() {
        linkService.follow(request, 1);
        List<Link> list = linkService.getUserLinks(1);
        assertEquals(1, list.size());
        Link link = list.getFirst();
        assertEquals(link.url(), "https://github.com/user/repo");
        assertEquals(link.tags(), List.of("tag"));
        assertEquals(link.filters(), List.of("filter"));
        linkService.unfollow("https://github.com/user/repo", 1);
    }

    @Test
    void testCorrectRemoveLink() {
        linkService.follow(request, 1);
        linkService.unfollow("https://github.com/user/repo", 1);
        assertEquals(0, linkService.getUserLinks(1).size());
    }

    @Test
    void testAddDuplicateLink() {
        linkService.follow(request, 1);
        Exception exception = assertThrows(IsAlreadyRegisteredException.class, () -> linkService.follow(request, 1));
        assertEquals("Ссылка уже отслеживается", exception.getMessage());
    }
}
