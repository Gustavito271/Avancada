package io.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AjustaVetorCarTest2 {
    /**
     * Teste para verificar o funcionamento do método ajustarVetor, quando a entrada é apenas um array de double.
     */
    @Test
    public void testeAjusteVetor() {
        double[] vetor = new double[] {760, 51.05, 24.15, 31.98, 32.54, 31.84, 194.38, 32.32, 27.40,
            153.63, 33.0, 43.83, 26.14, 38.51, 44.76, 64.32, 53.90};
        double[] res = new double[] {760, 24.15, 31.98, 32.54, 31.84, 194.38, 32.32, 27.40,
            153.63, 33.0, 43.83, 26.14, 38.51, 44.76, 64.32, 53.90};

        Car car = new Car();

        double[] resultado = car.ajustaVetor(vetor);

        for (int i = 0; i < res.length; i++) {
            assertEquals(res[i], resultado[i], 0.01);
        }
    }
}
