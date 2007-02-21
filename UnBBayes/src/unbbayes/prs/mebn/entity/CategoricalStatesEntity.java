package unbbayes.prs.mebn.entity;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeChangeNotAllowedException;

public class CategoricalStatesEntity extends Entity {
	
	private static List<CategoricalStatesEntity> listEntity = new ArrayList<CategoricalStatesEntity>();	
	
	public CategoricalStatesEntity(String name) {
		this.type = "CategoryLabel";
		this.name = name;
		CategoricalStatesEntity.addEntity(this);
	}
	
	public void setType(String type) throws TypeChangeNotAllowedException {
		throw new TypeChangeNotAllowedException(
				"This entity is not allowed to change its type.");
	}

	
	public static void addEntity(CategoricalStatesEntity entity) {
		CategoricalStatesEntity.listEntity.add(entity);
	}
	
	// TODO Possivelmente fazer alguma limpeza de variáveis ou dependências aqui.
	public static void removeEntity(CategoricalStatesEntity entity) {
		CategoricalStatesEntity.listEntity.remove(entity);
	}
	
	public static CategoricalStatesEntity getCategoricalState(String name) 
	                         throws CategoricalStateDoesNotExistException{
		
		for(CategoricalStatesEntity teste: listEntity){
			if (teste.name.compareTo(name) == 0){
				return teste; 
			}
		}
		
		// não foi achado!!! Retornar exceção... 
		
		throw new CategoricalStateDoesNotExistException(); 
		
	}
	
	public static List<CategoricalStatesEntity> getListEntity(){
		return listEntity; 
	}

}
