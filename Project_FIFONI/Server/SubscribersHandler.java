import java.util.HashSet;
import java.util.Set;

// ALTERNATIVA A CUNCURRENTHASHMAP.NEWKEYSET()

public class SubscribersHandler {
    Set<ClientHandler> subscribers = new HashSet<>();
    int readCount = 0;

    public synchronized void subscribe(ClientHandler c) throws InterruptedException {
        while(readCount > 0)
            wait();

        subscribers.add(c);
        notifyAll();
    }

    public synchronized void unsubscribe(ClientHandler c) throws InterruptedException {
        while(readCount > 0)
            wait();

        subscribers.remove(c);
        notifyAll();
    }

    private synchronized void startRead() throws InterruptedException {
        readCount++;
    }

    private synchronized void endRead() {
        readCount--;
        if(readCount == 0)
            notifyAll();
    }

    // Oppure get che restituisce il set subscribers in notifySubscribers() in Topic

    public void notifySubscribers() throws InterruptedException {
        startRead();
        // Notifica i subscribers --> come in Topic, invocato in ClientHandler
        endRead();
    }
}
