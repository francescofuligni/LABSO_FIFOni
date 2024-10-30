import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.List; 
import java.util.ArrayList; 

public class ClientHandler implements Runnable {

    private enum Role {
        PUBLISHER, SUBSCRIBER, UNDEFINED
    }

    private Socket socket;
    private String currentTopic;
    private String clientId;
    private Role role = Role.UNDEFINED;     // Ruolo inizialmente non specificato

    // Mappa che associa topic a una lista di messaggi
    //private static HashMap<String, HashMap<String, List<Message>>> topics = new HashMap<>();
    //private static HashSet<String> availableTopics = new HashSet<>(); // Contiene i nomi dei topic disponibili


    // Associo ogni topic al suo nome
    private static HashMap<String,Topic> topics = new HashMap<>();




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

            System.out.println("Thread " + Thread.currentThread() + " listening...");

            boolean closed = false;

            while (!closed) {
                String request = fromClient.nextLine().trim();

                if (!Thread.interrupted()) {
                    System.out.println("Richiesta: " + request);

                    String[] parts = request.split(" ");
                    switch (parts[0]) {

                        case "quit":
                            closed = true;
                            toClient.println("Connessione chiusa.");
                            break;
                        // Visualizza tutti i topic DA CHIEDERE !!!! 
                        // SE SI VUOLE FARLI CREARE SOLO DA PUBLISHER O DA TUTTI I CLIENT
                        case "show":
                            String show="Topics: ";
                             for(String t: topics.keySet())
                                show +=( "\n  - "+ t);
                                
                             toClient.println(show);
                            break;

                        case "publish":
                            if (role == Role.UNDEFINED) {
                                if (parts.length > 1) {
                                    currentTopic = parts[1];
                                    role = Role.PUBLISHER;
                                    topics.putIfAbsent(currentTopic, new Topic(currentTopic));
                                    toClient.println("Registrato come publisher su: " + currentTopic);
                                } else {
                                    toClient.println("Errore: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("Errore: Sei già registrato come " + role + ".");
                            }
                            break;

                        case "subscribe":
                            if (role == Role.UNDEFINED) {
                                if (parts.length > 1) {
                                    currentTopic = parts[1];
                                    role = Role.SUBSCRIBER;
                                    topics.putIfAbsent(currentTopic, new Topic(currentTopic));
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
                                String messageContent = request.substring(5).trim();  // Rimuove 'send ' dal messaggio

                                Message message = new Message( "CODICE DA INSERIRE", messageContent);

                                ///////////
                                message.setReceivingTime(); // Imposta la data di ricezione
                                ////////////

                                topics.get(currentTopic).publishMessage(clientId, message);
                                
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

                                String clientMessages = topics.get(currentTopic).formattedClientMessages(clientId);
                                
                                if (clientMessages != null && !clientMessages.isEmpty()) {
                                    toClient.println("Messaggi inviati dal client " + clientId + " sul topic " + currentTopic + ":\n" + clientMessages);
                                    
                                } else {
                                    toClient.println("Nessun messaggio inviato dal client " + clientId + " su questo topic.");
                                }
                            } else {
                                toClient.println("Errore: non sei un publisher o nessun topic selezionato.");
                            }
                            break;
                        

                            case "listall":
                            if (currentTopic != null) {
                            String allMessages = topics.get(currentTopic).formattedAllMessages();
                            if (allMessages == null || allMessages.isEmpty()) {
                                toClient.println("Nessun messaggio presente sul topic " + currentTopic + ".");
                                } else {
                                    toClient.println("Tutti i messaggi sul topic " + currentTopic + ":\n" + allMessages);
                                    
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
