package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Classe que visa simular o funcionamento de um banco (AlphaBank), o qual deve controlar o saldo de Contas-Corrente
 * (Account), bem como realizar pagamentos.
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 02/10/2023
 */
public class AlphaBank extends Thread{

    //ArrayList contendo todas as Accounts dos clientes do banco.
    private static ArrayList<Account> accounts;

    //Flag para analisar se o banco está realizando operações em alguma Account.
    private static boolean is_consulting = false;

    //ArrayList contendo os clientes do banco (AlphaBank).
    private static ArrayList<BufferedWriter> clientes = new ArrayList<>();

    //Comunicação Cliente-Servidor
    private static ServerSocket server;
    private Socket socket_servidor;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader bfr;

    //----------------------------------------------------------------------------------------------------
    
    /**
     * Construtor do Servidor.
     * @param socket {@link Socket} contendo a instância de comunicação cliente/servidor.
     */
    public AlphaBank(Socket socket) {
        this.socket_servidor = socket;
        try {
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            bfr = new BufferedReader(isr);
        } catch (Exception e) {
            System.out.println("Erro no construtor\nException: " + e);
        }
    }

    @Override
    public synchronized void run() {
        try{
            String msg;
            OutputStream ou =  this.socket_servidor.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            clientes.add(bfw);

            msg = "inicio";

            while (msg != null) {
                msg = bfr.readLine();

                while (is_consulting) {
                    try {
                        wait();
                    } catch (Exception e) {
                        System.out.println("Falha no Wait do ConsultarSaldo.\nException: " + e);
                    }
                }

                Criptografia criptografia = new Criptografia();
                
                String decriptografa = criptografia.decriptografa(msg);
                
                JsonFile jsonFile = new JsonFile(decriptografa);

                String comando = jsonFile.getComando();

                if (comando.equals(Constantes.comando_consulta)) {
                    consultarSaldo(jsonFile, bfw);
                } else if (comando.equals(Constantes.comando_pagar)) {
                    realizarPagamento(jsonFile, bfw);          
                } else if (comando.equals(Constantes.comando_conexao)) {
                    String dados[] = jsonFile.receberConexao();
                    addAccount(dados[0], dados[1]);
                    //System.out.println(msg);
                    Thread.sleep(200);
                }
            }   

        }catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * Faz a consulta do saldo da conta requisitada
     * @param jsonFile {@link JsonFile} contendo o objeto para utilização dos métodos.
     * @param bfw {@link BufferedWriter} contendo o destinatário para retorno.
     */
    private synchronized void consultarSaldo(JsonFile jsonFile, BufferedWriter bfw) {
        is_consulting = true;

        ArrayList<String> dados = jsonFile.recebeConsultaSaldo();

        String login = dados.get(0);
        String senha = dados.get(1);

        Account account = searchAccount(login, senha, false);

        //if (account != null) {

        jsonFile.escreveSaldo(account.getSaldo_atual());

        account.startThread();

        String json = jsonFile.getJSONAsString();

        Criptografia criptografia = new Criptografia();

        try {
            bfw.write(criptografia.criptografa(json) +"\r\n");
            bfw.flush();
        } catch (Exception e) {
            System.out.println("Erro na escrita da consulta do saldo.\nException: " + e);
        }
        //}

        is_consulting = false;

        notifyAll();
    }

    /**
     * Realiza o pagamento destinado a uma conta específica (podendo ser um {@link Driver} ou a {@link FuelStation}).
     * @param jsonFile {@link JsonFile} contendo o objeto para utilização dos métodos.
     * @param bfw {@link BufferedWriter} contendo o destinatário para retorno.
     */
    private synchronized void realizarPagamento(JsonFile jsonFile, BufferedWriter bfw) {
        is_consulting = true;

        ArrayList<Object> dadosJSON = jsonFile.recebeDadosPagamento();

        String login = (String) dadosJSON.get(0);
        String senha = (String) dadosJSON.get(1);
        String destino = (String) dadosJSON.get(2);
        double valor = ((BigDecimal) dadosJSON.get(3)).doubleValue();

        //System.out.println(destino);

        Account account_owner = searchAccount(login, senha, false);
        Account account_destino = searchAccount(destino, null, true);

        account_owner.saque(valor, destino);
        account_owner.startThread();

        account_destino.deposito(valor);
        account_destino.startThread();

        while (!account_owner.getCompletouTransacao() && !account_destino.getCompletouTransacao()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {

            }
        }

        //System.out.println(account_destino.getSaldo());
        
        is_consulting = false;
        notify();
    }
    
    /**
     * Adiciona uma conta de um cliente no ArrayList {@link AlphaBank#accounts}.
     * @param login {@link String} contendo o login do usuário.
     * @param senha {@link String} contendo o login
     */
    private void addAccount(String login, String senha) {
        double saldo_inicial;
        
        if (login.equalsIgnoreCase("Company")) {
            saldo_inicial = 10000000;
        } else if (login.equalsIgnoreCase("FuelStation")) {
            saldo_inicial = 0;
        } else {
            saldo_inicial = 100;
        }
        
        Account account = new Account(login, senha, saldo_inicial);
        accounts.add(account);
    }

    /**
     * Método para buscar uma Account no sistema.
     * @param login {@link String} contendo o login do usuário.
     * @param senha {@link String} contendo a senha do usuário.
     * @param is_paying {@link Boolean} contendo uma identificação para saber se é pagamento ou não.
     * @return {@link Account} contendo a Account procurada // null caso nenhuma Account seja encontrada.
     */
    private Account searchAccount(String login, String senha, boolean is_paying) {
        for (int i = 0; i< accounts.size(); i++) {
            Account account = accounts.get(i);
            if (account.getLogin().equals(login)) {
                if (is_paying) {
                    return account;
                } else if (account.getSenha().equals(senha)) {
                    return account;
                }
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
