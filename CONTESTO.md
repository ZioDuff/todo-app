# Contesto del progetto TodoApp

Ultimo aggiornamento: 10 novembre 2026.

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
- `RestExceptionHandler`: attualmente gestisce `TaskNotFoundException` con risposta 404 e `ErrorResponse`.
- DTO di richiesta separati: `CreateTaskRequest`, `UpdateTaskRequest`, `UpdateStatusRequest`.
- Le risposte delle operazioni che restituiscono task usano ancora direttamente l'entity.

## Endpoint implementati

| Metodo | Percorso | Comportamento attuale |
| --- | --- | --- |
| POST | `/api/tasks` | Crea una task con stato iniziale TODO; restituisce la task, attualmente con 200 |
| GET | `/api/tasks` | Restituisce tutte le task |
| GET | `/api/tasks/{id}` | Restituisce una task; 404 se assente |
| PATCH | `/api/tasks/{id}/details` | Aggiorna i dettagli, mantenendo separata la modifica dello stato |
| PATCH | `/api/tasks/{id}/status` | Aggiorna lo stato e restituisce la task con 200 |
| DELETE | `/api/tasks/{id}` | Elimina la task e restituisce 204 senza corpo; 404 se assente |

Il controller del delete usa `ResponseEntity<Void>`. Il controllo sull'esistenza è nel service, tramite `findTaskById`, prima della cancellazione.

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
- Una descrizione non blank viene assegnata se non supera 255 caratteri; altrimenti il service lancia `IllegalArgumentException`.
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
- Il service rifiuta uno stato nullo con `IllegalArgumentException`; il controller usa anche `@Valid` sul DTO.
- Il service restituisce il risultato del salvataggio e il controller risponde con la task aggiornata.

### PUT e PATCH

- È stato scelto PATCH per i dettagli perché alcuni valori nulli significano mantenere il dato esistente.
- PUT rappresenta la sostituzione completa della risorsa esposta dall'endpoint, non necessariamente di tutta l'entity nel database.
- PATCH può modificare anche tutti i campi in una singola richiesta: non dipende dal numero di campi cambiati.

## Database e verifiche effettuate

- Database PostgreSQL; configurazione con `spring.jpa.hibernate.ddl-auto=validate`.
- Non sono stati trovati script SQL o migrazioni che documentino lo schema nel progetto durante la revisione.
- I vincoli effettivi della colonna description devono ancora essere verificati tramite schema esportato oppure database accessibile.
- Le ultime revisioni sono state effettuate leggendo i file, senza avviare il database né eseguire richieste HTTP.
- In `TaskServiceTest` risultano tre test sulla creazione: richiesta valida, titolo nullo, scadenza precedente a sette giorni fa. Non sono stati eseguiti in questa revisione.

## Prossimi passi, in ordine suggerito

1. **Gestione degli errori**, prossimo argomento concordato.
   - Gestire gli errori applicativi oggi espressi come `IllegalArgumentException`, evitando che diventino risposte 500 anziché 400.
   - Uniformare le risposte per errori di validazione dei DTO e JSON non leggibile, compresi nomi di stato inesistenti.
   - Attualmente soltanto il caso task inesistente ha un handler dedicato.
2. **Uniformare le validazioni**.
   - Confermare il limite reale/scelto della descrizione e applicarlo coerentemente a creazione e aggiornamento.
   - Nell'update completare le validazioni prima di eseguire i setter: attualmente il titolo viene modificato prima di validare descrizione e scadenza.
3. **Ampliare i test**.
   - Lettura, aggiornamento dettagli e stato, eliminazione.
   - ID inesistenti, dati non validi, casi limite della data, mantenimento dei campi quando arriva null.
   - Test del controller per verificare status HTTP e validazione delle richieste.
4. **Creazione con 201 Created**, eventualmente con header Location della nuova task.
5. **Versionare lo schema del database** tramite migrazioni, per renderlo riproducibile e documentarne i vincoli.

Evoluzioni successive facoltative: DTO di risposta, filtri per stato e scadenza, ordinamento, paginazione. Non sono requisiti mancanti per il CRUD di base.
