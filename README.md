# 📒 NoteBot – Blocco note tramite Telegram Bot

## 📌 Descrizione del progetto
NoteBot è un semplice applicativo scritto in **Java** che permette di gestire un blocco note tramite un **bot Telegram**.  
L’applicazione non accetta connessioni in ingresso: funziona interamente come **client HTTP** che interroga periodicamente la Bot API di Telegram usando il metodo `getUpdates`, interpreta i messaggi ricevuti e risponde con `sendMessage`.

---

## 🧱 Architettura del progetto

Il progetto è composto da tre classi principali:

### **1. Main.java**
- Avvia il poller.
- Legge il token del bot da un file di testo esterno (`token.txt`).
- Esegue un ciclo infinito chiamando `processOneBatch()` ogni 2 secondi per interrogare l’API.

### **2. BotServer.java**
Il cuore dell’applicazione.  
Responsabilità:
- interrogare Telegram (`getUpdates`)
- interpretare messaggi e comandi `/nota`, `/leggi`, `/help`
- salvare e leggere note tramite file locali
- mantenere `offset.txt` per non rielaborare gli stessi update
- inviare risposte con `NoteBot`

### **3. NoteBot.java**
- Classe che invia messaggi tramite `sendMessage`.
- Costruisce l’URL dell’API e manda richieste HTTP GET.
- Restituisce la risposta JSON.

---

## 📡 Come funziona lo scambio con Telegram

L’applicazione comunica **solo in uscita** con Telegram mediante richieste HTTP:

1. L’utente invia un messaggio al bot in Telegram.
2. Telegram lo registra come *update*.
3. Il nostro programma chiama periodicamente:
   ```
   https://api.telegram.org/bot<TOKEN>/getUpdates?offset=X
   ```
4. Il programma interpreta i comandi e risponde con `sendMessage`.

> **Non esiste un server HTTP locale.**  
> Tutta la comunicazione passa attraverso i server di Telegram.

---

## 📝 Comandi disponibili

| Comando | Funzione |
|--------|----------|
| `/nota <testo>` | Salva una nota associata alla chat |
| `/leggi` | Mostra le ultime 5 note |
| `/leggi N` | Mostra le ultime N note |
| `/help` | Mostra l’elenco dei comandi |

Il sistema salva le note in file separati:

```
notes_<chatId>.txt
```

---

## 💾 Persistenza

Il programma salva:

- **Le note** → un file per ogni chat
- **L’ultimo update elaborato** → `offset.txt`

In questo modo il bot non risponde due volte agli stessi messaggi.

---

## 🔐 Gestione del token del bot

Per motivi di **sicurezza** e di **versionamento del codice**, il token del bot Telegram **non è incluso nel codice sorgente**.

Il programma legge il token da un file di testo esterno chiamato:

```
token.txt
```

Il file deve contenere **solo il token**, ad esempio:

```
123456789:AAEsempioTokenFornitoDaBotFather
```

All’avvio dell’applicazione, la classe `Main`:
- legge il contenuto del file `token.txt`;
- passa il token al `BotServer`;
- termina il programma mostrando un messaggio di errore se il file non esiste o non è leggibile.

Questa scelta permette di:
- evitare la pubblicazione accidentale del token su GitHub;
- separare configurazione e codice;
- semplificare l’uso del progetto in laboratorio o su più macchine.

👉 **Il file `token.txt` non deve essere versionato**.

---

## 📄 `.gitignore` (consigliato)

Si consiglia di aggiungere al file `.gitignore` la seguente riga:

```
token.txt
```

---

## ▶️ Avvio

1. Creare un bot con **BotFather** e ottenere il token.
2. Creare un file `token.txt` nella directory principale del progetto e inserirvi il token.
3. Eseguire il progetto da IntelliJ (o da riga di comando se configurato).

4. Scrivere al bot su Telegram!

---

## 📂 Struttura del progetto

```
lib/
 ├── TelegramJsonParser.java
 └── TelegramMessage.java
src/
 ├── Main.java
 ├── BotServer.java
 └── NoteBot.java
token.txt              (da creare manualmente, NON versionato)
offset.txt             (generato automaticamente)
notes_<chatId>.txt     (generato automaticamente)
```

---

## 🔍 Parsing del JSON di Telegram

Il progetto **non utilizza librerie JSON esterne** (come Gson o Jackson).  
Il parsing delle risposte JSON restituite dalla Bot API di Telegram è gestito tramite
una **libreria interna minimale**, pensata con scopo didattico.

La libreria è composta da:

- `TelegramMessage` → rappresenta un messaggio estratto da un update
- `TelegramJsonParser` → estrae i messaggi testuali dal JSON di `getUpdates`

Questa scelta permette di:
- ridurre le dipendenze del progetto;
- analizzare in modo diretto la struttura del JSON;
- comprendere i limiti di un parsing manuale.

📄 **Documentazione dettagliata:**  
`docs/parser_json_doc.md`

---

## 🎯 Obiettivi didattici

Il progetto permette di comprendere:

- il funzionamento delle API di Telegram e del protocollo HTTP
- il polling tramite `getUpdates`
- gestione dei file in Java
- parsing JSON
- differenza tra server reale e applicazione client

---

## 🚀 Possibili estensioni

- comando per cancellare note
- categorie/tag
- esportazione note
- migrazione da file a database
- passaggio da polling a webhook (richiede server pubblico)

---

## 📜 Licenza

MIT
