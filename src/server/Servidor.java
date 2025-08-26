package server;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Servidor {
    private static class UserInfo {
        PrintWriter out;
        String ip;
        int porta;

        UserInfo(String ip, int porta, PrintWriter out) {
            this.ip = ip;
            this.porta = porta;
            this.out = out;
        }
    }

    private static ConcurrentHashMap<String, UserInfo> usuariosOnline = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor de Chat iniciado. Porta: 2004");
        
        try (ServerSocket serverSocket = new ServerSocket(2004)) {
            while (true) {
            	Socket clientSocket = serverSocket.accept();
            	String clientIP = clientSocket.getInetAddress().getHostAddress();
            	int clientPort = clientSocket.getPort();
            	System.out.println("Novo cliente conectado: " + clientIP + ":" + clientPort);
            	new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private String clienteApelido = null;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                String requisicao;

                while ((requisicao = in.readLine()) != null) {
                    System.out.println("Requisição recebida: " + requisicao);
                    
                    if (requisicao.startsWith("REGISTRO:")) {
                        String[] parte = requisicao.split(":");
                        if (parte.length < 2) {
                            out.println("Formato de registro inválido.");
                            continue;
                        }
                        String apelido = parte[1];
                        
                        String ip = clientSocket.getInetAddress().getHostAddress();
                        int porta = clientSocket.getPort();

                        synchronized (usuariosOnline) {
                            if (usuariosOnline.containsKey(apelido)) {
                                out.println("ERRO:Apelido já em uso.");
                            } else {
                                usuariosOnline.put(apelido, new UserInfo(ip, porta, out));
                                clienteApelido = apelido;
                                out.println("OK:Registrado com sucesso.");
                                String listaUsuarios = String.join(",", usuariosOnline.keySet());
                                out.println("USUARIOS:" + listaUsuarios);
                                System.out.println("Registrado: " + apelido + " IP: " + ip + " Porta: " + porta);
                            }
                        }
                    } 
                    else if (requisicao.startsWith("MENSAGEM:")) {
                        if (clienteApelido == null) {
                            out.println("ERRO:Você não está registrado.");
                            continue;
                        }
                        String[] partes = requisicao.split(":", 3);
                        if (partes.length < 3) {
                            out.println("ERRO:Formato de mensagem inválido.");
                            continue;
                        }
                        String destinatario = partes[1];
                        String mensagem = partes[2];
                        UserInfo destinoInfo = usuariosOnline.get(destinatario);
                        if (destinoInfo == null) {
                            out.println("OFFLINE:" + destinatario);
                        } else {
                            destinoInfo.out.println("MENSAGEM:" + clienteApelido + ":" + mensagem);
                        }
                    }
                    else if (requisicao.equals("LISTA_USUARIOS")) {
                        String listaUsuarios = String.join(",", usuariosOnline.keySet());
                        out.println("USUARIOS:" + listaUsuarios);
                    }
                    else if (requisicao.startsWith("DESCONECTAR:")) {
                        String[] parte = requisicao.split(":");
                        if (parte.length >= 2) {
                            String apelido = parte[1];
                            usuariosOnline.remove(apelido);
                            clienteApelido = null;
                            out.println("OK:Desconectado");
                            break;
                        }
                    }
                    else {
                        out.println("ERRO:Comando inválido.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
            } finally {
                if (clienteApelido != null) {
                    usuariosOnline.remove(clienteApelido);
                    System.out.println("Usuário " + clienteApelido + " desconectado.");
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar socket: " + e.getMessage());
                }
            }
        }
    }
}