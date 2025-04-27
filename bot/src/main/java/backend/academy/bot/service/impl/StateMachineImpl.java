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
import backend.academy.bot.util.BotMessages;
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
public class StateMachineImpl implements StateMachine, BotMessages {

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
        Long actualChatId = getChatId(update);
        if (actualChatId == null) return null;

        UserState userState = stateRepository.getStateById(actualChatId);
        if (userState == null) {
            telegramBot.execute(new SendMessage(actualChatId, REGISTRATION_NEED));
            return null;
        }

        KeyBoardInitializer keyBoardInitializer =
            new KeyBoardInitializer(scrapperClient.getUserTags(actualChatId).block());

        return switch (userState) {
            case AWAITING_TRACK_URL -> handleAwaitingTrackUrl(update, actualChatId, keyBoardInitializer);
            case AWAITING_TAGS -> handleAwaitingTags(update, actualChatId, keyBoardInitializer);
            case AWAITING_FILTERS -> handleAwaitingFilters(update, actualChatId);
            case AWAITING_UNTRACK_URL -> handleAwaitingUntrackUrl(update, actualChatId);
            case AWAITING_TAG_NAME -> handleAwaitingTagName(update, actualChatId);
            case AWAITING_ADD_TAGS_URL -> handleAwaitingAddTagsUrl(update, actualChatId, keyBoardInitializer);
            case AWAITING_REMOVE_TAGS_URL -> handleAwaitingRemoveTagsUrl(update, actualChatId, keyBoardInitializer);
            case AWAITING_REMOVE_TAGS_NAME -> handleAwaitingRemoveTagsName(update, actualChatId, keyBoardInitializer);
            case AWAITING_ADD_TAGS_NAME -> handleAwaitingAddTagsName(update, actualChatId, keyBoardInitializer);
            case DEFAULT -> handleDefault(update, actualChatId);
        };
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

    private Long getChatId(Update update) {
        if (update.message() != null) return update.message().chat().id();
        if (update.callbackQuery() != null)
            return update.callbackQuery().message().chat().id();
        return null;
    }

    private Command handleAwaitingTrackUrl(Update update, Long chatId, KeyBoardInitializer keyBoardInitializer) {
        String text = update.message().text();
        if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
            telegramBot.execute(new SendMessage(chatId, INVALID_LINK));
        } else {
            CommandContext context = new CommandContext().url(text).tags(new ArrayList<>());
            commandContexts.put(chatId, context);
            stateRepository.setState(chatId, UserState.AWAITING_TAGS);
            telegramBot.execute(
                new SendMessage(chatId, CHOOSE_TAGS).replyMarkup(keyBoardInitializer.generateKeyboard("")));
        }
        return null;
    }

    private Command handleAwaitingTags(Update update, Long chatId, KeyBoardInitializer keyBoardInitializer) {
        if (update.callbackQuery() != null) {
            String callbackData = update.callbackQuery().data();
            List<String> selectedTags = handleCallback(update.callbackQuery(), keyBoardInitializer);
            if (callbackData.startsWith("done_")) {
                commandContexts.get(chatId).tags(selectedTags);
                stateRepository.setState(chatId, UserState.AWAITING_FILTERS);
                telegramBot.execute(new SendMessage(chatId, ENTER_FIlTERS));
            }
        }
        return null;
    }

    private Command handleAwaitingFilters(Update update, Long chatId) {
        List<String> filters =
            Arrays.stream(update.message().text().split("\\s+")).toList();
        CommandContext context = commandContexts.get(chatId);
        context.filters(filters);
        stateRepository.setState(chatId, UserState.DEFAULT);
        commandContexts.remove(chatId);
        return new TrackCommand(chatId, context.url(), context.tags(), context.filters());
    }

    private Command handleAwaitingUntrackUrl(Update update, Long chatId) {
        String text = update.message().text();
        if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
            telegramBot.execute(new SendMessage(chatId, INVALID_LINK));
            return null;
        }
        stateRepository.setState(chatId, UserState.DEFAULT);
        return new UntrackCommand(chatId, text);
    }

    private Command handleAwaitingTagName(Update update, Long chatId) {
        stateRepository.setState(chatId, UserState.DEFAULT);
        return new CreateTagCommand(chatId, update.message().text());
    }

    private Command handleAwaitingAddTagsUrl(Update update, Long chatId, KeyBoardInitializer keyBoardInitializer) {
        return handleUrlInputWithTagStep(
            update, chatId, keyBoardInitializer, UserState.AWAITING_ADD_TAGS_NAME, ENTER_ADD_TAG_NAME);
    }

    private Command handleAwaitingRemoveTagsUrl(Update update, Long chatId, KeyBoardInitializer keyBoardInitializer) {
        return handleUrlInputWithTagStep(
            update, chatId, keyBoardInitializer, UserState.AWAITING_REMOVE_TAGS_NAME, ENTER_REMOVE_TAG_NAME);
    }

    private Command handleAwaitingRemoveTagsName(Update update, Long chatId, KeyBoardInitializer keyBoardInitializer) {
        return handleTagSelection(
            update, chatId, keyBoardInitializer, UserState.DEFAULT, RemoveTagsFromLinkCommand::new);
    }

    private Command handleAwaitingAddTagsName(Update update, Long chatId, KeyBoardInitializer keyBoardInitializer) {
        return handleTagSelection(update, chatId, keyBoardInitializer, UserState.DEFAULT, AddTagsToLinkCommand::new);
    }

    private Command handleDefault(Update update, Long chatId) {
        if (update.message() != null) {
            telegramBot.execute(
                new SendMessage(chatId, "Команды " + update.message().text() + " не существует"));
        }
        return null;
    }

    private Command handleUrlInputWithTagStep(
        Update update, Long chatId, KeyBoardInitializer keyboard, UserState nextState, String prompt) {
        String text = update.message().text();
        if (!Validator.isGitHubRepo(text) && !Validator.isStackOverflowQuestion(text)) {
            telegramBot.execute(new SendMessage(chatId, INVALID_LINK));
            return null;
        }

        CommandContext context = new CommandContext().url(text).tags(new ArrayList<>());
        commandContexts.put(chatId, context);
        stateRepository.setState(chatId, nextState);
        telegramBot.execute(new SendMessage(chatId, prompt).replyMarkup(keyboard.generateKeyboard("")));
        return null;
    }

    private Command handleTagSelection(
        Update update,
        Long chatId,
        KeyBoardInitializer keyboard,
        UserState nextState,
        TagApplyer<Long, String, List<String>, Command> commandConstructor) {
        if (update.callbackQuery() != null) {
            String callbackData = update.callbackQuery().data();
            List<String> selectedTags = handleCallback(update.callbackQuery(), keyboard);
            if (callbackData.startsWith("done_")) {
                CommandContext context = commandContexts.get(chatId);
                context.tags(selectedTags);
                stateRepository.setState(chatId, nextState);
                commandContexts.remove(chatId);
                return commandConstructor.apply(chatId, context.url(), context.tags());
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface TagApplyer<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
