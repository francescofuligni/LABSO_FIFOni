import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
    private int idCount;
    private int listCount;

    public Topic(String name) {
        this.name = name;
        this.messages = new HashMap<>();
        this.subscribers = new HashSet<>();
        this.idCount = 0;
        this.listCount = 0;
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

    private boolean deleteMessage(String messageID) {
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


    private synchronized void startList() throws InterruptedException {
        listCount++;
    }

    private synchronized void endList() {
        listCount--;
        if(listCount == 0)
            notifyAll();
    }

    
    // metodo che permette a un publisher di aggiungere un messaggio al topic
    public synchronized void send(String clientID, String text) throws InterruptedException {
        while(listCount > 0)
            wait();

        idCount++;
        Message m = new Message(text, idCount);
        if(this.messages.containsKey(clientID)) {
            this.messages.get(clientID).add(m);
        }
        else{
            this.messages.put(clientID, new LinkedList<>());
            this.messages.get(clientID).add(m);
        }

        notifySubscribers(m);
        notifyAll();
    }

    public synchronized void subscribe(ClientHandler c) {
        this.subscribers.add(c);
    }

    // da invocare sul topic quando un client subscriber viene terminato
    public synchronized void unscribe(ClientHandler c) {
        this.subscribers.remove(c);
    }

    public Set<ClientHandler> getSubscribers() {
        return subscribers;
    }

    public String list(String clientID) throws InterruptedException {
        startList();

        String print = "";
        List<Message> clientMessages = this.getClientMessages(clientID);
        if(clientMessages != null) {
            for(Message m : clientMessages) {
                print += "\n  - " + m.toString();
            }
            endList();
            return "Messaggi inviati dal client '" +  clientID + "' sul topic '" +  this.name + "':" + print;
        }
        endList();
        return "Nessun messaggio inviato dal client '" + clientID + "' sul topic '" + this.name + "'.";
    }

    public String listAll() throws InterruptedException {
        startList();

        String print = "";
        for(Message m : this.getAllMessages()) {
            print += "\n  - " + m.toString();
        }
        if(print != "") {
            endList();
            return "Tutti i messaggi sul topic '" + this.name + "':" + print;
        }
        endList();
        return "Nessun messaggio sul topic '" + this.name + "'.";
    }


    // Sessione interattiva avviata dal server
    public synchronized void interactiveSession(Scanner input) throws InterruptedException {
        System.out.println("\n* SESSIONE INTERATTIVA AVVIATA *\nComandi sessione interattiva:\n  > :listall\n  > :delete <id>\n  > :end");
        
        boolean closed = false;
        while (!closed) {
            String command = input.nextLine().trim();
            String[] parts = command.split(" ", 2);

            switch (parts[0]) {

                // Elenca tutti i messaggi sul topic
                case ":listall":
                    String print = "";
                    for(Message m : this.getAllMessages())
                        print += "\n  - " + m.toString();
                    if(print != "")
                        System.out.println("Tutti i messaggi sul topic '" + this.name + "':" + print);
                    else
                        System.out.println("Nessun messaggio sul topic '" + this.name + "'.");
                    break;
                
                // Elimina un messaggio su un topic
                case ":delete":
                    if(parts.length>1) {
                        String messageID = parts[1].trim();
                        if(this.deleteMessage(messageID)) {
                            System.out.println("Messaggio '" + messageID + "' eliminato.");
                        } else {
                            System.out.println("ERRORE: messageID '" +  messageID + "' inesistente.");
                        }
                    } else {
                        System.out.println("ERRORE: nessun messageID selezionato.");
                    }
                    break;
                
                // Termina la sessione interattiva
                case ":end":
                    closed = true;
                    break;

                default:
                    System.out.println("Comando <" + command + "> sconosciuto."); 
                    break;
            }
        }
        System.out.println("* SESSIONE INTERATTIVA TERMINATA *\n");
    }

    
    @Override
    public String toString() {
        return this.name;
    }
}