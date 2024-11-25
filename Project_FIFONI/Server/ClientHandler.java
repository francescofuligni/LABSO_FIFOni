import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/*
 * La classe `ClientHandler` gestisce le connessioni dei client con il server.
 * Ogni client connesso viene gestito da una propria istanza di `ClientHandler`, che si occupa di interpretare
 * i comandi ricevuti dal client, come l'iscrizione a un topic, la pubblicazione di messaggi e la disconnessione.
 * La classe è progettata per supportare ruoli differenti per i client, come `publisher` e `subscriber`, e per interagire
 * con un sistema di topic dove i client possono registrarsi per ricevere o inviare messaggi.
 * 
 * La classe supporta diversi comandi che i client possono inviare:
 * - `quit`: Chiude la connessione.
 * - `show`: Mostra la lista dei topic disponibili.
 * - `publish`: Consente di registrarsi come publisher su un topic e inviare messaggi.
 * - `subscribe`: Consente di registrarsi come subscriber su un topic.
 * - `send`: Permette al publisher di inviare un messaggio.
 * - `list`: Permette al publisher di vedere i propri messaggi inviati.
 * - `listall`: Mostra tutti i messaggi di un topic, visibili sia ai publisher che ai subscriber.
 * 
 * Ogni comando viene gestito in un ciclo continuo finché il client non invia un comando di disconnessione o il thread viene interrotto.
 * Se il client invia comandi non riconosciuti, viene inviata una risposta di errore.
 * 
 */
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
   /*
     * Metodo principale che gestisce il ciclo di vita del client, ascoltando i comandi e rispondendo.
     * - Legge i comandi dal client.
     * - Elabora le richieste e invia risposte appropriate.
     * - Gestisce i ruoli (publisher, subscriber).
     * - Interrompe il ciclo quando il client invia il comando "quit" o quando si verifica un errore.
     */
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

                        // Comando "quit": chiude la connessione e deregistra il client, se necessario
                        case "quit":
                            if(role == Role.subscriber) {
                                Server.topics.get(topicName).unsubscribe(this);
                            }
                            closed = true;
                            toClient.println("Connessione chiusa.");
                            break;

                        // Comando "show": mostra la lista dei topic se il client non è ancora registrato    
                        case "show":
                            if(role == Role.undefined) {
                                toClient.println(Server.topics.show());
                            } else {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            }
                            break;

                        // Comando "publish": registra il client come publisher su un topic    
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

                        // Comando "subscribe": registra il client come subscriber su un topic    
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

                        // Comando "send": invia un messaggio sul topic per i publisher    
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

                        // Comando "list": elenca i messaggi inviati dal publisher        
                        case "list":
                            if (role == Role.publisher) {
                                toClient.println(Server.topics.get(topicName).list(clientID));
                            } else if(role == Role.subscriber) {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            } else {
                                toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            }
                            break;

                        // Comando "listall": mostra tutti i messaggi del topic        
                        case "listall":
                            if(role != Role.undefined) {
                                toClient.println(Server.topics.get(topicName).listAll());
                            } else {
                                toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            }
                            break;

                        // Risposta per comandi non riconosciuti
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

            // Chiude le risorse al termine del ciclo
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
