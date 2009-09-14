package unbbayes.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.Debug;

public class EvaluationAnalysis {
	
	protected ProbabilisticNetwork net;
	protected int statesProduct;
	protected IEvaluation evaluation;
	
	public void computeSampleSizeByErrorVariance(String netFileName, List<String> targetNodeNameList,
			List<String> evidenceNodeNameList, float error, float exactPcc) throws Exception {
		
		loadNetwork(netFileName);
		computeStatesProduct(targetNodeNameList, evidenceNodeNameList);
		
		int times = 10;
		float approximatePcc;
		long init;
		long end;
		do {
			evaluation = new MemoryEfficientApproximateEvaluation(times*statesProduct);
			init = System.currentTimeMillis();
			evaluation.evaluate(net, targetNodeNameList, evidenceNodeNameList, true);
			end = System.currentTimeMillis();
//			Debug.setDebug(true);
//			Debug.println("Sample size: " + (times-1) + " * " + statesProduct + " = " + ((times-1)*statesProduct));
//			Debug.println("Time elapsed for evaluating: " + (float)(end-init)/1000);
//			Debug.setDebug(false);
			approximatePcc = evaluation.getEvidenceSetPCC();
			times *= 10;
			System.gc();
		} while (exactPcc + error < approximatePcc || approximatePcc < exactPcc - error);
		
		Debug.setDebug(true);
		Debug.println("Target: "+ targetNodeNameList.get(0));
		Debug.print("Evidences: ");
		for (String evidence : evidenceNodeNameList) {
			Debug.print(evidence + " ");
		}
		Debug.println("");
		Debug.println("States product: " + statesProduct);
		Debug.println("Sample size: " + (times/10) + " * " + statesProduct + " = " + ((times/10)*statesProduct));
		Debug.println("Time elapsed for evaluating: " + (float)(end-init)/1000 + " seconds");
		Debug.println("Exact Pcc: " + exactPcc);
		Debug.println("Approximate Pcc: " + approximatePcc);
		Debug.println("Error Pcc: " + error);
		Debug.println("");
		Debug.setDebug(false);
	}
	
	public void computeExactSampleSize(String netFileName, List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) throws Exception {
		
		loadNetwork(netFileName);
		computeStatesProduct(targetNodeNameList, evidenceNodeNameList);
		
		float exactPcc;
		long init;
		long end;
		evaluation = new ExactEvaluation();
		init = System.currentTimeMillis();
		evaluation.evaluate(net, targetNodeNameList, evidenceNodeNameList, true);
		end = System.currentTimeMillis();
//			Debug.setDebug(true);
//			Debug.println("Sample size: " + (times-1) + " * " + statesProduct + " = " + ((times-1)*statesProduct));
//			Debug.println("Time elapsed for evaluating: " + (float)(end-init)/1000);
//			Debug.setDebug(false);
		exactPcc = evaluation.getEvidenceSetPCC();
		System.gc();
		
		Debug.setDebug(true);
		Debug.println("Target: "+ targetNodeNameList.get(0));
		Debug.print("Evidences: ");
		for (String evidence : evidenceNodeNameList) {
			Debug.print(evidence + " ");
		}
		Debug.println("");
		Debug.println("States product: " + statesProduct);
		Debug.println("Time elapsed for evaluating: " + (float)(end-init)/1000 + " seconds");
		Debug.println("Exact Pcc: " + exactPcc);
		Debug.println("");
		Debug.setDebug(false);
	}
	
	/**
	 * Computes the product of all states.
	 */
	protected void computeStatesProduct(List<String> targetNodeNameList,
			List<String> evidenceNodeNameList) {
		TreeVariable[] targetNodeList = new TreeVariable[targetNodeNameList.size()];
		TreeVariable[] evidenceNodeList = new TreeVariable[evidenceNodeNameList.size()];
		statesProduct = 1;
		int targetStatesProduct = 1;
		int evidenceStatesProduct = 1;

		// Create list of target TreeVariable
		int count = 0;
		for (String targetNodeName : targetNodeNameList) {
			Node targetNode = net.getNode(targetNodeName);

			targetNodeList[count] = (TreeVariable) targetNode;

			targetStatesProduct *= targetNode.getStatesSize();
			count++;
		}

		// Create list of evidence TreeVariable
		count = 0;
		for (String evidenceNodeName : evidenceNodeNameList) {
			Node evidenceNode = net.getNode(evidenceNodeName);

			evidenceNodeList[count] = (TreeVariable) evidenceNode;

			evidenceStatesProduct *= evidenceNode.getStatesSize();
			count++;
		}

		statesProduct = targetStatesProduct * evidenceStatesProduct;
	}
	
	protected void loadNetwork(String netFileName) throws LoadException, IOException, JAXBException {
		File netFile = new File(netFileName);
		String fileExt = netFileName.substring(netFileName.length() - 3);

		BaseIO io = null;
		if (fileExt.equalsIgnoreCase("xml")) {
			io = new XMLBIFIO();
		} else if (fileExt.equalsIgnoreCase("net")) {
			io = new NetIO();
		} else {
			throw new LoadException(
					"The network must be in XMLBIF 0.5 or NET format!");
		}
		net = io.load(netFile);
	}
	
	public static void main(String[] args) throws Exception {
		
		EvaluationAnalysis an = new EvaluationAnalysis();
		
		String netFileName = "src/test/resources/testCases/evaluation/AirID.xml";
		List<String> targetNodeNameList = new ArrayList<String>();
		
		List<String> evidenceNodeNameList = new ArrayList<String>();
		
		boolean approximate = false;
		
		float error = .0025f;
		
		targetNodeNameList = new ArrayList<String>();
		targetNodeNameList.add("TargetType");

		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");

		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2228f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2373f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2382f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2867f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
		an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6102f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2785f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2781f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .2891f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6548f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .3172f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .3876f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .3872f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .3967f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6561f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6558f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6616f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
//		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .4214f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
//		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6755f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
//		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6932f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
//		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6931f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
		
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
//		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .6978f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
	
		evidenceNodeNameList = new ArrayList<String>();
		evidenceNodeNameList.add("UHRR_Confusion");
		evidenceNodeNameList.add("ModulationFrequency");
		evidenceNodeNameList.add("CenterFrequency");
		evidenceNodeNameList.add("PRI");
		evidenceNodeNameList.add("PRF");
		
		if (approximate) {
			an.computeSampleSizeByErrorVariance(netFileName, targetNodeNameList, evidenceNodeNameList, error, .7095f);
		} else {
			an.computeExactSampleSize(netFileName, targetNodeNameList, evidenceNodeNameList);
		}
	}

}
