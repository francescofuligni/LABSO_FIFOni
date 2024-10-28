import java.util.ArrayList;

public class Topic {

    /*
     * Rappresenta un topic.
     * 
     * Il topic ha un nome e una lista di messaggi.
     * 
     * NOTA: la lista potrebbe essere realizzata con un hashmap di tipo <Thread ID, Arraylist di messaggi>
     * per tenere traccia di quali messaggi sono di quale client e poi quando occorre stamparli tutti basta
     * ordinarli per Datetime.
     */

    public String name;
    private ArrayList<Message> messages = new ArrayList<Message>();

    public Topic(String name) {
        this.name = name;
        this.messages = new ArrayList<Message>();
    }

    // metodo che permette a un publisher di aggiungere un messaggio al topic
    public void publishMessage(Message message) {
        this.messages.add(message);
    }

    // metodo che restituisce TUTTI i messaggi sul  topic
    public ArrayList<Message> getMessages() {
        return this.messages;
    }
}