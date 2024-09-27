import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        final String serverAddress = "localhost";   // Indirizzo del server (usare l'IP se non è locale)
        final int serverPort = 9000;                // Porta del server

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
             Scanner fromServer = new Scanner(socket.getInputStream());
             Scanner userInput = new Scanner(System.in)) {
            
            // Ricevere il primo messaggio dal server
            String serverMessage = fromServer.nextLine();
            System.out.println("Messaggio dal server: " + serverMessage);

            


            // Primo comando per registrazione
            String message = "";
            boolean unknown = true;     // Flag per comandi sconosciuti

            // IDEA STRUTTURA:
            do {
                System.out.print("Inserire comando 'publisher'/'subscriber'/'show'/'quit': ");
                message = userInput.nextLine();
                
                if(message.startsWith("publisher")) {
                    // Registra il client come publisher sul topic
                    System.out.println("Registrato correttamente come publisher");
                    unknown = false;
                } else if(message.startsWith("subscriber")) {
                    // Registra il client come subscriber sul topic
                    System.out.println("Registrato correttamente come subscriber");
                    unknown = false;
                } else if(message.equals("show")) {
                    // Mostra la lista di tutti i topic creati dai publisher
                    System.out.println("Lista dei topic creati: empty");
                    unknown = false;
                } else {
                    // Riprova: comando sconosciuto
                    System.out.println("Comando sconoscuto, riprovare");
                }

            } while(unknown);   // Riprova se il comando è sconosciuto


            while(!message.equals("quit")) {    // Finché non decide di terminare la connessione
                // Richiede altri comandi
                System.out.print("Nuovo comando: ");
                message = userInput.nextLine();
                toServer.println(message);

                // Risposta dal server (echo)
                String serverResponse = fromServer.nextLine();
                System.out.println("Echo dal server: " + serverResponse);
            }
            System.out.println("Client arrestato");





        } catch (IOException e) {
            System.err.println("Errore durante la connessione al server");
            e.printStackTrace();
        }
    }
}
