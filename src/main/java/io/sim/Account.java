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

    public Account(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    @Override
    public void run() {
        
    }

    public String getLogin() {
        return this.login;
    }

    public String getSenha() {
        return this.senha;
    }
    
}
