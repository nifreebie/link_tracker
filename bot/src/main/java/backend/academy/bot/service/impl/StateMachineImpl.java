package backend.academy.bot.service.impl;

import backend.academy.bot.model.CommandContext;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.StateMachine;
import backend.academy.bot.util.Validator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StateMachineImpl implements StateMachine {

    private final TelegramBot telegramBot;

    private final StateRepository stateRepository;

    private final Map<Long, CommandContext> commandContexts;

    @Autowired
    public StateMachineImpl(TelegramBot telegramBot, StateRepository stateRepository) {
        this.telegramBot = telegramBot;
        this.stateRepository = stateRepository;
        this.commandContexts = new HashMap<>();
    }

    @Override
    public Command process(Long chatId, Update update) {
        UserState userState = stateRepository.getStateById(chatId);
        if (userState == null) {
            telegramBot.execute(new SendMessage(chatId, "Сначала нужно зарегистрироваться, нажмите /start"));
        } else {
            switch (userState) {
                case UserState.AWAITING_TRACK_URL -> {
                    if (!Validator.isGitHubRepo(update.message().text())
                            && !Validator.isStackOverflowQuestion(
                                    update.message().text())) {
                        telegramBot.execute(new SendMessage(chatId, "Невалидная ссылка"));
                    } else {
                        CommandContext commandContext = new CommandContext();
                        commandContext.url(update.message().text());
                        commandContexts.put(chatId, commandContext);
                        stateRepository.setState(chatId, UserState.AWAITING_TAGS);
                        telegramBot.execute(new SendMessage(chatId, "Введите тэги"));
                    }
                }
                case UserState.AWAITING_TAGS -> {
                    String list = update.message().text();
                    List<String> tags = Arrays.stream(list.split("\\s+")).toList();
                    commandContexts.get(chatId).tags(tags);
                    stateRepository.setState(chatId, UserState.AWAITING_FILTERS);
                    telegramBot.execute(new SendMessage(chatId, "Введите фильтры"));
                }
                case UserState.AWAITING_FILTERS -> {
                    String list = update.message().text();
                    long id = update.message().chat().id();
                    List<String> filters = Arrays.stream(list.split("\\s+")).toList();
                    commandContexts.get(chatId).filters(filters);
                    stateRepository.setState(chatId, UserState.DEFAULT);
                    CommandContext context = commandContexts.get(chatId);
                    commandContexts.remove(chatId);
                    return new TrackCommand(id, context.url(), context.tags(), context.filters());
                }
                case UserState.AWAITING_UNTRACK_URL -> {
                    if (!Validator.isGitHubRepo(update.message().text())
                            && !Validator.isStackOverflowQuestion(
                                    update.message().text())) {
                        telegramBot.execute(new SendMessage(chatId, "Невалидная ссылка"));
                    } else {
                        stateRepository.setState(chatId, UserState.DEFAULT);
                        return new UntrackCommand(chatId, update.message().text());
                    }
                }
                case UserState.DEFAULT -> {
                    telegramBot.execute(new SendMessage(
                            chatId, "Комманды " + update.message().text() + " не существует"));
                }
            }
        }
        return null;
    }
}
