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
     * Inizializ