/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.util.SetToolkit;


/**
 * Classe que impementa os vetores que contem estados a serem contados
 * (Nij e Nijk).Os objetos dessa classe contem um vetor com os estados
 * lidos em um determinado caso do registro e o numero de vezes  que
 * ele � repetido no arquivo
 * @author Danilo Cust�dio da Silva
 * @version 1.0
 */

public class Tnij implements Cloneable
{
    private List estados;
    private int repeticoes;
    private byte[] b = new byte[4];

    /**
     * M�todo construtor do objeto,constroi o objeto passando
     * os estados deste. E Coloca o n�mero de repeti��es desse
     * padr�o como 1.
     * @param estados - O Vetor de estados(<code>List<code>)
     */
    Tnij(List estados){
        this.estados = estados;
        this.repeticoes = 1;
    }

    /**
     * M�todo construtor do objeto,constroi o objeto passando
     * os estados deste. E Coloca o n�mero de repeti��es desse
     * padr�o como o setado pelo usu�rio.Util para arquivos com
     * pactados
     * @param estados - O Vetor de estados(<code>List<code>)
     * @param repeticoes - N�mero de vezes que esse padr�o se repete
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
     * M�todo que cria uma c�pia exata do objeto Tnij, mudando
     * a refer�ncia deste.
     * @return Object - Objeto do tipo Tnij
     * @see Object
     */
    public Object clone(){
        Tnij tnij = new Tnij(SetToolkit.clone((List)estados));
        tnij.repeticoes = this.repeticoes;
        return tnij;
    }

    /**
     * M�todo que aumenta o n�mero de repeti��es do padr�o em 1.	 *
     */
    public void incrementaRepeticoes(){
        repeticoes ++ ;
    }

    /**
     * M�todo que indica quantas vezes esse padr�o se repetiu
     * no arquivo.
     * @return int - N�mero de repeti�oes no arquivo
     */
    public int getRepeticoes(){
        return this.repeticoes;
    }

    /**
     * M�todo que indica o vetor de estados que formam o
     * padr�o do objeto.
     * @return List - Os estados do padr�o do objeto
     */
    public List getEstados(){
        return estados;

    }


    /**
     * M�todo que coloca o n�mero de repeti��es do padr�o determinado
     * pelo usu�rio. Util para arquivos compactados.
     * @param repeticoes - N�mero de vezes que um padr�o de estados ocorre
     * no arquivo(<code>int<code>)
     */
    public void setRepeticoes(int repeticoes){
        this.repeticoes = repeticoes;
    }

    /**
     * M�todo que aumenta o n�mero de repeti��es do padr�o determinado
     * pelo usu�rio. Util para arquivos compactados.
     * @param repeticoes - N�mero de vezes que um padr�o de estados ocorre
     * no arquivo(<code>int<code>)
     */
    public void aumentaRepeticoes(int rep){
        this.repeticoes += rep;
    }

}
