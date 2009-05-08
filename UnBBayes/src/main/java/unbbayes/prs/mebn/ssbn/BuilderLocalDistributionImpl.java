package unbbayes.prs.mebn.ssbn;

import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.cptgeneration.CPTForSSBNNodeGenerator;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

public class BuilderLocalDistributionImpl implements IBuilderLocalDistribution {

	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	
	
	private BuilderLocalDistributionImpl(){
		
	}
	
	public static BuilderLocalDistributionImpl newInstance(){
		return new BuilderLocalDistributionImpl();
	}
	
	public void buildLocalDistribution(SSBN ssbn) {
		
		ProbabilisticNetwork pn; 
		
		try {
			pn =  new ProbabilisticNetwork(this.resource.getString("DefaultNetworkName"));
			List<SSBNNode> listSSBNNode = SimpleSSBNNodeUtils.translateSimpleSSBNNodeListToSSBNNodeList(ssbn.getSimpleSsbnNodeList(), pn);
		    ssbn.setSsbnNodeList(listSSBNNode); 
			ssbn.setProbabilisticNetwork(pn); 
		} catch (SSBNNodeGeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		System.out.println("\n\nTranslated\n\n");
		
	    CPTForSSBNNodeGenerator build = new CPTForSSBNNodeGenerator();
	    
	    try {
			build.generateCPTForAllSSBNNodes(ssbn.getSsbnNodeList().get(0));
		} catch (MEBNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SSBNNodeGeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


	
	
}
