package backend.academy.bot.configuration;

import backend.academy.bot.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfiguration {

    private final BotConfig botConfig;

    @Autowired
    public BotConfiguration(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public TelegramBot telegramBot() {
        TelegramBot bot = new TelegramBot(botConfig.telegramToken());
        bot.execute(new SetMyCommands(initCommands()));
        return bot;
    }

    private BotCommand[] initCommands() {
        BotCommand startCommand = new BotCommand("/start", "регистрация пользователя");
        BotCommand helpCommand = new BotCommand("/help", "вывод списка доступных команд");
        BotCommand trackCommand = new BotCommand("/track", "начать отслеживание ссылки");
        BotCommand untrackCommand = new BotCommand("/untrack", "прекратить отслеживание ссылки");
        BotCommand listCommand =
                new BotCommand("/list", "показать список отслеживаемых ссылок (cписок ссылок, полученных при /track)");
        BotCommand cancelCommand = new BotCommand("/cancel", "отменить выполнение команды");
        BotCommand createTagCommand = new BotCommand("/create_tag", "создать тэг");
        BotCommand addTagsToLinkCommand = new BotCommand("/add_tags_to_link", "добавить тэги к ссылке");
        BotCommand removeTagsFromLinkCommand = new BotCommand("/remove_tags_from_link", "удалить тэги у ссылки");
        return new BotCommand[] {
            startCommand,
            helpCommand,
            trackCommand,
            untrackCommand,
            listCommand,
            cancelCommand,
            createTagCommand,
            addTagsToLinkCommand,
            removeTagsFromLinkCommand
        };
    }
}
