package backend.academy.bot.util;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeyBoardInitializer {
    private final List<String> options;

    public KeyBoardInitializer(List<String> options) {
        this.options = options;
    }

    public InlineKeyboardMarkup generateKeyboard(String selectedTags) {
        List<String> selectedTagList =
                selectedTags.isEmpty() ? new ArrayList<>() : Arrays.asList(selectedTags.split(","));

        List<InlineKeyboardButton[]> rows = options.stream()
                .map(tag -> {
                    boolean isSelected = selectedTagList.contains(tag);
                    String buttonText = (isSelected ? "✅ " : "") + tag;
                    return new InlineKeyboardButton[] {
                        new InlineKeyboardButton(buttonText).callbackData("toggle_" + tag)
                    };
                })
                .collect(Collectors.toList());

        rows.add(new InlineKeyboardButton[] {new InlineKeyboardButton("Выбрать").callbackData("done_" + selectedTags)});

        return new InlineKeyboardMarkup(rows.toArray(new InlineKeyboardButton[0][]));
    }
}
