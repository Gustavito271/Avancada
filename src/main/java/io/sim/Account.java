package io.sim;

/*
 * "Account", devendo possuir as seguintes características
 *      -> Cada Account deve possuir Login e Senha (CONCLUÍDO)
 *      -> 
 */
public class Account extends Thread{ 

    //Parâmetros de acesso à conta.
    private String login;
    private String senha;

    private double saldo;

    public Account(String login, String senha, double saldo) {
        this.login = login;
        this.senha = senha;
        this.saldo = saldo;
    }

    @Override
    public void run() {
        
    }

    /**
     * Representa o saque de dinheiro de uma Account.
     * @param valor {@link Double} contendo o valor a ser sacado.
     */
    public void saque(double valor) {
        this.saldo -= valor;
    }

    /**
     * Representa o depósito de dinheiro em uma Account.
     * @param valor {@link Double} contendo o valor a ser depositado.
     */
    public void deposito(double valor) {
        this.saldo += valor;
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
     * Método GET para o saldo da conta.
     * @return {@link Double} contendo o valor do saldo da Account.
     */
    public double getSaldo() {
        return this.saldo;
    }
}
