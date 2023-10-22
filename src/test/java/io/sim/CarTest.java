package io.sim;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CarTest {
    /**
     * Teste para averiguar se o método de converter a distância de decâmetro para quilomêtro está adequado.
     */
    @Test
    public void testeCalculoDistancia() {
        double distancia_decametros = 1500;
        String distancia_km = (distancia_decametros/100) + "";

        Car car = new Car();

        String resultado = car.calculaDistancia(distancia_decametros) + "";

        assertEquals(distancia_km, resultado);
    }
}
