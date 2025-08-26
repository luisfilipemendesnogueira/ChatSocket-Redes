package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;

public class InterfaceCliente extends JFrame {
    private JTextField apelidoField;
    private JList<String> usuariosList;
    private DefaultListModel<String> listModel;
    private JButton conectarBtn, atualizarBtn, iniciarChatBtn;
    
    private HashMap<String, ChatWindow> chatsAbertos = new HashMap<>();
    private Cliente cliente;

    public InterfaceCliente() {
        setTitle("Cliente de Chat");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());

        apelidoField = new JTextField(15);
        conectarBtn = new JButton("Conectar");
        atualizarBtn = new JButton("Atualizar");
        iniciarChatBtn = new JButton("Iniciar Chat");

        topPanel.add(new JLabel("Seu Apelido:"));
        topPanel.add(apelidoField);
        topPanel.add(conectarBtn);

        listModel = new DefaultListModel<>();
        usuariosList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(usuariosList);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(atualizarBtn);
        bottomPanel.add(iniciarChatBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        atualizarBtn.setEnabled(false);
        iniciarChatBtn.setEnabled(false);

        cliente = new Cliente(this);

        conectarBtn.addActionListener(e -> {
            String apelido = apelidoField.getText().trim();
            if (apelido.isEmpty()) {
                mostrarErro("Por favor, digite um apelido.");
                return;
            }
            String ipServidor = JOptionPane.showInputDialog(this, "Digite o IP do Servidor:", "localhost");
            if (ipServidor == null || ipServidor.trim().isEmpty()) {
                return;
            }
            cliente.conectar(apelido, ipServidor.trim());
        });

        atualizarBtn.addActionListener(e -> cliente.atualizarListaUsuarios());

        iniciarChatBtn.addActionListener(e -> {
            String selectedUser = usuariosList.getSelectedValue();
            if (selectedUser != null) {
                abrirJanelaChat(selectedUser);
            } else {
                mostrarErro("Selecione um usuário para iniciar o chat.");
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cliente.desconectar();
                System.exit(0);
            }
        });
    }

    public void conexaoBemSucedida() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Conectado com sucesso!");
            conectarBtn.setEnabled(false);
            apelidoField.setEnabled(false);
            atualizarBtn.setEnabled(true);
            iniciarChatBtn.setEnabled(true);
        });
    }

    public void mostrarErro(String mensagem) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, mensagem));
    }

    public void atualizarListaUsuarios(String[] users) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            if (users != null) {
                Arrays.stream(users)
                        .filter(user -> !user.equals(cliente.getMeuApelido()) && !user.isEmpty())
                        .forEach(listModel::addElement);
            }
        });
    }

    public void exibirMensagem(String remetente, String mensagem) {
        SwingUtilities.invokeLater(() -> {
            ChatWindow chat = chatsAbertos.get(remetente);
            if (chat == null) {
                chat = new ChatWindow(remetente, cliente);
                chatsAbertos.put(remetente, chat);
                chat.setVisible(true);
            }
            chat.adicionarMensagem(remetente, mensagem);
        });
    }

    public void abrirJanelaChat(String destinatario) {
        SwingUtilities.invokeLater(() -> {
            ChatWindow chat = new ChatWindow(destinatario, cliente);
            chatsAbertos.put(destinatario, chat);
            chat.setVisible(true);
        });
    }

    private class ChatWindow extends JFrame {
        private JTextArea chatArea;
        private JTextField messageField;
        private String destinatario;
        private Cliente cliente;

        public ChatWindow(String destinatario, Cliente cliente) {
            this.destinatario = destinatario;
            this.cliente = cliente;

            setTitle("Conversa com " + destinatario);
            setSize(400, 300);
            setLocationRelativeTo(null);

            chatArea = new JTextArea();
            chatArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(chatArea);

            JPanel southPanel = new JPanel(new BorderLayout());
            messageField = new JTextField();
            JButton sendButton = new JButton("Enviar");

            ActionListener sendMessage = e -> {
                String message = messageField.getText();
                if (!message.isEmpty()) {
                    cliente.enviarMensagem(destinatario, message);
                    chatArea.append("Você: " + message + "\n");
                    messageField.setText("");
                }
            };

            messageField.addActionListener(sendMessage);
            sendButton.addActionListener(sendMessage);

            southPanel.add(messageField, BorderLayout.CENTER);
            southPanel.add(sendButton, BorderLayout.EAST);

            add(scrollPane, BorderLayout.CENTER);
            add(southPanel, BorderLayout.SOUTH);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    chatsAbertos.remove(destinatario);
                    dispose();
                }
            });
        }

        public void adicionarMensagem(String remetente, String mensagem) {
            chatArea.append(remetente + ": " + mensagem + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceCliente().setVisible(true));
    }
}