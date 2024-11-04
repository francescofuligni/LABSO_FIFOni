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

    public void addIfAbsent(String topicName) {
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public boolean contains(String topicName) {
        return this.topics.containsKey(topicName);
    }

    public String show() {
        String print = "";
        if(!topics.isEmpty()) {
            for(String t : this.topics.keySet()) {
                print += "\n  - " + t;
            }
            return "Tutti i topic:" + print;
        }
        return "Nessun topic creato.";
    }

    public boolean deleteMessage(String topicName, String messageID) {
        return this.topics.get(topicName).deleteMessage(messageID);
    }

    // TODO Metodo remove(topic) non richiesto -> complesso da gestire (vogliamo farlo lo stesso?)
}