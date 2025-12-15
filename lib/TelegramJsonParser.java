import java.util.ArrayList;
import java.util.List;

public class TelegramJsonParser {

    /** API principale: estrae una lista di Update dal JSON grezzo di getUpdates */
    public static List<TelegramMessage> parseMessages(String json) {
        List<TelegramMessage> out = new ArrayList<>();
        List<String> chunks = splitUpdates(json);

        for (String c : chunks) {
            long updateId = extractLong(c, "\"update_id\":");
            if (updateId < 0) continue;

            String chatId = extractStringOrNumber(c, "\"chat\":{\"id\":");
            String text = extractQuoted(c, "\"text\":\"");

            if (chatId != null && text != null) {
                out.add(new TelegramMessage(updateId, chatId, text));
            }
        }
        return out;
    }

    // -----------------------
    // Metodi di supporto
    // -----------------------

    /** Taglia grossolanamente il JSON in “blocchi” che iniziano con "update_id" */
    private static List<String> splitUpdates(String json) {
        List<String> out = new ArrayList<>();
        int idx = 0;
        while (true) {
            int start = json.indexOf("\"update_id\":", idx);
            if (start < 0) break;
            int next = json.indexOf("\"update_id\":", start + 1);
            String chunk = (next < 0) ? json.substring(start) : json.substring(start, next);
            out.add(chunk);
            idx = (next < 0) ? json.length() : next;
        }
        return out;
    }

    /** Estrae un long subito dopo la chiave indicata (es. "\"update_id\":") */
    private static long extractLong(String src, String key) {
        int p = src.indexOf(key);
        if (p < 0) return -1;
        p += key.length();
        int q = p;
        while (q < src.length() && Character.isDigit(src.charAt(q))) q++;
        try { return Long.parseLong(src.substring(p, q)); } catch (Exception e) { return -1; }
    }

    /** Estrae una stringa quotata gestendo le escape minime (\n, \t, \") */
    private static String extractQuoted(String src, String keyWithOpeningQuote) {
        int p = src.indexOf(keyWithOpeningQuote);
        if (p < 0) return null;
        p += keyWithOpeningQuote.length();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = p; i < src.length(); i++) {
            char c = src.charAt(i);
            if (escaped) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
                escaped = false;
            } else {
                if (c == '\\') escaped = true;
                else if (c == '"') break;
                else sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Estrae un valore dopo la chiave che può essere stringa quotata o numero non quotato.
     * Esempio chiave: "\"chat\":{\"id\":"
     */
    private static String extractStringOrNumber(String src, String key) {
        int p = src.indexOf(key);
        if (p < 0) return null;
        p += key.length();
        while (p < src.length() && (src.charAt(p) == ' ' || src.charAt(p) == ':')) p++;

        if (p < src.length() && src.charAt(p) == '"') {
            // Valore quotato → ricomponiamo una chiave che apra la stringa
            String opening = key.replace(":", ":\"");
            return extractQuoted(src, opening);
        } else {
            // Numero non quotato (es. chat.id)
            int q = p;
            while (q < src.length() && (Character.isDigit(src.charAt(q)) || src.charAt(q) == '-')) q++;
            return src.substring(p, q);
        }
    }
}
