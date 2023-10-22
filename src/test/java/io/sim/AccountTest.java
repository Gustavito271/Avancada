package io.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccountTest {
    
    /**
     * Teste para averiguar se o depósito foi feito de maneira adequada, ou seja, somando o valor ao saldo corrente.
     */
    @Test
    public void testeDeposito() {
        double saldo_inicial = 120;
        double valor_deposito = 30;
        String resultado_esperado = (saldo_inicial + valor_deposito) + "";

        Account account = new Account("Teste Unitario", "testando", 120);
        account.deposito(30);
        String saldo = account.getSaldo_atual() + "";

        assertEquals(resultado_esperado, saldo);
    }
}
