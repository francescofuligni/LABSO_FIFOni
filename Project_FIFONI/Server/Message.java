import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

    /*
     * Classe che rappresenta un messaggio:
     * - Ogni messaggio è un'entità autonoma con un identificativo univoco, 
     *   un contenuto testuale e un timestamp.
     * - Utilizzata per memorizzare e gestire informazioni in un sistema di comunicazione,
     *   come topic o sistemi di messaggistica.
     * - Fornisce metodi per ottenere dettagli del messaggio e per formattare
     *   le informazioni in modo leggibile.
     */
    
public class Message{

     /*
     * Rappresentazione di un messaggio:
     * - Ogni messaggio ha un identificativo univoco nel contesto del topic.
     * - Contiene il testo del messaggio.
     * - Registra la data e l'ora in cui è stato creato.
     */

    private String id;
    private String text;
    private LocalDateTime date;

<<<<<<< Updated upstream
    public Message(String text){
        this.id = "msg_" + System.currentTimeMillis();
=======
     /*
     * Message:
     * Inizializza un messaggio assegnandogli:
     * - Un l'ID basato su un contatore incrementale del topic.
     * - Il testo fornito dall'utente.
     * - La data e ora corrente al momento della creazione.
     */

    public Message(String text, int id){
        this.id = "msg_" + id;  
        // this.id = "msg_" + System.currentTimeMillis(); // Univoco globalmente
>>>>>>> Stashed changes
        this.text = text;
        this.date = LocalDateTime.now();
    }

    //Restituisce l'ID del messaggio.
    public String getID() {
        return this.id; 
    }

    // Formatta correttamente la data per la stampa
    private String printDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return date.format(formatter);
    }
    /* toString:
     * Sovrascrive il metodo `toString` per una rappresentazione leggibile:
     * Mostra l'ID, il testo e la data del messaggio in un formato chiaro.
     */
    @Override
    public String toString() {
        return  "ID: " + this.id +
                "\n    Testo: '" + this.text +
                "'\n    Data: " + printDate();
    }
}