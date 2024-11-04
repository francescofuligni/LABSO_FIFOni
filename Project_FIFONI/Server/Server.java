import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {

    public static TopicsHandler topics = new TopicsHandler();

    private static void interactiveSession(Scanner input, String topicName) {
        System.out.println("\n* SESSIONE INTERATTIVA AVVIATA *\nComandi sessione interattiva:\n  > :listall\n  > :delete <id>\n  > :end");

        boolean closed = false;
        while (!closed) {
            String command = input.nextLine().trim();
            String[] parts = command.split(" ", 2);

            switch (parts[0]) {

                // Elenca tutti i messaggi sul topic
                case ":listall":
                    System.out.println(topics.get(topicName).printAllMessages());
                    break;
                
                // Elimina un messaggio su un topic
                case ":delete":
                    if(parts.length>1) {
                        String messageID = parts[1].trim();
                        if(topics.deleteMessage(topicName, messageID)) {
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
                        System.out.println("Interrompendo il server...");
                        closed = true;
                        break;
                
                    // Mostra la lista di tutti i topic creati
                    case "show":
                        System.out.println(topics.show());
                        break;

                    // Apre una sessione interattiva per analizzare un topic
                    case "inspect":
                        if(parts.length>1) {
                            String topicName = parts[1].trim();
                            if(topics.contains(topicName)) {
                                interactiveSession(input, topicName);
                            } else {
                                System.out.println("ERRORE: topic '" + topicName + "' insesitente.");
                            }
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
