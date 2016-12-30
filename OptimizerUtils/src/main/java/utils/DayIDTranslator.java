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

	/** If true, then the CSV header will be substituted with {@link #defaultHeader}. If false, header will be read from file. */
	public static boolean isToUseDefaultHeader = true;
	
	/** If {@link #isToUseDefaultHeader} is true, then this value will be used as default header of output csv */
	public static String defaultHeader = "\"type\",\"timeid\",\"grpid\",\"userid\",\"dayid\",\"Proxy_Personal_Use\",\"Proxy_Social_Media\",\"Proxy_News\",\"Proxy_Sports\",\"Proxy_Shopping\",\"Proxy_Webmail\",\"Bytes_Personal_Use\",\"Bytes_Social_Media\",\"Bytes_News\",\"Bytes_Sports\",\"Bytes_Shopping\",\"Bytes_Webmail\"";

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

		String[] row = reader.readNext();
		if (isToUseDefaultHeader) {
			// "type","timeid","grpid","userid","dayid","Proxy_Personal_Use","Proxy_Social_Media","Proxy_News","Proxy_Sports","Proxy_Shopping","Proxy_Webmail","Bytes_Personal_Use","Bytes_Social_Media","Bytes_News","Bytes_Sports","Bytes_Shopping","Bytes_Webmail"
			printer.println(defaultHeader);
		} else {
			// "type","timeid","grpid","userid","dayid","det1","det2","det3","det4","det5","det6","det7","det8","det9","det10","det11","det12"
			for (int i = 0; i < row.length; i++) {
				printer.print("\""+row[i]+"\"");
				if (i + 1 < row.length) {	
					printer.print(",");
				}
			}
			printer.println();
		}
		
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
