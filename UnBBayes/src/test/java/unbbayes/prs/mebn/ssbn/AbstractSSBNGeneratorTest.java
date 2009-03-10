package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;
import unbbayes.prs.mebn.ssbn.giaalgorithm.AbstractSSBNGenerator;
import unbbayes.prs.mebn.ssbn.giaalgorithm.ExplosiveSSBNGenerator;

public class AbstractSSBNGeneratorTest extends TestCase{

	public static final String TCU_UBF = "examples/mebn/CGU/Licitacao04.ubf"; 
	public static final String KB_FINDING_FILE = "examples/mebn/CGU/kb.plm";
	
	private AbstractSSBNGenerator abstractSSBNGenerator; 
	
	public AbstractSSBNGeneratorTest(){
		super(); 
		abstractSSBNGenerator = new ExplosiveSSBNGenerator(); 
	}
	
	public void testEvaluateSearchContextNode(){
		
		MultiEntityBayesianNetwork mebn = loadTCUOntologyExample();
		KnowledgeBase kb = createGenerativeKnowledgeBase(mebn);
		loadFindingModule(kb);
		abstractSSBNGenerator.setKnowledgeBase(kb); 
		
		MFrag mFrag = mebn.getMFragByName("IrregularidadeLicitacaoMF");
		
		List<OrdinaryVariable> ovFaultList = new ArrayList<OrdinaryVariable>(); 
		ovFaultList.add(mFrag.getOrdinaryVariableByName("emp")); 
		ovFaultList.add(mFrag.getOrdinaryVariableByName("empB")); 
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		OrdinaryVariable lic = mFrag.getOrdinaryVariableByName("lic"); 
		OVInstance ovInstance = OVInstance.getInstance(lic, LiteralEntityInstance.getInstance("LICITACAO1", lic.getValueType())); 
		ovInstanceList.add(ovInstance); 
		
		try {
			List<OVInstance> listResult = abstractSSBNGenerator.getContextNodeAvaliator().evaluateSearchContextNode(
					mFrag, ovFaultList, ovInstanceList);
		    
			assertNotNull(listResult); 
			
			System.out.println("Print result: ");
			for(OVInstance ovInst: listResult){
				System.out.println(ovInst);
			}
			
		} catch (ImplementationRestrictionException e) {
			e.printStackTrace();
			fail(); 
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			fail(); 
		} 
		
	}
	
	private MultiEntityBayesianNetwork loadTCUOntologyExample() {
		UbfIO io = UbfIO.getInstance(); 
		MultiEntityBayesianNetwork mebn = null; 
		
		try {
			mebn = io.loadMebn(new File(TCU_UBF));
		} catch (Exception e) {
			e.printStackTrace();
			fail(); 
		}
		return mebn;
	}
	
	private static KnowledgeBase createGenerativeKnowledgeBase(
			MultiEntityBayesianNetwork mebn) {
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			kb.createEntityDefinition(entity);
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				kb.createRandomVariableDefinition(resident);
			}
		}
		return kb;
	}
	
	private static void loadFindingModule(KnowledgeBase kb) {
		try {
			kb.loadModule(new File(KB_FINDING_FILE), true); 
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
