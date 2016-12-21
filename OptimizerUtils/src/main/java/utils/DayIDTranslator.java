/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class DayIDTranslator {

	/**
	 * 
	 */
	public DayIDTranslator() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public static Map<Integer, String> readDateMap(String fileName) throws IOException {
		
		Map<Integer, String> ret = new HashMap<Integer, String>();
		CSVReader reader = new CSVReader(new FileReader(new File(fileName)));
		
		String[] row = reader.readNext();	// skip 1st row
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			ret.put(Integer.parseInt(row[0]), row[1]);
		}
		
		reader.close();
		return ret;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String fileName = "userActivity.csv";
		String outputName = "userActivity_days.csv";
		String dateFileName = "MeaningDayId.csv";
		
		if (args.length > 0) {
			fileName = args[0];
		}
		
		PrintStream printer = null;
		if (args.length > 1) {
			printer = new PrintStream(new FileOutputStream(args[1], false));
		} else {
			printer = new PrintStream(new FileOutputStream(outputName, false));
		}
		
		if (args.length > 2) {
			dateFileName = args[2];
		}
		
		Map<Integer, String> dateMap = readDateMap(dateFileName);
		
		CSVReader reader = new CSVReader(new FileReader(new File(fileName)));

		// "type","timeid","grpid","userid","dayid","det1","det2","det3","det4","det5","det6","det7","det8","det9","det10","det11","det12"
		String[] row = reader.readNext();
		for (int i = 0; i < row.length; i++) {
			printer.print("\""+row[i]+"\"");
			if (i + 1 < row.length) {
				printer.print(",");
			}
		}
		printer.println();
		
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			for (int i = 0; i < row.length; i++) {
				if (i == 4) {
					printer.print(dateMap.get(Integer.parseInt(row[i])));
				} else {
					printer.print(row[i]);
				}
				if (i + 1 < row.length) {
					printer.print(",");
				}
			}
			printer.println();
		}
		
		
		reader.close();
		printer.close();
	}


}
