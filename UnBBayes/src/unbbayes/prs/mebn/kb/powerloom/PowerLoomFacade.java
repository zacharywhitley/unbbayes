package unbbayes.prs.mebn.kb.powerloom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.ssbn.OVInstance;
import edu.isi.powerloom.Environment;
import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.TruthValue;
import edu.isi.stella.Module;
import edu.isi.stella.Stella_Object;

public class PowerLoomFacade implements KBFacade{

	private String moduleName = ""; 
	private Module module = null; 
	private Environment environment = null; 
	
	public PowerLoomFacade(String moduleName, Module module){
		this.moduleName = moduleName; 
		this.module = module; 
	}
	
	public PowerLoomFacade(String moduleName){
		this.moduleName = moduleName;  
	}
	
	public boolean existEntity(String name) {
		
		Stella_Object so = PLI.sGetObject(name, moduleName, environment); 
		
		if (so != null){
			return true;
		}else{
			return false; 
		}
	}

//	public String searchFinding(String nameRV, List<String> listArguments) {
//		
//		String finding = "";
//		
//		finding+="all (?x)"; 
//		finding+="("; 
//		finding+= nameRV;
//		
//		for(String argument: listArguments){
//			finding+= " " + argument; 
//		}
//		
//		finding += " ?x"; 
//		finding+= ")";
//		
//		PlIterator iterator = PLI.sRetrieve(finding, moduleName, environment); 
//		
//		if(iterator.nextP()){
//			return PLI.getNthString(iterator, 0, module, environment); 
//		}else{
//			return null; 
//		}
//		
//	}
	
    public StateLink searchFinding(DomainResidentNode randomVariable, Collection<OVInstance> listArguments) {
		
		String finding = "";
		
		if(randomVariable.getTypeOfStates() == DomainResidentNode.BOOLEAN_RV_STATES){
			finding+= randomVariable.getName() + " ";
			for(OVInstance argument: listArguments){
				finding+= " " + argument.getEntity().getInstanceName(); 
			}
			TruthValue value = PLI.sAsk(finding, moduleName, environment);
			
			StateLink exactValue = null; 
			//TODO throw a exception when the node dont have the argument... kb inconsistency. 
			if(PLI.isTrue(value)){
				exactValue = randomVariable.getPossibleValueByName("true"); 
			}else{
				if(PLI.isFalse(value)){
					exactValue = randomVariable.getPossibleValueByName("false"); 
				}else{
					if(PLI.isUnknown(value)){
						exactValue = null; 
					}
				}
			}

			return exactValue; 
			
		}else{
			finding+="all ?x"; 
			finding+="("; 
			finding+= randomVariable.getName();
			
			for(OVInstance argument: listArguments){
				finding+= " " + argument.getEntity().getInstanceName(); 
			}
			
			finding += " ?x"; 
			finding+= ")";
			PlIterator iterator = PLI.sRetrieve(finding, moduleName, environment); 
			//TODO throw a exception when the search return more than one result...
			
			if(iterator.nextP()){
				String state = PLI.getNthString(iterator, 0, module, environment);
				return randomVariable.getPossibleValueByName(state); 
			}else{
				return null; 
			}	
		}
		
		
	}
	

	public List<String> getEntityByType(String type) {
		
		List<String> list = new ArrayList<String>(); 
		
		PlIterator iterator = PLI.sGetConceptInstances(type, moduleName, environment); 
		while (iterator.nextP()){
			list.add(PLI.getNthString(iterator, 0, module, environment)); 
		}
			
		return list; 
	}

	public boolean executeAsk(String query) {

		TruthValue value = PLI.sAsk(query, moduleName, environment); 
		
		return false;
	}

	public Boolean evaluateSimpleFormula(ContextNode context, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<OVInstance> evaluateComplexFormula(ContextNode context, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

}
