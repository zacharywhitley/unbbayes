package unbbayes.prs.mebn.kb.powerloom;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.ContextNode;
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
	
	public PowerLoomFacade(String moduloName, Module module){
		this.moduleName = moduloName; 
		this.module = module; 
	}
	
	public PowerLoomFacade(String moduloName){
		this.moduleName = moduloName;  
	}
	
	public boolean existEntity(String name) {
		
		Stella_Object so = PLI.sGetObject(name, moduleName, environment); 
		
		if (so != null){
			return true;
		}else{
			return false; 
		}
	}

	public String searchFinding(String nameRV, List<String> listArguments) {
		
		String finding = "";
		
		finding+="all (?x)"; 
		finding+="("; 
		finding+= nameRV;
		
		for(String argument: listArguments){
			finding+= " " + argument; 
		}
		
		finding += " ?x"; 
		finding+= ")";
		
		PlIterator iterator = PLI.sRetrieve(finding, moduleName, environment); 
		
		if(iterator.nextP()){
			return PLI.getNthString(iterator, 0, module, environment); 
		}else{
			return null; 
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
