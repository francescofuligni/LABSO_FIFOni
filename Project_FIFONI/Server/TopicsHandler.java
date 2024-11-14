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


    private synchronized void startRead() throws InterruptedException {
        while(isAdding)
            wait();
        readCount++;
    }

    private synchronized void endRead() {
        readCount--;
        if(readCount == 0)
            notifyAll();
    }

    private synchronized void startAdd() throws InterruptedException {
        while(readCount > 0 || isAdding)
            wait();
        isAdding = true;
    }

    private synchronized void endAdd() {
        isAdding = false;
        notifyAll();
    }
    

    
    public Topic get(String topicName) {
        return this.topics.get(topicName);
    }

    public void addIfAbsent(String topicName) throws InterruptedException {
        startAdd();
        topics.putIfAbsent(topicName, new Topic(topicName));
        endAdd();
    }

    public boolean contains(String topicName) throws InterruptedException {
        startRead();
        boolean flag = this.topics.containsKey(topicName);
        endRead();
        return flag;
    }

    public String show() throws InterruptedException {
        startRead();

        String print = "";
        if(!topics.isEmpty()) {
            for(String t : this.topics.keySet()) {
                print += "\n  - " + t;
            }
            endRead();
            return "Tutti i topic:" + print;
        }
        endRead();
        return "Nessun topic creato.";
    }
}