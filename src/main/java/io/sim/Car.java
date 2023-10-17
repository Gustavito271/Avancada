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

import org.json.JSONArray;
import org.json.JSONObject;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
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
 *      -> 
 */
public class Car extends Vehicle implements Runnable{
    private double fuel_tank = 10;
    private String ID;
    private SumoTraciConnection sumo;
    private String IP;

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

        conectar();
    }

    /**
     * Conexão com o Servidor (Company)
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
     * Inicia a execução da thread do Car
     */
    public void startThread() {
        thread.start();
    }

    @Override
    public void run() {

        try {
            Thread.sleep(200);
        } catch (Exception e) {

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

    public SumoTraciConnection getSumo() {
        return this.sumo;
    }
    
    public double getFuel_Tank() {
        return fuel_tank;
    }

    public void setFuel_Tank(double fuel_tank) {
        this.fuel_tank = fuel_tank;
    }
}
