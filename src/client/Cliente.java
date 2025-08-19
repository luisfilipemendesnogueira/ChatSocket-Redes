package client;

import java.io.*;
import java.net.*;

public class Cliente {
	public Cliente(int port) throws IOException {
		System.out.println("Cliente conectado");
		Socket socket = new Socket("localhost", port);	
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
}