package backend.academy.bot.service.impl;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.CommandContext;
import backend.academy.bot.model.UserState;
import backend.academy.bot.model.command.Command;
import backend.academy.bot.model.command.impl.AddTagsToLinkCommand;
import backend.academy.bot.model.command.impl.CreateTagCommand;
import backend.academy.bot.model.command.impl.RemoveTagsFromLinkCommand;
import backend.academy.bot.model.command.impl.TrackCommand;
import backend.academy.bot.model.command.impl.UntrackCommand;
import backend.academy.bot.repository.StateRepository;
import backend.academy.bot.service.StateMachine;
import backend.academy.bot.util.KeyBoardInitializer;
import backend.academy.bot.util.Validator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Setter
public class StateMachineImpl implements StateMachine {

    private final TelegramBot telegramBot;
    private final StateRepository stateRepository;
    private final Map<Long, CommandContext> commandContexts;
    private final ScrapperClient scrapperClient;

    @Autowired
    public StateMachineImpl(TelegramBot telegramBot, StateRepository stateRepository, ScrapperClient scrapperClient) {
        this.telegramBot = telegramBot;
        this.stateRepository = stateRepository;
        this.scrapperClient = scrapperClient;
        this.commandContexts = new HashMap<>();
    }

    @Override
    public Command process(Long chatId, Update update) {
        Long actualChatId;
        if (update.message() != null) {
            actualChatId = update.message().chat().id();
        } else if (update.callbackQuery() != null) {
            actualChatId = update.callbackQuery().message().chat().id();
        } else {
            return null;
        }

        KeyBoardInitializer keyBoardInitializer =
                new KeyBoardInitializer(scrapperClient.getUserTags(actualChatId).block());
        UserState userState = stateRepository.getStateById(actualChatId);
        if (userState == null) {
            telegramBot.execute(new SendMessage(actualChatId, "Сначала нужно зарегистрироваться, нажмите /start"));
        } else {
            switch (userState) {
                case AWAITING_TRACK_URL -> {
                    String text = update.message().text();
                    if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
                        telegramBot.execute(new SendMessage(actualChatId, "Невалидная ссылка"));
                    } else {
                        CommandContext commandContext = new CommandContext();
                        commandContext.url(text);
                        commandContext.tags(new ArrayList<>());
                        commandContexts.put(actualChatId, commandContext);
                        stateRepository.setState(actualChatId, UserState.AWAITING_TAGS);
                        telegramBot.execute(new SendMessage(actualChatId, "Выберите тэги")
                                .replyMarkup(keyBoardInitializer.generateKeyboard("")));
                    }
                }
                case AWAITING_TAGS -> {
                    if (update.callbackQuery() != null) {
                        String callbackData = update.callbackQuery().data();
                        List<String> selectedTags = handleCallback(update.callbackQuery(), keyBoardInitializer);
                        if (callbackData.startsWith("done_")) {
                            commandContexts.get(actualChatId).tags(selectedTags);
                            stateRepository.setState(actualChatId, UserState.AWAITING_FILTERS);
                            telegramBot.execute(new SendMessage(actualChatId, "Введите фильтры"));
                        }
                    }
                }
                case AWAITING_FILTERS -> {
                    String list = update.message().text();
                    List<String> filters = Arrays.stream(list.split("\\s+")).toList();
                    commandContexts.get(actualChatId).filters(filters);
                    stateRepository.setState(actualChatId, UserState.DEFAULT);
                    CommandContext context = commandContexts.get(actualChatId);
                    commandContexts.remove(actualChatId);
                    return new TrackCommand(actualChatId, context.url(), context.tags(), context.filters());
                }
                case AWAITING_UNTRACK_URL -> {
                    String text = update.message().text();
                    if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
                        telegramBot.execute(new SendMessage(actualChatId, "Невалидная ссылка"));
                    } else {
                        stateRepository.setState(actualChatId, UserState.DEFAULT);
                        return new UntrackCommand(actualChatId, text);
                    }
                }
                case AWAITING_TAG_NAME -> {
                    stateRepository.setState(actualChatId, UserState.DEFAULT);
                    return new CreateTagCommand(actualChatId, update.message().text());
                }

                case AWAITING_ADD_TAGS_URL -> {
                    String text = update.message().text();
                    if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
                        telegramBot.execute(new SendMessage(actualChatId, "Невалидная ссылка"));
                    } else {
                        CommandContext commandContext = new CommandContext();
                        commandContext.url(text);
                        commandContext.tags(new ArrayList<>());
                        commandContexts.put(actualChatId, commandContext);
                        stateRepository.setState(actualChatId, UserState.AWAITING_ADD_TAGS_NAME);
                        telegramBot.execute(new SendMessage(actualChatId, "Выберите тэги, которые хотите добавить")
                                .replyMarkup(keyBoardInitializer.generateKeyboard("")));
                    }
                }

                case AWAITING_REMOVE_TAGS_URL -> {
                    String text = update.message().text();
                    if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
                        telegramBot.execute(new SendMessage(actualChatId, "Невалидная ссылка"));
                    } else {
                        CommandContext commandContext = new CommandContext();
                        commandContext.url(text);
                        commandContext.tags(new ArrayList<>());
                        commandContexts.put(actualChatId, commandContext);
                        stateRepository.setState(actualChatId, UserState.AWAITING_REMOVE_TAGS_NAME);
                        telegramBot.execute(new SendMessage(actualChatId, "Выберите тэги, которые хотите удалить")
                                .replyMarkup(keyBoardInitializer.generateKeyboard("")));
                    }
                }

                case AWAITING_REMOVE_TAGS_NAME -> {
                    if (update.callbackQuery() != null) {
                        String callbackData = update.callbackQuery().data();
                        List<String> selectedTags = handleCallback(update.callbackQuery(), keyBoardInitializer);
                        if (callbackData.startsWith("done_")) {
                            commandContexts.get(actualChatId).tags(selectedTags);
                            stateRepository.setState(actualChatId, UserState.DEFAULT);
                            CommandContext context = commandContexts.get(actualChatId);
                            commandContexts.remove(actualChatId);
                            return new RemoveTagsFromLinkCommand(actualChatId, context.url(), context.tags());
                        }
                    }
                }

                case AWAITING_ADD_TAGS_NAME -> {
                    if (update.callbackQuery() != null) {
                        String callbackData = update.callbackQuery().data();
                        List<String> selectedTags = handleCallback(update.callbackQuery(), keyBoardInitializer);
                        if (callbackData.startsWith("done_")) {
                            commandContexts.get(actualChatId).tags(selectedTags);
                            stateRepository.setState(actualChatId, UserState.DEFAULT);
                            CommandContext context = commandContexts.get(actualChatId);
                            commandContexts.remove(actualChatId);
                            return new AddTagsToLinkCommand(actualChatId, context.url(), context.tags());
                        }
                    }
                }

                case DEFAULT -> {
                    if (update.message() != null) {
                        telegramBot.execute(new SendMessage(
                                actualChatId, "Команды " + update.message().text() + " не существует"));
                    }
                }
            }
        }
        return null;
    }

    private List<String> handleCallback(CallbackQuery callbackQuery, KeyBoardInitializer keyBoardInitializer) {
        telegramBot.execute(new AnswerCallbackQuery(callbackQuery.id()));
        long chatId = callbackQuery.message().chat().id();
        int messageId = callbackQuery.message().messageId();
        String data = callbackQuery.data();

        CommandContext context = commandContexts.get(chatId);
        List<String> selectedTagList =
                context != null && context.tags() != null ? new ArrayList<>(context.tags()) : new ArrayList<>();

        if (data.startsWith("done_")) {
            return selectedTagList;
        }

        if (data.startsWith("toggle_")) {
            String tag = data.replace("toggle_", "");

            if (selectedTagList.contains(tag)) {
                selectedTagList.remove(tag);
            } else {
                selectedTagList.add(tag);
            }

            String newSelectedTags = String.join(",", selectedTagList).replaceAll("^,|,$", "");

            if (context != null) {
                context.tags(selectedTagList);
            }
            telegramBot.execute(new EditMessageReplyMarkup(chatId, messageId)
                    .replyMarkup(keyBoardInitializer.generateKeyboard(newSelectedTags)));

            return selectedTagList;
        }

        return new ArrayList<>();
    }
}
