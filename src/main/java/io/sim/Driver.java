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

 /**
  * Classe que simula um Motorista (Driver), cuja responsabilidade é executar as rotas através do seu {@link Car},
  * além de abastece-lo conforme necessário, utilizando sua {@link Account}.
  * 
  * @author Gustavo Henrique Tostes
  * @version 1.0
  * @since 02/10/2023
  */
public class Driver extends Thread{

    //Atributos relativos ao carro.
    private Car carro;
    private ArrayList<Route> rotas_prontas;
    private Route rota_em_execucao;
    private ArrayList<Route> rotas_executadas;
    private String ID;
    
    //Flags
    private boolean abastecendo = false;

    //Atributos relativos à Account.
    private final String senha = "driver";

    //Comunicação Cliente-Servidor
    private String IP;
    private Socket socket_cliente;
    private OutputStream os;
    private Writer writer;
    private BufferedWriter bfw;

    //----------------------------------------------------------------------------------------------------------------------
    /**
     * Classe para a realização de pagamentos aos motoristas no valor de R$3.25 por km rodado.
     */
    private class BotPayment extends Thread{
        private final String fuelStation = "FuelStation";
        private String login;
        private String senha;
        private double valor_pagamento;

        /**
         * Construtor do BotPayment.
         * @param login {@link String} contendo o login da conta de origem do pagamento.
         * @param senha {@link String} contendo senha da conta de origem do pagamento.
         * @param litros {@link Double} contendo o valor, em litros, a ser pago.
         */
        public BotPayment(String login, String senha, double litros) {
            this.login = login;
            this.senha = senha;
            this.valor_pagamento = litros*5.87;
        }

        @Override
        public void run() {
            JsonFile jsonFile = new JsonFile();
            Criptografia criptografia = new Criptografia();

            jsonFile.escreverDadosPagamento(this.login, this.senha, this.fuelStation, this.valor_pagamento);

            String json = jsonFile.getJSONAsString();

            try {
                bfw.write(criptografia.criptografa(json) +"\r\n");
                bfw.flush();
            } catch (Exception e) {
                System.out.println("Erro na escrita do Json com o pagamento para o AlphaBank.\nException: " + e);
            }
        }
    }

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

        this.rotas_executadas = new ArrayList<>();

        conectar();
    }

    @Override
    public void run() {
        this.rotas_prontas = carro.retrieveRoutes();

        try {
            for (int i = 0; i < this.rotas_prontas.size(); i++) {
                carro.getSumo().do_job_set(this.rotas_prontas.get(i).addRotaSumo());
            }
        } catch (Exception e) {
            System.out.println("Erro ao inserir as rotas no Sumo.\nException: " + e);
        }


        while (this.rotas_executadas.size() < 9) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {

            }

            carro.acionarRota(this.rotas_prontas.get(0).getIdRoute());

            this.rota_em_execucao = this.rotas_prontas.get(0);
            this.rotas_prontas.remove(0);

            //Inicializa o envio de reports do carro.
            this.carro.setTerminoURota(false);
            this.carro.startThread();

            while (!this.carro.getTerminouRota()) {
                if (this.carro.getFlag()) {
                
                    this.carro.stopThread();
                    this.carro.startThread();
                }
                verificaCombustível();
            }

            this.carro.stopThread();
            this.rotas_executadas.add(this.rota_em_execucao);
        }
        
        
    }

    /**
     * Driver verifica se o seu carro ({@link Driver#carro}) precisa abastecer e/ou se não está abastecendo.
     * Caso ambas as opções sejam verdadeiras, o driver aciona o método para que o carro seja abastecido.
     */
    private void verificaCombustível() {
        try{
            System.out.print("");
            if (carro.getFuel_Tank() <= 3 && !abastecendo) {

                this.abastecendo = true;

                double saldo = consultarSaldo();
                double litros;

                if (saldo > 58.7) {
                    litros = 10;
                } else {
                    litros = saldo/5.87;
                }

                BotPayment botPayment = new BotPayment(this.ID, this.senha, litros);
                botPayment.start();

                FuelStation fuelStation = new FuelStation(carro, litros, true);
                fuelStation.start();

                while (fuelStation.getAbastecendo()) {
                    Thread.sleep(200);
                }

                this.abastecendo = false;
            }
        } catch (Exception e) {
            System.out.println("Falha no abastecimento.\nException: " + e);
        }
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

            JsonFile jsonFile = new JsonFile();
            jsonFile.enviarConexao(this.ID, this.senha);
            String criptografa = new Criptografia().criptografa(jsonFile.getJSONAsString());

            bfw.write(criptografa +"\r\n");
            bfw.flush();
        } catch (Exception e) {
            System.out.println("Erro na conexão com o Servidor Alpha Bank.\nException: " + e);
            conectar();
        }
        
    }

    /**
     * Realiza a consulta de saldo para analisar quanto de gasolina poderá abastecer.
     */
    private double consultarSaldo() {
        JsonFile jsonFile = new JsonFile();

        jsonFile.escreveConsultaSaldo(this.ID, this.senha);

        String msgJson = jsonFile.getJSONAsString();

        Criptografia criptografia = new Criptografia();

        String msgCriptografada = criptografia.criptografa(msgJson);

        try {
            bfw.write(msgCriptografada + "\r\n");
            bfw.flush();
        } catch (Exception e) {
            System.out.println("Falha no envio de mensagem para Consulta de Saldo.\n Exception: " + e);
        }

        return recebeSaldoServer();
    }

    /**
     * Método para receber o valor do saldo contido em sua Account.
     */
    private double recebeSaldoServer() {
        try {
            Criptografia criptografia = new Criptografia();

            InputStream in = this.socket_cliente.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader bfr = new BufferedReader(inr);
            String msg = "";
            msg = bfr.readLine();

            String descriptografa = criptografia.decriptografa(msg);

            JsonFile jsonFile = new JsonFile(descriptografa);

            return jsonFile.recebeSaldo();
        } catch (Exception e) {
            System.out.println("Erro ao receber Mensagem do Servidor.\nException: " + e);
        }

        return 0;
    }

    /**
     * Método GET para as rotas Prontas relativas à esse Driver/Car.
     * @return {@link ArrayList} contendo as rotas.
     */
    public ArrayList<Route> getRotas_Prontas() {
        return rotas_prontas;
    }

    /**
     * Método GET para as rotas Executadas relativas à esse Driver/Car.
     * @return {@link ArrayList} contendo as rotas.
     */
    public ArrayList<Route> getRotas_Executadas() {
        return rotas_executadas;
    }

    /**
     * Método GET para a rota em execução relativa à esse Driver/Car.
     * @return {@link Route} contendo a rota atual.
     */
    public Route getRota_Em_Execucao() {
        return rota_em_execucao;
    }

    public void setRotas_Prontas(ArrayList<Route> rotas_prontas) {
        this.rotas_prontas = rotas_prontas;
    }
    
    //Acho que não será usado!!!!
    public void setRota_Em_Execucao(Route rota_em_execucao) {
        this.rota_em_execucao = rota_em_execucao;
    }

    //Acho que não será usado!!!!
    public void setRotas_Executadas(ArrayList<Route> rotas_executadas) {
        this.rotas_executadas = rotas_executadas;
    }
}
