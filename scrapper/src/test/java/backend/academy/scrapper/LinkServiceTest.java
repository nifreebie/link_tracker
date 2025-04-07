package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.exceptions.IsAlreadyRegisteredException;
import backend.academy.scrapper.model.domain.LinkType;
import backend.academy.scrapper.model.dto.LinkDTO;
import backend.academy.scrapper.model.dto.request.AddLinkRequest;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.service.impl.LinkServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private LinkServiceImpl linkService;

    private AddLinkRequest request;

    private LinkDTO link;

    private Long telegramChatId;

    private String url;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @BeforeEach
    void setUp() {
        telegramChatId = 1L;
        url = "https://github.com/user/repo";
        request = new AddLinkRequest(url, List.of("tag"), List.of("filter"));
        link = new LinkDTO(1, url, List.of("tag"), List.of("filter"), null, List.of(telegramChatId), LinkType.GITHUB);
    }

    @Test
    void testCorrectSavingLink() {
        when(linkRepository.isUrlExists(url)).thenReturn(false);
        when(linkRepository.saveLink(url, request.tags(), request.filters(), telegramChatId))
                .thenReturn(link);

        LinkDTO result = linkService.follow(request, telegramChatId);

        assertNotNull(result);
        assertEquals(link.url(), result.url());
        assertEquals(link.tags(), result.tags());
    }

    @Test
    void testCorrectRemoveLink() {
        when(linkRepository.removeLinkByUrlAndTelegramChatId(url, telegramChatId))
                .thenReturn(link);
        LinkDTO removed = linkService.unfollow(url, telegramChatId);

        assertEquals(url, removed.url());
        assertTrue(linkService.getUserLinks(telegramChatId).isEmpty());
    }

    @Test
    void testAddDuplicateLink() {
        when(linkRepository.isUrlExists(url)).thenReturn(true);
        when(linkRepository.findLinkByUrl(url)).thenReturn(link);

        Exception ex =
                assertThrows(IsAlreadyRegisteredException.class, () -> linkService.follow(request, telegramChatId));

        assertEquals("Ссылка уже отслеживается", ex.getMessage());
    }
}
