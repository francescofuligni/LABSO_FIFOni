import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

 /*
     * Classe `ClientHandler`:
     * - Gestisce la connessione di ciascun client con il server.
     * - Gestisce i comandi inviati dal client, come la registrazione su un topic, l'invio di messaggi e la disconnessione.
     * - Ogni client connesso al server è gestito da un'istanza separata di questa classe.
     * - Supporta diversi ruoli: `publisher` (che può inviare messaggi) e `subscriber` (che può ricevere messaggi).
     * - Gestisce errori di connessione, eccezioni di input/output e interruzioni del thread, garantendo che ogni client possa interagire con il server in modo sicuro e isolato.
     * 
     * Caratteristiche principali:
     * - Supporto per comandi come `publish`, `subscribe`, `send`, `list`, `listall` e `quit`.
     * - Gestione dinamica dei topic, con la possibilità di iscriversi a uno o più topic e pubblicare messaggi su di essi.
     * - Implementazione di un ciclo che ascolta continuamente i comandi inviati dal client fino a che la connessione non viene chiusa.
     *
     * Scopo:
     * - Fornire una gestione scalabile e separata di ciascun client, permettendo al server di gestire più connessioni simultanee senza conflitti.
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

    /*
     *  ClientHandler:
     * Inizializza il socket del client e assegna un identificativo univoco
     * basato sul timestamp corrente.
     */

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientID = "clt_" + System.currentTimeMillis();
    }

    public Socket getSocket() {
        return this.socket;
    }

    /*
     * Metodo `run`:
     * 1. Gestisce le richieste del client tramite un ciclo continuo.
     * 2. Analizza i comandi ricevuti, ne verifica la validità, e invia risposte appropriate.
     * 3. Supporta diverse operazioni, tra cui registrazione al topic, invio di messaggi, e terminazione della connessione.
     * 4. Gestisce errori di connessione, eccezioni di input/output e interruzioni del thread.
     */

    @Override
    public void run() {
        try {
            // Scanner per leggere input dal client e PrintWriter per inviare risposte.
            Scanner fromClient = new Scanner(socket.getInputStream());
            PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " in ascolto...");

            boolean closed = false; // Flag per determinare se terminare il ciclo principale.

            while (!closed) {
                String request = fromClient.nextLine().trim(); // Legge e pulisce la richiesta dal client.

                if (!Thread.interrupted()) {
                    System.out.println("Richiesta: " + request);

                    // Divide la richiesta in 2 parti (al massimo): comando e input (a seconda del comando)
                    String[] parts = request.split(" ", 2);

                    // Gestisce i comandi principali del protocollo client-server.
                    switch (parts[0]) {

<<<<<<< Updated upstream
                        case "quit":
                            if(this.role == Role.subscriber) {
=======
                        case "quit": // Chiude la connessione e deregistra l'utente se è un sottoscrittore.
                            if(role == Role.subscriber) {
>>>>>>> Stashed changes
                                Server.topics.get(topicName).unscribe(this);
                            }
                            closed = true;
                            toClient.println("Connessione chiusa.");
                            break;

<<<<<<< Updated upstream
                        case "show":
                            // Visualizza tutti i topic -> TODO !!! DA CHIEDERE !!!
                            // SE SI VUOLE FARLI CREARE SOLO DA PUBLISHER O DA TUTTI I CLIENT
                            toClient.println(Server.topics.show());
=======
                        case "show": // Mostra la lista dei topic se il ruolo non è ancora definito.
                            if(role == Role.undefined) {
                                toClient.println(Server.topics.show());
                            } else {
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            }
>>>>>>> Stashed changes
                            break;

                        case "publish": // Registra l'utente come publisher su un topic specificato.
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

                        case "subscribe":  // Registra l'utente come subscriber su un topic specificato.
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

<<<<<<< Updated upstream
                        case "send":
                            if (role == Role.publisher && parts.length > 1) {

                                Server.topics.get(topicName).sendMessage(clientID, parts[1].trim());
                                // Notifica i subscriber all'interno del metodo !
                                toClient.println("Messaggio inviato sul topic '" + topicName + "'.");

                            } else if (role != Role.publisher) {
=======
                        case "send": // Permette al publisher di inviare un messaggio sul topic.
                            if (role == Role.publisher) {
                                if (parts.length > 1) {
                                    Server.topics.get(topicName).send(clientID, parts[1].trim());
                                    toClient.println("Messaggio inviato sul topic '" + topicName + "'.");
                                } else {
                                    toClient.println("ERRORE: nessun messaggio specificato.");
                                }
                            } else if(role == Role.subscriber) {
>>>>>>> Stashed changes
                                toClient.println("ERRORE: già registrato come " + role + ".");
                            } else {
                                toClient.println("ERRORE: nessun messaggio specificato.");
                            }
                            break;

<<<<<<< Updated upstream
                        case "list":
                            if (role == Role.publisher && topicName != null) {
                                toClient.println(Server.topics.get(topicName).printClientMessages(clientID));
=======
                        case "list": // Permette al publisher di elencare i propri messaggi pubblicati.
                            if (role == Role.publisher) {
                                toClient.println(Server.topics.get(topicName).list(clientID));
                            } else if(role == Role.subscriber) {
                                toClient.println("ERRORE: già registrato come " + role + ".");
>>>>>>> Stashed changes
                            } else {
                                toClient.println("ERRORE: nessun topic selezionato.");
                            }
                            break;

<<<<<<< Updated upstream
                        case "listall":
                            if (topicName != null) {
                                toClient.println(Server.topics.get(topicName).printAllMessages());
=======
                        case "listall": // Mostra tutti i messaggi del topic se registrato come publisher o subscriber.
                            if(role != Role.undefined) {
                                toClient.println(Server.topics.get(topicName).listAll());
>>>>>>> Stashed changes
                            } else {
                                toClient.println("Errore: nessun topic selezionato.");
                            }
                            break;

                        default: // Risponde con un errore per comandi non riconosciuti.
                            toClient.println("Comando <" + parts[0] + "> sconosciuto.");
                            break;
                    }
                } else {
                    toClient.println("quit"); // Se il thread è stato interrotto, invia un comando "quit" e termina.
                    break;
                }
            }
            // Chiude le risorse al termine del ciclo.
            fromClient.close();
            socket.close();
            //Gestione delle eccezioni.
            System.out.println("Connessione chiusa.");
        } catch (IOException e) {
            System.err.println("CLIENTHANDLER - IOException catturata: " + e);
            e.printStackTrace();
        }
    }
}
