package io.sim;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

/**
 * Classe que visa simular o funcionamento de um Posto de Gasolina (Fuel Station), sendo responsável por
 * abastecer os veículos que encontrem-se com menos de 3L de combustível. Entretanto, há apenas duas
 * bombas disponíveis, sendo que se amabs estiverem ocupadas o carro deverá aguardar.
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 18/10/2023
 */
public class FuelStation implements Runnable{

    //Comunicação Cliente/Servidor
    private static Socket socket;
    private static OutputStream ou ;
    private static Writer ouw;
    private static BufferedWriter bfw;

    //Relativo ao acesso da Account.
    private static final String login = "FuelStation";
    private static final String senha = "fuel_station";

    //"Flag" para a bomba 1
    private static boolean bomba1_ocupada = false;

    //Flag para a bomba 2
    private static boolean bomba2_ocupada = false;

    //Contem a bomba usada pelo carro que está tentando ser abastecido
    private int bomba_usada;

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

    @Override
    public void run() {
        try {
            System.out.println(this.carro_abastecendo.getID() + " Abastecendo...");

            //Duração do abastecimento.
            Thread.sleep(120000);

            carro_abastecendo.abastecerFuelTank(this.litros_abastecer);

            this.abastecendo = false;

            System.out.println("Deixando o posto...");
            
            if (this.bomba_usada == 1) {
                bomba1_ocupada = false;
            } else {
                bomba2_ocupada = false;
            }
        } catch (Exception e) {
            System.out.println("Falha no abastecimento do veículo");
        }
    }

    /**
     * Verifica se é possível abastecer, ou seja, se alguma das duas bombas estão livres.
     * Caso positivo, retorna o número da bomba que pode ser utilizada (1 ou 2). Caso ambas
     * as bombas estejam ocupadas, retorna 0.
     * @return {@link Integer} contendo o número da bomba a ser utilizada // 0 caso as bombas estejam ocupadas.
     */
    public synchronized static int tentarAbastecer() {
        if (!bomba1_ocupada) {
            bomba1_ocupada = true;
            return 1;
        } else if (!bomba2_ocupada) {
            bomba2_ocupada = true;
            return 2;
        } else {
            return 0;
        }
    }
   

    /**
     * Inicia a execução da Thread para executar a função do abastecimento.
     * @param num {@link Integer} contendo o número da bomba a ser utilizada.
     */
    public void iniciarThread(int num) {
        Thread thread = new Thread(this);
        this.bomba_usada = num;
        thread.start();
    }

    /**
     * Método GET para o atributo {@link FuelStation#abastecendo}.
     * @return {@link Boolean} indicando o valor contido no atributo.
     */
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

            JsonFile jsonFile = new JsonFile();
            jsonFile.enviarConexao(login, senha);
            String criptografa = new Criptografia().criptografa(jsonFile.getJSONAsString());

            bfw.write(criptografa + "\r\n");
            bfw.flush();

        } catch (Exception e) {
            System.out.println("Erro na conexão com o Servidor Alpha Bank. Conectando novamente...");
            conectar();
        }
        
    }
}
