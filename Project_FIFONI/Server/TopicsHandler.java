import java.util.HashMap;

public class TopicsHandler {
    private HashMap<String,Topic> topics;
    // private int count;       // Non necessario -> uso TIMESTAMP
    // TODO MECCANISMO PER GESTIRE ID MESSAGGI -> counter ?

    public TopicsHandler() {
        this.topics =  new HashMap<>();
        // this.count = 0;
    }

    public Topic get(String topicName) {
        return this.topics.get(topicName);
    }

    public boolean isEmpty() {
        return topics.isEmpty();
    }

    public void addIfAbsent(String topicName) {
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

    // Necessario? -> Può essere chiamato direttamente sul topic
    /*
    public void sendMessage(String topicName, String clientID, String text) {
        this.count++;
        Message message = new Message("msg-" + this.count, text);
        topics.get(topicName).sendMessage(clientID, message);
    }
    */

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
