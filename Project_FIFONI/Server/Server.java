import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
/*
 * Classe `Server`:
 * - Punto di ingresso per un server socket che gestisce connessioni client e topic.
 * - La porta viene specificata come argomento all'avvio.
 * - Gestione concorrente delle connessioni tramite un thread dedicato (`SocketListener`).
 * - Interfaccia interattiva per l'amministrazione con comandi:
 *   - `show`: Mostra i topic attivi.
 *   - `inspect <topic>`: Analizza un topic specifico.
 *   - `quit`: Termina il server.
 * - Gestisce eccezioni di rete (`IOException`) e interruzioni dei thread (`InterruptedException`).
 * - Garantisce chiusura ordinata delle risorse al termine.
 */
public class Server {
    
    public static TopicsHandler topics = new TopicsHandler();

    // Verifica che sia stata passata una porta come argomento e se non è cosi termina l'esecuzione
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
            // Legge un comando dalla console dividendo il comando e i parametri
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
                                topics.get(topicName).interactiveSession(input);
                            } else {
                                System.out.println("ERRORE: topic '" + topicName + "' insesitente.");
                            }
                        } else {
                            System.out.println("ERRORE: nessun topic selezionato.");
                        }
                        break;
                
                    default:
                        System.out.println("Comando <" +  parts[0] + "> sconosciuto.");
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
        } catch (InterruptedException e) {
            System.err.println("SERVER - InterruptedException catturata: " + e);
            e.printStackTrace();
        } finally {
            input.close();
        }
    }
}
