package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Classe que simula o funcionamento de uma Empresa (Company), sendo responsável por gerir um conjunto
 * de rotas, bem como realizar pagamento aos motoristas (Driver) por km rodado.
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 02/10/2023
 */
public class Company extends Thread {
    //Conjunto de rotas: Prontas para serem executas // Em execução // Já foram executadas
    private static ArrayList<Rota> rotas_prontas;
    private static ArrayList<Rota> rotas_em_execucao;
    private static ArrayList<Rota> rotas_executadas;
    private String idRotaAtual = "";

    //Parâmetros de acesso à Account.
    private static final String login = "Company";
    private static final String senha = "company";

    //Arquivo contendo as rotas.
    private static String arquivo = "sim/data/dados2.xml";

    //Comunicação Cliente-Servidor
    private static ServerSocket server;
    private Socket socket;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;
    private static Socket socket_client;
    private static OutputStream ou ;
    private static Writer ouw;
    private static BufferedWriter bfw;

    private double distancia_paga = 0;

    //------------------------------------------------------------------------------------------------------
    
    /**
     * Classe para a realização de pagamentos aos motoristas no valor de R$3.25 por km rodado.
     */
    private class BotPayment extends Thread{
        private final double valor_pagamento = 3.25;
        private String driverID;
        private String login;
        private String senha;

        public BotPayment(String driverID, String login, String senha) {
            this.driverID = driverID;
            this.login = login;
            this.senha = senha;
        }

        @Override
        public void run() {
            JsonFile jsonFile = new JsonFile();
            Criptografia criptografia = new Criptografia();

            jsonFile.escreverDadosPagamento(this.login, this.senha, this.driverID, this.valor_pagamento);

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
     * Construtor para a classe Company, a ser utilizada como uma Thread e Server.
     * 
     * @param socket {@link Socket} contendo o meio de comunicação cliente/servidor.
     * (Definição: <a href="https://www.devmedia.com.br/como-criar-um-chat-multithread-com-socket-em-java/33639">
     * Site indicado pelo professor</a>).
     */
    public Company(Socket socket) {

        this.socket = socket;
        try {
            in  = socket.getInputStream();
            inr = new InputStreamReader(in);
            bfr = new BufferedReader(inr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método Run() - Thread.
     */
    @Override
    public void run() {

        try{
            String msg;
            OutputStream ou =  this.socket.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            //msg = bfr.readLine();
            msg = "inicio";

            //System.out.println(msg);
            //Thread.sleep(300);

            while (msg != null) {
                msg = bfr.readLine();

                Criptografia criptografia = new Criptografia();
                String decriptografa = criptografia.decriptografa(msg);
                JsonFile jsonFile = new JsonFile(decriptografa);

                String comando = jsonFile.getComando();

                if (comando.equals(Constantes.comando_rotas)) {
                    enviarRotas(jsonFile, bfw);
                } else if (comando.equals(Constantes.comando_relatorio)) {
                    relatorioCarro(jsonFile);
                } else if (comando.equals(Constantes.comando_conexao)) {
                    Thread.sleep(300);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recebe o relatório de um carro e faz as operações necessárias a partir das informações enviadas
     * pelo mesmo.
     * @param jsonFile {@link JsonFile} contendo o objeto a ser manipulado para a retirada de informações.
     */
    private void relatorioCarro(JsonFile jsonFile) {
        ArrayList<Object> arrayList = jsonFile.pegarRelatorio();

        String idCarro = (String) arrayList.get(1);
        String idRota = (String) arrayList.get(2);
        double distancia = convertNumero(arrayList.get(4));

        if (!this.idRotaAtual.equals(idRota)) {
            if (!this.idRotaAtual.equals("")) {
                rotas_executadas.add(searchRouteEmExecucao(this.idRotaAtual));
            }
            rotas_em_execucao.add(searchRoutePronta(idRota));
            this.idRotaAtual = idRota;
        }

        //System.out.println(distancia + "//" + distancia_paga);

        if (distancia < distancia_paga) {
            distancia_paga = 0;
        } else {
            if (distancia - distancia_paga >= 1) {
                distancia_paga = distancia;
                String idDriver = "Driver_" + idCarro.split("_")[1];
                BotPayment botPayment = new BotPayment(idDriver, login, senha);
                botPayment.start();
            }
        }

        //Gerar Excel com os dados!
        ExportaExcel excel = new ExportaExcel();
        excel.escreveRelatorio(arrayList);
        //excel.start();
    }

    /**
     * Método de conversão de número, considerando que a chamada do arquivo JSON altera o tipo dos números
     * enviados (double -> int -> BigDecimal);
     * @param numero {@link Object} contendo o tipo de número recebido
     * @return {@link Double} contendo o número no tipo "adequado".
     */
    private double convertNumero(Object numero) {
        try {
            return (double) numero;
        } catch (Exception e) {
            try {
                return (Integer) numero;
            } catch (Exception e2) {
                return ((BigDecimal) numero).doubleValue();
            }
        }
    }

    /**
     * Procura por rotas prontas (a serem executadas) no atributo {@link Company#rotas_prontas}.
     * @param idRota {@link String} contendo o ID da rota a ser procurada.
     * @return {@link Rota} contendo o objeto encontrado // Nulo caso nenhuma tenha sido encontrada.
     */
    private Rota searchRoutePronta(String idRota) {
        for (int i = 0; i < rotas_prontas.size(); i++) {
            if (rotas_prontas.get(i).getIdRoute().equals(idRota)) {
                return rotas_prontas.get(i);
            }
        }

        System.out.println("Rota não encontrada.");
        return null;
    }

    /**
     * Procura por rotas prontas (a serem executadas) no atributo {@link Company#rotas_em_execucao}.
     * @param idRota {@link String} contendo o ID da rota a ser procurada.
     * @return {@link Rota} contendo o objeto encontrado // Nulo caso nenhuma tenha sido encontrada.
     */
    private Rota searchRouteEmExecucao(String idRota) {
        for (int i = 0; i < rotas_em_execucao.size(); i++) {
            if (rotas_em_execucao.get(i).getIdRoute().equals(idRota)) {
                return rotas_em_execucao.get(i);
            }
        }

        System.out.println("Rota não encontrada.");
        return null;
    }

    /**
     * Envia as rotas solicitadas por um {@link Car} específico.
     * @param jsonFile {@link JsonFile} contendo a instância recebida através da comunicação.
     * @param comando {@link Strind} contendo o comando utilizado ("comando").
     * @param bfw {@link BufferedWriter} contendo quem enviou a mensagem (para retorno).
     */
    private void enviarRotas(JsonFile jsonFile, BufferedWriter bfw) {
        ArrayList<Rota> rotas = new ArrayList<>();

        String[] id = jsonFile.receberRequestRoutes().split("_");
        
        int num = Integer.parseInt(id[1]);

        for (int i = (num-1)*9; i < num*9; i++) {
            rotas.add(rotas_prontas.get(i));
        }

        jsonFile.escreveRoutes(rotas);

        String json = jsonFile.getJSONAsString();
        
        Criptografia criptografia = new Criptografia();

        try {
            bfw.write(criptografia.criptografa(json) +"\r\n");
            bfw.flush();
        } catch (Exception e) {
            System.out.println("Erro na escrita do Json de volta para o Carro.\nException: " + e);
        }
    }

    /**
     * Pega todas as rotas do arquivo XML entitulado através da variávei {@link Company#arquivo};
     * @return {@link NodeList} contendo todas as rotas presentes no arquivo.
     */
    private static ArrayList<String> listaRotas() {
        ArrayList<String> rotas = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(arquivo);
            NodeList nList = doc.getElementsByTagName("vehicle");
            
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) nNode;
					Node node = elem.getElementsByTagName("route").item(0);
					Element edges = (Element) node;
					rotas.add(edges.getAttribute("edges"));
				}
            }

        } catch (Exception e) {
            System.out.println("Erro na listagem de rotas disponíveis (Company.java)\nException: " + e);
        }

        return rotas;
    }

    /**
     * Preenche a ArrayList de rotas prontas a serem executadas.
     * @param rotas ArrayList contendo as coordenadas (?) (Edges) das rotas escolhidas (total de 900 rotas).
     */
    private static void preencheRotasProntas(ArrayList<String> rotas)  {
        for (int i = 0; i < rotas.size(); i++) {
            String id = "Route_" + (i+1);
            Rota route = new Rota(id, rotas.get(i));
            rotas_prontas.add(route);
        }
    }

    /**
     * Método Get() para o atributo {@link Company#rotas_prontas}
     * @return {@link ArrayList} contendo as rotas a serem executadas.
     */
    public ArrayList<Rota> getRotasProntas() {
        return rotas_prontas;
    }

    /**
     * Método Get() para o atributo {@link Company#rotas_em_execucao}
     * @return {@link ArrayList} contendo as rotas em execução.
     */
    public ArrayList<Rota> getRotasEmExecucao() {
        return rotas_em_execucao;
    }

    /**
     * Método Get() para o atributo {@link Company#rotas_executadas}
     * @return {@link ArrayList} contendo as rotas que já foram executadas.
     */
    public ArrayList<Rota> getRotasExecutadas() {
        return rotas_executadas;
    }

    /**
     * Método para se conectar ao Servidor Alpha Bank, já que a empresa (Company) tem uma conta lá.
     */
    public static void conectar() {
        try {
            String IP = Constantes.IP_COMPANY + "1";//+ (clientes.size() + 1);

            socket_client = new Socket(IP, Constantes.porta_AlphaBank);
            ou = socket_client.getOutputStream();
            ouw = new OutputStreamWriter(ou);
            bfw = new BufferedWriter(ouw);

            JsonFile jsonFile = new JsonFile();
            jsonFile.enviarConexao(login, senha);
            String criptografa = new Criptografia().criptografa(jsonFile.getJSONAsString());

            bfw.write(criptografa + "\r\n");
            bfw.flush();

        } catch (Exception e) {
            System.out.println("Erro na conexão com o Servidor Alpha Bank.\nException: " + e);
        }
        
    }

    /**
     * Seta uma rota que está sendo executada por um carro como em execução.
     * @param idRoute {@link String} contendo o ID da rota em execução.
     */
    public static void setRotaEmExecucao(String idRoute) {
        for (int i = 0; i < rotas_prontas.size(); i++) {
            if (idRoute.equals(rotas_prontas.get(i).getIdRoute())) {
                Rota rota = rotas_prontas.get(i);

                rotas_em_execucao.add(rota);
                rotas_prontas.remove(rota);

                return;
            }
        }
    }

    /**
     * Seta uma rota que está já foi executada por um carro como executada.
     * @param idRoute {@link String} contendo o ID da rota em execução.
     */
    public static void setRotaExecutada(String idRoute) {
        for (int i = 0; i < rotas_em_execucao.size(); i++) {
            if (idRoute.equals(rotas_em_execucao.get(i).getIdRoute())) {
                Rota rota = rotas_em_execucao.get(i);

                rotas_executadas.add(rota);
                rotas_em_execucao.remove(rota);

                return;
            }
        }
    }

    /**
     * Método principal de execução da classe. Inicia o servidor da Company.
     * @param args
     */
    public static void main (String[] args) {
        conectar();
        
        rotas_prontas = new ArrayList<>();
        rotas_em_execucao = new ArrayList<>();
        rotas_executadas = new ArrayList<>();

        preencheRotasProntas(listaRotas());

        Thread thread_company = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //Cria os objetos necessário para instânciar o servidor
                    server = new ServerSocket(Constantes.porta_Company);
            
                while(true){
                    //System.out.println("Aguardando conexão");
                    Socket con = server.accept();
                    //System.out.println("Conectou Company");
                    
                    Company company = new Company(con);
                    company.start();
                }

        }catch (Exception e) {

            e.printStackTrace();
        }
            }
        });
        
        thread_company.start();
    }
}
