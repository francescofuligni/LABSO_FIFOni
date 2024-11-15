import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
/*
     * Classe `Receiver`:
     * - Responsabile della gestione della ricezione di messaggi dal server in un sistema client-server.
     * - Opera in un thread separato per garantire che l'applicazione client rimanga reattiva durante la comunicazione.
     * - Legge i dati provenienti dal server attraverso un socket e li visualizza sulla console.
     * - È progettata per rilevare comandi speciali (ad esempio, "quit") e gestire la terminazione ordinata del client.
     *
     * Caratteristiche principali:
     * - Utilizza un flusso di input associato al socket per leggere i messaggi in modo continuo e riga per riga.
     * - Gestisce errori di input/output durante la connessione al server, assicurando robustezza nel caso di problemi di rete.
     * - Fornisce un meccanismo per interrompere un thread collegato (`sender`) in caso di terminazione della connessione.
     * - Garantisce che le risorse (scanner e socket) siano chiuse correttamente alla fine dell'esecuzione.
     */
public class Receiver implements Runnable {

    private Socket s;
    private Thread sender;

    /*
     * Inizializza il socket di connessione al server e un riferimento al thread `sender`.
     * Questo riferimento consente di interrompere il thread `sender` quando necessario.
     */

    public Receiver(Socket s, Thread sender) {
        this.s = s;
        this.sender = sender;
    }
    /*
     * RUN: Responsabile della lettura dei messaggi provenienti dal server.
     * 1. Utilizza uno scanner per leggere l'input dal socket riga per riga, finché possibile.
     * 2. Stampa ogni messaggio ricevuto sulla console.
     * 3. Rileva il comando speciale `quit` e interrompe l'esecuzione del thread.
     * 4. In caso di errore o chiusura, garantisce che il thread `sender` venga interrotto.
     *
     * Blocco Try-catch:
     * Gestisce eventuali errori di input/output durante la lettura dei dati dal server.
     * Fornisce informazioni utili per il debug.
     *
     * Blocco Finally:
     * Garantisce la terminazione del thread `sender` quando il Receiver viene chiuso,
     * evitando che il programma rimanga in uno stato bloccato.
     */
    @Override
    public void run() {
        try {
            // OCCHIO ALLA CONCORRENZA I COMANDI DI STAMPA VENGONO INVIATI SULLA SOCKET 
            // E LETTI RIGA PER RIGA ANCHE SE SONO UNA STRINGA UNICA
            Scanner from = new Scanner(this.s.getInputStream());
            //Legge i messaggi dal server finchè possibile e con questa condizione non da eccezione in caso di "quit" da client.
            while (true && from.hasNextLine()) {        
                String response = from.nextLine();
                System.out.println(response);
                if (response.equals("quit")) {
                    break;
                }
            }
            from.close();
            
        } catch (IOException e) {
            System.err.println("RECEIVER - IOException caught: " + e);
            e.printStackTrace();
        } finally {
            System.out.println("Receiver terminato.");
            this.sender.interrupt();
        }
    }
}
