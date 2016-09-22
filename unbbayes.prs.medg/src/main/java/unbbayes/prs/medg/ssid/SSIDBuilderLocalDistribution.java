/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.io.log.ISSBNLogManager;
import unbbayes.io.log.IdentationLevel;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl;
import unbbayes.prs.mebn.ssbn.IBuilderLocalDistribution;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.cptgeneration.CPTForSSBNNodeGenerator;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * This is the SSID version of {@link BuilderLocalDistributionImpl}.
 * Again, a lot of code was copied because of private methods and static method calls
 * (that's why programmers should not use too much private methods and static methods).
 * 
 * @author Shou Matsumoto
 *
 */
public class SSIDBuilderLocalDistribution extends BuilderLocalDistributionImpl {
	
	private ISimpleSSIDNodeTranslator simpleSSBNNodeTranslator = SSIDNodeTranslator.newInstance();
	
	private IdentationLevel level1 = new IdentationLevel(null); 
	private IdentationLevel level2 = new IdentationLevel(level1); 
	private IdentationLevel level3 = new IdentationLevel(level2); 
	private IdentationLevel level4 = new IdentationLevel(level3); 
	private IdentationLevel level5 = new IdentationLevel(level4); 
	private IdentationLevel level6 = new IdentationLevel(level5);

	private ResourceBundle hybridResource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName(),
				Locale.getDefault(),
				SSIDBuilderLocalDistribution.class.getClassLoader());

	
	/**
	 * Constructor is not private to allow inheritance.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected SSIDBuilderLocalDistribution() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @deprecated use {@link #getInstance()} instead
	 * @see BuilderLocalDistributionImpl#newInstance()
	 * @return
	 */
	public static BuilderLocalDistributionImpl newInstance(){
		return new SSIDBuilderLocalDistribution();
	}
	
	/**
	 * default contructor method
	 */
	public static IBuilderLocalDistribution getInstance(){
		return new SSIDBuilderLocalDistribution();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.BuilderLocalDistributionImpl#buildLocalDistribution(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	public void buildLocalDistribution(SSBN ssbn) throws MEBNException,
			SSBNNodeGeneralException {

		
		ProbabilisticNetwork pn; 
		SimpleSSBNNode nodeOfQuery;
		
		
		ISSBNLogManager logManager = ssbn.getLogManager();
		try {
			// we do not need to create multiple networks anymore, because disconnected networks are behaving as separate BNs in the new core. 
			
			if (logManager != null) {
				logManager.skipLine(); 
			}
			
			if (ssbn.getNetwork() == null) {
				// Generating the SSBN network, because it was not generated yet.
				if (logManager != null) {
					logManager.printText(level1, false, "Generating the SSBN network");
				}
				pn =  new ProbabilisticNetwork(this.getHybridResource().getString("DefaultNetworkName"));
				List<SSBNNode> listSSBNNode = this.getSimpleSSBNNodeTranslator().translateSimpleSSBNNodeListToSSBNNodeList(ssbn.getSimpleSsbnNodeList(), pn);
				ssbn.setSsbnNodeList(listSSBNNode); 
				ssbn.setProbabilisticNetwork(pn); 
				if (logManager != null) {
					logManager.skipLine();
				}
			}
			
		} catch (SSBNNodeGeneralException e) {
			e.printStackTrace();
			throw e; 
		} catch (ImplementationRestrictionException e) {
			//This exception don't should be throw in a correct algorithm. 
			e.printStackTrace();
			throw new RuntimeException(e.getMessage()); 
		} 
		
		if (logManager != null) {
			logManager.printText(level2, false, "Simple Nodes translated to SSBNNodes");
		}
		
		// clearing simple ssbn nodes
		if (isClearSimpleSSBNNodeListAfterLPD()) {
			ssbn.getSimpleSsbnNodeList().clear();
			// clearing memory before we continue
//			System.gc();
		}

	    CPTForSSBNNodeGenerator build = this.getCptForSSBNNodeGeneratorBuilder().buildCPTForSSBNNodeGenerator(logManager);
	    
	    if(ssbn.getSsbnNodeList().size() > 0){
	    	if (logManager != null) {
				logManager.printText(level2, false, "Generate CPT for the SSBNNodes");
	    	}
	    	build.generateCPTForAllSSBNNodes(ssbn);
	    }else{
	    	throw new SSBNNodeGeneralException(getHybridResource().getString("NotNodeInSSBN")); 
	    }
	    
	}

	/**
	 * @return the level1
	 */
	public IdentationLevel getLevel1() {
		return level1;
	}

	/**
	 * @param level1 the level1 to set
	 */
	public void setLevel1(IdentationLevel level1) {
		this.level1 = level1;
	}

	/**
	 * @return the level2
	 */
	public IdentationLevel getLevel2() {
		return level2;
	}

	/**
	 * @param level2 the level2 to set
	 */
	public void setLevel2(IdentationLevel level2) {
		this.level2 = level2;
	}

	/**
	 * @return the level3
	 */
	public IdentationLevel getLevel3() {
		return level3;
	}

	/**
	 * @param level3 the level3 to set
	 */
	public void setLevel3(IdentationLevel level3) {
		this.level3 = level3;
	}

	/**
	 * @return the level4
	 */
	public IdentationLevel getLevel4() {
		return level4;
	}

	/**
	 * @param level4 the level4 to set
	 */
	public void setLevel4(IdentationLevel level4) {
		this.level4 = level4;
	}

	/**
	 * @return the level5
	 */
	public IdentationLevel getLevel5() {
		return level5;
	}

	/**
	 * @param level5 the level5 to set
	 */
	public void setLevel5(IdentationLevel level5) {
		this.level5 = level5;
	}

	/**
	 * @return the level6
	 */
	public IdentationLevel getLevel6() {
		return level6;
	}

	/**
	 * @param level6 the level6 to set
	 */
	public void setLevel6(IdentationLevel level6) {
		this.level6 = level6;
	}

	/**
	 * Resource file for this class. Changing this value will not change
	 * the routines inherent from superclasses.
	 * @return the hybridResource
	 */
	public ResourceBundle getHybridResource() {
		return hybridResource;
	}

	/**
	 * Resource file for this class. Changing this value will not change
	 * the routines inherent from superclasses.
	 * @param hybridResource the hybridResource to set
	 */
	public void setHybridResource(ResourceBundle hybridResource) {
		this.hybridResource = hybridResource;
	}


	/**
	 * @return the simpleSSBNNodeTranslator
	 */
	public ISimpleSSIDNodeTranslator getSimpleSSBNNodeTranslator() {
		return simpleSSBNNodeTranslator;
	}

	/**
	 * @param simpleSSBNNodeTranslator the simpleSSBNNodeTranslator to set
	 */
	public void setSimpleSSBNNodeTranslator(
			ISimpleSSIDNodeTranslator simpleSSBNNodeTranslator) {
		this.simpleSSBNNodeTranslator = simpleSSBNNodeTranslator;
	}

	

	
	
}
