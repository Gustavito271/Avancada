package io.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JsonFileTest {
    /**
     * Teste para averiguar se o método de receber os dados de conexão, utilizando o arquivo JSON, está adequado.
     */
    @Test
    public void testeReceberEnviarConexao() {
        String login = "Teste Unitario";
        String senha = "testando";

        JsonFile jsonFile = new JsonFile();
        jsonFile.enviarConexao(login, senha);

        String[] respostas = jsonFile.receberConexao();

        assertEquals(login, respostas[0]);

        assertEquals(senha, respostas[1]);
    }
}
