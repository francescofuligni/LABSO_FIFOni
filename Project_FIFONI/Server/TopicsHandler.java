import java.util.HashMap;

public class TopicsHandler {
    private HashMap<String,Topic> topics;

    private boolean isAdding;
    private int readCount;

    public TopicsHandler() {
        this.topics =  new HashMap<>();
        this.isAdding = false;
        this.readCount = 0;
    }


    private synchronized void startReading() throws InterruptedException {
        while(isAdding)
            wait();
        readCount++;
    }

    private synchronized void endReading() {
        notifyAll();
        readCount--;
    }

    
    public Topic get(String topicName) {
        return this.topics.get(topicName);
    }

    public synchronized void addIfAbsent(String topicName) throws InterruptedException {
        while(readCount > 0 || isAdding)
            wait();
        isAdding = true;

        topics.putIfAbsent(topicName, new Topic(topicName));
        
        isAdding = false;
        notifyAll();
    }

    public boolean contains(String topicName) throws InterruptedException {
        startReading();

        boolean flag = this.topics.containsKey(topicName);

        endReading();
        return flag;
    }

    public String show() throws InterruptedException {
        startReading();

        String print = "";
        if(!topics.isEmpty()) {
            for(String t : this.topics.keySet()) {
                print += "\n  - " + t;
            }
            endReading();
            return "Tutti i topic:" + print;
        }
        endReading();
        return "Nessun topic creato.";
    }
}