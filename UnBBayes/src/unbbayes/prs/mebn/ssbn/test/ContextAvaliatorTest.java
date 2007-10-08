package unbbayes.prs.mebn.ssbn.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.EntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;

/*
 * only tests... 
 */
public class ContextAvaliatorTest {

	KnowledgeBase kb; 
	KBFacade kbFacade; 
	static MultiEntityBayesianNetwork mebn; 
	
	/* 
	 * considere que o sistema pegou o nó a ser avaliado e 
	 * tem as entidades que serão utilizadas na avaliação destes... 
	 */
     public static void main(String[] args) throws Exception {
		
		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
		
		kb.loadModule(new File("examples/mebn/sample.plm"));
		KBFacade kbFacade = new PowerLoomFacade("/PL-KERNEL-KB/PL-USER/GENERATIVE_MODULE/FINDINGS_MODULE"); 
		
		PrOwlIO io = new PrOwlIO(); 
		mebn = io.loadMebn(new File("examples/mebn/StarTrek30.owl"));
		
		/* Avaliação de um nó de contexto */
		/* nó IsOwnStarship */
		
		ContextNode context; 
		List<OVInstance> ovInstances;
		EntityInstance ei; 
		OrdinaryVariable ov; 
		OVInstance oi; 
		Boolean result; 
		List<String> listResult; 
		
	    System.out.println("Caso 1: IsOwnStarship(!ST0)"); 
		context = mebn.getContextNode("CX10"); 
		ei = EntityInstance.getInstance("!ST0", mebn.getTypeContainer().getType("Starship_label")); 
		ov = mebn.getMFragByName("Starship_MFrag").getOrdinaryVariableByName("st"); 
		oi = OVInstance.getInstance(ov, ei); 
		ovInstances = new ArrayList<OVInstance>(); 
		ovInstances.add(oi); 
		result = kb.evaluateSimpleFormula(context, ovInstances); 
		
	    System.out.println("Caso 2: IsOwnStarship(!ST1)"); 
		context = mebn.getContextNode("CX10"); 
		ei = EntityInstance.getInstance("!ST1", mebn.getTypeContainer().getType("Starship_label")); 
		ov = mebn.getMFragByName("Starship_MFrag").getOrdinaryVariableByName("st"); 
		oi = OVInstance.getInstance(ov, ei); 
		ovInstances = new ArrayList<OVInstance>(); 
		ovInstances.add(oi); 
		result = kb.evaluateSimpleFormula(context, ovInstances); 
		
	    System.out.println("Caso 3: z = StarshipZone(!ST0)"); 
		context = mebn.getContextNode("CX11"); 
		ei = EntityInstance.getInstance("!ST0", mebn.getTypeContainer().getType("Starship_label")); 
		ov = mebn.getMFragByName("Starship_MFrag").getOrdinaryVariableByName("st"); 
		oi = OVInstance.getInstance(ov, ei); 
		ovInstances = new ArrayList<OVInstance>(); 
		ovInstances.add(oi); 
		listResult = kb.evaluateComplexFormula(context, ovInstances); 
		for(String r: listResult){
			System.out.println(r); 
		}
		
	    System.out.println("Caso 4: z = StarshipZone(!ST1)"); 
		context = mebn.getContextNode("CX11"); 
		ei = EntityInstance.getInstance("!ST1", mebn.getTypeContainer().getType("Starship_label")); 
		ov = mebn.getMFragByName("Starship_MFrag").getOrdinaryVariableByName("st"); 
		oi = OVInstance.getInstance(ov, ei); 
		ovInstances = new ArrayList<OVInstance>(); 
		ovInstances.add(oi); 
		listResult = kb.evaluateComplexFormula(context, ovInstances); 
		for(String r: listResult){
			System.out.println(r); 
		}
		
		
     }	
	
}
