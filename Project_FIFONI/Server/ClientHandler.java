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

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientID = "clt_" + System.currentTimeMillis();
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
                            toClient.println(Server.topics.show());
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
                                if (parts.length > 1) {
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
                            if (role == Role.publisher && parts.length > 1) {

                                Server.topics.get(topicName).sendMessage(clientID, parts[1].trim());
                                // Notifica i subscriber all'interno del metodo !
                                toClient.println("Messaggio inviato sul topic '" + topicName + "'.");

                            } else if (role != Role.publisher) {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            } else {
                                toClient.println("ERRORE: nessun messaggio specificato.");
                            }
                            break;

                        case "list":
                            if (role == Role.publisher && topicName != null) {
                                toClient.println(Server.topics.get(topicName).printClientMessages(clientID));
                            } else {
                                toClient.println("ERRORE: nessun topic selezionato.");
                            }
                            break;

                        case "listall":
                            if (topicName != null) {
                                toClient.println(Server.topics.get(topicName).printAllMessages());
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
    }
}
