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
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Classe cujo objetivo é simular um Carro (Car), sendo que é responsável pela, de fato, execução de rotas,
 * as quais são de possde da {@link Company}, além de enviar relatórios para a mesma com detalhes de execução.
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 02/10/2023
 */
public class Car extends Vehicle implements Runnable{

    //Parâmetros do Carro no Sumo
    private SumoTraciConnection sumo;
    private final int fuelType = 2;                 // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
    private final int personCapacity = 1;
    private final int personNumber = 1;
	private final long acquisitionRate = 500;
    private String tipo_combustivel;
    private SumoColor color;

    //Parâmetros do Carro como objeto.
    private String ID;
    private double fuel_tank = 10;
    // private double fuel_tank = 3.01;                 //Novo
    private double distancia_percorrida;
    private boolean terminou_rota = false;

    //Parâmetros para Reconciliação de Dados.
    private double last_distance;               //Novo
    private long last_time = 0;                 //Novo
    private double[] y = new double[] {760, 51.05, 24.15, 31.98, 32.54, 31.84, 194.38, 32.32, 27.40,
                                            153.63, 33.0, 43.83, 26.14, 38.51, 44.76, 64.32, 53.90};                //Novo
    private double[] v = new double[] {0.5, 0.5719, 3.0751, 6.5083, 6.8557, 3.0186, 2545.3587, 11.7552, 17.1524,
                                          23.1312, 1.0435, 9.5197, 1.2204, 4.5040, 2.9270, 25.4682, 34.9596};       //Novo
    private double[][] A = new double[][] {{1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};    //Novo
    double speed = 0;                           //Novo

    //Objeto da Thread a ser executada (devido ao implements utilizado)
    private Thread thread = new Thread(this);

    //Comunicação Cliente-Servidor
    private Socket socket_cliente;
    private OutputStream os;
    private Writer writer;
    private BufferedWriter bfw;
    private String IP;

    private boolean flag_try_catch = false;

    //-------------------------------------------------------------------------------------------------------

    /**
     * Construtor da classe.
     * @param ID {@link String} contendo o ID do carro.
     * @param IP {@link String} contendo o endereço de IP para conexão com o servidor ({@link Company}).
     * @param sumo {@link SumoTraciConnection} utilizado para realizar operações com o Sumo.
     */
    public Car(String ID, String IP, SumoTraciConnection sumo) {
        this.ID = ID;
        this.IP = IP;
        this.sumo = sumo;
        this.color = genColor();
        this.tipo_combustivel = combustivel();

        this.distancia_percorrida = 0;                  //Novo
        this.last_distance = 0;                         //Novo

        conectar();
    }

    /**
     * Construtor default para testes.
     */
    public Car() {

    }

    /**
     * Conexão com o Servidor (Company).
     */
    public void conectar() {
        try {
            this.socket_cliente = new Socket(this.IP, Constantes.porta_Company);

            this.os = socket_cliente.getOutputStream();
            this.writer = new OutputStreamWriter(os);
            this.bfw = new BufferedWriter(writer);

            JsonFile jsonFile = new JsonFile();
            jsonFile.enviarConexao("Conectou: ", this.ID);
            String criptografa = new Criptografia().criptografa(jsonFile.getJSONAsString());

            this.bfw.write(criptografa + "\r\n");
            this.bfw.flush();

        } catch (Exception e) {
            System.out.println(this.ID + " Erro na conexão com o Servidor Company.\nException: " + e);
            conectar();
        }
        
    }

    /**
     * Método para "pegar" as rotas a serem executadas pelo Driver (owner - dono) do carro em questão.
     * @return {@link ArrayList} contendo as rotas a serem executadas pelo Driver/Car.
     */
    public ArrayList<Route> retrieveRoutes() {
        enviarPegarRotasServer();

        return receberPegarRotasServerServer();
    }

    /**
     * Envia mensagem para o servidor para fazer a requisição das rotas a serem executadas por esse carro.
     */
    public void enviarPegarRotasServer() {
        try {
            JsonFile jsonFile = new JsonFile();

            jsonFile.escreverRequestRoutes(this.ID);

            String msgJson = jsonFile.getJSONAsString();

            Criptografia criptografia = new Criptografia();

            String msgCriptografada = criptografia.criptografa(msgJson);

            this.bfw.write(msgCriptografada + "\r\n");
            this.bfw.flush();

        } catch (Exception e) {
            System.out.println("Erro ao enviar Mensagem para o Servidor.\nException: " + e);
        }
        
    }

    /**
     * Recebe a mensagem do servidor contendo os dados necessários para a construção de uma Rota ({@link Route}).
     * @return {@link ArrayList}<Rota> contendo as rotas "resgatadas" de Company.
     */
    private ArrayList<Route> receberPegarRotasServerServer() {
        ArrayList<Route> routes = new ArrayList<>();

        try {
            Criptografia criptografia = new Criptografia();

            InputStream in = this.socket_cliente.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader bfr = new BufferedReader(inr);
            String msg = bfr.readLine();

            String descriptografa = criptografia.decriptografa(msg);

            JsonFile jsonFile = new JsonFile(descriptografa);

            routes = jsonFile.recebeRoutes();

        } catch (Exception e) {
            System.out.println("Erro ao receber Mensagem do Servidor.\nException: " + e);
        }

        return routes;
        
    }

    /**
     * Inicia a execução da thread.
     */
    public void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Para a exeecução da thread.
     */
    public void stopThread() {
        thread.interrupt();
    }

    /**
     * Execução da thread, contendo a captação de dados do veículo (e envio para o Servidor (Company)), além de
     * verificaçã de combustível
     */
    @Override
    public void run() {
        // System.out.println("Iniciou a execução " + this.ID + " : " + System.nanoTime());        //Novo
        try {
            boolean is_stopped = false;
            boolean flag = true, carregou_carros = false;

            SumoStringList arrayList = (SumoStringList) sumo.do_job_get(super.getIDList());

            while (!carregou_carros) {
                if (arrayList.size() != 0 && arrayList.contains(this.ID)) {
                    carregou_carros = true;
                }

                arrayList = (SumoStringList) sumo.do_job_get(super.getIDList());
            }

            while (flag) {
                //if (arrayList.size() != 0) {
                    arrayList = (SumoStringList) sumo.do_job_get(super.getIDList());

                    if (arrayList.size() != 0 && arrayList.contains(this.ID)) {
                        
                        if (last_time == 0) {
                            last_time = System.nanoTime();
                        }

                        double consumo = converteConsumo((double) sumo.do_job_get(super.getFuelConsumption(ID)), is_stopped);

                        enviarRelatorio(consumo, is_stopped);

                        this.fuel_tank -= consumo;

                        //System.out.println("Gasolina: " + this.fuel_tank);

                        if (fuel_tank <= 3) {
                            sumo.do_job_set(super.setSpeed(ID, 0));
                            if ((double) sumo.do_job_get(super.getSpeed(ID)) == 0) {
                                is_stopped = true;
                            }
                        } else {
                            if (is_stopped) {
                                is_stopped = false;
                                sumo.do_job_set(super.setSpeed(ID, 20.60));
                            }
                        }

                        //Atualiza a edge atual.
                        //edge_atual = (Integer) sumo.do_job_get(super.getRouteIndex(ID));
                        Thread.sleep(acquisitionRate);
                    } else {
                        flag = false;
                    }
                //}            
            }

            // System.out.println(distancia_percorrida);
        } catch (Exception e) {
            flag_try_catch = true;
        }

        terminou_rota = true;
        // System.out.println("Fim da Thread " + this.ID + " : " + System.nanoTime());         //Novo
    }


    /**
     * Método para envio do relatório, contendo os dados relativos ao desempenho do carro, sendo eles:
     * <ol>
     *      <li> Timestamp em nanosegundos </li>
     *      <li> ID do carro </li>
     *      <li> ID da rota sendo executada </li>
     *      <li> Distância percorrida </li>
     *      <li> Consumo de Combustível, em Litros</li>
     *      <li> Tipo de combustível utilizado (Gasolina) </li>
     *      <li> Emissão de CO2 </li>
     *      <li> Latitude (Coordenada x)</li>
     *      <li> Longitude (Coordenada y)</li>
     * </ol>
     * @param consumo {@link Double} contendo o valor do consumo de combustível, em Litros.
     * @param is_stopped {@link Boolean} para saber se o veículo está parado (abastecendo) ou não.
     */
    private void enviarRelatorio(double consumo, boolean is_stopped) {
        JsonFile jsonFile = new JsonFile();

        double latitude = 0, longitude = 0;
        try {
            SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(super.getPosition(ID));
            latitude = sumoPosition2D.x;
            longitude = sumoPosition2D.y;

            double dist = (double) sumo.do_job_get(super.getDistance(ID));
            if (dist >= 0) {
                distancia_percorrida = calculaDistancia(dist);
            }

            long tempo = System.nanoTime();                                 //Novo

            if (distancia_percorrida - last_distance >= 1) {                //Novo
                last_distance = distancia_percorrida;                       //Novo
                sumo.do_job_set(super.setSpeed(ID, reconcilia(tempo)));     //Novo
            }                                                               //Novo

            if (speed != 0) {                                               //Novo
                sumo.do_job_set(super.setSpeed(ID, speed));                 //Novo
            }                                                               //Novo

            jsonFile.escreverRelatorio(this.ID, 
                                        (String) sumo.do_job_get(super.getRouteID(ID)),
                                        (double) sumo.do_job_get(super.getSpeed(ID)), 
                                        distancia_percorrida, 
                                        consumo,
                                        tipo_combustivel,
                                        (double) sumo.do_job_get(super.getCO2Emission(ID)),
                                        latitude, longitude,
                                        tempo);

            String json = jsonFile.getJSONAsString();
            Criptografia criptografia = new Criptografia();

            try {
                bfw.write(criptografia.criptografa(json) +"\r\n");
                bfw.flush();
            } catch (Exception e) {
                System.out.println("Erro na escrita do relatório.\nException: " + e);
            }

        } catch (Exception e) {
            System.out.println(ID + " Erro ao captar os dados do veículo.\nException: " + e);
        }
    }

    /**
     * Calcula a distância percorrida, em km, dado que o comando utilizado retorna a distância percorrida
     * durante o timestep, por assim dizer.
     * @param dist {@link Double} contendo a distância percorrida durante o último timestep em dm (decâmetro)
     * @return {@link Double} contendo a distância total percorrida pelo veículo.
     */
    public double calculaDistancia(double dist) {
        double dist_metros = dist/100;

        return dist_metros;
    }

    /**
     * Converte o consumo de combustível, sendo que a medida é dada em mg/s. Leva-se em consideração
     * o tempo de aquisição ({@link Car#acquisitionRate}).
     * @param consumo {@link Double} contendo o consumo de combustível em mg/s
     * @return {@link Double} contendo o valor consumido em Litros.
     */
    private double converteConsumo(double consumo, boolean is_stopped) {
        int densidade = 770000;            // Densidade da gasolina: 770 g/L (ou 770000 mg/L)

        if (consumo >= 0 && !is_stopped) {
            return (consumo*acquisitionRate)/(densidade*1000);
        } else {
            return 0;
        } 
    }

    /**
     * Tipo de combustível do carro, considerando o número atribuído ao atributo {@link Car#fuelType}.
     * @return {@link String} contendo o tipo de combustível
     */
    private String combustivel() {
        if (this.fuelType == 1) {
            return "Diesel";
        } else if (this.fuelType == 2) {
            return "Gasolina";
        } else if (this.fuelType == 3) {
            return "Etanol";
        } else {
            return "Híbrido";
        }
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
     * Método GET para o atributo {@link Car#terminou_rota}.
     * @return {@link Boolean} contendo o valor lógico do atributo.
     */
    public boolean getTerminouRota() {
        return this.terminou_rota;
    }

    public boolean getFlag() {
        return this.flag_try_catch;
    }

    public String getID() {
        return this.ID;
    }

    /**
     * Método SET para o atributo {@link Car#terminou_rota}.
     * @param terminou_rota {@link Boolean} contendo o valor lógico a ser inserido no atributo.
     */
    public void setTerminoURota(boolean terminou_rota) {
        this.terminou_rota = terminou_rota;
    }

    /**
     * Método SET para o atributo {@link Car#fuel_tank}.
     * @param fuel_tank {@link Double} contendo o valor a ser inserido no atributo.
     */
    public void abastecerFuelTank(double fuel_tank) {
        this.fuel_tank += fuel_tank;
    }

    //----------------------------------- Novo ----------------------------------------

    /**
     * Realiza a reconciliação dos dados baseado no trecho percorrido de 1km.
     * @param tempo {@link Long} contendo o tempo entre a medição anterior/inicial e a atual.
     * @return {@link Double} contendo a velocidade a ser utilizada pelo carro.
     */
    private double reconcilia(long tempo) {
        long auxT = (tempo - last_time)/1000000000;

        y = ajustaVetor(y, auxT);
        v = ajustaVetor(v);
        A = ajustaVetor(A);

        Reconciliation rec = new Reconciliation(y, v, A);
        double[] res = rec.getReconciledFlow();

        // System.out.println("Tempo atual: " + tempo/1000000000.00);
        // System.out.println("Tempo anterior: " + last_time/1000000000.00);
        // System.out.println("Tempo resultante: " + res[0] + " " + res[1]);
        System.out.println("Velocidade sugerida: " + 1000/res[1]);

        y = res;

        last_time = tempo;

        ExportaExcel excel = new ExportaExcel();
        excel.escreveRecon(res);

        return 1000/res[1];
    }

    /**
     * Ajusta o vetor y, considerando o tempo percorrido e, consequentemente, o restante.
     * @param vetor {@link Double[]} contendo o vetor a ser ajustado.
     * @param tempo {@link Long} contendo o tempo passado.
     * @return {@link Double[]} contendo o vetor ajustado (com uma posição a menos).
     */
    public double[] ajustaVetor(double[] vetor, long tempo) {
        int tamN = vetor.length - 1;
        double[] aux = new double[tamN];

        aux[0] = vetor[0] - tempo;

        for (int i = 1; i < tamN; i++) {
            aux[i] = vetor[i+1];
        }

        if (tamN >= 2) {
            aux[1] += (vetor[1] - tempo);
        }

        vetor = null;
        
        return aux;
    }

    /**
     * Ajusta o vetor, basicamente excluindo a posição 1.
     * @param vetor {@link Double[]} contendo o vetor a ser ajustado.
     * @return {@link Double[]} contendo o vetor ajustado (com uma posição a menos).
     */
    public double[] ajustaVetor(double[] vetor) {
        int tamN = vetor.length - 1;
        double[] aux = new double[tamN];

        aux[0] = vetor[0];

        for (int i = 1; i < tamN; i++) {
            aux[i] = vetor[i+1];
        }

        vetor = null;
        
        return aux;
    }

    /**
     * Ajusta a matriz, basicamente excluindo a posição [0][1].
     * @param vetor {@link Double[][]} contendo a matriz a ser ajustada.
     * @return {@link Double[][]} contendo a matriz ajustada (com uma posição a menos).
     */
    public double[][] ajustaVetor(double[][] vetor) {
        int tamN = vetor[0].length - 1;
        double[][] aux = new double[1][tamN];

        aux[0][0] = vetor[0][0];

        for (int i = 1; i < tamN; i++) {
            aux[0][i] = vetor[0][i+1];
        }

        vetor = null;
        
        return aux;
    }
}
