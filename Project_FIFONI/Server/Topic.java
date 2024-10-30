import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
    public HashMap<String, List<Message>> messages; // <Thread ID, LinkedList di messaggi> per tenere traccia di quali messaggi sono di qual client e poi quando occorre stamparli tutti basta ordinarli per Datetime. public ArrayList<Message> messages = new ArrayList<Message>();

    public Topic(String name) {
        this.name = name;
        this.messages = new HashMap<>();
    }



    // metodo che permette a un publisher di aggiungere un messaggio al topic
    public void publishMessage( String ThreadID, Message message) {

        if(this.messages.containsKey(ThreadID)) {
            this.messages.get(ThreadID).add(message);
        }
        else{
            this.messages.put(ThreadID, new LinkedList<>());
            this.messages.get(ThreadID).add(message);
        }
        
        
    }


      // metodo che restituisce TUTTI i messaggi di un client
      public List<Message> getClientMessages(String ThreadID) {
        return this.messages.get(ThreadID);
    }

    // metodo che restituisce TUTTI i messaggi sul  topic NON in ORDINE 
    public List<Message> getAllMessages() {
        LinkedList<Message> allMessages = new LinkedList<>();
        for(String key : this.messages.keySet()) {
            allMessages.addAll(this.messages.get(key));
        }

        return allMessages;
    }

    public String formattedClientMessages(String ThreadID) {
        String formattedMessages = "";
        for(Message message : this.messages.get(ThreadID)) {
            formattedMessages += message.toString();
        }
        return formattedMessages;
    }

    public String formattedAllMessages() {
        String formattedMessages = "";
        for(Message message : this.getAllMessages()) {
            formattedMessages += message.toString();
        }
        return formattedMessages;
    }

    public String toString() {
        return this.name;
    }
}