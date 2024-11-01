import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class TopicsHandler {
    private HashMap<String,Topic> topics;
    // TODO MECCANISMO PER GESTIRE ID MESSAGGI -> counter ?

    public TopicsHandler() {
        this.topics =  new HashMap<>();
    }

    public Topic get(String topicName) {
        return this.topics.get(topicName);
    }

    public void add(String topicName) {
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public boolean contains(String topicName) {
        return this.topics.containsKey(topicName);
    }

    public String show() {
        String print = "Tutti i topic:";
        for(String t : this.topics.keySet()) {
            print += "\n  - " + t;
        }
        return print;
    }

    // Metodo remove(topic) non richiesto -> complesso da gestire

    public boolean removeMessage(String topicName, String messageID) {
        return this.topics.get(topicName).deleteMessage(messageID);
    }

    public void sendMessage(String topicName, String clientID, String text) {

        // Necessario per la gestione del MESSAGEID (va fatta qui, non in Topic)
        
        Message message = new Message("*MESSAGEID NON IMPLEMENTATO*", text);
        topics.get(topicName).sendMessage(clientID, message);
        notifySubscribers(topics.get(topicName), message);
    }

    private void notifySubscribers(Topic topic, Message message) {
        for (ClientHandler c : topic.getSubscribers()) {

            // controlli non necessari -> sappiamo che i subscribers nel topic sono corretti perché li aggiungiamo quando si iscrivono (TODO)
            
            try {
                PrintWriter toSubscriber = new PrintWriter(c.getSocket().getOutputStream(), true);
                toSubscriber.println("Nuovo messaggio sul topic '" + topic + "': " + message);
            } catch (IOException e) {
                System.err.println("CLIENTHANDLER - Eccezione nell'invio del messaggio al subscriber: " + e);
            }
        }
    }

    // Necessario? -> Può essere chiamato direttamente sul topic
    /*
    public String printClientMessages(String topicName, String clientID) {
        return this.topics.get(topicName).printClientMessages(clientID);
    }
    */

    // Necessario? -> Può essere chiamato direttamente sul topic
    /*
    public String printAllMessages(String topicName) {
        return this.topics.get(topicName).printAllMessages();
    }
    */
}
