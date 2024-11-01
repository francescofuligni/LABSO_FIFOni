import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

    // TODO: Map contenente tutti i topic (visibile a tutte le classi) -> classe TopicsHandler ??
    //public HashMap<String,Topic> allTopics = new HashMap<>();

    private static void interactiveSession(Scanner input, String topicName) {
        System.out.println("Inizio sessione interattiva...\nComandi sessione interattiva:\n  > listall\n  > delete <id>\n  > end");

        boolean closed = false;
        while (!closed) {
            String command = input.nextLine().trim();
            String[] parts = command.split(" ", 2);

            switch (parts[0]) {

                // Elenca tutti i messaggi sul topic
                case "listall":
                    System.out.println("*LISTALL NON IMPLEMENTATO*");
                    // TODO stampa tutti i messaggi sul topic selezionato
                    break;
                
                // Elimina un messaggio su un topic
                case "delete":
                    if(parts.length>1) {
                        // TODO controllo sul formato dell'ID
                        System.out.println("*DELETE NON IMPLEMENTATO*");
                        // TODO: if( topic.deleteMessage() ) -> OK, else -> ERRORE
                    } else {
                        System.out.println("ERRORE: nessun id selezionato.");
                    }
                    break;
                
                // Termina la sessione interattiva
                case "end":
                    closed = true;
                    break;

                default:
                    System.out.println("Comando <" + command + "> sconosciuto."); 
                    break;
            }
        }
        System.out.println("Sessione interattiva terminata.");
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Utilizzo: java Server <porta>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Scanner input = new Scanner(System.in);

        try {
            ServerSocket server = new ServerSocket(port);
            /*
             * deleghiamo a un altro thread la gestione di tutte le connessioni; nel thread
             * principale ascoltiamo solo l'input da tastiera dell'utente (in caso voglia
             * chiudere il programma)
             */
            Thread serverThread = new Thread(new SocketListener(server));
            serverThread.start();
            String command = "";
            System.out.println("Comandi server:\n  > show\n  > inspect <topic>\n  > quit");

            boolean closed = false;
            while (!closed) {
                command = input.nextLine().trim();
                String[] parts = command.split(" ", 2);

                switch (parts[0]) {

                    // Interrompe il server
                    case "quit":
                        closed = true;
                        break;
                
                    // Mostra la lista di tutti i topic creati
                    case "show":
                        System.out.println("*SHOW NON IMPLEMENTATO*");
                        break;

                    // Apre una sessione interattiva per analizzare un topic
                    case "inspect":
                        if(parts.length>1) {
                            // TODO CONTROLLO ESISTENZA TOPIC -> classe TopicsHandler ??
                            interactiveSession(input, parts[1].trim());
                        } else {
                            System.out.println("ERRORE: nessun topic selezionato.");
                        }
                        break;
                
                    default:
                        System.out.println("Comando <" +  command + "> sconosciuto.");
                        break;
                }
            }

            try {
                serverThread.interrupt();
                /* Attendi la terminazione del thread */
                System.out.println("In attesa della terminazione del thread...");
                serverThread.join();
            } catch (InterruptedException e) {
                /*
                 * se qualcuno interrompe questo thread nel frattempo, terminiamo
                 */
                return;
            }
            System.out.println("Thread principale terminato.");
        } catch (IOException e) {
            System.err.println("SERVER - IOException catturata: " + e);
            e.printStackTrace();
        } finally {
            input.close();
        }
    }
}
