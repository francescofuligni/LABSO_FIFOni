import java.util.HashMap;

public class TopicsHandler {
    private HashMap<String,Topic> topics;

    public TopicsHandler() {
        this.topics =  new HashMap<>();
    }

    public Topic get(String topicName) {
        return this.topics.get(topicName);
    }

    public synchronized void addIfAbsent(String topicName) {
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
}