import java.time.LocalDateTime;

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

    /* MESSO NEL COSTRUTTORE !!
    // Invocato dal server quando il messaggio viene ricevuto (?)
    public void setReceivingTime(){
        this.date = LocalDateTime.now();
    }
    */

    public String getID() {
        return this.id;
    }

    // Formatta correttamente la data per la stampa
    private String printDate() {
        return date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear() + " " + date.getHour() + ":" + date.getMinute() + ":" + date.getSecond();
    }

    @Override
    public String toString() {
        return  "\n  - ID: " + this.id +
                "\n    Testo: '" + this.text +
                "'\n    Data: " + printDate();
    }
}