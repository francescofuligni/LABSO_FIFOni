import java.time.LocalDateTime;

public class Message{

    /*
     * Rappresentazione di un messaggio.
     * 
     * Il messaggio ha un id, il suo testo e la data e ora ricezione
     */

    public String id;
    public String content;
    public LocalDateTime date;

    public Message(String id, String content){
        this.id = id;
        this.content = content;
        this.date = null;
    }

    //va fatto invocare al server quando il messaggio viene ricevuto.
    public void setReceivingTime(){
        this.date = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return  "ID:'" + id + "\n" +
                "Testo:' " + content + "\n" +
                "Data:" + date;
    }
}