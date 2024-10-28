import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.List; 
import java.util.ArrayList; 
import java.util.Map; 

public class ClientHandler implements Runnable {

    private enum Role {
        PUBLISHER, SUBSCRIBER, UNSPECIFIED
    }

    private Socket socket;
    private String currentTopic;
    private String clientId;
    private Role role = Role.UNSPECIFIED; // Ruolo inizialmente non specificato

    // Mappa che associa topic a una lista di messaggi
    private static HashMap<String, HashMap<String, List<Message>>> topics = new HashMap<>();

    private static HashSet<String> availableTopics = new HashSet<>(); // Contiene i nomi dei topic disponibili

    // Lista di client handler attivi
    private static final HashSet<ClientHandler> clients = new HashSet<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        synchronized (clients) {
            clients.add(this); // Aggiungi il client handler alla lista
        }
    }

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(socket.getInputStream());
            PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " in ascolto...");

            boolean closed = false;
            while (!closed) {
                String request = fromClient.nextLine();
                if (!Thread.interrupted()) {
                    System.out.println("Richiesta: " + request);
                    String[] parts = request.split(" ");
                    switch (parts[0]) {
                        case "quit":
                            closed = true;
                            toClient.println("Connessione chiusa.");
                            break;

                        case "show":
                            toClient.println("Topics:");
                            for (String topic : availableTopics) {
                                toClient.println("- " + topic);
                            }
                            break;

                        case "publish":
                            if (role == Role.UNSPECIFIED) {
                                if (parts.length > 1) {
                                    currentTopic = parts[1];
                                    role = Role.PUBLISHER;
                                    availableTopics.add(currentTopic);
                                    topics.putIfAbsent(currentTopic, new HashMap<>());
                                    toClient.println("Registrato come publisher su: " + currentTopic);
                                } else {
                                    toClient.println("Errore: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("Errore: Sei già registrato come " + role + ".");
                            }
                            break;

                        case "subscribe":
                            if (role == Role.UNSPECIFIED) {
                                if (parts.length > 1) {
                                    currentTopic = parts[1];
                                    role = Role.SUBSCRIBER;
                                    availableTopics.add(currentTopic);
                                    topics.putIfAbsent(currentTopic, new HashMap<>());
                                    toClient.println("Iscritto al topic: " + currentTopic);
                                } else {
                                    toClient.println("Errore: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("Errore: Sei già registrato come " + role + ".");
                            }
                            break;

                        case "send":
                            if (role == Role.PUBLISHER && parts.length > 1) {
                                String messageContent = request.substring(5);  // Rimuove 'send ' dal messaggio
                                Message message = new Message(clientId, messageContent);
                                message.setReceivingTime(); // Imposta la data di ricezione
                                topics.get(currentTopic).computeIfAbsent(clientId, k -> new ArrayList<>()).add(message);

                                
                                // Notifica i subscriber
                                notifySubscribers(currentTopic, message);
                                
                                toClient.println("Messaggio inviato sul topic: " + currentTopic);
                            } else if (role != Role.PUBLISHER) {
                                toClient.println("Errore: non sei un publisher.");
                            } else {
                                toClient.println("Errore: nessun messaggio specificato.");
                            }
                            break;

                            case "list":
                            if (role == Role.PUBLISHER && currentTopic != null) {
                                List<Message> messages = topics.get(currentTopic).get(clientId);
                                if (messages != null && !messages.isEmpty()) {
                                    toClient.println("Messaggi inviati dal client " + clientId + " sul topic " + currentTopic + ":");
                                    for (Message msg : messages) {
                                        toClient.println(msg.content + " (Data: " + msg.date + ")");
                                    }
                                } else {
                                    toClient.println("Nessun messaggio inviato dal client " + clientId + " su questo topic.");
                                }
                            } else {
                                toClient.println("Errore: non sei un publisher o nessun topic selezionato.");
                            }
                            break;
                        

                            case "listall":
                            if (currentTopic != null) {
                            HashMap<String, List<Message>> allMessages = topics.get(currentTopic);
                            if (allMessages == null || allMessages.isEmpty()) {
                                toClient.println("Nessun messaggio presente sul topic " + currentTopic + ".");
                                } else {
                                    toClient.println("Tutti i messaggi sul topic " + currentTopic + ":");
                                    allMessages.forEach((senderId, messages) -> messages.forEach(msg ->
                                        toClient.println(senderId + ": " + msg.content + " (Data: " + msg.date + ")")
                                    ));
                                }
                            } else {
                                toClient.println("Errore: nessun topic selezionato.");
                            }
                            break;

                        default:
                            toClient.println("Comando sconosciuto: " + parts[0]);
                    }
                } else {
                    toClient.println("quit");
                    break;
                }
            }

            fromClient.close();
            socket.close();
            System.out.println("Connessione chiusa");
        } catch (IOException e) {
            System.err.println("ClientHandler: IOException caught: " + e);
            e.printStackTrace();
        } finally {
            synchronized (clients) {
                clients.remove(this); // Rimuovi il client handler dalla lista
            }
        }
    }

    private void notifySubscribers(String topic, Message message) {
        synchronized (clients) {
            for (ClientHandler handler : clients) {
                if (handler.role == Role.SUBSCRIBER && handler.currentTopic.equals(topic)) {
                    try {
                        PrintWriter toSubscriber = new PrintWriter(handler.socket.getOutputStream(), true);
                        toSubscriber.println("Nuovo messaggio su " + topic + ": " + message.content + " (Inviato da: " + message.id + ", Data: " + message.date + ")");
                    } catch (IOException e) {
                        System.err.println("Errore nell'invio del messaggio al subscriber: " + e);
                    }
                }
            }
        }
    }
}
