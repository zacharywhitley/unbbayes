import tuffy.db.RDB;
import tuffy.infer.DataMover;
import tuffy.main.Infer;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;
import tuffy.parse.CommandOptions;
import tuffy.util.ExceptionMan;
import tuffy.util.UIMan;


public class NewInfer extends Infer{
	
	protected NewDataMover dmover = null;

	protected void loadMLN(MarkovLogicNetwork mln, RDB adb, CommandOptions opt){
		
		String[] progFiles = opt.fprog.split(",");
		mln.loadPrograms(progFiles);

		if(opt.fquery != null){
			String[] queryFiles = opt.fquery.split(",");
			mln.loadQueries(queryFiles);
		}
		
		if(opt.queryAtoms != null){
			UIMan.verbose(2, ">>> Parsing query atoms in command line");
			mln.parseQueryCommaList(opt.queryAtoms);
		}

		if(opt.cwaPreds != null){
			String[] preds = opt.cwaPreds.split(",");
			for(String ps : preds){
				Predicate p = mln.getPredByName(ps);
				if(p == null){
					mln.closeFiles();
					ExceptionMan.die("COMMAND LINE: Unknown predicate name -- " + ps);
				}else{
					p.setClosedWorld(true);
				}
			}
		}
		
		mln.prepareDB(adb);
		
		String[] evidFiles = opt.fevid.split(",");
		mln.loadEvidences(evidFiles);

		dmover = new NewDataMover(mln);
	}
	
}
