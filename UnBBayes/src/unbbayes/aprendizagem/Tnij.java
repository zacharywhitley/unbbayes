package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.jprs.jbn.SetToolkit;


/**
 * Classe que impementa os vetores que contem estados a serem contados
 * (Nij e Nijk).Os objetos dessa classe contem um vetor com os estados
 * lidos em um determinado caso do registro e o numero de vezes  que
 * ele é repetido no arquivo
 * @author Danilo Custódio da Silva
 * @version 1.0
 */

public class Tnij implements Cloneable
{
    private List estados;
    private int repeticoes;
    private byte[] b = new byte[4];

    /**
     * Método construtor do objeto,constroi o objeto passando
     * os estados deste. E Coloca o número de repetições desse
     * padrão como 1.
     * @param estados - O Vetor de estados(<code>List<code>)
     */
    Tnij(List estados){
        this.estados = estados;
        this.repeticoes = 1;
    }

    /**
     * Método construtor do objeto,constroi o objeto passando
     * os estados deste. E Coloca o número de repetições desse
     * padrão como o setado pelo usuário.Util para arquivos com
     * pactados
     * @param estados - O Vetor de estados(<code>List<code>)
     * @param repeticoes - Número de vezes que esse padrão se repete
     */
    Tnij(List estados, int repeticoes){
        this(estados);
        if(repeticoes == 0){
            this.repeticoes =1;
        } else{
            this.repeticoes = repeticoes;
        }
    }

    /**
     * Método que cria uma cópia exata do objeto Tnij, mudando
     * a referência deste.
     * @return Object - Objeto do tipo Tnij
     * @see Object
     */
    public Object clone(){
        Tnij tnij = new Tnij(SetToolkit.clone((List)estados));
        tnij.repeticoes = this.repeticoes;
        return tnij;
    }

    /**
     * Método que aumenta o número de repetições do padrão em 1.	 *
     */
    public void incrementaRepeticoes(){
        repeticoes ++ ;
    }

    /**
     * Método que indica quantas vezes esse padrão se repetiu
     * no arquivo.
     * @return int - Número de repetiçoes no arquivo
     */
    public int getRepeticoes(){
        return this.repeticoes;
    }

    /**
     * Método que indica o vetor de estados que formam o
     * padrão do objeto.
     * @return List - Os estados do padrão do objeto
     */
    public List getEstados(){
        return estados;

    }


    /**
     * Método que coloca o número de repetições do padrão determinado
     * pelo usuário. Util para arquivos compactados.
     * @param repeticoes - Número de vezes que um padrão de estados ocorre
     * no arquivo(<code>int<code>)
     */
    public void setRepeticoes(int repeticoes){
        this.repeticoes = repeticoes;
    }

    /**
     * Método que aumenta o número de repetições do padrão determinado
     * pelo usuário. Util para arquivos compactados.
     * @param repeticoes - Número de vezes que um padrão de estados ocorre
     * no arquivo(<code>int<code>)
     */
    public void aumentaRepeticoes(int rep){
        this.repeticoes += rep;
    }

}
