import java.io.IOException;
import java.net.Socket;
/*
     * Classe principale per il lato client di un sistema di comunicazione:
     * - Si occupa di stabilire una connessione con il server, utilizzando host e porta specificati.
     * - Permette l'interazione con il server tramite comandi inviati dall'utente.
     * - Utilizza un'architettura multi-thread per gestire simultaneamente l'invio e la ricezione di messaggi.
     * - Garantisce la corretta chiusura delle risorse (socket e thread) per evitare perdite o problemi di connessione.
     * 
     * Funzionalità principali:
     * - Validazione dei parametri di avvio.
     * - Creazione e gestione di una connessione socket al server.
     * - Esecuzione dei thread `Sender` e `Receiver` per le operazioni di input/output.
     * - Gestione robusta di errori relativi alla connessione e all'I/O.
     */
public class Client {

    /*
     * Il Main si occupa di:
     * 1. Validare gli argomenti passati da linea di comando (host e porta).
     * 2. Stabilire una connessione con il server tramite socket.
     * 3. Creare e avviare due thread dedicati alla gestione dell'invio e della ricezione di messaggi.
     * 4. Garantire una corretta chiusura della connessione al termine dell'esecuzione.
     */
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
                 * Se il thread principale viene interrotto durante l'attesa, esce immediatamente.
                 * Non tenta di eseguire altre operazioni.
                 */
                return;
            }
            /*
             * Gestisce eventuali errori relativi all'I/O durante la connessione al server
             * o durante la creazione del socket.
             * 
             */
        } catch (IOException e) {
            System.err.println("CLIENT - IOException catturata: " + e);
            e.printStackTrace();
        }
    }
}
