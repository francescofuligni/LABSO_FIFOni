import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        final String serverAddress = "localhost";  // L'indirizzo del server (usare l'IP se non è locale)
        final int serverPort = 9000;  // La porta del server

        try (Socket socket = new Socket(serverAddress, serverPort);
             Scanner fromServer = new Scanner(socket.getInputStream());
             PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
             Scanner userInput = new Scanner(System.in)) {

            // Ricevere il primo messaggio dal server
            String serverMessage = fromServer.nextLine();
            System.out.println("Messaggio dal server: " + serverMessage);

            // Scrivere un messaggio al server
            System.out.print("Inserisci un messaggio da inviare al server: ");
            String message = userInput.nextLine();
            toServer.println(message);

            // Leggere la risposta dal server (echo del messaggio inviato)
            String serverResponse = fromServer.nextLine();
            System.out.println("Risposta dal server: " + serverResponse);

        } catch (IOException e) {
            System.err.println("Errore durante la connessione al server");
            e.printStackTrace();
        }
    }
}