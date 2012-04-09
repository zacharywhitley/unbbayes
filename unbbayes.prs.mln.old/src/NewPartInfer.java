

import tuffy.ground.partition.PartitionScheme;
import tuffy.infer.InferPartitioned;
import tuffy.parse.CommandOptions;
import tuffy.util.Settings;
import tuffy.util.UIMan;


public class NewPartInfer extends NewInfer{

	public String run(CommandOptions opt){
		String result = "";
		
		UIMan.println(">>> Running partition-aware inference.");
		setUp(opt);

		ground();
		
		InferPartitioned ip = new InferPartitioned(grounding, dmover);
		
		if(options.maxFlips == 0){
			options.maxFlips = 10 * grounding.getNumAtoms();
		}
		if(options.maxTries == 0){
			options.maxTries = 1;
		}
		PartitionScheme pmap = ip.getPartitionScheme();
		int ncomp = pmap.numComponents();
		int nbuck = ip.getNumBuckets();
		String sdata = UIMan.comma(ncomp) + (ncomp > 1 ? " components" : "component");
		sdata += " (grouped into ";
		sdata += UIMan.comma(nbuck) + (nbuck > 1 ? " buckets" : " bucket)");
		
		
		Settings settings = new Settings(); 
		Double fpa = ((double)options.maxFlips)/grounding.getNumAtoms();
		
		if(!opt.marginal || opt.dual){
			UIMan.println(">>> Running MAP inference on " + sdata);
			String mapfout = options.fout;
			if(opt.dual) mapfout += ".map";
			
			settings.put("task", "MAP");
			settings.put("ntries", new Integer(options.maxTries));
			settings.put("flipsPerAtom", fpa);
			double lowCost = ip.infer(settings);
			
			UIMan.println("### Best answer has cost " + UIMan.decimalRound(2,lowCost));
			UIMan.println(">>> Writing answer to file: " + mapfout);
			result += dmover.NewdumpTruthToFile(mln.relAtoms, mapfout);
		}
		
		if(opt.marginal || opt.dual){
			UIMan.println(">>> Running marginal inference on " + sdata);
			String mfout = options.fout;
			if(opt.dual) mfout += ".marginal";
			
			settings.put("task", "MARGINAL");
			settings.put("nsamples", new Integer(options.mcsatSamples));
			settings.put("flipsPerAtom", fpa);
			ip.infer(settings);
			
			UIMan.println(">>> Writing answer to file: " + mfout);
			result += dmover.NewdumpProbsToFile(mln.relAtoms, mfout);
		}

		cleanUp();
		return result;
	}
}
