package io.sim;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONObject;

import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

/*
 * "Driver", devendo possuir as seguintes características:
 *      -> Deve ser uma Thread (CONCLUÍDO)
 *      -> Deve ser um Client para o AlphaBank (CONCLUÍDO)
 *      -> Deve conter um Car como atributo (CONCLUÍDO)
 *      -> Deve conter um ArrayList<Route> de rotas a serem executadas (CONCLUÍDO)
 *      -> Deve conter um objeto de Route de rota em andamento (CONCLUÍDO)
 *      -> Deve conter um ArrayList<Route> de rotas executadas (CONCLUÍDO)
 *      -> Acessos aos atributos controlados por métodos (CONCLUÍDO)
 * 
 *      -> Deve ter uma conta (Account) no AlphaBank
 *      -> Deve conter uma classe (interna) BotPayment para ABASTECER
 *      -> Preço a se pagar para a FuelStation (posto): R$5.87
 */

public class Driver extends Thread{

    private Car carro;
    private ArrayList<Rota> rotas_prontas;
    private Rota rota_em_execucao;
    private ArrayList<Rota> rotas_executadas;

    private String IP;

    private String ID;
    
    //Comunicação Cliente-Servidor
    private Socket socket_cliente;
    private OutputStream os;
    private Writer writer;
    private BufferedWriter bfw;

    /**
     * Construtor da classe em questão.
     * @param ID {@link String} contendo o ID do carro pertencente ao motorista (atribuído com base na ordem de criação).
     * @param IP {@link String} contendo o endereço de IP associado ao Driver (DRIVERS contém o endereço "padrão" 127.0.1.x).
     * @param carro {@link Car} contendo o objeto pertencente à classe.
     */
    public Driver(String ID, String IP, Car carro) {
        this.IP = IP;
        this.ID = ID;
        this.carro = carro;

        conectar();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {

        }
        this.rotas_prontas = carro.retrieveRoutes();
        System.out.println(rotas_prontas.get(0).getIdRoute());

        // try {
        //     for (int i = 0; i < this.rotas_prontas.size(); i++) {
        //         this.carro.getSumo().do_job_set(this.rotas_prontas.get(i).addRotaSumo());
        //     }
        // } catch (Exception e) {
        //     System.out.println("Erro ao inserir as rotas no Sumo.\nException: " + e);
        // }
    }


    /**
     * Conexão com o Servidor (AlphaBank)
     */
    public void conectar() {
        try {
            int porta_AlphaBank = Constantes.porta_AlphaBank;

            socket_cliente = new Socket(this.IP, porta_AlphaBank);

            os = socket_cliente.getOutputStream();
            writer = new OutputStreamWriter(os);
            bfw = new BufferedWriter(writer);
            bfw.write("Conectou: " + this.ID +"\r\n");
            bfw.flush();
        } catch (Exception e) {
            System.out.println("Erro na conexão com o Servidor Alpha Bank.\nException: " + e);
            conectar();
        }
        
    }

    public ArrayList<Rota> getRotas_Prontas() {
        return rotas_prontas;
    }

    public ArrayList<Rota> getRotas_Executadas() {
        return rotas_executadas;
    }

    public Rota getRota_Em_Execucao() {
        return rota_em_execucao;
    }

    public void setRotas_Prontas(ArrayList<Rota> rotas_prontas) {
        this.rotas_prontas = rotas_prontas;
    }
    
    //Acho que não será usado!!!!
    public void setRota_Em_Execucao(Rota rota_em_execucao) {
        this.rota_em_execucao = rota_em_execucao;
    }

    //Acho que não será usado!!!!
    public void setRotas_Executadas(ArrayList<Rota> rotas_executadas) {
        this.rotas_executadas = rotas_executadas;
    }
}
