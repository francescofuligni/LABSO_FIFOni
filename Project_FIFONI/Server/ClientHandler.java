import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class ClientHandler implements Runnable {

    // Classe per gestire il ruolo del client
    private enum Role {
        publisher, subscriber, undefined;
    }

    private Socket socket;
    private String topicName;
    private String clientID;
    private Role role = Role.undefined;     // Ruolo inizialmente non specificato

    // Mappa che associa topic a una lista di messaggi
    //private static HashMap<String, HashMap<String, List<Message>>> topics = new HashMap<>();
    //private static HashSet<String> availableTopics = new HashSet<>(); // Contiene i nomi dei topic disponibili

    // Associo ogni topic al suo nome
    //private static HashMap<String,Topic> topics = new HashMap<>();
    //private TopicsHandler topics = new TopicsHandler();

    // TODO ! SOLUZIONE TEMPORANEA (DA MODIFICARE) -> TopicsHandler ??
    // Lista di client handler attivi
    // private static final HashSet<ClientHandler> clients = new HashSet<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientID = "clt_" + System.currentTimeMillis();
        /*
        synchronized (clients) {
            clients.add(this); // Aggiungi il client handler alla lista
        }
        */
    }

    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(socket.getInputStream());
            PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " in ascolto...");

            boolean closed = false;

            while (!closed) {
                String request = fromClient.nextLine().trim();

                if (!Thread.interrupted()) {
                    System.out.println("Richiesta: " + request);

                    // Divide la richiesta in 2 parti (al massimo): comando e input (a seconda del comando)
                    String[] parts = request.split(" ", 2);

                    switch (parts[0]) {

                        case "quit":
                            closed = true;
                            toClient.println("Connessione chiusa.");
                            break;

                        case "show":
                            // Visualizza tutti i topic -> TODO !!! DA CHIEDERE !!!
                            // SE SI VUOLE FARLI CREARE SOLO DA PUBLISHER O DA TUTTI I CLIENT

                            if(Server.topics.isEmpty()) {
                                toClient.println("Nessun topic creato.");
                            } else {
                                toClient.println(Server.topics.show());
                            }
                            break;

                        case "publish":
                            if (role == Role.undefined) {
                                if (parts.length > 1) {
                                    topicName = parts[1].trim();
                                    role = Role.publisher;
                                    
                                    Server.topics.addIfAbsent(topicName);
                                    toClient.println("Registrato come publisher sul topic '" + topicName + "'.\nComandi " + role
                                    + ":\n  > send <message>\n  > list\n  > listall\n  > quit");
                                } else {
                                    toClient.println("ERRORE: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("ERRORE: già registrato come '" + role + "'.");
                            }
                            break;

                        case "subscribe":
                            if (role == Role.undefined) {
                                if (parts.length>1) {
                                    topicName = parts[1].trim();
                                    role = Role.subscriber;

                                    Server.topics.addIfAbsent(topicName);
                                    Server.topics.get(topicName).subscribe(this);      // aggiunge il client alla lista degli iscritti al topic
                                    
                                    toClient.println("Iscritto al topic '" + topicName + "'.\nComandi " + role 
                                    + ":\n  > listall\n  > quit");
                                } else {
                                    toClient.println("ERRORE: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            }
                            break;

                        case "send":
                            if (role == Role.publisher && parts.length>1) {

                                Server.topics.get(topicName).sendMessage(request, parts[1].trim());
                                // Notifica i subscriber all'interno del metodo !
                                
                                toClient.println("Messaggio inviato correttamente sul topic '" + topicName + "'.");
                            } else if (role != Role.publisher) {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            } else {
                                toClient.println("ERRORE: nessun messaggio specificato.");
                            }
                            break;

                        case "list":
                            if (role == Role.publisher && topicName!=null) {
                                String clientMessages = Server.topics.get(topicName).printClientMessages(clientID);
                                if (clientMessages == "") {
                                    toClient.println("Nessun messaggio inviato dal client '" + clientID + "' sul topic '" + topicName + "'.");
                                } else {
                                    toClient.println("Messaggi inviati dal client '" + clientID + "' sul topic '" + topicName + "':" + clientMessages);
                                }
                            } else {
                                toClient.println("ERRORE: nessun topic selezionato.");
                            }
                            break;

                        case "listall":
                            if (topicName != null) {
                                String allMessages = Server.topics.get(topicName).printAllMessages();
                                if (allMessages == "") {
                                    toClient.println("Nessun messaggio creato sul topic '" + topicName + "'.");
                                } else {
                                    toClient.println("Tutti i messaggi sul topic '" + topicName + "':" + allMessages);
                                }
                            } else {
                                toClient.println("Errore: nessun topic selezionato.");
                            }
                            break;

                        default:
                            toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            break;
                    }
                } else {
                    toClient.println("quit");
                    break;
                }
            }

            fromClient.close();
            socket.close();
            System.out.println("Connessione chiusa.");
        } catch (IOException e) {
            System.err.println("CLIENTHANDLER - IOException catturata: " + e);
            e.printStackTrace();
        } 
        /*finally {
            synchronized (clients) {
                clients.remove(this); // Rimuovi il client handler dalla lista
            }
        }
        */
    }

    // TODO SOLUZIONE TEMPORANEA (DA MODIFICARE)
    /*
    private void notifySubscribers(String topic, Message message) {
        synchronized (clients) {
            for (ClientHandler handler : clients) {
                if (handler.role == Role.subscriber && handler.topicName.equals(topic)) {
                    try {
                        PrintWriter toSubscriber = new PrintWriter(handler.socket.getOutputStream(), true);
                        toSubscriber.println("Nuovo messaggio sul topic '" + topic + "': " + message);
                    } catch (IOException e) {
                        System.err.println("CLIENTHANDLER - Eccezione nell'invio del messaggio al subscriber: " + e);
                    }
                }
            }
        }
    }
    */
}
