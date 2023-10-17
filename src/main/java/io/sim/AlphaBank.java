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

    private ArrayList<Account> accounts;

    //ArrayList contendo os clientes do banco (AlphaBank).
    private static ArrayList<BufferedWriter> clientes = new ArrayList<>();

    //Comunicação Cliente-Servidor
    private static ServerSocket server;
    private Socket socket_servidor;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader bfr;

    String nome;

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
            nome = msg = bfr.readLine();

            System.out.println(msg);

            Thread.sleep(200);
        
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
     * Método principal de execução da classe. Inicia o servidor do AlphaBank.
     * @param args
     */
    public static void main (String[] args) {
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
                        System.out.println("Aguardando conexão");
                        Socket socket = server.accept();
                        System.out.println("Conectou no AlphaBank!");

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
