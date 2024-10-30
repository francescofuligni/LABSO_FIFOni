
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Scanner;

/*RIMUOVERE ECHO SUL SERVERS SOCKET */

public class Server {

    public HashSet<Topic> topics = new HashSet<Topic>();

    private static void startInteractiveSession(Scanner userInput) {
        System.out.println("Inizio sessione interattiva...");

        while (true) {
            String command = userInput.nextLine();
            if (command.equals("end")) {
                break;
            } 
            else if (command.equals("listall")) {
                System.out.println("listall non ancora implementato");
            } 
            else {
                if (command.contains("delete")) {
                    String[] parts = command.split(" ");
                    if (parts[0].equals("delete")) {
                        // TODO
                        // IMPLEMENTARE LOGICA PER DELETE MESSAGE
                        System.out.println("delete non ancora implementato");
                    } 
                    else {
                        System.out.println("Comando non riconosciuto");
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Utilizzo: java Server <porta>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Scanner userInput = new Scanner(System.in);

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

            while (true) {

                command = userInput.nextLine();

                if (command.equals("quit")) {
                    break;
                }

                else if (command.equals("show")) {

                    
                }

                else if (command.equals("inspect")) {
                    startInteractiveSession(userInput);

                } else {
                    System.out.println("Comando non riconosciuto");
                }
            }

            try {
                serverThread.interrupt();
                /* attendi la terminazione del thread */
                serverThread.join();
            } catch (InterruptedException e) {
                /*
                 * se qualcuno interrompe questo thread nel frattempo, terminiamo
                 */
                return;
            }
            System.out.println("Thread principale terminato");
        } catch (IOException e) {
            System.err.println("IOException catturata: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }
}
