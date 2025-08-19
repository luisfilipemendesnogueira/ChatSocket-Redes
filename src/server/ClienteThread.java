package server;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ClienteThread extends Thread{
	private Socket socketClient;
	private String apelido;
	private Map<String, ClienteThread > usuarios;
	
	private PrintWriter out;
	private BufferedReader in;

	public ClienteThread(Socket socketClient, Map<String, ClienteThread> usuarios) {
	    this.socketClient = socketClient;
	    this.usuarios = usuarios;
	}
	
	@Override
    public void run() {
        try {
            out = new PrintWriter(socketClient.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));

            // Primeiro, receber o apelido do cliente
            apelido = in.readLine();
            usuarios.put(apelido, this); // adiciona na lista de clientes conectados
            System.out.println(apelido + " conectado!");

            String mensagem;
            while ((mensagem = in.readLine()) != null) {
                System.out.println("Recebido de " + apelido + ": " + mensagem);
                // Aqui podemos enviar para outros clientes, dependendo da lógica
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            	socketClient.close();
                usuarios.remove(apelido);
                System.out.println(apelido + " desconectou!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	public void enviarMensagem(String msg) {
		out.println(msg);
	}
}
