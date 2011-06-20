import java.io.BufferedWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import tuffy.db.RDB;
import tuffy.infer.DataMover;
import tuffy.mln.MarkovLogicNetwork;
import tuffy.mln.Predicate;
import tuffy.mln.Type;
import tuffy.util.Config;
import tuffy.util.ExceptionMan;
import tuffy.util.FileMan;
import tuffy.util.StringMan;
import tuffy.util.UIMan;


public class NewDataMover extends DataMover{

	RDB db;
	MarkovLogicNetwork mln;
	
	
	public NewDataMover(MarkovLogicNetwork mln){
		super(mln);
		this.mln = mln;
		this.db = mln.getRDB();
	}
	
	private String atomToString(Predicate p, ResultSet rs, HashMap<Integer,String> cmap){
		String line = p.getName() + "(";
		ArrayList<String> cs = new ArrayList<String>();
		try{
			for(int i=0; i<p.arity(); i++){
				String a = p.getArgs().get(i);
				Type t = p.getTypeAt(i);
				String v;
				if(!t.isNonSymbolicType()){
					v = cmap.get(rs.getInt(a));
				}else{
					v = rs.getString(a);
				}
				cs.add("\"" + StringMan.escapeJavaString(v) + "\"");
			}
		}catch(Exception e){
			ExceptionMan.handle(e);
		}
		line += StringMan.commaList(cs) + ")";
		return line;
	}
	
	public void dumpProbsToFile(String relAtoms, String fout){
		BufferedWriter bufferedWriter = FileMan.getBufferedWriterMaybeGZ(fout);
		HashMap<Integer,String> cmap = db.loadIdSymbolMapFromTable();
		int digits = 4;
		String sql;
		try {
			for(Predicate p : mln.getAllPredOrderByName()){			
				if(p.isImmutable()) continue;
				String atomTypeCond = (Config.mcsat_output_hidden_atoms ?					
						" " : " AND (club=1 OR club=3) ");
				String orderBy = " ORDER BY ";
				switch(Config.mcsat_output_order){
				case PRED_ARGS:
					orderBy += StringMan.commaList(p.getArgs());				
					break;
				case PROBABILITY:
					orderBy += " prob DESC ";
				}
				sql = "SELECT * FROM " + p.getRelName() + " pt, " + relAtoms + " ra " +
				" WHERE pt.id = ra.tupleID AND ra.predID = " + p.getID() +  
				" AND ra.prob >= " + Config.marginal_output_min_prob + " and ra.prob>0 " +
				atomTypeCond + orderBy;
				ResultSet rs = db.query(sql);
				while(rs.next()) {
					double prob = rs.getFloat("prob");
					double prior = rs.getFloat("prior");
					if(rs.wasNull()){
						prior = -1;
					}
					String satom = atomToString(p, rs, cmap);
					String line = null;
					if(Config.output_prolog_format){
						line = "tuffyPrediction(" + UIMan.decimalRound(digits, prob) +
							", " + satom + ").";
					}else{
						line = UIMan.decimalRound(digits, prob) + "\t" + satom;
					}
					if(Config.output_prior_with_marginals && prior >= 0){
						line += " // prior = " + UIMan.decimalRound(digits, prior);
						line += " ; delta = " + UIMan.decimalRound(digits, prob - prior);
					}
					bufferedWriter.append(line + "\n");
					System.out.println(line + "\n");
				}
				rs.close();
			}
			bufferedWriter.close();
		}catch (Exception e) {
			ExceptionMan.handle(e);
		}
	}
	
}
