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
     * Inizializza il socket tramite il quale il client invierà i messaggi al serv