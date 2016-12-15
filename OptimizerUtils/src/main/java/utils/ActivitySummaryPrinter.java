/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class ActivitySummaryPrinter {

	/**
	 * 
	 */
	public ActivitySummaryPrinter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String fileName = "userActivity.csv";
		
		if (args.length > 0) {
			fileName = args[0];
		}
		
		PrintStream printer = System.out;
		if (args.length > 1) {
			printer = new PrintStream(new FileOutputStream(args[1], false));
		}
		
		CSVReader reader = new CSVReader(new FileReader(new File(fileName)));

		// "type","timeid","grpid","userid","dayid","det1","det2","det3","det4","det5","det6","det7","det8","det9","det10","det11","det12"
		String[] row = reader.readNext();
		printer.println("\"userid\",\"grpid\",\"det1\",\"det2\",\"det3\",\"det4\",\"det5\",\"det6\",\"det7\",\"det8\",\"det9\",\"det10\",\"det11\",\"det12\"");
		
		
		// read 1st data row
		row = reader.readNext();
		while (row != null) {
			int userid = Integer.parseInt(row[3]);
			int grpid = Integer.parseInt(row[2]);
			double sums[] = new double[12];
			Arrays.fill(sums, 0f);
			
			while (row != null) {
				int type = Integer.parseInt(row[0]);
				if (type == 2) {
					for (int i = 0; i < sums.length; i++) {
						sums[i] += Double.parseDouble(row[i+5]);
					}
				}
				
				row = reader.readNext();
				if (row == null) {
					break;
				}
				int currentUserId = Integer.parseInt(row[3]);
				int currentGrpId = Integer.parseInt(row[2]);
				if (currentUserId != userid
						|| currentGrpId != grpid) {
					break;
				}
			}
			
			//"\"userid\",\"grpid\",\"det1\",\"det2\",\"det3\",\"det4\",\"det5\",\"det6\",\"det7\",\"det8\",\"det9\",\"det10\",\"det11\",\"det12\""
			printer.print(userid + "," + grpid);
			for (int i = 0; i < sums.length; i++) {
				printer.print("," + sums[i]);
			}
			printer.println();
		}
		
		reader.close();
	}

}
