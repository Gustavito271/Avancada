package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoColor;
import it.polito.appeal.traci.SumoTraciConnection;

/*
 * "Car", devendo conter as seguintes características:
 *      -> Executam as rotas da Company
 *      -> Deve ser uma Thread (CONCLUÍDO)
 *      -> Deve "herdar" a classe Vehicle (CONCLUÍDO)
 *      -> Deve possuir um atributo privado "fuelTank" (CONCLUÍDO)
 *      -> Ao iniciar, "fuelTank" deve estar associado ao valor 10 (litros) (CONCLUÍDO)
 *      -> A cada km rodado o atributo deve ser decrementado ao equivalente consumido (CONCLUÍDO ?)
 *      -> Quando estiver com 3 litros, deve ocorrer o abastecimento
 *      -> Só quem pode incrementar o "fuelTank" é a FuelStation
 */
public class Car extends Vehicle implements Runnable{
    private double fuel_tank = 10;
    private String ID;
    private SumoTraciConnection sumo;
    private String IP;

    //Parâmetros do Carro no Sumo
    private final int fuelType = 2;                 // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
    private final int fuelPreferential = 2;         // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
    private final double fuelPrice = 3.40;
    private final int personCapacity = 1;
    private final int personNumber = 1;
	private final long acquisitionRate = 500;
    private SumoColor color;

    private int numero_edges;

    private Thread thread = new Thread(this);

    //Comunicação Cliente-Servidor
    private Socket socket_cliente;
    private OutputStream os;
    private Writer writer;
    private BufferedWriter bfw;

    public Car(String ID, String IP, SumoTraciConnection sumo) {
        this.ID = ID;
        this.IP = IP;
        this.sumo = sumo;
        this.color = genColor();

        conectar();
    }

    /**
     * Conexão com o Servidor (Company).
     */
    public void conectar() {
        try {
            socket_cliente = new Socket(this.IP, Constantes.porta_Company);

            os = socket_cliente.getOutputStream();
            writer = new OutputStreamWriter(os);
            bfw = new BufferedWriter(writer);

            bfw.write("Conectou: " + this.ID + "\r\n");
            bfw.flush();

        } catch (Exception e) {
            System.out.println(this.ID + " Erro na conexão com o Servidor Company.\nException: " + e);
            conectar();
        }
        
    }

    /**
     * Método para "pegar" as rotas a serem executadas pelo Driver (owner - dono) do carro em questão.
     * @return {@link ArrayList} contendo as rotas a serem executadas pelo Driver/Car.
     */
    public ArrayList<Rota> retrieveRoutes() {
        enviarComandoServer();

        return receberComandoServer();
    }

    /**
     * Envia mensagem para o servidor para fazer a requisição das rotas a serem executadas por esse carro.
     */
    public void enviarComandoServer() {
        try {
            JsonFile jsonFile = new JsonFile();

            jsonFile.writeString("comando", "enviarRotas");
            jsonFile.writeString("carID", this.ID);

            String msgJson = jsonFile.getJSONObjectAsString();

            Criptografia criptografia = new Criptografia();

            String msgCriptografada = criptografia.criptografa(msgJson);

            bfw.write(msgCriptografada + "\r\n");
            bfw.flush();

        } catch (Exception e) {
            System.out.println("Erro ao enviar Mensagem para o Servidor.\nException: " + e);
        }
        
    }

    /**
     * Recebe a mensagem do servidor contendo os dados necessários para a construção de uma Rota ({@link Rota}).
     * @return {@link ArrayList}<Rota> contendo as rotas "resgatadas" de Company.
     */
    private ArrayList<Rota> receberComandoServer() {
        ArrayList<Rota> routes = new ArrayList<>();

        try {
            Criptografia criptografia = new Criptografia();

            InputStream in = this.socket_cliente.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader bfr = new BufferedReader(inr);
            String msg = "";
            msg = bfr.readLine();

            String descriptografa = criptografia.decriptografa(msg);

            JsonFile jsonFile = new JsonFile(descriptografa);

            routes = jsonFile.getRoutesFromJSONArray();

        } catch (Exception e) {
            System.out.println("Erro ao receber Mensagem do Servidor.\nException: " + e);
        }

        return routes;
        
    }

    /**
     * Inicia a execução da thread.
     */
    public void startThread() {
        thread.start();
    }

    @Override
    public void run() {

        
        enviarRelatorio();
        try {
            while(true) {
                //System.out.println(sumo.do_job_get(super.getRouteIndex(ID)));
                System.out.println(sumo.do_job_get(super.getDistance(ID)));
                Thread.sleep(500);
            }
            
        } catch (Exception e){

        }
        
        /*try {
            double fuel_consumption = (double) sumo.do_job_get(super.getFuelConsumption(ID))/1000;
            fuel_tank -= fuel_consumption;
        } catch (Exception exception) {
            System.out.print("Exception: " + exception);
        }*/
        

        if (fuel_tank <= 3) {

        }


    }

    private void enviarRelatorio() {
        JsonFile jsonFile = new JsonFile();

        jsonFile.writeString("comando", "relatorio");
        jsonFile.writeString(Constantes.KEY_ID_CAR, this.ID);
        jsonFile.writeString(Constantes.KEY_ID_CAR, this.ID);
    }

    /**
     * Método para acionar a Rota atual do carro no Sumo.
     * @param idRoute {@link String} contendo a ID da rota a ser executada.
     */
    public void acionarRota(String idRoute) {
        try {
            sumo.do_job_set(super.addFull(this.ID,
                                            idRoute,
                                            "DEFAULT_VEHTYPE",
                                            "now", 								//depart  
                                            "0", 								//departLane 
                                            "0", 								//departPos 
                                            "0",								//departSpeed
                                            "current",							//arrivalLane 
                                            "max",								//arrivalPos 
                                            "current",							//arrivalSpeed 
                                            "",									//fromTaz 
                                            "",									//toTaz 
                                            "", 								//line 
                                            this.personCapacity,
                                            this.personNumber
                                            ));

            sumo.do_job_set(super.setColor(this.ID, this.color));

        } catch (Exception e) {
            System.out.println("Erro ao iniciar a Rota para o Carro " + this.ID + ".\nException: " + e);
        }
    }

    /**
     * Gera uma cor "aleatória" para o Carro a ser inserido.
     * @return {@link SumoColor} contendo a cor do carro a ser "utilizada.
     */
    private SumoColor genColor() {
        Random random = new Random();

        int color1 = random.nextInt(255);
        int color2 = random.nextInt(255);
        int color3 = random.nextInt(255);

        return new SumoColor(color1, color2, color3, 126);
    }

    /**
     * Método GET para o atributo {@link Car#sumo}.
     * @return {@link SumoTraciConnection} contendo o objeto para manipulação.
     */
    public SumoTraciConnection getSumo() {
        return this.sumo;
    }
    
    /**
     * Método GET para o atributo {@link Car#fuel_tank}.
     * @return {@link Double} contendo o valor contido no atributo.
     */
    public double getFuel_Tank() {
        return fuel_tank;
    }

    /**
     * Método SET para o atributo {@link Car#fuel_tank}.
     * @param fuel_tank {@link Double} contendo o valor a ser inserido no atributo.
     */
    public void setFuel_Tank(double fuel_tank) {
        this.fuel_tank = fuel_tank;
    }

    /**
     * Método SET para o atributo {@link Car#numero_edges}.
     * @param numero_edges {@link Integer} contendo o valor a ser inserido no atributo.
     */
    public void setNumEdges(int numero_edges) {
        this.numero_edges = numero_edges;
    }
}
