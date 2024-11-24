import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
    /*
     * Classe `Message`:
     * - Rappresenta un messaggio scambiato tra client e server.
     * - Ogni messaggio ha un ID univoco, un testo e un timestamp che ne indica la data e l'ora di ricezione.
     * - Il formato dell'ID è basato su un contatore, garantendo che ogni messaggio sia unico.
     * - La data di ricezione viene automaticamente registrata al momento della creazione del messaggio.
     * - (NB. sulla socket viene inviato solo il testo del messaggio, l'oggetto message viene creato solo quando viene invocato il metodo send)
     * - La classe fornisce metodi per ottenere l'ID del messaggio e per formattare la data di ricezione in modo leggibile.
     * 
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