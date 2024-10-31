import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Utilizzo:\n> java Client <host> <porta>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Socket s = new Socket(host, port);
            System.out.println("Connesso al server.");
            System.out.println("Comandi client:\n  > publish <topic>\n  > subscribe <topic>\n  > show\n  > quit");

            /*
             * Delega la gestione di input/output a due thread separati, uno per inviare
             * messaggi e uno per leggerli
             */
            Thread sender = new Thread(new Sender(s));
            Thread receiver = new Thread(new Receiver(s, sender));

            sender.start();
            receiver.start();

            try {
                /* 
                 * Rimane in attesa che sender e receiver terminino la loro esecuzione
                 */
                sender.join();
                receiver.join();
                s.close();
                System.out.println("Socket chiusa.");
            } catch (InterruptedException e) {
                /*
                 * Se qualcuno interrompe questo thread nel frattempo, terminiamo
                 */
                return;
            }

        } catch (IOException e) {
            System.err.println("CLIENT - IOException catturata: " + e);
            e.printStackTrace();
        }
    }
}
