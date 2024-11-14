import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
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
                    // Divide la richiesta in 2 parti (al massimo): comando e input (a seconda del comando)
                    String[] parts = request.split(" ", 2);

                    switch (parts[0]) {

                        case "quit":
                            if(role == Role.subscriber) {
                                Server.topics.get(topicName).unscribe(this);
                            }
                            closed = true;
                            toClient.println("Connessione chiusa.");
                            break;

                        case "show":
                            if(role == Role.undefined) {
                                toClient.println(Server.topics.show());
                            } else {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            }
                            break;

                        case "publish":
                            if (role == Role.undefined) {
                                if (parts.length > 1) {
                                    topicName = parts[1].trim();
                                    role = Role.publisher;
                                    
                                    Server.topics.putIfAbsent(topicName);
                                    toClient.println("Registrato come " + role + " sul topic '" + topicName + 
                                    "'.\nComandi " + role + ":\n  > send <message>\n  > list\n  > listall\n  > quit");
                                } else {
                                    toClient.println("ERRORE: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            }
                            break;

                        case "subscribe":
                            if (role == Role.undefined) {
                                if (parts.length > 1) {
                                    topicName = parts[1].trim();
                                    role = Role.subscriber;

                                    Server.topics.putIfAbsent(topicName);
                                    Server.topics.get(topicName).subscribe(this);
                                    
                                    toClient.println("Registrato come " + role + " al topic '" + topicName + 
                                    "'.\nComandi " + role + ":\n  > listall\n  > quit");
                                } else {
                                    toClient.println("ERRORE: nessun topic specificato.");
                                }
                            } else {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            }
                            break;

                        case "send":
                            if (role == Role.publisher) {
                                if (parts.length > 1) {
                                    Server.topics.get(topicName).send(clientID, parts[1].trim());
                                    toClient.println("Messaggio inviato sul topic '" + topicName + "'.");
                                } else {
                                    toClient.println("ERRORE: nessun messaggio specificato.");
                                }
                            } else if(role == Role.subscriber) {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            } else {
                                toClient.println("Comando <" + parts[0] + "> sconosciuto."); 
                            }
                            break;

                        case "list":
                            if (role == Role.publisher) {
                                toClient.println(Server.topics.get(topicName).list(clientID));
                            } else if(role == Role.subscriber) {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            } else {
                                toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            }
                            break;

                        case "listall":
                            if(role != Role.undefined) {
                                toClient.println(Server.topics.get(topicName).listAll());
                            } else {
                                toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            }
                            break;

                        default:
                            toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            break;
                    }
                    System.out.println("Richiesta: " + request);
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
        } catch (NoSuchElementException e) {
            System.err.println("CLIENTHANDLER - NoSuchElementException catturata: " + e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("CLIENTHANDLER - InterruptedException catturata: " + e);
            e.printStackTrace();
        }
    }
}
