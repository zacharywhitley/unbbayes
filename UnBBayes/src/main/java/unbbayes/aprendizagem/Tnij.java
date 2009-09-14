/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 package unbbayes.aprendizagem;

import java.util.List;

import unbbayes.util.SetToolkit;


/**
 * Class which implements the vectors containing the states to be count (Nij e Nijk).
 * The objects from this class contain a vector with the already read states from
 * a particular case of the registers, and how many times it's repeating within the archive
 * @author Danilo Custdio da Silva
 * @version 1.0
 */

public class Tnij implements Cloneable
{
    private List estados;
    private int repeticoes;
    private byte[] b = new byte[4];

    /**
     * Constructor method of the object. Constructs the object passing its states, and
     * sets the default repeating count to 1.
     * @param estados - state vector(<code>List<code>)
     */
    Tnij(List estados){
        this.estados = estados;
        this.repeticoes = 1;
    }

    /**
     * Constructor method of the object. Constructs the object passing its states,
     * and sets the repeating count as set by usurio.Util for compressed archives.
     * @param states - O Vetor de estados(<code>List<code>)
     * @param repeat - how many times that pattern repeats
     */
    Tnij(List states, int repeat){
        this(states);
        if(repeat == 0){
            this.repeticoes =1;
        } else{
            this.repeticoes = repeat;
        }
    }

    /**
     * Method which creates an exact copy of Tnij, changing its reference
     * @return Object - Tnij type object
     * @see Object
     */
    public Object clone(){
        Tnij tnij = new Tnij(SetToolkit.clone((List)estados));
        tnij.repeticoes = this.repeticoes;
        return tnij;
    }

    /**
     * Method that increments the number of times a pattern repeats
     */
    public void incrementaRepeticoes(){
        repeticoes ++ ;
    }

    /**
     * Method which indicates how many times that patterns has repeated within the archive.
     * @return int - how many times it's repeating within the archive.
     */
    public int getRepeticoes(){
        return this.repeticoes;
    }

    /**
     * Method which indicates the states' vector which form the object's pattern.
     * @return List - States of object's patterns
     */
    public List getEstados(){
        return estados;

    }


    /**
     * Sets the pattern repeating count to a user given value.
     * Useful for compressed archives.
     * @param repeticoes - How many times a state patterns occurs within the archive(<code>int<code>)
     */
    public void setRepeticoes(int repeticoes){
        this.repeticoes = repeticoes;
    }

    /**
     * increases the pattern repeating count by a number given by a user.
     * Useful for compressed archives.
     * @param repeticoes - how many times a pattern occurs inside the archive(<code>int<code>)
     */
    public void aumentaRepeticoes(int rep){
        this.repeticoes += rep;
    }

}
