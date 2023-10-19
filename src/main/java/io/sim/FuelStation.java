package io.sim;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

/*
 * "Fuel Station", devendo conter as seguintes características:
 *      -> Deve ser uma Thread (CONCLUÍDO)
 *      -> Deve ser cliente do AlphaBank
 *      -> Deve possuir uma conta no AlphaBank
 *      -> Deve existir, no máximo, duas bombas de abastecimento
 *      -> Deve incrementar o atributo "FuelTank" do Car
 *      -> O processo de abastecer leva 2 minutos.
 */
public class FuelStation extends Thread {

    //Comunicação Cliente/Servidor
    private static Socket socket;
    private static OutputStream ou ;
    private static Writer ouw;
    private static BufferedWriter bfw;

    private static final String login = "FuelStation";
    private static final String senha = "fuel_station";

    //Número de bombas ocupadas, sendo que o máximo de bombas é 2
    private static int bombas_ocupadas = 0;

    //Objeto do carro cujo abastecimento está sendo realizado.
    Car carro_abastecendo;

    //Quantidade de litros a ser abastecida.
    private double litros_abastecer;

    //Flag para saber se o processo de abastecer terminou!
    private boolean abastecendo;

    //-----------------------------------------------------------------------------------------------------------
    /**
     * Construtor da FuelStation, para utilização de sua Thread.
     * @param carro_abastecendo {@link Car} sendo o carro cujo abastecimento será realizado.
     * @param litros_abastecer {@link Double} contendo o valor, em litros, a ser abastecido no carro.
     */
    public FuelStation(Car carro_abastecendo, double litros_abastecer, boolean abastecendo) {
        this.carro_abastecendo = carro_abastecendo;
        this.litros_abastecer = litros_abastecer;
        this.abastecendo = abastecendo;
    }

    //Como os objetos são passados por referencia, tentar passar o carro pra fuel station!! 
    //Além disso, colocar a conexão da account por referencia.
    @Override
    public synchronized void run() {
        try {
            while (bombas_ocupadas == 2) {
                wait();
            }

            System.out.println("veio aqui....");

            bombas_ocupadas++;

            //Duração do abastecimento.
            Thread.sleep(120000);

            carro_abastecendo.abastecerFuelTank(this.litros_abastecer);

            bombas_ocupadas--;

            this.abastecendo = false;

            System.out.println("abasteceu....");

            notify();
        } catch (Exception e) {
            System.out.println("Falha no abastecimento do veículo");
        }
        
    }

    public boolean getAbastecendo() {
        return this.abastecendo;
    }

    public static void main(String[] args) {
        conectar();
    }

    /**
     * Método para se conectar ao Servidor Alpha Bank, já que o posto de gasolina (FuelStation) tem uma conta lá.
     */
    public static void conectar() {
        try {
            String IP = Constantes.IP_FUEL_STATION; 

            socket = new Socket(IP, Constantes.porta_AlphaBank);
            ou = socket.getOutputStream();
            ouw = new OutputStreamWriter(ou);
            bfw = new BufferedWriter(ouw);
            bfw.write(login + " " + senha + "\r\n");
            bfw.flush();

        } catch (Exception e) {
            System.out.println("1Erro na conexão com o Servidor Alpha Bank.\nException: " + e);
            conectar();
        }
        
    }
    
}
