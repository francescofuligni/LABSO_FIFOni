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

    // <Client ID, LinkedList di messaggi> traccia i messaggi inviati da ciascun client
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

    
    // Restituisce TUTTI i messaggi di un client
    private List<Message> getClientMessages(String clientID) {
        return this.messages.get(clientID);
    }

    // Restituisce TUTTI i messaggi sul topic NON in ORDINE 
    private List<Message> getAllMessages() {
        LinkedList<Message> allMessages = new LinkedList<>();
    // Itera sulla mappa e aggiunge tutti i messaggi sulla lista aggregata
        for(String key : this.messages.keySet()) {
            allMessages.addAll(this.messages.get(key));
        }
        return allMessages;
    }
    // Trova un messaggio tramite il suo ID
    private Message findMessage(String messageID) {
        for(Message m : this.getAllMessages()) {
            if(m.getID().equals(messageID))
                return m;
        }
        return null;
    }
    /* 
    Notifica i Subscribers di un nuovo messaggio pubblicato sul Topic a cui sono iscritti
    Invia il messaggio ai Subscribers tramite Socket 
    */
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
    // Elimina un messaggio dal Topic usando il suo ID, trova il messaggio e lo rimuove dalla lista associata al Client che lo ha inviato
    private boolean deleteMessage(String messageID) {
        Message messageToRemove = findMessage(messageID);
        if (messageToRemove != null) {
            for (List<Message> clientMessages : messages.values()) {
                if (clientMessages.remove(messageToRemove)) {
                    return true;
                }
            }
        }
        return false; // Messaggio non trovato
    }

    // Incrementa il contatore quando viene usato il comando List o Listall
    private synchronized void startList() throws InterruptedException {
        listCount++;
    }
    /* Decrementa il contatore quando finiscono le operazioni di List o Listall
       Quando non c i sono più operazioni in corso notifica i thread in attesa
       */
    private synchronized void endList() {
        listCount--;
        if(listCount == 0)
            notifyAll();
    }

    
    /*Permette a un publisher di aggiungere un messaggio con il proprio ID al topic 
      Aggiunge il messaggio alla lista del Client o ne crea una nuova se il client non è presente
      */
    public synchronized void send(String clientID, String text) throws InterruptedException {
        while(listCount > 0) // Se ci sono operazioni List o Listall in corso aspetta 
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
        // Notifica i Subscribers al Topic del nuovo messaggio
        notifySubscribers(m);
        // Notifica i thread in attesa
        notifyAll();
    }

    // Registra un subscriber al Topic
    public synchronized void subscribe(ClientHandler c) {
        this.subscribers.add(c);
    }

    // da invocare sul topic quando un client subscriber viene terminato
    public synchronized void unscribe(ClientHandler c) {
        this.subscribers.remove(c);
    }

    // Restituisce i subscribers registrati al Topic
    public Set<ClientHandler> getSubscribers() {
        return subscribers;
    }

    // Restituisce i messaggi inviati da un Client sul Topic
    public String list(String clientID) throws InterruptedException {
        startList(); // Segnala l'inizio dell'operazione List per gestire la concorrenza con send

        String print = "";
        List<Message> clientMessages = this.getClientMessages(clientID);
        if(clientMessages != null) {
            for(Message m : clientMessages) {
                print += "\n  - " + m.toString();
            }
            endList(); // Segnala la fine dell'operazione List per gestire la concorrenza con send
            return "Messaggi inviati dal client '" +  clientID + "' sul topic '" +  this.name + "':" + print;
        }
        endList(); // Segnala la fine dell'operazione List anche in caso di messaggi nulli per gestire la concorrenza con send
        return "Nessun messaggio inviato dal client '" + clientID + "' sul topic '" + this.name + "'.";
    }

    // Restituisce tutti i messaggi inviati su un Topic
    public String listAll() throws InterruptedException {
        startList(); // Segnala l'inizio dell'operazione List per gestire la concorrenza con send

        String print = "";
        for(Message m : this.getAllMessages()) {
            print += "\n  - " + m.toString();
        }
        if(print != "") {
            endList(); // Segnala la fine dell'operazione List per gestire la concorrenza con send
            return "Tutti i messaggi sul topic '" + this.name + "':" + print;
        }
        endList(); // Segnala la fine dell'operazione List anche in caso di messaggi nulli per gestire la concorrenza con send
        return "Nessun messaggio sul topic '" + this.name + "'.";
    }


    /* Sessione interattiva avviata dal server
       Rimane attiva fino a quando non viene eseguito il comando :end,
       permette di vedere tutti i messaggi ed eventualmente eliminarli 
    */
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