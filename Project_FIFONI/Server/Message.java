import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message{

    /*
     * Rappresentazione di un messaggio
     * Il messaggio ha un id, il suo testo e la data e ora ricezione
     */

    private String id;
    private String text;
    private LocalDateTime date;

    public Message(String text){
        this.id = "msg_" + System.currentTimeMillis();
        this.text = text;
        this.date = LocalDateTime.now();
    }

    public String getID() {
        return this.id;
    }

    // Formatta correttamente la data per la stampa
    private String printDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return date.format(formatter);
    }

    @Override
    public String toString() {
        return  "ID: " + this.id +
                "\n    Testo: '" + this.text +
                "'\n    Data: " + printDate();
    }
}