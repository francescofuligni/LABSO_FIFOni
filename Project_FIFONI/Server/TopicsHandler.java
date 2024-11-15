import java.util.HashMap;

public class TopicsHandler {

    /*
        Gestisce un insieme di Topic rappresentati da un Hashmap dove la chiave è il nome del topic
        e il valore è l'oggetto del Topic associato



    */
    private HashMap<String,Topic> topics;
    private int readCount; // Contatore di lettori attivi per gestire la concorrenza

    public TopicsHandler() {
        this.topics =  new HashMap<>();
        this.readCount = 0;
    }

    // Indica l'inizio di una lettura per gestire la concorrenza tra la creazione di un nuovo Topic e show
    private synchronized void startRead() throws InterruptedException {
        readCount++;
    }

     // Indica la fine di una lettura per gestire la concorrenza tra la creazione di un nuovo Topic e show
    private synchronized void endRead() {
        readCount--;
        if(readCount == 0) // Quando non ci sono operazioni di lettura notifica eventuali thread in attesa
            notifyAll();
    }
    

    public Topic get(String topicName) {
        return this.topics.get(topicName);
    }

    // Aggiunge un nuovo topic se non esiste già, blocca i writer concorrenti e gestisce la concorrenza con show
    public synchronized void putIfAbsent(String topicName) throws InterruptedException {
        while(readCount > 0)
            wait();
        
        topics.putIfAbsent(topicName, new Topic(topicName));
        notifyAll();
    }

    // Verifica se un Topic esiste nella mappa ed utilizza il contatore readCount per gestire la concorrenza
    public boolean contains(String topicName) throws InterruptedException {
        startRead();
        boolean flag = this.topics.containsKey(topicName);
        endRead();
        return flag;
    }

    /* Restituisce l'elenco di tutti i Topic, supporta la lettura concorrente e 
    gestisce la concorrenza con la creazione di nuovi topic
    */
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