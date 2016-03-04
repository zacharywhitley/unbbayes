package unbbayes.prs.mebn.ssbn.bayesballalgorithm;

import unbbayes.prs.mebn.ssbn.ISSBNGenerator;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;

public class BayesBallSSBNGeneratorBuilder implements ISSBNGeneratorBuilder{

	private String name = "Bayes-Ball Algorithm"; 
	
	public ISSBNGenerator buildSSBNGenerator() throws InstantiationException {
		return new BayesBallSSBNGenerator();
	}

	public String getName() {
		return "Bayes-Ball Algorithm";
	}

	public void setName(String name) {
		this.name = name; 
	}

}
