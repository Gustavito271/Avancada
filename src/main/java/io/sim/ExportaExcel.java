package io.sim;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * Classe para escrita de dados em um arquivo de Excel (.xlsx).
 * 
 * Para tanto utilizei como base <a href="https://acervolima.com/como-escrever-dados-em-planilhas-do-excel-usando-java/"> 
 * 
 * @author Gustavo Henrique Tostes
 * @version 1.0
 * @since 20/10/2023
 */
public class ExportaExcel extends Thread{

    //Escrita de dados no Arquivo .xlsx.
    private static XSSFWorkbook workbook;
    private static XSSFSheet pasta_dados;
    private static XSSFSheet pasta_extratos;
    private static XSSFSheet pasta_recon;                                   //Novo

    //Armazenamento dos dados advindos das classes Company e Account (utilizam o Excel).
    private static ArrayList<ArrayList<String>> conjunto_dados;
    private static ArrayList<ArrayList<String>> conjunto_extratos;
    private static ArrayList<ArrayList<String>> conjunto_recon;             //Novo

    //Flag de funcionamento.
    private static boolean flag = true;

    //---------------------------------------------------------------------------------------------------------------

    /**
     * Construtor da Classe (default).
     */
    public ExportaExcel() {

    }

    @Override
    public void run() {
        while(flag) {
            if (conjunto_dados.size() != 0) {
                escreveCelulasRelatorio();
            }

            if (conjunto_extratos.size() != 0) {
                escreveCelulasExtrato();
            }

            if (conjunto_recon.size() != 0) {       //Novo
                escreveCelulasRecon();              //Novo
            }                                       //Novo

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Falha no Sleep da Thread do ExportaExcel.\nException: " + e);
            }
        }
    }

    /**
     * Escreve as células do relatório utilizando os dados obtidos em um dado intervalo de tempo (definido
     * pelo Thread.sleep do método run).
     */
    private void escreveCelulasRelatorio() {
        ArrayList<ArrayList<String>> aux = (ArrayList<ArrayList<String>>) conjunto_dados.clone();
        conjunto_dados = new ArrayList<>();

        for (int i = 0; i < aux.size(); i++) {
            ArrayList<String> dados = aux.get(i);

            XSSFRow row = pasta_dados.createRow(pasta_dados.getLastRowNum() + 1);
            int cellID = 0;
            for (int j = 0; j < dados.size(); j++) {
                Cell cell = row.createCell(cellID++);
                cell.setCellValue(dados.get(j));
            }
        }
    }

    /**
     * Escreve as células do extrato utilizando os dados obtidos em um dado intervalo de tempo (definido
     * pelo Thread.sleep do método run).
     */
    private void escreveCelulasExtrato() {
        ArrayList<ArrayList<String>> aux = (ArrayList<ArrayList<String>>) conjunto_extratos.clone();
        conjunto_extratos = new ArrayList<>();

        for (int i = 0; i < aux.size(); i++) {
            ArrayList<String> dados = aux.get(i);

            XSSFRow row = pasta_extratos.createRow(pasta_extratos.getLastRowNum() + 1);
            int cellID = 0;
            for (int j = 0; j < dados.size(); j++) {
                Cell cell = row.createCell(cellID++);
                cell.setCellValue(dados.get(j));
            }
        }
    }

    /**
     * NOVO!
     * Escreve as células do extrato utilizando os dados obtidos em um dado intervalo de tempo (definido
     * pelo Thread.sleep do método run).
     */
    private void escreveCelulasRecon() {
        ArrayList<ArrayList<String>> aux = (ArrayList<ArrayList<String>>) conjunto_recon.clone();
        conjunto_recon = new ArrayList<>();

        for (int i = 0; i < aux.size(); i++) {
            ArrayList<String> dados = aux.get(i);

            XSSFRow row = pasta_recon.createRow(pasta_recon.getLastRowNum() + 1);
            int cellID = 0;
            for (int j = 0; j < dados.size(); j++) {
                Cell cell = row.createCell(cellID++);
                cell.setCellValue(dados.get(j));
            }
        }
    }

    /**
     * NOVO!
     * Escreve os dados referentes ao relatório no atributo {@link ExportaExcel#conjunto_dados}.
     * @param arrayList {@link ArrayList} contendo os dados a serem inseridos.
     */
    public void escreveRecon(double[] tempos) {
        double aux1[] = tempos.clone();
        for (int i = 0; i < tempos.length; i++) {
            aux1[i] = 1000/tempos[i];
        }

        double[] aux2 = new double[aux1.length-1];
        for (int i = 0; i < aux1.length-1; i++) {
            aux2[i] = aux1[i+1];
        }

        ArrayList<String> dados = new ArrayList<>();

        int inicio = 15 - aux2.length;
        int indice = 0;

        dados.add(Double.toString(tempos[0]));
        
        for (int i = 0; i < 15; i++) {
            if (i >= inicio) {
                dados.add(Double.toString(aux2[indice]));
                indice++;
            } else {
                dados.add("");
            }
        }

        conjunto_recon.add(dados);
    }

    /**
     * Escreve os dados referentes ao relatório no atributo {@link ExportaExcel#conjunto_dados}.
     * @param arrayList {@link ArrayList} contendo os dados a serem inseridos.
     */
    public void escreveRelatorio(ArrayList<Object> arrayList) {
        ArrayList<String> dados = new ArrayList<>();

        for (int i = 0; i < arrayList.size(); i++) {
            dados.add(arrayList.get(i).toString());
        }

        conjunto_dados.add(dados);
    }

    /**
     * Escreve os dados referentes ao relatório no atributo {@link ExportaExcel#conjunto_extratos}.
     * @param arrayList {@link ArrayList} contendo os dados a serem inseridos.
     */
    public void escreveExtrato(ArrayList<Object> arrayList) {
        ArrayList<String> dados = new ArrayList<>();

        for (int i = 0; i < arrayList.size(); i++) {
            dados.add(arrayList.get(i).toString());
        }

        conjunto_extratos.add(dados);
    }

    /**
     * Cria o cabeçalho do Relatório, contendo os itens solicitados:
     * TimeStamp // ID do Carro // ID da Rota // Velocidade // Distância // Consumo de Combustível //
     * Tipo de Combustível // Emissão de CO2 // Longitude (Coord X) // Latitude (Coord Y)
     */
    private static void criarCabecalhoRelatorio() {
        String[] cabecalho = new String[] {"TimeStamp", "ID Car", "ID Route", "Speed", "Distance", "FuelConsumption", 
                                           "FuelType", "CO2 Emisison", "Coordenada X", "Coordenada Y"};

        XSSFRow row = pasta_dados.createRow(1);

        int cellID = 0;
        for (String dados : cabecalho) {
            Cell cell = row.createCell(cellID++);
            cell.setCellValue(dados);
        }
    }

    /**
     * Cria o cabeçalho do Extrato, contendo os itens:
     * TimeStamp // Dono da Conta // Conta Destino // Saldo Anterior // Saldo Atual
     */
    private static void criarCabecalhoExtrato() {
        String[] cabecalho = new String[] {"TimeStamp", "Account Owner", "Destiny Account", "Saldo Anterior", "Saldo Atual"};

        XSSFRow row = pasta_extratos.createRow(1);

        int cellID = 0;
        for (String dados : cabecalho) {
            Cell cell = row.createCell(cellID++);
            cell.setCellValue(dados);
        }
    }

    /**
     * NOVO!
     * Cria o cabeçalho dos dados de Reconciliação de Dados, contendo os itens:
     * Trecho 02 // Trecho 03 // Trecho 04 // Trecho 05 // Trecho 06 // Trecho 07 // Trecho 08 // Trecho 
     */
    private static void criarCabecalhoRecon() {
        String[] cabecalho = new String[] {"Tempo Restante", "Trecho 02", "Trecho 03", "Trecho 04", "Trecho 05", "Trecho 06", 
                                           "Trecho 07", "Trecho 08", "Trecho 09", "Trecho 10", "Trecho 11",
                                           "Trecho 12", "Trecho 13", "Trecho 14", "Trecho 15", "Trecho 16 (Fim)"};

        XSSFRow row = pasta_recon.createRow(1);

        int cellID = 0;
        for (String dados : cabecalho) {
            Cell cell = row.createCell(cellID++);
            cell.setCellValue(dados);
        }
    }

    /**
     * Método SET para o atributo {@link ExportaExcel#flag}.
     * @param flag_new {@link Boolean} contendo o novo valor a ser inserido no atributo.
     */
    public static void setFlag(boolean flag_new) {
        flag = flag_new;
    }

    public static void main(String[] args) {
        conjunto_dados = new ArrayList<>();
        conjunto_extratos = new ArrayList<>();
        conjunto_recon = new ArrayList<>();     //Novo

        workbook = new XSSFWorkbook();

        pasta_dados = workbook.createSheet("Relatorio");
        pasta_extratos = workbook.createSheet("Extratos");
        pasta_recon = workbook.createSheet("Reconciliacao");        //Novo

        criarCabecalhoRelatorio();
        criarCabecalhoExtrato();
        criarCabecalhoRecon();          //Novo

        Thread thread = new Thread(new Runnable() {
            public void run() {
                boolean flag = true;
                while (flag) {
                    String mensagem = "Deseja baixar o relatório/extrato?";
                    int result = JOptionPane.showConfirmDialog(null, mensagem, "Confirmation",JOptionPane.YES_NO_OPTION);

                    if (result == JOptionPane.YES_OPTION) {

                        try {
                            FileOutputStream out = new FileOutputStream(new File(Constantes.path_arquivo));
                            workbook.write(out);
                            out.close();

                            FileInputStream inp = new FileInputStream(new File(Constantes.path_arquivo));

                            workbook = new XSSFWorkbook(inp);

                            pasta_dados = workbook.getSheet("Relatorio");
                            pasta_extratos = workbook.getSheet("Extratos");
                            pasta_recon = workbook.getSheet("Reconciliacao");       //Novo
                            
                        } catch (Exception e) {
                            System.out.println("Erro na escrita do arquivo.\nException: " + e);
                        }
                        JOptionPane.showMessageDialog(null, "Baixado com sucesso");
                    } else {
                        flag = false;
                    }
                }
            }
        });

        thread.start();
    }

}
