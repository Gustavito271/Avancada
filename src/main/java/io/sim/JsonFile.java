package io.sim;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe cuja responsabilidade é manipular as operações referentes à um arquivo JSON, trocado entre os servidores.
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 10/10/2023
 */
public class JsonFile {

    //Objeto que contém todas as informações. Basicamente é o arquivo JSON.
    private JSONObject object;

    //Key para COMANDO
    private final String key_comando = "comando";

    //Keys para RELATÓRIO
    private final String key_ID_rotas = "idRotas";
    private final String key_edges_rotas = "edgeRotas";
    private final String key_id_carro = "carID";
    private final String key_speed = "speed";
    private final String key_distance = "distance";
    private final String key_fuel_consumption = "fuelConsumption";
    private final String key_fuel_type = "fuelType";
    private final String key_co2 = "co2";
    private final String key_long = "longitude";
    private final String key_lat = "latitude";
    private final String key_time_stamp = "nanoStamp";

    //Keys para PAGAMENTO e CONSULTA DE SALDO
    private final String key_login = "login";
    private final String key_senha = "senha";
    private final String key_contaDestino = "contaDestino";
    private final String key_valor = "valor";
    private final String key_saldo = "saldo";

    //----------------------------------------------------------------------------------------------------------------
    /**
     * Construtor da classe em questão, inicializando o atributo {@link JsonFile#object}.
     */
    public JsonFile() {
        object = new JSONObject();
    }

    /**
     * Construtor da classe, inicializando o atributo a partir de uma {@link String} que contenha dados de um 
     * {@link JSONObject}
     * @param JSONObject_as_String
     */
    public JsonFile(String JSONObject_as_String) {
        object = new JSONObject(JSONObject_as_String);
    }

    /**
     * Método GET para a mensagem contida no comando a ser realizado.
     * @return {@link String} contendo o comando a ser realizado (Ex: pagar, consultar, etc.).
     */
    public String getComando() {
        return object.getString(key_comando);
    }

    /**
     * Método GET para o objeto ({@link JSONObject}) relacionado ao arquivo JSON como uma String.
     * @return Objeto do tipo {@link String} contendo o objeto convertido para o tipo mencionado.
     */
    public String getJSONAsString() {
        return this.object.toString();
    }

    /**
     * Escreve todos os parâmetros necessários no relatório gerencial requisitado.
     * @param IDcar {@link String} contendo a ID do carro.
     * @param IDroute {@link String} contendo a ID da rota sendo executada.
     * @param speed {@link Double} contendo a velocidade atual do carro.
     * @param distance {@link Double} contendo a distância percorrida pelo carro.
     * @param fuel_consumption {@link Double} contendo o consumo de combustível.
     * @param fuel_type {@link String} contendo o tipo de combustível utilizado.
     * @param CO2_emission {@link Double} contendo a emissão de CO2.
     * @param latitude {@link Double} contendo a coordenada de latitude.
     * @param longitude {@link Double} contendo a coordenada de longitude.
     * @param timeStamp {@link Long} contendo o tempo, em nanosegundos, que os dados foram obtidos.
     */
    public void escreverRelatorio(String IDcar, String IDroute, double speed, double distance,
                                  double fuel_consumption, String fuel_type, double CO2_emission, 
                                  double latitude, double longitude, long timeStamp) {

        object.put("comando", "relatorio");
        object.put(key_id_carro, IDcar);
        object.put(key_ID_rotas, IDroute);
        object.put(key_speed, speed);
        object.put(key_distance, distance);
        object.put(key_fuel_consumption, fuel_consumption);
        object.put(key_fuel_type, fuel_type);
        object.put(key_co2, CO2_emission);
        object.put(key_long, longitude);
        object.put(key_lat, latitude);
        object.put(key_time_stamp, timeStamp);
    }

    /**
     * "Pega" todos os dados presentes no arquivo JSON do relatório e armazena em um {@link ArrayList}
     * @return {@link ArrayList} contendo todos os dados para o relatório.
     */
    public ArrayList<Object> pegarRelatorio() {
        ArrayList<Object> arrayList = new ArrayList<>();
        
        arrayList.add(object.get(key_time_stamp));
        arrayList.add(object.get(key_id_carro));
        arrayList.add(object.get(key_ID_rotas));
        arrayList.add(object.get(key_speed));
        arrayList.add(object.get(key_distance));
        arrayList.add(object.get(key_fuel_consumption));
        arrayList.add(object.get(key_fuel_type));
        arrayList.add(object.get(key_co2));
        arrayList.add(object.get(key_long));
        arrayList.add(object.get(key_lat));

        return arrayList;
    }

    /**
     * Escreve os dados necessários para a realização de um pagamento em um arquivo JSON.
     * @param login {@link String} contendo o login da conta de onde o dinheiro será retirado.
     * @param senha {@link String} contendo a senha da conta de onde o dinheiro será retirado.
     * @param destino {@link String} contendo a conta onde o dinheiro será depositado.
     * @param valor {@link Double} contendo o valor a ser transacionado.
     */
    public void escreverDadosPagamento(String login, String senha, String destino, double valor) {
        object.put(key_comando, "pagar");
        object.put(key_login, login);
        object.put(key_senha, senha);
        object.put(key_contaDestino, destino);
        object.put(key_valor, valor);
    }

    /**
     * Retorna os dados de um pagamento para o AlphaBank.
     * @return {@link ArrayList} contendo todos os dados referentes ao pagamento.
     */
    public ArrayList<Object> recebeDadosPagamento() {
        ArrayList<Object> arrayList = new ArrayList<>();

        arrayList.add(object.get(key_login));
        arrayList.add(object.get(key_senha));
        arrayList.add(object.get(key_contaDestino));
        arrayList.add(object.get(key_valor));

        return arrayList;
    }
    
    /**
     * Escreve a mensagem de request de rotas a ser enviada.
     * @param IDcarro {@link String} contendo a ID do carro.
     */
    public void escreverRequestRoutes(String IDcarro) {
        object.put(key_comando, "enviarRotas");
        object.put(key_id_carro, IDcarro);
    }
    
    /**
     * Recebe o request de route, realizando as operações envolvidas com o arquivo.
     * @return {@link String} contendo a ID do carro que enviou o request.
     */
    public String receberRequestRoutes() {
        return object.getString(key_id_carro);
    }
    
    /**
     * Escreve a mensagem para realizar a consulta de saldo.
     * @param login {@link String} contendo o login de quem tenta realizar a consulta.
     * @param senha {@link String} contendo a senha de quem tenta realizar a consulta.
     */
    public void escreveConsultaSaldo(String login, String senha) {
        object.put(key_comando, Constantes.comando_consulta);
        object.put(key_login, login);
        object.put(key_senha, senha);
    }
    
    /**
     * Recebe a mensagem de consulta de saldo com os parâmetros adequados.
     * @return {@link ArrayList} contendo os dados de login e senha para acesso do saldo.
     */
    public ArrayList<String> recebeConsultaSaldo() {
        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add(object.getString(key_login));
        arrayList.add(object.getString(key_senha));

        return arrayList;
    }

    /**
     * Escreve uma mensagem para um cliente contendo o saldo.
     * @param saldo {@link Double} contendo o saldo.
     */
    public void escreveSaldo(double saldo) {
        object.put(key_comando, Constantes.comando_saldo);
        object.put(key_saldo, saldo);
    }

    /**
     * Recebe o saldo requisitado.
     * @return {@link Double} contendo o saldo.
     */
    public double recebeSaldo() {
        return object.getDouble(key_saldo);
    }
    
    /**
     * Escreve um objeto do tipo {@link ArrayList}<Route> dentro do arquivo JSON.
     * @param routes {@link ArrayList} contendo as rotas a serem inseridas.
     */
    public void escreveRoutes(ArrayList<Rota> routes) {
        JSONArray arrayID = new JSONArray();
        JSONArray arrayEdges = new JSONArray();

        for (int i = 0; i < routes.size(); i++) {
            arrayID.put(routes.get(i).getIdRoute());
            arrayEdges.put(routes.get(i).getEdges());
        }

        object.put(key_ID_rotas, arrayID);
        object.put(key_edges_rotas, arrayEdges);
    }
    
    /**
     * Método GET para rotas presentes em um {@link JSONArray} dentro de um JSONObject
     * @return {@link ArrayList}<Route> contendo as informações necessárias.
     */
    public ArrayList<Rota> recebeRoutes() {
        ArrayList<Rota> routes = new ArrayList<>();
        
        JSONArray idArray = (JSONArray) this.object.get(key_ID_rotas);
        JSONArray edgeArray = (JSONArray) this.object.get(key_edges_rotas);
        
        for (int i = 0; i < idArray.length(); i++) {
            Rota rota = new Rota(idArray.getString(i), edgeArray.getString(i));
            routes.add(rota);
        }

        return routes;
    }

    /**
     * Método para o envio de uma mensagem indicando a conexão do cliente no servidor.
     * @param login {@link String} contendo o login do usuário (AlphaBank) // Mensagem "aleatória" (Company)
     * @param senha {@link String} contendo o senha do usuário (AlphaBank) // Mensagem "aleatória" (Company)
     */
    public void enviarConexao(String login, String senha) {
        object.put(key_comando, Constantes.comando_conexao);
        object.put(key_login, login);
        object.put(key_senha, senha);
    }
}
