import java.io.IOException;
import java.io.PrintWriter;
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
    private int count;

    public Topic(String name) {
        this.name = name;
        this.messages = new HashMap<>();
        this.subscribers = new HashSet<>();
        this.count = 0;
    }

    
    // metodo che restituisce TUTTI i messaggi di un client
    private List<Message> getClientMessages(String clientID) {
        return this.messages.get(clientID);
    }

    // metodo che restituisce TUTTI i messaggi sul  topic NON in ORDINE 
    private List<Message> getAllMessages() {
        LinkedList<Message> allMessages = new LinkedList<>();
        for(String key : this.messages.keySet()) {
            allMessages.addAll(this.messages.get(key));
        }
        return allMessages;
    }

    private Message findMessage(String messageID) {
        for(Message m : this.getAllMessages()) {
            if(m.getID().equals(messageID))
                return m;
        }
        return null;
    }

    private void notifySubscribers(Message message) {
        for (ClientHandler c : this.subscribers) {
            try {
                PrintWriter toSubscriber = new PrintWriter(c.getSocket().getOutputStream(), true);
                toSubscriber.println("Nuovo messaggio sul topic '" + this.name + "':\n    " + message);
            } catch (IOException e) {
                System.err.println("TOPIC - Eccezione nell'invio del messaggio al subscriber: " + e);
            }
        }
    }
    
    
    // metodo che permette a un publisher di aggiungere un messaggio al topic
    public void sendMessage(String clientID, String text) {
        count++;
        Message m = new Message(text, count);
        if(this.messages.containsKey(clientID)) {
            this.messages.get(clientID).add(m);
        }
        else{
            this.messages.put(clientID, new LinkedList<>());
            this.messages.get(clientID).add(m);
        } 
        notifySubscribers(m);
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

    public String printClientMessages(String clientID) {
        String print = "";
        List<Message> clientMessages = this.getClientMessages(clientID);
        if(clientMessages != null) {
            for(Message m : clientMessages) {
                print += "\n  - " + m.toString();
            }
            return "Messaggi inviati dal client '" +  clientID + "' sul topic '" +  this.name + "':" + print;
        }
        return "Nessun messaggio inviato dal client '" + clientID + "' sul topic '" + this.name + "'.";
    }

    public String printAllMessages() {
        String print = "";
        for(Message m : this.getAllMessages()) {
            print += "\n  - " + m.toString();
        }
        if(print != "") {
            return "Tutti i messaggi sul topic '" + this.name + "':" + print;
        }
        return "Nessun messaggio sul topic '" + this.name + "'.";
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