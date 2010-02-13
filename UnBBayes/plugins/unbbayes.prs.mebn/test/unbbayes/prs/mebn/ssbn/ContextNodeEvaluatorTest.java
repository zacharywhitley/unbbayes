package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/**
 * Test the class ContextNodeAvaliator
 */
public class ContextNodeEvaluatorTest extends TestCase{

	private static String FILE_GENERATIVE = "examples/mebn/KnowledgeBase/KnowledgeBaseGenerative.plm";
	private static String FILE_INPUT = "examples/mebn/KnowledgeBase/KnowledgeBaseWithStarshipZoneST4ver2.plm";
	private static String FILE_MTHEORY = "examples/mebn/StarTrek52.owl"; 
	
	private ResourceBundle resourceFiles = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.resources.ResourceFiles.class.getName());
	
	private MultiEntityBayesianNetwork mebn = null; 
	KnowledgeBase kb = null;
	
	@Before
	public void setUp() throws Exception {
		
		System.out.println("SetUp");
		mebn = loadStarTrekOntologyExample(); 
		
		kb = PowerLoomKB.getNewInstanceKB(); 
		kb.createGenerativeKnowledgeBase(mebn); 
		
		loadFindingsSet(kb, resourceFiles.getString("StarTrekKB_Situation1")); 
	
		System.out.println("SetUp End");
	}

	@After
	public void tearDown() throws Exception {
		mebn = null; 
	}
	

	
//	public void testEvaluateUncertaintyReferenceContextCase(){
//		
//		kb.clearFindings(); 
//		loadFindingsSet(kb, resourceFiles.getString("StarTrekKB_Situation1")); 
//		
//		//Working with the StarshipMFrag
//		
//		//ST= ST0!, ST1!, ST2!, ST3!, ST4!, ST5!
//		//Z = Z0!, Z1!, Z2
//		
//		MFrag starshipMFrag = mebn.getMFragByName("Starship_MFrag"); 
//		MFragInstance starshipMFragInstanced = MFragInstance.getInstance(starshipMFrag);
//		
//		Type type = mebn.getTypeContainer().getType("Starship_label"); 
//		LiteralEntityInstance literalEntityInstance = LiteralEntityInstance.getInstance("ST0", type); 
//		OrdinaryVariable ov = new OrdinaryVariable("st", type, starshipMFrag); 
//		OVInstance ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
//		try {
//			starshipMFragInstanced.addOVValue(ov, literalEntityInstance.getInstanceName());
//		} catch (MFragContextFailException e1) {
//			e1.printStackTrace();
//			fail(); 
//		} 
//		
//		ContextNodeEvaluator evaluator = new ContextNodeEvaluator(kb); 
//		
//		try {
//			evaluator.evaluateMFragContextNodes(starshipMFragInstanced);
//		} catch (ImplementationRestrictionException e) {
//			e.printStackTrace();
//			fail(); 
//		} catch (SSBNNodeGeneralException e) {
//			e.printStackTrace();
//			fail(); 
//		} catch (OVInstanceFaultException e) {
//			e.printStackTrace();
//			fail(); 
//		} 
//		
//		System.out.println("Executed");
//	}
	
	private MultiEntityBayesianNetwork loadStarTrekOntologyExample() {
		UbfIO io = UbfIO.getInstance(); 
		MultiEntityBayesianNetwork mebn = null; 
		
		try {
			mebn = io.loadMebn(new File(resourceFiles.getString("UnBBayesUBF")));
		} catch (Exception e) {
			e.printStackTrace();
			fail(); 
		}
		return mebn;
	}
	
	private void loadFindingsSet(KnowledgeBase kb, String nameOfFile) {
		try {
			kb.loadModule(new File(nameOfFile), true);
		} catch (Exception e) {
			e.printStackTrace();
			fail(); 
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    public static void main(String[] args) throws Exception {
		MultiEntityBayesianNetwork mebn = null; 
    	
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		
		PrOwlIO io = new PrOwlIO(); 
		try {
			mebn = io.loadMebn(new File(FILE_MTHEORY));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			kb.createEntityDefinition(entity);
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				kb.createRandomVariableDefinition(resident);
			}
		}
		
//		kb.loadModule(new File(FILE_GENERATIVE), false); 
		kb.loadModule(new File(FILE_INPUT), true); 
		
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb); 
		
		MFrag mFrag = mebn.getMFragByName("DangerToSelf_MFrag");


		LiteralEntityInstance literalEntityInstance; 
		OVInstance ovInstance; 
		OrdinaryVariable ov; 
		
		Type type = null;
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		List<OrdinaryVariable> ordVariableList = new ArrayList<OrdinaryVariable>(); 
		
		type = mebn.getTypeContainer().getType("Starship_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("ST4", type); 
		ov = new OrdinaryVariable("st", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("TimeStep_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("T0", type); 
		ov = new OrdinaryVariable("t", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		Map<OrdinaryVariable, List<String>> map = kb.evaluateMultipleSearchContextNodeFormula(mFrag.getContextNodeList(), ovInstanceList); 
		
		for(OrdinaryVariable ovKey: map.keySet()){
			for(String s: map.get(ovKey)){
				System.out.println("Return:" + ovKey.getName() + "=" + s);
			}
			
		}
		
    }


	private static void mainAlternative() throws UBIOException {
		MultiEntityBayesianNetwork mebn = null; 
    	
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		
		kb.loadModule(new File("testeGenerativeStarship.plm"), false); 
		kb.loadModule(new File("testeFindingsStarship.plm"), true); 
		
		PrOwlIO io = new PrOwlIO(); 
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek.owl"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		
		LiteralEntityInstance literalEntityInstance; 
		OVInstance ovInstance; 
		OrdinaryVariable ov; 
		
		Type type = null;
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		List<OrdinaryVariable> ordVariableList = new ArrayList<OrdinaryVariable>(); 
		
		type = mebn.getTypeContainer().getType("Starship_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("ST0", type); 
		ov = new OrdinaryVariable("st", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("TimeStep_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("T0", type); 
		ov = new OrdinaryVariable("t", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("Zone_label"); 
		ov = new OrdinaryVariable("z", type, mFrag); 
		ordVariableList.add(ov); 
		
		System.out.println("MFrag: " + mFrag.getName());
		for(OVInstance ovInstanc: ovInstanceList){
			System.out.println("OVInstance: " + ovInstanc);
		}
		for(OrdinaryVariable ovInstanc: ordVariableList){
			System.out.println("OV: " + ovInstanc);
		}
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstanc: ovInstanceList){
			ovList.add(ovInstanc.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		ContextNodeEvaluator avaliator = new ContextNodeEvaluator(kb); 
		
		evaluateContextNodes(mFrag, ovInstanceList, ordVariableList, avaliator, kb);
	}	
    

	/**
	 * True - All nodes ok
	 * False - Use default distribution
	 * 
	 * List<entities> resultado. -> normal, busca geral. 
	 * 
	 * @param mFrag
	 * @param ovInstanceList
	 * @param ordVariableList
	 */
	public static void evaluateContextNodes(MFrag mFrag, List<OVInstance> ovInstanceList, List<OrdinaryVariable> ordVariableList, 
			ContextNodeEvaluator avaliator, KnowledgeBase kb){
		
		Debug.setDebug(true); 
		
		Collection<ContextNode> contextNodeList; 
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		contextNodeList = mFrag.getContextByOVCombination(ovList); 
		
		Debug.println(""); 
		Debug.println("Evaluating... "); 
		Debug.println(""); 
		
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getFormula()); 
			try{
				if(!avaliator.evaluateContextNode(context, ovInstanceList)){
					Debug.println("Result = FALSE. Use default distribution "); 
//					return false;  //use the default distribution. 
				}
			}
			catch(OVInstanceFaultException e){
				try {
					Debug.println("OVInstance Fault. Try evaluate a search. "); 
					List<String> result = null;
					
					try {
						result = avaliator.evalutateSearchContextNode(context, ovInstanceList);
					} catch (OVInstanceFaultException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return; 
					}
					
					if(result.isEmpty()){
						
						OrdinaryVariable rigthTerm = context.getFreeVariable(); 
						result = kb.getEntityByType(rigthTerm.getValueType().getName());
						
						Debug.println("No information in Knowlege Base"); 
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
						
//						return false; 
					}else{
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
//						return true; 
					}
				} catch (InvalidContextNodeFormulaException ie) {
					Debug.println("Invalid Context Node: the formula don't is accept."); 
					// TODO Auto-generated catch block
					ie.printStackTrace();
				} 
			}
			Debug.println(""); 
		}
		
//		return true; 
		
	}
	
}
