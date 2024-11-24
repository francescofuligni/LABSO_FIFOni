import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

/*
 * Classe `SocketListener`:
 * - Listener per il server che gestisce connessioni client tramite un `ServerSocket`.
 * - Per ogni client connesso, crea un nuovo thread (`ClientHandler`) dedicato alla gestione della connessione.
 * - Supporta timeout configurabile per il socket, permettendo controlli regolari sullo stato del thread principale.
 * - Gestisce in modo sicuro le eccezioni di rete (`SocketTimeoutException`, `IOException`) e garantisce la chiusura del server.
 * - Alla terminazione, interrompe tutti i thread figli creati per i client.
 * - Consente di continuare l'ascolto fintanto che il thread principale non viene interrotto.
 */

public class SocketListener implements Runnable {

    private ServerSocket server;
    private List<Thread> children = new LinkedList<>();

    public SocketListener(ServerSocket server) {
        this.server = server;
    }

    // Metodo eseguito quando il thread associato a SocketListener viene avviato
    @Override
    public void run() {
        try {
            System.out.println("In attesa di connessioni...");
            this.server.setSoTimeout(5000);
            // Ascolta le connessioni finchè il thread non viene interrotto
            while (!Thread.interrupted()) {
                try {
                    

                    /*
                     * Questa istruzione è bloccante, a prescindere da Thread.interrupt(). Occorre
                     * quindi controllare, una volta accettata la connessione, che il server non sia
                     * stato interrotto.
                     * 
                     * In caso venga raggiunto il timeout, viene sollevata una
                     * SocketTimeoutException, dopo la quale potremo ricontrollare lo stato del
                     * Thread nella condizione del while().
                     */
                    Socket s = this.server.accept();
                    if (!Thread.interrupted()) {
                        System.out.println("Client connesso.");

                        /* crea un nuovo thread per lo specifico socket */
                        Thread handlerThread = new Thread(new ClientHandler(s));
                        handlerThread.start();
                        this.children.add(handlerThread);
                        /*
                         * una volta creato e avviato il thread, torna in ascolto per il prossimo client
                         */
                    } else {
                        s.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    /* in caso di timeout procediamo semplicemente con l'esecuzione */
                    //System.out.println("Timeout...");
                    continue;
                } catch (IOException e) {
                    /*
                     * s.close() potrebbe sollevare un'eccezione; in questo caso non vogliamo finire
                     * nel "catch" esterno, perché non abbiamo ancora chiamato this.server.close()
                     */
                    break;
                }
            }
            this.server.close();
        } catch (IOException e) {
            System.err.println("SOCKETLISTENER - IOException catturata: " + e);
            e.printStackTrace();
        }

        System.out.println("Interrompendo i vari children...");
        for (Thread child : this.children) {
            System.out.println("Interrompendo " + child + "...");
            /*
             * child.interrupt() non è bloccante; una volta inviato il segnale
             * di interruzione proseguiamo con l'esecuzione, senza aspettare che "child"
             * termini
             */
            child.interrupt();
        }

    }

}
