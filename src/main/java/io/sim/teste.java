package io.sim;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class teste {
    public static void main(String[] args) throws JSONException {

		/* -------------------------------------------------------
		 * TESTE 1
		 * cria um JSONObject para armazenar dados de um filme
		 * -------------------------------------------------------*/

		//instancia um novo JSONObject
		JSONObject my_obj = new JSONObject();

		//preenche o objeto com os campos: titulo, ano e genero
		my_obj.put("titulo", "JSON x XML: a Batalha Final");
		my_obj.put("ano", 2012);
		my_obj.put("genero", "Ação");

		//serializa para uma string e imprime
		String json_string = my_obj.toString();
		System.out.println("objeto original -> " + json_string);
		System.out.println();

		//altera o titulo e imprime a nova configuração do objeto
		my_obj.put("titulo", "JSON x XML: o Confronto das Linguagens");
		json_string = my_obj.toString();
		System.out.println("objeto com o título modificado -> " + json_string);
		System.out.println();

		//recupera campo por campo com o método get() e imprime cada um
		String titulo = my_obj.getString("titulo");
		Integer ano = my_obj.getInt("ano");
		String genero = my_obj.getString("genero");

		System.out.println("titulo: " + titulo);
		System.out.println("ano: " + ano);
		System.out.println("genero: " + genero);

        JSONArray my_genres = new JSONArray();

		my_genres.put("aventura");
		my_genres.put("ação");
		my_genres.put("ficção");

		//insere o array no JSONObject com o rótulo "generos"
		my_obj.put("generos", my_genres);

		//serializa para uma string e imprime
		String json_string2 = my_obj.toString();
		JSONArray js = new JSONArray();

		Rota route = new Rota("rota1", "");

		js.put(route);

		System.out.println(js.get(0));

		Rota route2 = (Rota) js.get(0);

		System.out.println(route2);

		my_obj.put("testeRoute", js);

		// ArrayList<String> aa = new ArrayList<>();

		// String t = my_obj.toString();
		// System.out.println(t);

		// Criptografia criptografia = new Criptografia();

		// String f = criptografia.criptografa(t, criptografia.genKey(t.length()));
		// System.out.println(f);
		// System.out.println(criptografia.decriptografa(f, criptografia.genKey(f.length())));
		

        /*Car car = new Car(titulo, genero, null);

        my_genres.put(car);

        car.getFuel_Tank();

		JSONObject ggg = new JSONObject();

		ggg.put("teste", car);

		Car ggggg = (Car) ggg.get("teste");
		
		System.out.println(car);

		System.out.println(ggg.toString());

		System.out.println(ggggg);*/
	}
}
