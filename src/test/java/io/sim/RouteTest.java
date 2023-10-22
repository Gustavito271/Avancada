package io.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RouteTest {
    
    /**
     * Teste para averiguar se o método de retornar o ID da rota está adequado.
     */
    @Test
    public void testeIDRoute() {
        String ID = "Teste Unitario";

        Rota rota = new Rota(ID, null);

        String resultado = rota.getIdRoute();

        assertEquals(ID, resultado);
    }
}
