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

	private List<CategoricalStatesEntity> listEntity;	
	

	public CategoricalStatesEntityContainer(){
		listEntity = new ArrayList<CategoricalStatesEntity>(); 
	}
	
	
	public CategoricalStatesEntity createCategoricalEntity(String name){
		
		
		for(CategoricalStatesEntity teste: listEntity){
			if (teste.name.compareTo(name) == 0){
				return teste; 
			}
		}
		
		CategoricalStatesEntity entity =  new CategoricalStatesEntity(name); 

		addEntity(entity);
		
		return entity; 
		
	}
	

	private void addEntity(CategoricalStatesEntity entity) {
		listEntity.add(entity);
	}
	
	// TODO Possivelmente fazer alguma limpeza de variáveis ou dependências aqui.
	//Problema: nao há referencia a partir da entidade!!!
	
	public void removeEntity(CategoricalStatesEntity entity) {
		listEntity.remove(entity);
		
	}
	
	public CategoricalStatesEntity getCategoricalState(String name) 
	                         throws CategoricalStateDoesNotExistException{
		
		for(CategoricalStatesEntity teste: listEntity){
			if (teste.name.compareTo(name) == 0){
				return teste; 
			}
		}
		
		throw new CategoricalStateDoesNotExistException(); 
		
	}
	
	public List<CategoricalStatesEntity> getListEntity(){
		return listEntity; 
	}

	
}
