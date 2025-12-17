# Parsing del JSON di Telegram (classe `TelegramJsonParser`)

Questa parte del progetto si occupa di estrarre dal JSON restituito dall’endpoint **`getUpdates`** di Telegram le informazioni minime necessarie per far funzionare il bot come “blocco note”:

- `update_id` (per sapere fino a quale update abbiamo già letto)
- `chat.id` (per sapere a quale chat rispondere)
- `text` (il contenuto del messaggio inviato dall’utente)

Nel progetto questi dati vengono rappresentati da una classe semplice (un “contenitore” di campi) e da un parser che trasforma una stringa JSON grezza in una lista di messaggi.

---

## 1) Modello dati: `TelegramMessage`

La classe `TelegramMessage` rappresenta un singolo messaggio “utile” estratto dagli update:

- `updateId` → l’identificativo progressivo dell’update
- `chatId` → l’identificativo della chat (salvato come stringa per gestire sia numeri che eventuali formati diversi)
- `text` → il testo del messaggio

È una classe volutamente minimale: contiene solo campi pubblici e un costruttore.
---

## 2) Parser: `TelegramJsonParser`

### API principale

L’entry point è:

```java
public static List<TelegramMessage> parseMessages(String json)
```

- Input: il JSON grezzo restituito da `getUpdates` (come stringa).
- Output: una `List<TelegramMessage>` con **solo** gli update che contengono sia `chat.id` sia `text`.

### Strategia adottata (senza dipendenze esterne)

Invece di usare una libreria JSON (Gson/Jackson ecc.), il parser lavora con semplici operazioni su stringa:

1. **Spezzare** il JSON in “blocchi” (uno per update) cercando ricorrenze di `"update_id":`.
2. Per ogni blocco:
   - estrarre `update_id` (numero)
   - estrarre `chat.id` (numero o stringa)
   - estrarre `text` (stringa quotata)
3. Se i campi essenziali esistono → creare un `TelegramMessage` e aggiungerlo alla lista.

Questa scelta mantiene il progetto molto “semplice” e riduce dipendenze, ma introduce alcune limitazioni (vedi sotto).

---

## 3) Metodi interni del parser

Il parser include alcuni metodi di supporto:

- `splitUpdates(json)`
  Taglia grossolanamente la stringa in più sottostringhe, ognuna a partire da `"update_id":`.
  Non è un vero parsing JSON: è una segmentazione “testuale”.

- `extractLong(src, key)`
  Estrae un `long` subito dopo una chiave (es. `"update_id":`).

- `extractQuoted(src, keyWithOpeningQuote)`
  Estrae una stringa racchiusa tra virgolette a partire da una chiave che termina con `"` (es. `"text":"`).
  Gestisce alcune escape minime: `\n`, `\t`, `\"` (e in generale “copia” il carattere dopo `\` se non è `n` o `t`).

- `extractStringOrNumber(src, key)`
  Estrae un valore che può essere:
  - una stringa quotata, oppure
  - un numero non quotato (tipico di `chat.id`)
  Restituisce sempre una `String`.

---

## 4) Cosa viene ignorato (scelte intenzionali)

Questo parser **non** gestisce tutti i tipi di update Telegram. In particolare:

- considera solo gli update che contengono `text`;
- non gestisce foto, sticker, audio, documenti, callback query, ecc.;
- non gestisce `edited_message` o altri rami possibili del JSON.

Per un bot “blocco note” basato su comandi testuali è spesso sufficiente, ma è importante esserne consapevoli.

---

## 5) Limitazioni e casi limite

Dato che è un parser “a stringhe”, ci sono alcune limitazioni tipiche:

- se il formato JSON cambia, oppure se le chiavi compaiono in ordine diverso o in punti inaspettati, l’estrazione potrebbe fallire;
- la segmentazione tramite `"update_id":` è una scorciatoia: funziona finché quell’attributo compare una volta per update e il JSON ha una struttura compatibile con `getUpdates`;
- `extractQuoted` gestisce solo un sottoinsieme delle escape JSON (per un supporto completo servirebbe una vera libreria JSON).

Se il progetto dovesse crescere, il passo naturale sarebbe sostituire questa classe con un parser JSON standard (ad es. Gson o Jackson) e mappare correttamente gli oggetti restituiti da Telegram.

---

## 6) Esempio d’uso

Esempio minimale (pseudo-codice):

```java
String json = httpGet("https://api.telegram.org/bot<TOKEN>/getUpdates");
List<TelegramMessage> msgs = TelegramJsonParser.parseMessages(json);

for (TelegramMessage m : msgs) {
    System.out.println("Update: " + m.updateId);
    System.out.println("Chat:   " + m.chatId);
    System.out.println("Text:   " + m.text);
}
```

---

## 7) Note per Git e versionamento

- Questa parte del codice **non contiene segreti**.
- Il token del bot **non dovrebbe mai** essere committato nel repository: tipicamente si mette in un file escluso da Git (`.gitignore`) o in variabili d’ambiente.
