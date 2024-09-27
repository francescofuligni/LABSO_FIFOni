import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {
        final int port = 9000;

        // Dichiarazione del ServerSocket
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server in ascolto sulla porta " + port);

            // Attendere una connessione da parte di un client
            Socket s = server.accept();
            System.out.println("Connessione accettata da " + s.getInetAddress());

            // Usa try-with-resources per garantire che le risorse siano chiuse automaticamente
            try (Scanner from = new Scanner(s.getInputStream());
                 PrintWriter to = new PrintWriter(s.getOutputStream(), true)) {

                // Inviare una richiesta al client
                to.println("connessione accettata");


                // IDEA STRUTTURA:
                String response = "";
                do {
                    // Ascolta i comandi del client
                    response = from.nextLine();
                    System.out.println("Ricevuto dal client: " + response);
                    
                    // echo
                    to.println(response);
                } while(!response.equals("quit"));
                
                System.out.println("Server arrestato");
                
            } catch (IOException e) {
                System.err.println("Errore durante la comunicazione con il client");
                e.printStackTrace();
            }

            // Chiudere il socket
            s.close();

        } catch (IOException e) {
            System.err.println("Errore durante la creazione del server socket sulla porta " + port);
            e.printStackTrace();
        }
    }
}
