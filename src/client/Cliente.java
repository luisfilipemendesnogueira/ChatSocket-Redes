package client;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

public class Cliente {
    private String meuApelido;
    private Socket servidorSocket;
    private PrintWriter outServidor;
    private BufferedReader inServidor;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private InterfaceCliente interfaceCliente;

    public Cliente(InterfaceCliente interfaceCliente) {
        this.interfaceCliente = interfaceCliente;
    }

    public void conectar(String apelido, String ipServidor) {
        this.meuApelido = apelido;
        executor.execute(() -> {
            try {
                servidorSocket = new Socket(ipServidor, 2004);
                outServidor = new PrintWriter(servidorSocket.getOutputStream(), true);
                inServidor = new BufferedReader(new InputStreamReader(servidorSocket.getInputStream()));

                outServidor.println("REGISTRO:" + meuApelido);
                String response = inServidor.readLine();

                if (response != null && response.startsWith("OK")) {
                    interfaceCliente.conexaoBemSucedida();
                    executor.execute(this::ouvirServidor);
                } else {
                    interfaceCliente.mostrarErro("Erro ao conectar: " + response);
                    servidorSocket.close();
                }
            } catch (IOException e) {
                interfaceCliente.mostrarErro("Erro ao conectar ao servidor: " + e.getMessage());
            }
        });
    }

    private void ouvirServidor() {
        try {
            String mensagem;
            while ((mensagem = inServidor.readLine()) != null) {
                if (mensagem.startsWith("MENSAGEM:")) {
                    String[] partes = mensagem.split(":", 3);
                    if (partes.length >= 3) {
                        String remetente = partes[1];
                        String conteudo = partes[2];
                        interfaceCliente.exibirMensagem(remetente, conteudo);
                    }
                } else if (mensagem.startsWith("USUARIOS:")) {
                    String lista = mensagem.substring(9);
                    String[] users = lista.split(",");
                    interfaceCliente.atualizarListaUsuarios(users);
                } else if (mensagem.startsWith("OFFLINE:")) {
                    String usuario = mensagem.substring(8);
                    interfaceCliente.exibirMensagem(usuario, "[Servidor]: Usuário está offline. Mensagem não entregue.");
                }
            }
        } catch (IOException e) {
            if (!servidorSocket.isClosed()) {
                interfaceCliente.mostrarErro("Conexão com servidor perdida: " + e.getMessage());
            }
        }
    }

    public void enviarMensagem(String destinatario, String mensagem) {
        if (outServidor != null) {
            outServidor.println("MENSAGEM:" + destinatario + ":" + mensagem);
        }
    }

    public void desconectar() {
        try {
            if (outServidor != null) {
                outServidor.println("DESCONECTAR:" + meuApelido);
            }
            if (servidorSocket != null) {
                servidorSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    public void atualizarListaUsuarios() {
        if (outServidor != null) {
            outServidor.println("LISTA_USUARIOS");
        }
    }

    public String getMeuApelido() {
        return meuApelido;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceCliente().setVisible(true));
    }
}