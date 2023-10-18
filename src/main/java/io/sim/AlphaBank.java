package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * "Banco", devendo conter as seguintes características:
 *      -> Ser um Servidor (CONCLUÍDO)
 *      -> Controlar as Accounts
 *      -> Controlar o saldo das Accounts
 *      -> Controlar o acesso às Accounts (não devendo ter acessos simultâneos)
 *      -> Registrar o Timestamp das trocas de mensagens
 *      -> Utilizar arquivos JSON e criptografia para a troca de mensagens com clientes/servidor
 */
public class AlphaBank extends Thread{

    //ArrayList contendo todas as Accounts dos clientes do banco.
    private static ArrayList<Account> accounts;

    //ArrayList contendo os clientes do banco (AlphaBank).
    private static ArrayList<BufferedWriter> clientes = new ArrayList<>();

    //Comunicação Cliente-Servidor
    private static ServerSocket server;
    private Socket socket_servidor;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader bfr;

    public AlphaBank(Socket socket) {
        this.socket_servidor = socket;
        try {
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            bfr = new BufferedReader(isr);

            //conectar();
        } catch (Exception e) {
            System.out.println("Erro no construtor\nException: " + e);
        }
    }

    @Override
    public void run() {
        try{
            String msg;
            OutputStream ou =  this.socket_servidor.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            clientes.add(bfw);

            msg = bfr.readLine();

            String dados[] = msg.split(" ");

            addAccount(dados[0], dados[1]);

            //System.out.println(dados[1]);

            Thread.sleep(200);

            while (msg != null) {
                msg = bfr.readLine();

                Criptografia criptografia = new Criptografia();

                String decriptografa = criptografia.decriptografa(msg);

                JsonFile jsonFile = new JsonFile(decriptografa);

                String comando = (String) jsonFile.getObject("comando");

                if (comando.equals("consultar")) {

                } else if (comando.equals("pagar")) {
                    
                }
            }   

            }catch (Exception e) {
            e.printStackTrace();
    
        }
    }


    //Talvez Private com a comunicação cliente/servidor
    public void addAccount(String login, String senha) {
        Account account = new Account(login, senha);
        accounts.add(account);
    }

    /**
     * Método para buscar uma Account no sistema.
     * @param login {@link String} contendo o login do usuário.
     * @param senha {@link String} contendo a senha do usuário.
     * @return {@link Account} contendo a Account procurada // null caso nenhuma Account seja encontrada.
     */
    private Account searchAccount(String login, String senha) {
        for (int i = 0; i< accounts.size(); i++) {
            Account account = accounts.get(i);
            if (account.getLogin().equals(login) && account.getSenha().equals(senha)) {
                return account;
            }
        }

        System.out.println("Nenhuma Account com esse login/senha foi encontrada.");
        return null;
    }

    /**
     * Método principal de execução da classe. Inicia o servidor do AlphaBank.
     * @param args
     */
    public static void main (String[] args) {
        //Inicializa a ArrayList de accounts.
        accounts = new ArrayList<>();

        Thread thread_alphaBank = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Criação do ArrayList de clientes do servidor, ou seja, onde todos aqueles que tenham uma conta no banco
                    //se conectarão.
                    clientes = new ArrayList<BufferedWriter>();
                    
                    //Criação do servidor, associado à uma porta, definida acima.
                    server = new ServerSocket(Constantes.porta_AlphaBank);

                    //Execução para aguardar os clientes do servidor se conectarem a ele.
                    while (true) {
                        //System.out.println("Aguardando conexão");
                        Socket socket = server.accept();
                        //System.out.println("Conectou no AlphaBank!");

                        Thread thread = new AlphaBank(socket);
                        thread.start();
                    }

                } catch (Exception e) {
                    System.out.println("Erro na execução principal da AlphaBank.java\nException: " + e);
                }
            }
        });
        thread_alphaBank.start();
    }
}
