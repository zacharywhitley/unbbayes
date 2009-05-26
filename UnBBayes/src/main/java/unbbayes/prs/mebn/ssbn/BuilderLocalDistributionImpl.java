package unbbayes.prs.mebn.ssbn;

import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.cptgeneration.CPTForSSBNNodeGenerator;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

public class BuilderLocalDistributionImpl implements IBuilderLocalDistribution {

	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	
	
	private BuilderLocalDistributionImpl(){
		
	}
	
	public static BuilderLocalDistributionImpl newInstance(){
		return new BuilderLocalDistributionImpl();
	}
	
	public void buildLocalDistribution(SSBN ssbn) throws MEBNException, SSBNNodeGeneralException {
		
		ProbabilisticNetwork pn; 
		
		try {
			pn =  new ProbabilisticNetwork(this.resource.getString("DefaultNetworkName"));
			List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(ssbn.getSimpleSsbnNodeList(), pn);
		    ssbn.setSsbnNodeList(listSSBNNode); 
			ssbn.setProbabilisticNetwork(pn); 
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			throw e; 
		} catch (ImplementationRestrictionException e) {
			//This exception don't should be throw in a correct algorithm. 
			e.printStackTrace();
			throw new RuntimeException(e.getMessage()); 
		} 
		
		ssbn.getLogManager().appendln("Simple Nodes translated to SSBNNodes"); 
		
	    CPTForSSBNNodeGenerator build = new CPTForSSBNNodeGenerator(ssbn.getLogManager());
	    
	    if(ssbn.getSsbnNodeList().size() > 0){
	    	build.generateCPTForAllSSBNNodes(ssbn.getSsbnNodeList().get(0));
	    }else{
	    	throw new SSBNNodeGeneralException(resource.getString("NotNodeInSSBN")); 
	    }
		
	}


	
	
}
