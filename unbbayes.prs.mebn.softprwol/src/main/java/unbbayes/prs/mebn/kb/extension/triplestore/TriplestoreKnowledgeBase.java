package unbbayes.prs.mebn.kb.extension.triplestore;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.exception.UBIOException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;

public class TriplestoreKnowledgeBase implements KnowledgeBase {

	private MultiEntityBayesianNetwork defaultMEBN;
	
	private IMEBNMediator defaultMediator;
	
	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		TriplestoreKnowledgeBase ret = new TriplestoreKnowledgeBase();
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}
	
	public void setDefaultMEBN(MultiEntityBayesianNetwork defaultMEBN) {
		this.defaultMEBN = defaultMEBN;
	}
	
	/**
	 * A {@link IMEBNMediator} to be used by this knowledge base if GUI or IO commands needs to be accessed.
	 * @param defaultMediator the defaultMediator to set
	 */
	public void setDefaultMediator(IMEBNMediator defaultMediator) {
		this.defaultMediator = defaultMediator;
	}	
	
	@Override
	public void clearKnowledgeBase() {
		// TODO Auto-generated method stub
		// No way!!! 
	}

	@Override
	public void clearFindings() {
		// TODO Auto-generated method stub
		// No way!!! 
	}

	@Override
	public void createGenerativeKnowledgeBase(MultiEntityBayesianNetwork mebn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createEntityDefinition(ObjectEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRandomVariableDefinition(ResidentNode resident) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertEntityInstance(ObjectEntityInstance entityInstance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertRandomVariableFinding(
			RandomVariableFinding randomVariableFinding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadModule(File file, boolean findingModule)
			throws UBIOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> evaluateSingleSearchContextNodeFormula(
			ContextNode context, List<OVInstance> ovInstances)
			throws OVInstanceFaultException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(
			List<ContextNode> contextList, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean existEntity(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StateLink searchFinding(ResidentNode randonVariable,
			Collection<OVInstance> listArguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getEntityByType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillFindings(ResidentNode resident) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsLocalFile(boolean isLoad) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSupportedLocalFileDescription(boolean isLoad) {
		// TODO Auto-generated method stub
		return null;
	}

}
