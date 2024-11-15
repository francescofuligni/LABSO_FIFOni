import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 /*
     * Classe `Message`:
     * - Rappresenta un messaggio scambiato tra client e server.
     * - Ogni messaggio ha un ID univoco, un testo e un timestamp che ne indica la data e l'ora di ricezione.
     * - Il formato dell'ID è basato su un contatore o un timestamp, garantendo che ogni messaggio sia unico.
     * - La data di ricezione viene automaticamente registrata al momento della creazione del messaggio.
     * - La classe fornisce metodi per ottenere l'ID del messaggio e per formattare la data di ricezione in modo leggibile.
     * 
     * Caratteristiche principali:
     * - L'ID del messaggio può essere generato come un valore univoco, ad esempio tramite un contatore o un timestamp.
     * - La data e l'ora sono registrate automaticamente in formato preciso al momento della creazione.
     * - La classe offre un metodo di stampa per la visualizzazione delle informazioni del messaggio.
     */
public class Message{

    private String id;
    private String text;
    private LocalDateTime date;

    public Message(String text, int id){
        this.id = "msg_" + id;  // Univoco nel topic
        // this.id = "msg_" + System.currentTimeMillis(); // Univoco globalmente
        this.text = text;
        this.date = LocalDateTime.now();
    }

    public String getID() {
        return this.id;
    }

    // Formatta correttamente la data per la stampa
    private String printDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss.SSSSS");
        return date.format(formatter);
    }

    @Override
    public String toString() {
        return  "ID: " + this.id +
                "\n    Testo: '" + this.text +
                "'\n    Data: " + printDate();
    }
}