/**
 * 
 */
package unbbayes;

import java.io.File;

import unbbayes.io.mebn.UbfIO2;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.extension.ontology.protege.PROWL2KnowledgeBaseBuilder;

/**
 * 
 * This class runs UnBBayes in text-mode
 * Currently, it works only for UnBBayes-MEBN and PR-OWL 2 by
 * loading a UBF version 2 (i.e. UBF + PR-OWL 2), 
 * generating SSBN using Laskey algorithm and HermiT,
 * and printing into sysout the result.
 * 
 * It may not be useful now, but it might a good sample as API.
 * 
 * Currently, it works only for a single query.
 * 
 * We are expecting to extend this class in order to execute
 * other modules as text mode as well.
 * @author Shou Matsumoto
 *
 */
public class PROWL2TextModeRunner extends TextModeRunner {

	/**
	 * 
	 */
	public PROWL2TextModeRunner() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Reads a UBF + PR-OWL 2, and executes a query using laskey's algorithm + HermiT
	 * @param args : ubfVer2File queryNodeName [argumentsOfQuery]+
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("params: <ubfVer2File> <queryNodeName> [<argumentsOfQuery>]*");
			return;
		}
		try {
			
			PROWL2TextModeRunner textModeRunner = new PROWL2TextModeRunner();
			
			// load ubf/owl2
			UbfIO2 ubf = UbfIO2.getInstance();
			File ubfFile = new File(args[0]);
			
			
			// use OWLAPI instead of protege 4.1, because protege4.1 pops up a Swing frame
			ubf.setProwlIO(OWLAPICompatiblePROWL2IO.newInstance()); 
			
			MultiEntityBayesianNetwork mebn = ubf.loadMebn(ubfFile);

			
			// initialize kb
			KnowledgeBase knowledgeBase = new PROWL2KnowledgeBaseBuilder().buildKB(mebn, null);
			knowledgeBase = textModeRunner.createKnowledgeBase(knowledgeBase, mebn);
			
			// load kb
			knowledgeBase.loadModule(ubfFile, true);
			
			
			knowledgeBase = textModeRunner.fillFindings(mebn,knowledgeBase);
			
			
			// extract params for queries
			String[] queryParam = new String[args.length - 2];
			for (int i = 0; i < queryParam.length; i++) {
				queryParam[i] = args[i+2];
			}
			
			ProbabilisticNetwork net = textModeRunner.callLaskeyAlgorithm(
						mebn, 
						knowledgeBase, 
						args[1], 
						queryParam
					);
			
			// do something to net if you want to do so
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
