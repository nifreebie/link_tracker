package backend.academy.bot.configuration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfiguration {

    @Value("${app.telegram-token}")
    private String token;

    @Bean
    public TelegramBot telegramBot() {
        TelegramBot bot = new TelegramBot(token);
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
        return new BotCommand[] {startCommand, helpCommand, trackCommand, untrackCommand, listCommand, cancelCommand};
    }
}
