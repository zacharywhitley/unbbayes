package unbbayes.prs.mebn.resources;

import java.util.ListResourceBundle;

public class ResourcesSSBNAlgorithmLog_pt extends ListResourceBundle {

    /**
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * The resources
	 */
	static final Object[][] contents =
	{	
		{"001_Title","Generation of SSBN"}, 
		
		{"002_Algorithm","Algorithm"},
		{"002_001_LaskeyAlgorithm", "Laskey's Algorithm"},
		{"002_002_GiaAlgorithm", "Gia's Algorithm"},
		
		{"003_Step1_Initialization",  "Step 1: Initialization"}, 
		{"004_Step2_BuildingGrandBN", "Step 2: Build Grand BN"}, 
		{"005_Step3_PruneGrandBN",    "Step 3: Prune Grand BN"}, 
		{"006_Step4_BuildCPT",        "Step 4: Build CPT's"}, 
		{"010_StepFinished",          "Step Finished"}, 
		
		{"011_BuildingSSBNForQueries", "Building the SSBNNodes for the queries"}, 
		{"012_BuildingSSBNForFindings", "Building the SSBNNodes for the findings"}, 
		
		
		{"007_ExecutionSucces",  "Execution Success"},
		{"008_ExecutionFail", "Execution Fail"}, 
		{"009_Time", "Time"},
			
		{"CycleFoundException","Cycle found"}
	};
}
