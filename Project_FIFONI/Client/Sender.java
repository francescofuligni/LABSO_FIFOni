import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
 /*
     * Classe `Sender`:
     * - Gestisce l'invio di messaggi dal client al server in un sistema client-server.
     * - Opera in un thread separato per consentire l'elaborazione parallela dei messaggi rispetto ad altre attività, come la ricezione.
     * - Raccoglie l'input dell'utente tramite tastiera e lo invia al server attraverso un socket.
     *
     * Caratteristiche principali:
     * - Utilizza un flusso di output associato al socket per trasmettere i messaggi al server.
     * - Supporta il comando speciale "quit" per interrompere la comunicazione e chiudere la connessione.
     * - Gestisce eventuali interruzioni del thread inviando un messaggio di chiusura al server prima di terminare.
     * - Include meccanismi per gestire in modo robusto errori di input/output durante la comunicazione.
     *
     * Scopo:
     * - Fornire un'interfaccia reattiva e separata per l'invio dei messaggi,
     *   mantenendo il client operativo anche durante l'attesa di input da parte dell'utente.
     */
public class Sender implements Runnable {

    private Socket s;
    /*
     * Sender:
     * Inizializza il socket tramite il quale il client invierà i messaggi al server.
     */
    public Sender(Socket s) {
        this.s = s;
    }
    /*
     * Metodo `run`:
     * Responsabile della gestione dell'input da tastiera dell'utente e dell'invio dei messaggi al server.
     * 1. Legge l'input dell'utente da console.
     * 2. Invia i messaggi al server attraverso il flusso di output del socket.
     * 3. Rileva interruzioni del thread e invia un messaggio "quit" al server prima di terminare.
     * 4. Gestisce il comando "quit" inserito dall'utente, terminando il ciclo di invio.
     */
    @Override
    public void run() {
        Scanner userInput = new Scanner(System.in);

        try {
            PrintWriter to = new PrintWriter(this.s.getOutputStream(), true);
            while (true) {
                String request = userInput.nextLine();
                /*
                 * se il thread è stato interrotto mentre leggevamo l'input da tastiera, inviamo
                 * "quit" al server e usciamo
                 */
                if (Thread.interrupted()) {
                    to.println("quit");
                    break;
                }
                /* in caso contrario proseguiamo e analizziamo l'input inserito */
                to.println(request);
                if (request.equals("quit")) {
                    break;
                }
            }
            System.out.println("Sender terminato.");

            /*
             * Blocco Try-Catch:
             * Gestisce eventuali errori di input/output durante l'invio dei messaggi al server.
             * Fornisce informazioni sull'eccezione per il debug.
             * 
             * Blocco Finally:
             * Garantisce che lo scanner venga chiuso indipendentemente dal successo o dal fallimento.
             */
            
        } catch (IOException e) {
            System.err.println("SENDER - IOException catturata: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }

}
