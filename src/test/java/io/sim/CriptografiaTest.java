package io.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CriptografiaTest {
    /**
     * Teste para averiguar se o método de criptografar e decriptografar funcionam de maneira adequada.
     */
    @Test
    public void testeCriptografaDecriptografa() {
        String mensagem = "Teste Unitario de Criptografia";
        

        Criptografia criptografia = new Criptografia();

        String cripto = criptografia.criptografa("Teste Unitario de Criptografia");
        String decripto = criptografia.decriptografa(cripto);

        assertEquals(mensagem, decripto);

    }
}
