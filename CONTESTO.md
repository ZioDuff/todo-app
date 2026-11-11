# Contesto del progetto TodoApp

Ultimo aggiornamento: 11 novembre 2026.

## Obiettivo e modalità di collaborazione

Il progetto serve a consolidare le basi di Java, Spring e dello sviluppo di API REST con PostgreSQL.

- L'utente implementa il codice e chiede spiegazioni, suggerimenti sulla logica e revisioni.
- Non scrivere o modificare codice senza una richiesta esplicita dell'utente.
- Spiegare il ragionamento e i concetti con gradualità, in italiano.
- Prima di valutare un'implementazione, leggere i file attuali: questo documento riassume una situazione, ma il codice può essere cambiato.
- Aggiornare questo documento quando richiesto, registrando decisioni e attività realmente completate. Non considerare automaticamente completati i prossimi passi.

## Struttura e stato attuale

Package principale: `com.demaio.jacopo.todoapp`.

- `Task`: entity con `id`, `title`, `description`, `status`, `dueDate`.
- `Status`: enum con `TODO`, `ONGOING`, `COMPLETED`.
- `TaskRepository`: accesso alla persistenza.
- `TaskService`: creazione, lettura singola e lista, aggiornamento dettagli, aggiornamento stato, eliminazione.
- `TaskController`: endpoint sotto `/api/tasks`.
- `RestExceptionHandler`: gestisce task inesistenti, dati applicativi non validi, errori di `@Valid` e JSON non leggibile; usa `ErrorResponse(errorCode, message)`.
- DTO di richiesta separati: `CreateTaskRequest`, `UpdateTaskRequest`, `UpdateStatusRequest`.
- Le risposte delle operazioni che restituiscono task usano ancora direttamente l'entity.

La prima versione didattica del backend CRUD è considerata conclusa nelle funzionalità principali. L'utente ha scelto di rimandare l'ampliamento dei test e l'uniformazione dei controlli di lunghezza. Non avviare automaticamente queste attività alla ripresa: concordare il prossimo obiettivo.

## Endpoint implementati

| Metodo | Percorso | Comportamento attuale |
| --- | --- | --- |
| POST | `/api/tasks` | Crea una task con stato iniziale TODO; restituisce la task con 201 Created e header Location della nuova risorsa |
| GET | `/api/tasks` | Restituisce tutte le task |
| GET | `/api/tasks/{id}` | Restituisce una task; 404 se assente |
| PATCH | `/api/tasks/{id}/details` | Aggiorna i dettagli, mantenendo separata la modifica dello stato |
| PATCH | `/api/tasks/{id}/status` | Aggiorna lo stato e restituisce la task con 200 |
| DELETE | `/api/tasks/{id}` | Elimina la task e restituisce 204 senza corpo; 404 se assente |

Il controller del delete usa `ResponseEntity<Void>`. Il controllo sull'esistenza è nel service, tramite `findTaskById`, prima della cancellazione.

Il controller della creazione usa `ResponseEntity<Task>` e `ResponseEntity.created(location).body(task)`. L'URI è costruito con `ServletUriComponentsBuilder.fromCurrentRequest()`, aggiungendo `/{id}`.

## Regole e decisioni concordate

### Titolo

- Obbligatorio anche nell'aggiornamento dei dettagli: se non cambia, il client deve inviare quello attuale.
- Il futuro frontend può precompilare il modulo con i valori esistenti.
- Nei DTO di creazione e aggiornamento: `@NotBlank` e `@Size(max = 200)`.
- Nel service sono presenti anche controlli su titolo nullo o blank; il limite di 200 non è verificato direttamente nel service.
- `@Valid` nel controller attiva la validazione del DTO per le richieste HTTP.

### Descrizione

- Facoltativa.
- Nell'update, `null`, stringa vuota o composta soltanto da spazi mantengono la descrizione precedente.
- Una descrizione non blank viene assegnata se non supera 255 caratteri; altrimenti il service lancia `InvalidTaskDataException`.
- Attualmente non è possibile cancellare la descrizione attraverso questo aggiornamento: è una conseguenza da tenere presente, non una funzionalità già risolta.
- Il controllo di lunghezza non è ancora presente nella creazione né come vincolo nei DTO.
- Nell'entity è dichiarato soltanto `@Column(name = "description")`: non prova quale sia il limite effettivo in PostgreSQL. Il valore predefinito JPA di 255 non basta a dedurre lo schema reale.

### Scadenza

- La data è rifiutata se precedente a `LocalDate.now().minusDays(7)`.
- Una data esattamente di sette giorni fa è accettata.
- La regola è presente sia in creazione sia nell'update.
- Nell'update, `null` mantiene la scadenza precedente; una data valida la sostituisce.
- Attualmente non c'è un meccanismo per rimuovere una scadenza esistente tramite questo endpoint.

### Stato

- Il DTO contiene direttamente l'enum `Status`, con `@NotNull`, non una String.
- Non occorre uno switch per assegnare lo stato: il valore del DTO ha già il tipo richiesto dal setter.
- Non sono state introdotte restrizioni sulle transizioni fra gli stati.
- Ricevere lo stato già presente è accettato senza eccezione. Attualmente il service esegue comunque il salvataggio.
- Il service rifiuta uno stato nullo con `InvalidTaskDataException`; il controller usa anche `@Valid` sul DTO.
- Il service restituisce il risultato del salvataggio e il controller risponde con la task aggiornata.

### PUT e PATCH

- È stato scelto PATCH per i dettagli perché alcuni valori nulli significano mantenere il dato esistente.
- PUT rappresenta la sostituzione completa della risorsa esposta dall'endpoint, non necessariamente di tutta l'entity nel database.
- PATCH può modificare anche tutti i campi in una singola richiesta: non dipende dal numero di campi cambiati.

### Gestione centralizzata degli errori

- `TaskNotFoundException`: risposta 404 con codice `TASK_NOT_FOUND`.
- `InvalidTaskDataException`: eccezione custom che estende `RuntimeException`, contiene `errorCode` e passa il messaggio a `super(message)`. Sostituisce le precedenti `IllegalArgumentException` del service; risposta 400 con codice `INVALID_TASK_DATA` e messaggi specifici per titolo, descrizione, scadenza e stato.
- `MethodArgumentNotValidException`: risposta 400 con codice `VALIDATION_ERROR`. L'handler usa `getBindingResult().getAllErrors()`, aggiunge il nome del campo per i `FieldError` e unisce tutti i messaggi con `; `. Se la stringa risultante è vuota o blank, usa `Request validation failed`.
- `HttpMessageNotReadableException`: risposta 400 con codice `INVALID_REQUEST_BODY` e messaggio generico `Request body is missing, malformed, or contains invalid values`. Copre corpo richiesto assente, JSON malformato e valori non deserializzabili, come enum inesistenti o date non interpretabili.
- Gli handler sono già registrati tramite `@RestControllerAdvice` e `@ExceptionHandler`: non devono essere chiamati dal controller o dal service.
- Lettura JSON e validazione `@Valid` avvengono prima dell'esecuzione del metodo del controller. Gli handler uniformano la risposta; non attivano né ripetono la validazione.

### Ordine delle operazioni nell'aggiornamento dettagli

- Sistemato: tutti i controlli applicativi precedono il primo setter.
- Il metodo recupera la task, valida il titolo, calcola `shouldUpdateDescription` e controlla la lunghezza della descrizione quando necessario; poi valida la scadenza.
- Il setter della data è ancora nel ramo della data valida: è corretto perché a quel punto tutti i controlli sono superati. Seguono il setter del titolo e quello della descrizione soltanto se `shouldUpdateDescription` è vero, infine `save`.
- Chiarita la differenza fra modifica dell'oggetto in memoria e persistenza: validare prima evita modifiche parziali dell'oggetto in caso di dati non validi. In JPA un'entity gestita può essere persistita tramite dirty checking; l'assenza di una chiamata esplicita a `save` non è una garanzia generale di mancato aggiornamento.

## Database e verifiche effettuate

- Database PostgreSQL; configurazione con `spring.jpa.hibernate.ddl-auto=validate`.
- Non sono stati trovati script SQL o migrazioni che documentino lo schema nel progetto durante la revisione.
- I vincoli effettivi della colonna description devono ancora essere verificati tramite schema esportato oppure database accessibile.
- Non sono stati avviati il database né eseguite richieste HTTP in questa sessione.
- In `TaskServiceTest` ci sono tre test sulla creazione: richiesta valida, titolo nullo, scadenza precedente a sette giorni fa. I due test di errore sono stati aggiornati per aspettarsi `InvalidTaskDataException`.
- L'11 novembre i tre test sono stati eseguiti con successo: 3 test, 0 fallimenti, 0 errori. Questo controllo precede gli ultimi aggiornamenti all'handler, all'update dettagli e alla risposta 201; questi ultimi sono stati revisionati sul codice, senza nuovi test.
- Il primo tentativo Maven era bloccato da `JAVA_HOME` non valido. Per la verifica riuscita è stato usato temporaneamente il JDK `C:\Users\LENOVO\.jdks\ms-21.0.10`, con `JAVA_HOME` impostato soltanto nel processo del comando e `-Djava.version=21`. Comando: `.\mvnw.cmd -o -Djava.version=21 -Dtest=TaskServiceTest test`.
- Il `pom.xml` resta configurato per Java 27; non sono state modificate le impostazioni Java permanenti. La verifica su JDK 21 non è una verifica su JDK 27.

## Attività lasciate da parte per una futura ripresa

1. **Uniformare le validazioni**, rimandato per scelta dell'utente.
   - Confermare il limite reale/scelto della descrizione e applicarlo coerentemente a creazione e aggiornamento.
   - Per ora mantenere i controlli già presenti: il limite del titolo resta nei DTO (`@Size(max = 200)`), senza aggiungerlo al service; la descrizione ha il controllo dei 255 caratteri solo nell'update.
2. **Ampliare i test**, rimandato esplicitamente dall'utente.
   - Lettura, aggiornamento dettagli e stato, eliminazione.
   - ID inesistenti, dati non validi, casi limite della data, mantenimento dei campi quando arriva null.
   - Test del controller o verifiche HTTP per 400/404, validazione, JSON non valido e creazione con 201, corpo e Location.
3. **Versionare lo schema del database** tramite migrazioni, per renderlo riproducibile e documentarne i vincoli; non ancora implementato.

La gestione degli errori principali, l'ordine delle validazioni nell'update e la creazione con 201 e Location sono implementati: non riproporli come lavoro ancora da svolgere.

Evoluzioni successive facoltative: DTO di risposta, filtri per stato e scadenza, ordinamento, paginazione. Non sono requisiti mancanti per il CRUD di base.
