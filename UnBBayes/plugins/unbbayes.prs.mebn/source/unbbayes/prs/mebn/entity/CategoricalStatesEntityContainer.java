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
package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;

/**
 * Contains the categorical states entities of a MEBN. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 06/03/07
 *
 */
public class CategoricalStatesEntityContainer {

	private List<CategoricalStateEntity> listEntity;	
	

	public CategoricalStatesEntityContainer(){
		listEntity = new ArrayList<CategoricalStateEntity>(); 
	}
	
	/**
	 * Create a new categorical entity. If a categorical entity already exists
	 * with the name, return it. 
	 */
	public CategoricalStateEntity createCategoricalEntity(String name){
		
		
		for(CategoricalStateEntity teste: listEntity){
			if (teste.getName().equals(name)){
				return teste; 
			}
		}
		
		CategoricalStateEntity entity =  new CategoricalStateEntity(name); 

		addEntity(entity);
		
		return entity; 
		
	}
	

	private void addEntity(CategoricalStateEntity entity) {
		listEntity.add(entity);
	}
	
	// TODO Possivelmente fazer alguma limpeza de vari�veis ou depend�ncias aqui.
	//Problema: nao h� referencia a partir da entidade!!!
	
	public void removeEntity(CategoricalStateEntity entity) {
		listEntity.remove(entity);
		
	}
	
	public CategoricalStateEntity getCategoricalState(String name) 
	                         throws CategoricalStateDoesNotExistException{
		
		for(CategoricalStateEntity teste: listEntity){
			if (teste.getName().equals(name)){
				return teste; 
			}
		}
		
		throw new CategoricalStateDoesNotExistException(); 
		
	}
	
	public List<CategoricalStateEntity> getListEntity(){
		return listEntity; 
	}

	
}
