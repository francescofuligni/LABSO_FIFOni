import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    Socket s;


    public ClientHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            Scanner from = new Scanner(s.getInputStream());
            PrintWriter to = new PrintWriter(s.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " listening...");

            boolean closed = false;
            while (!closed) {
                String request = from.nextLine();
                if (!Thread.interrupted()) {
                    System.out.println("Request: " + request);
                    String[] parts = request.split(" ");
                    switch (parts[0]) {
                        case "quit":
                            closed = true;
                            break;
                        case "info":
                            if (parts.length > 1) {
                                String response = "Request recieved correctly: " + parts[1];
                                to.println(response);
                            } else {
                                to.println("No key");
                            }
                            break;

                        default:
                            to.println("Unknown cmd");
                    }
                } else {
                    to.println("quit");
                    break;
                }
            }

            to.println("quit");
            from.close();
            s.close();
            System.out.println("Closed");
        } catch (IOException e) {
            System.err.println("ClientHandler: IOException caught: " + e);
            e.printStackTrace();
        }
    }

}
