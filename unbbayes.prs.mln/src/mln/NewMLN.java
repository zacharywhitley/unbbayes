package mln;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import tuffy.db.RDB;
import tuffy.mln.Predicate;
import util.Config;
import tuffy.util.ExceptionMan;
import tuffy.util.StringMan;


public class NewMLN {
	
	/**
	 * The object of relational database associated with this MLN.
	 */
	private RDB db = null;
	
	/**
	 * List of all predicates appearing in this MLN.
	 */
	private ArrayList<Predicate> listPred = new ArrayList<Predicate>();

	/**
	 * Copy atomID of relevant atoms to {@link Config#relTrueAtoms}. Only
	 * copy those atoms with truth value equals to TRUE.
	 */
	private void spreadTruth(){
		String trel = Config.relTrueAtoms;
		db.dropTable(trel);
		String sql = "CREATE TEMPORARY TABLE " + trel + " AS " +
		" SELECT atomID FROM " + Config.relAtoms + 
		" WHERE truth = TRUE";
		db.update(sql);
	}
	
	/**
	 * Dump a MAP world produced by MAP inference.
	 * 
	 * @param fout path of output file
	 */
	public void dumpMapAnswer(String fout) {
		spreadTruth();
		HashMap<Integer,String> cmap = db.loadIdSymbolMapFromTable();
		try {
			BufferedWriter bufferedWriter = null;
			bufferedWriter = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(fout),"UTF8"));
			for(Predicate p : listPred) {
				if(p.isImmutable()) continue;
				String sql = "SELECT * FROM " + p.getRelName() +
				" WHERE (club = -1214 OR club = 3) AND " +
				" id IN (SELECT atomID FROM " + Config.relTrueAtoms +
				") " +
				" ORDER BY " + StringMan.commaList(p.getArgs());
				ResultSet rs = db.query(sql);
				while(rs.next()) {
					String line = p.getName() + "(";
					ArrayList<String> cs = new ArrayList<String>();
					for(String a : p.getArgs()) {
						int c = rs.getInt(a);
						cs.add("\"" + StringMan.escapeJavaString(cmap.get(c)) + "\"");
					}
					line += StringMan.commaList(cs) + ")";
					bufferedWriter.append(line + " pra ver se ta dando certo\n");
					System.out.println("TESTEEEEEEEEEEEEEE" + line + "\n");
				}
				rs.close();
			}
			bufferedWriter.close();
		} catch (Exception e) {
			ExceptionMan.handle(e);
		}
	}
	
}
