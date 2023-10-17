package io.sim;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonFile {

    //Objeto a ser manipulado, contendo todas as informaçõea a serem enviadas.
    private JSONObject object;

    private final String key_ID_rotas = "idRotas";

    private final String key_edges_rotas = "edgeRotas";

    private final String key_car = "carro";


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
     * Escreve um objeto do tipo {@link Car} dentro do arquivo JSON.
     * @param key {@link String} contendo a chave a ser utilizada para inserir o objeto.
     * @param car {@link Car} contendo o objeto a ser inserido.
     */
    public void writeCar(String key, Car car) {
        object.put(key, car);
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
