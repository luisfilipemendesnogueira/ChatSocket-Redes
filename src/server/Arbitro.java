package server;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Arbitro {
    private Map<String, ClienteThread> usuarios = new ConcurrentHashMap<>();

    public Arbitro(int port) throws IOException {
        System.out.println("Servidor Iniciado");
        ServerSocket socketServer = new ServerSocket(port);

        while (true) {
            Socket socketClient = socketServer.accept();
            System.out.println("Novo usuário conectado!");

            // Criar a thread para este cliente
            ClienteThread cliente = new ClienteThread(socketClient, usuarios);
            cliente.start(); // roda em paralelo
        }
    }
}
