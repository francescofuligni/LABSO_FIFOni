import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Receiver implements Runnable {

    private Socket s;
    private Thread sender;

    public Receiver(Socket s, Thread sender) {
        this.s = s;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            // OCCHIO ALLA CONCORRENZA I COMANDI DI STAMPA VENGONO INVIATI SULLA SOCKET 
            // E LETTI RIGA PER RIGA ANCHE SE SONO UNA STRINGA UNICA
            Scanner from = new Scanner(this.s.getInputStream());
            
            while (true && from.hasNextLine()) {        // ! Con questa condizione non da eccezione in caso di <quit> da client
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
