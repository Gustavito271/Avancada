package io.sim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * "Empresa", devendo possuir as seguintes características:
 *      -> Ser uma Thread (CONCLUÍDO)
 *      -> Ser um Servidor (CONCLUÍDO)
 *      -> Ser um Cliente para o AlphaBank (conectar-se à ele) (CONCLUÍDO)
 *      -> Conter uma ArrayList<Route> de rotas a serem executadas (CONCLUÍDO)
 *      -> Conter uma ArrayList<Route> de rotas em execução (CONCLUÍDO)
 *      -> Conter uma ArrayList<Route> de rotas executadas (CONCLUÍDO)
 *      -> Conter uma classe BotPayment, que deve ser uma Thread
 *      -> Pagar R$3.25 por km/rodado
 *      -> Utilizar arquivos JSON e criptografia para a troca de mensagens com clientes/servidor
 */
public class Company extends Thread {
    
    //Sockets de comunicacao;
    //private Socket socket_servidor;
    //private Socket socket_cliente;

    //Server para a comunicação
    //private static ServerSocket server;

    //private String nome;

    //Comunicação Cliente-Servidor
    /*private OutputStream os;
    private Writer writer;
    private BufferedWriter bfw;
    private OutputStream os_client;
    private Writer writer_client;*/

    //private static BufferedWriter bfw_client;

    //Conjunto de rotas: Prontas para serem executas // Em execução // Já foram executadas
    private ArrayList<Rota> rotas_prontas;
    private ArrayList<Rota> rotas_em_execucao;
    private ArrayList<Rota> rotas_executadas;

    //Arquivo contendo as rotas.
    private String arquivo = "sim/data/dados2.xml";

    //Comunicação Cliente-Servidor
    private static ServerSocket server;
    private Socket socket;
    private InputStream in;
    private InputStreamReader inr;
    private BufferedReader bfr;

    private Socket socket_client;
    private OutputStream ou ;
    private Writer ouw;
    private BufferedWriter bfw;

    /**
     * Construtor para a classe Company, a ser utilizada como uma Thread e Server.
     * 
     * @param socket {@link Socket} contendo o meio de comunicação cliente/servidor.
     * (Definição: <a href="https://www.devmedia.com.br/como-criar-um-chat-multithread-com-socket-em-java/33639">
     * Site indicado pelo professor</a>).
     */
    public Company(Socket socket) {
        //conectar();
        this.rotas_prontas = new ArrayList<>();
        this.rotas_em_execucao = new ArrayList<>();
        this.rotas_executadas = new ArrayList<>();

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
        //conectar();

        try{
            String msg;
            OutputStream ou =  this.socket.getOutputStream();
            Writer ouw = new OutputStreamWriter(ou);
            BufferedWriter bfw = new BufferedWriter(ouw);
            //clientes.add(bfw);
            msg = bfr.readLine();

            System.out.println(msg);

            preencheRotasProntas(listaRotas());

            Thread.sleep(200);

            if (bfr.ready()) {
                msg = bfr.readLine();
                Criptografia criptografia = new Criptografia();

                String decriptografa = criptografia.decriptografa(msg);

                JsonFile jsonFile = new JsonFile(decriptografa);

                String comando = (String) jsonFile.getObject("comando");

                System.out.println(comando);
                if (comando.equals("enviarRotas")) {
                    enviarRotas(jsonFile, comando, bfw);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia as rotas solicitadas por um {@link Car} específico.
     * @param jsonFile {@link JsonFile} contendo a instância recebida através da comunicação.
     * @param comando {@link Strind} contendo o comando utilizado ("comando").
     * @param bfw {@link BufferedWriter} contendo quem enviou a mensagem (para retorno).
     */
    private void enviarRotas(JsonFile jsonFile, String comando, BufferedWriter bfw) {
        ArrayList<Rota> rotas = new ArrayList<>();

        String[] id = jsonFile.getStringFromJSONObject("carID").split("_");
        
        int num = Integer.parseInt(id[1]);

        for (int i = (num-1)*9; i < num*9; i++) {
            rotas.add(rotas_prontas.get(i));
        }

        jsonFile.writeRoutes(rotas);

        String json = jsonFile.getJSONObjectAsString();
        
        Criptografia criptografia = new Criptografia();

        try {
            bfw.write(criptografia.criptografa(json) +"\r\n");
            bfw.flush();
        } catch (Exception e) {

        }
    }

    /**
     * Pega todas as rotas do arquivo XML entitulado através da variávei {@link Company#arquivo};
     * @return {@link NodeList} contendo todas as rotas presentes no arquivo.
     */
    private ArrayList<String> listaRotas() {
        ArrayList<String> rotas = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(this.arquivo);
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
    private void preencheRotasProntas(ArrayList<String> rotas)  {
        for (int i = 0; i < rotas.size(); i++) {
            String id = (i+1) + "";
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
    public void conectar() {
        try {
            String IP = Constantes.IP_COMPANY + "";//+ (clientes.size() + 1);

            socket_client = new Socket(IP, Constantes.porta_AlphaBank);
            ou = socket_client.getOutputStream();
            ouw = new OutputStreamWriter(ou);
            bfw = new BufferedWriter(ouw);
            bfw.write("Company"+"\r\n");
            bfw.flush();

        } catch (Exception e) {
            System.out.println("Erro na conexão com o Servidor Alpha Bank.\nException: " + e);
        }
        
    }

    /***
     * Método usado para enviar mensagem para todos os clients
     * Acho que não será usado!!!!!!!!! Pelo menos não assim.
     * @param bwSaida do tipo BufferedWriter
     * @param msg do tipo String
     * @throws IOException
     */
    public void sendToAll(BufferedWriter bwSaida, String msg) throws  IOException {

        BufferedWriter bwS;

        /*for(BufferedWriter bw : clientes){
            bwS = (BufferedWriter)bw;
            if(!(bwSaida == bwS)){
                bw.write(nome + " -> " + msg+"\r\n");
                bw.flush();
            }
        }*/
    }

    /**
     * Método principal de execução da classe. Inicia o servidor da Company.
     * @param args
     */
    public static void main (String[] args) {
        Thread thread_company = new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    //Criação do servidor, associado à uma porta, definida acima.
                    //server = new ServerSocket(Constantes.porta_Company);
                    server = new ServerSocket(11111);

                    //Criação do ArrayList de clientes do servidor, ou seja, onde todos os motoristas (Driver) se conectarão.
                    //clientes = new ArrayList<BufferedWriter>();

                    //Execução para aguardar os clientes do servidor se conectarem a ele.
                    while (true) {
                        System.out.println("Aguardando conexão");
                        Socket socket = server.accept();
                        System.out.println("Conectou na Company!");

                        Thread t = new Company(socket);
                        t.start();
                    }

                } catch (Exception e) {
                    System.out.println("Erro na execução principal da Company.java\nException: " + e);
                }*/
                
            
                try{
                    //Cria os objetos necessário para instânciar o servidor
                    server = new ServerSocket(Constantes.porta_Company);
            
                while(true){
                    System.out.println("Aguardando conexão");
                    Socket con = server.accept();
                    System.out.println("Conectou Company");
                    
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
