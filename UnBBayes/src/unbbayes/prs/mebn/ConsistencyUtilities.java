package unbbayes.prs.mebn;

public class ConsistencyUtilities {

	public static boolean hasCycle(DomainResidentNode origin, DomainResidentNode destination){
		
		/* Caso trivial */
		if(origin == destination){
			return true; 
		}
		
		for (DomainResidentNode teste: origin.getResidentNodeFatherList()){
			if (hasCycle(teste, destination)){
				return true; 
			}
		}
		
		for(GenerativeInputNode teste: origin.getInputNodeFatherList()){
			if (hasCycle(teste, destination)){
				return true; 
			}
		}
		
		return false; 
	}

	/**
	 * Verifies if exists a cycle from the origin to destination. 
	 * 
	 * @param origin
	 * @param destination
	 * @return
	 */
	public static  boolean hasCycle(GenerativeInputNode origin, DomainResidentNode destination){
		
		if(origin.getInputInstanceOf() != null){
			if(origin.getInputInstanceOf() instanceof ResidentNode){
				
				if (hasCycle((DomainResidentNode)(origin.getInputInstanceOf()), destination)){
					return true; 
				}
				
			}
		}
		
		return false; 
	}
	
}
