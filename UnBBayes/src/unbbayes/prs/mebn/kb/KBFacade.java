package unbbayes.prs.mebn.kb;

import java.util.List;

public interface KBFacade {

	/** 
	 * Verifica se existe a entidade na base, retornando o seu tipo em caso positivo, 
	 * ou null caso contrario. 
	 */
	public boolean existEntity(String name);

	/**
	 * Verifica se existe o finding na base. Caso positivo, retorna o valor (ou seja, 
	 * o estado para a variável ordinária, dados os argumentos), caso negativo, 
	 * retorna null.  
	 * @param nameRV
	 * @param listArguments
	 * @return
	 */
	public String searchFinding(String nameRV, List<String> listArguments);

	/**
	 * Retorna todas as entidades de um dado tipo. 
	 * @param type
	 * @return
	 */
	public List<String> getEntityByType(String type);
	
	
	/**
	 * Executa um comando ask (nó de contexto), retornando true ou false. 
	 * O comando deve ser construído através de método da classe KnowledgeBase. 
	 * @param query
	 */
	public boolean executeAsk(String query); 
	
}
