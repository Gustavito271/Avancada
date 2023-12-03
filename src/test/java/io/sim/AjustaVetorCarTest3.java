package io.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AjustaVetorCarTest3 {
    /**
     * Teste para verificar o funcionamento do método ajustarVetor, para o caso de o parâmetro ser uma matriz de double.
     */
    @Test
    public void testeAjusteMatriz() {
        double[][] matriz = new double[][] {{1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};    //Novo
        double[][] res = new double[][] {{1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};

        Car car = new Car();

        double[][] resultado = car.ajustaVetor(matriz);

        for (int i = 0; i < res.length; i++) {
            assertEquals(res[0][i], resultado[0][i], 0.01);
        }
    }
}
