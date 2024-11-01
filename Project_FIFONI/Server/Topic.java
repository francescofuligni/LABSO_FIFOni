import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Topic {

    /*
     * Rappresenta un topic.
     * Il topic ha un nome e una lista di messaggi associata.
     */

    // <Client ID, LinkedList di messaggi> per tenere traccia di quali messaggi sono di qual client e poi quando occorre stamparli tutti basta ordinarli per Datetime
    private Map<String, List<Message>> messages;
    private String name;
    private Set<ClientHandler> subscribers;

    public Topic(String name) {
        this.name = name;
        this.messages = new HashMap<>();
        this.subscribers = new HashSet<>();
    }

    // metodo che permette a un publisher di aggiungere un messaggio al topic
    public void sendMessage(String clientID, Message message) {
        if(this.messages.containsKey(clientID)) {
            this.messages.get(clientID).add(message);
        }
        else{
            this.messages.put(clientID, new LinkedList<>());
            this.messages.get(clientID).add(message);
        } 
    }

    public void subscribe(ClientHandler c) {
        this.subscribers.add(c);
    }

    // da invocare sul topic quando un client subscriber viene terminato
    public void unscribe(ClientHandler c) {
        this.subscribers.remove(c);
    }

    public Set<ClientHandler> getSubscribers() {
        return subscribers;
    }

    // metodo che restituisce TUTTI i messaggi di un client
    public List<Message> getClientMessages(String clientID) {
        return this.messages.get(clientID);
    }

    // metodo che restituisce TUTTI i messaggi sul  topic NON in ORDINE 
    public List<Message> getAllMessages() {
        LinkedList<Message> allMessages = new LinkedList<>();
        for(String key : this.messages.keySet()) {
            allMessages.addAll(this.messages.get(key));
        }

        return allMessages;
    }

    public String printClientMessages(String clientID) {
        String formattedMessages = "";
        for(Message m : this.getClientMessages(clientID)) {
            formattedMessages += m.toString();
        }
        return formattedMessages;
    }

    public String printAllMessages() {
        String formattedMessages = "";
        for(Message m : this.getAllMessages()) {
            formattedMessages += m.toString();
        }
        return formattedMessages;
    }

    private Message findMessage(String messageID) {
        for(Message m : this.getAllMessages()) {
            if(m.getID().equals(messageID))
                return m;
        }
        return null;
    }

    public boolean deleteMessage(String messageID) {
        Message messageToRemove = findMessage(messageID);
        
        if (messageToRemove != null) {
            for (List<Message> clientMessages : messages.values()) {
                if (clientMessages.remove(messageToRemove)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }
}