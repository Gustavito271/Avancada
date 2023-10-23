package io.sim;

import java.util.ArrayList;

/**
 * Classe que visa simular o funcionamento de uma Conta-Corrente (Account), sendo que
 * os clientes do Banco ({@link AlphaBank}) todos possuem uma conta registrada. Ressalta-se
 * que a conta possui um método com login e senha.
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 16/10/2023
 */
public class Account implements Runnable { 

    //Parâmetros de acesso à conta.
    private String login;
    private String senha;

    //Referências ao saldo da conta - Anterior e posterior à transações.
    private double saldo_atual;
    private double saldo_anterior;

    //Conta destino da operação feita (apenas em caso de Saque, indicando transferência).
    private String destino;

    //Objeto da Thread para execução.
    private Thread thread;

    //Flag para demarcar finalização do extrato.
    private boolean completou_transacao = false;

    //------------------------------------------------------------------------------------------------
    /**
     * Construtor da classe.
     * @param login {@link String} contendo o login do usuário para acesso da Account.
     * @param senha {@link String} contendo a senha do usuário para acesso da Account.
     * @param saldo {@link Double} contendo o saldo inicial da Account.
     */
    public Account(String login, String senha, double saldo) {
        this.login = login;
        this.senha = senha;
        this.saldo_atual = saldo;
    }

    /**
     * Inicia a execução da thread.
     */
    public void startThread() {
        completou_transacao = false;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Para a exeecução da thread.
     */
    public void stopThread() {
        thread.interrupt();
    }

    @Override
    public void run() {
        ExportaExcel excel = new ExportaExcel();

        ArrayList<Object> arrayList = new ArrayList<>();

        arrayList.add(System.nanoTime());
        arrayList.add(this.login);
        arrayList.add(this.destino);
        arrayList.add(this.saldo_anterior);
        arrayList.add(this.saldo_atual);

        extrato(arrayList, excel);

        completou_transacao = true;
    }

    private static synchronized void extrato(ArrayList<Object> arrayList, ExportaExcel excel) {
        excel.escreveExtrato(arrayList);
    }

    /**
     * Representa o saque de dinheiro de uma Account.
     * @param valor {@link Double} contendo o valor a ser sacado.
     */
    public void saque(double valor, String destino_saque) {
        this.destino = destino_saque;

        this.saldo_anterior = this.saldo_atual;
        this.saldo_atual -= valor;
    }

    /**
     * Representa o depósito de dinheiro em uma Account.
     * @param valor {@link Double} contendo o valor a ser depositado.
     */
    public void deposito(double valor) {
        this.destino = "-";

        this.saldo_anterior = this.saldo_atual;
        this.saldo_atual += valor;
    }

    /**
     * Método GET para o saldo da conta.
     * @return {@link Double} contendo o valor do saldo da Account.
     */
    public double getSaldo_atual() {
        this.destino = "-";
        this.saldo_anterior = this.saldo_atual;
        return this.saldo_atual;
    }
    
    /**
     * Método GET para o login da conta.
     * @return {@link String} contendo o login do usuário da Account.
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Método GET para a senha da conta.
     * @return {@link String} contendo a senha do usuário da Account.
     */
    public String getSenha() {
        return this.senha;
    }


    /**
     * Método GET para o atributo {@link Account#completou_transacao}.
     * @return {@link Boolean} contendo o valor contido no atributo.
     */
    public boolean getCompletouTransacao() {
        return this.completou_transacao;
    }
}
