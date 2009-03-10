package unbbayes.prs.mebn.ssbn.laskeyalgorithm;

import unbbayes.prs.mebn.ssbn.Parameters;

public class LaskeyAlgorithmParameters extends Parameters{

	public static final int RECURSIVE_LIMIT = 1; 
	
	public LaskeyAlgorithmParameters(){
		addParameter(RECURSIVE_LIMIT, "100"); 
	}
	
}
