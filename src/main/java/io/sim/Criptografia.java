package io.sim;

import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Classe para realizar a Criptografia do arquivo JSON a ser enviado através da comunicação Cliente/Servidor.
 * 
 * Utilizei como base:  <a href="https://www.devmedia.com.br/utilizando-criptografia-simetrica-em-java/31170">
 * Site indicado pelo professor</a>.
 */
public class Criptografia {
    public String criptografa(String mensagem) {
        String chave = genKey(mensagem.length());
        if (mensagem.length() != chave.length()) {
            error("O tamanho da mensagem e da chave devem ser iguais.");
        }
        
        int[] im = charArrayToInt(mensagem.toCharArray());
        int[] ik = charArrayToInt(chave.toCharArray());
        int[] data = new int[mensagem.length()];

        for (int i = 0; i < mensagem.length(); i++) {
            data[i] = im[i] + ik[i];
        }

        return new String(intArrayToChar(data));
    }

    public String decriptografa(String mensagem) {
        String chave = genKey(mensagem.length());

        if (mensagem.length() != chave.length()) {
                error("O tamanho da mensagem e da chave devem ser iguais.");
        }

        int[] im = charArrayToInt(mensagem.toCharArray());
        int[] ik = charArrayToInt(chave.toCharArray());
        int[] data = new int[mensagem.length()];

        for (int i=0;i<mensagem.length();i++) {
            data[i] = im[i] - ik[i];
        }

        return new String(intArrayToChar(data));
    }

    public String genKey(int tamanho) {
        String chave = "";
        for (int i = 0; i < tamanho; i++) {
            chave += "a";
        }

        /*Random randomico = new Random();
        char[] key = new char[tamanho];
        for (int i = 0; i < tamanho; i++) {
            key[i] = (char) randomico.nextInt(132);
            if ((int) key[i] < 97)  {
                key[i] = (char) (key[i] + 72);
            }
            if ((int) key[i] > 122) {
                key[i] = (char) (key[i] - 72);
            }
        }*/

        return chave;
    }

    public static void main(String[] args){
        Criptografia otp = new Criptografia();
        String menssagem = "hello";
        String chave = otp.genKey(menssagem.length());
        String msgCriptografada = otp.criptografa(menssagem);
        String msgDecriptografada = otp.decriptografa(msgCriptografada);

        System.out.println("Menssagem: "+menssagem);
        System.out.println("Chave: "+chave);
        System.out.println("Mensagem Criptografada: "+msgCriptografada);
        System.out.println("Mensagem Decriptografada: "+msgDecriptografada);

    }

    private int chartoInt(char c) {
        return (int) c;
    }

    private char intToChar(int i) {
        return (char) i;
    }
    
    private int[] charArrayToInt(char[] cc) {
        int[] ii = new int[cc.length];
        for(int i = 0; i < cc.length; i++){
            ii[i] = chartoInt(cc[i]);
        }
        return ii;
    }

    private char[] intArrayToChar(int[] ii) {
        char[] cc = new char[ii.length];
        for(int i = 0; i < ii.length; i++){
        cc[i] = intToChar(ii[i]);
        }
        return cc;
    }

    private void error(String msg) {
        System.out.println(msg);
        System.exit(-1);
    }
}