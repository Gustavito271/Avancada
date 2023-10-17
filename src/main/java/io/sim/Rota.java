package io.sim;

import de.tudresden.sumo.objects.SumoStringList;
import de.tudresden.sumo.util.SumoCommand;

public class Rota {
    //ID da Rota em questão
    private String id_route;

    //Conjunto de edges de uma rota em uma 
    private String edges;

    /**
     * Construtor da classe em questão, considerando o ID da rota, bem como suas Edges (única String contendo as edges).
     * @param id_route {@link String} contendo o ID da rota.
     * @param edges {@link String}, sendo uma única String contendo todas as Edges.
     */
    public Rota(String id_route, String edges) {
        this.id_route = id_route;
        this.edges = edges;
    }

    /**
     * Método GET para o atributo {@link Rota#id_route}.
     * @return {@link String} contendo o ID da rota.
     */
    public String getIdRoute() {
        return this.id_route;
    }

    /**
     * Método GET para o atributo {@link Rota#edges}.
     * @return {@link String} uma única String contendo todas as edges.
     */
    public String getEdges() {
        return this.edges;
    }

    /**
     * Separa as Edges em um objeto do tipo {@link SumoStringList}.
     * @return {@link SumoStringList} contendo todas as edges "separadas".
     */
    private SumoStringList separaEdges() {
        SumoStringList edges = new SumoStringList();
        edges.clear();

        String[] aux = this.edges.split(" ");

        for (int i = 0; i < aux.length; i++) {
            edges.add(aux[i]);
        }

        return edges;
    }

    /**
     * Adiciona a rota ao Sumo com base em sua ID e suas Edges.
     * @return {@link SumoCommand} contendo a "informação necessária" para a real adição da Rota.
     */
    public SumoCommand addRotaSumo() {
        return new SumoCommand(198, 128, this.id_route, separaEdges());
    }
}
