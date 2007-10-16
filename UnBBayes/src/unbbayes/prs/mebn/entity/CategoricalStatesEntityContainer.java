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
			if (teste.name.compareTo(name) == 0){
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
			if (teste.name.compareTo(name) == 0){
				return teste; 
			}
		}
		
		throw new CategoricalStateDoesNotExistException(); 
		
	}
	
	public List<CategoricalStateEntity> getListEntity(){
		return listEntity; 
	}

	
}
