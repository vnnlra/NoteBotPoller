public class TelegramMessage {
    public long updateId;
    public String chatId;
    public String text;

    public TelegramMessage(long updateId, String chatId, String text) {
        this.updateId = updateId;
        this.chatId = chatId;
        this.text = text;
    }
}
