package io.sim;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonFile {

    //Objeto a ser manipulado, contendo todas as informaçõea a serem enviadas.
    private JSONObject object;

    //Key "padrão" para a ID da rota.
    private final String key_ID_rotas = "idRotas";

    //Key "padrão" para as edges da rota.
    private final String key_edges_rotas = "edgeRotas";

    //Key "padrão" para a ID do carro.
    private final String key_id_carro = "IDcar";

    //Key "padrão" para a velocidade do carro.
    private final String key_speed = "speed";

    //Key "padrão" para a distância percorrida.
    private final String key_distance = "distance";

    //Key "padrão" para o consumo de combustível.
    private final String key_fuel_consumption = "fuelConsumption";

    //Key "padrão" para o tipo de combustível.
    private final String key_fuel_type = "fuelType";

    //Key "padrão" para a emissão de CO2.
    private final String key_co2 = "co2";

    //Key "padrão" para a longitude.
    private final String key_long = "longitude";

    //Key "padrão" para a latitude.
    private final String key_lat = "latitude";

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
     */
    public void escreverRelatorio(String IDcar, String IDroute, double speed, double distance, double fuel_consumption, String fuel_type, double CO2_emission, double latitude, double longitude) {
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
    }

    /**
     * "Pega" todos os dados presentes no arquivo JSON do relatório e armazena em um {@link ArrayList}
     * @return {@link ArrayList} contendo todos os dados para o relatório.
     */
    public ArrayList<Object> pegarRelatorio() {
        ArrayList<Object> arrayList = new ArrayList<>();
        
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
     * Escreve um objeto do tipo {@link ArrayList}<Route> dentro do arquivo JSON.
     * @param routes {@link ArrayList} contendo as rotas a serem inseridas.
     */
    public void writeRoutes(ArrayList<Rota> routes) {
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
     * Escreve uma {@link String} contendo uma mensagem/comando dentro do arquivo JSON.
     * @param key {@link String} contendo a chave a ser utilizada para inserir o objeto.
     * @param comando {@link String} contendo a mensagem/comando a ser inserida.
     */
    public void writeString(String key, String comando) {
        object.put(key, comando);
    }
    
    /**
     * Método GET para o objeto ({@link JSONObject}) relacionado ao arquivo JSON.
     * @return Objeto do tipo {@link JSONObject} contendo as informações inseridas.
     */
    public JSONObject getJSONObject() {
        return this.object;
    }

    /**
     * Método GET para o objeto ({@link JSONObject}) relacionado ao arquivo JSON como uma String.
     * @return Objeto do tipo {@link String} contendo o objeto convertido para o tipo mencionado.
     */
    public String getJSONObjectAsString() {
        return this.object.toString();
    }

    /**
     * Método GET para o objeto ({@link Object}) dentro do JSON.
     * @param key {@link String} contendo a chave de acesso do JSON.
     * @return Objeto do tipo {@link Object} contendo as informações inseridas.
     */
    public Object getObject(String key) {
        return this.object.get(key);
    }

    /**
     * Método GET para rotas presentes em um {@link JSONArray} dentro de um JSONObject
     * @return {@link ArrayList}<Route> contendo as informações necessárias.
     */
    public ArrayList<Rota> getRoutesFromJSONArray() {
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
     * Método GET para uma {@link String} contida em um campo do {@link JSONObject}.
     * @param key {@link String} contendo a chave de acesso.
     * @return {@link String} contendo as informações necessárias.
     */
    public String getStringFromJSONObject(String key) {
        return this.object.getString(key);
    }
    
}
