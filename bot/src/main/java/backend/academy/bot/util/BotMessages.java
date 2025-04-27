package backend.academy.bot.util;

public interface BotMessages {
    String UPDATE_OK_RESPONSE = "Обновление обработано";
    String REGISTRATION_NEED = "Сначала нужно зарегистрироваться, нажмите /start";
    String COMMAND_NOT_ALLOWED = "Сейчас вы не можете использовать эту команду";
    String ENTER_LINK = "Введите ссылку";
    String ENTER_TAG_NAME = "Введите название тэга";
    String ENTER_ADD_TAG_NAME = "Выберите тэги, которые хотите добавить";
    String ENTER_REMOVE_TAG_NAME = "Выберите тэги, которые хотите удалить";
    String CHOOSE_TAGS = "Выберите тэги";
    String ENTER_FIlTERS = "Введите фильтры";
    String CANCEL_NOT_ALLOWED = "Сейчас вам нечего отменять";
    String INVALID_LINK = "Невалидная ссылка";
    String CANCELED_COMMAND = "Команда отменена";
    String HELP_COMMAND =
            """
        /start - регистрация пользователя.
        /help - вывод списка доступных команд.
        /track - начать отслеживание ссылки.
        /untrack - прекратить отслеживание ссылки.
        /list - показать список отслеживаемых ссылок (cписок ссылок, полученных при /track)
        /cancel - отменить выполнение команды.
        """;
    String EMPTY_LINK_LIST = "У вас нет отслеживаемых ссылок";
}
