/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.util.Debug;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class InverseCopyPasteRawActivityMonthExtender {
	
	public static int BEGIN_DAY = 1;
	public static int BEGIN_TESTING_DAY = 144;	//144 = 1st work day of May; 124 = 1st work day of April; 101 = 1st work day of March
	public static int ABORT_DAY = 9999;			// use big values to avoid aborting operation at the middle
	public static int END_DAY = 163;			// use the theoretic max number. If you need to finish before this day, use ABORT_DAY instead
	public static boolean isCopyOnlyTrainingData = true;	// if false, all data (type 1 and 2) will be copied. If false, only type=1 will be copied and pasted.
	public static boolean isToFixZeroProxyDetectors = true;	// if true, proxy log detectors with 0 entries byt non-zero downloads will be set to 1.
	
	public static int NUM_TIMEBLOCKS = 6;
	
	/**
	 * 
	 */
	public InverseCopyPasteRawActivityMonthExtender() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String fileName = "userActivity.csv";	// default place to look for file
		if (args != null && args.length > 0) {
			fileName = args[0];	// extract name from argument
		}
		System.out.println("Loading file: " + fileName);
		
		String outputName = "userActivity_extended.csv";	// default place to save
		if (args != null && args.length > 1) {
			outputName = args[1];	// extract name from argument
		}
		System.out.println("Saving to file: " + outputName);
		

		// read file and fill cache
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)))));
		List<String[]> rows = reader.readAll();
		reader.close();
		
		PrintStream printer = new PrintStream(new FileOutputStream(outputName, false));
		
		// read and copy 1st row
		String[] row = rows.get(0);
		int dayColumn = 4; 
		int typeColumn = 0; 
		int userColumn = 3; 
		int timeIDColumn = 1; 
		List<Integer> proxyDetectorColumns = null;
		if (isToFixZeroProxyDetectors) {
			proxyDetectorColumns = new ArrayList<Integer>();
		}
		for (int column = 0; column < row.length; column++) {
			String attributeName = row[column];
			if (attributeName.equalsIgnoreCase("dayid")) {
				dayColumn = column;
			} else if (attributeName.equalsIgnoreCase("type")) {
				typeColumn = column;
			} else if (attributeName.equalsIgnoreCase("userid")) {
				userColumn = column;
			} else if (attributeName.equalsIgnoreCase("timeid")) {
				timeIDColumn = column;
			} else if (attributeName.toLowerCase().startsWith("det")) {
				proxyDetectorColumns.add(column);
			}
			printer.print("\""+ attributeName +"\"");
			if (column + 1 < row.length) {
				printer.print(",");
			}
		}
		printer.println();
		
		// half of detector columns are download detectors
		List<Integer> downloadDetectorColumns = null;
		if (isToFixZeroProxyDetectors) {
			downloadDetectorColumns = proxyDetectorColumns.subList(proxyDetectorColumns.size()/2, proxyDetectorColumns.size());
			proxyDetectorColumns = proxyDetectorColumns.subList(0, proxyDetectorColumns.size()/2);
		}
		
		// read how many days and users we have in data
		List<Integer> daysInData = new ArrayList<Integer>();	// day ids in data
		List<Integer> testDaysInData = new ArrayList<Integer>();	// day ids in test data (type = 2)
		List<Integer> usersInData = new ArrayList<Integer>();	// user ids in data
		Map<Integer, Map<Integer, Integer>> userToDayRowMap = new HashMap<Integer, Map<Integer,Integer>>();	// mapping from user id to another map from day id to row
		for (int rowNumber = 1; rowNumber < rows.size(); rowNumber++) {
			row = rows.get(rowNumber);
			int day = Integer.parseInt(row[dayColumn]);
			int type = Integer.parseInt(row[typeColumn]);
			if (isCopyOnlyTrainingData) {
				if (type == 2) {
					if (!testDaysInData.contains(day)) {
						testDaysInData.add(day);
					}
				} else {
					if (!daysInData.contains(day)) {
						daysInData.add(day);
					}
				}
			} else {
				if (!daysInData.contains(day)) {
					daysInData.add(day);
				}
			}
			int user = Integer.parseInt(row[userColumn]);
			if (!usersInData.contains(user)) {
				usersInData.add(user);
			}
			Map<Integer, Integer> dayToRowMap = userToDayRowMap.get(user);
			if (dayToRowMap == null) {
				dayToRowMap = new HashMap<Integer, Integer>();
				userToDayRowMap.put(user, dayToRowMap);
			}
			if (!dayToRowMap.containsKey(day)) {
				dayToRowMap.put(day, rowNumber);	// only include the 1st row we found
			}
		}
		
		// How many days the resulting file must have
		int totalDays = END_DAY - BEGIN_DAY + 1;	
		int numDaysToExtend = totalDays - daysInData.size() - testDaysInData.size(); // copy only the training data, so exclude test data from days to copy
		int mod = (numDaysToExtend % (daysInData.size()-1));
		int numCopies = (int)((numDaysToExtend / (daysInData.size()-1)) + ((mod>0)?1:0)) + 1;	// how many copies of data to write
		
		// generate the extended data.
		for (Integer userId : usersInData) {
			
			// even = read data in opposite order; odd = read data in straight order
			boolean isReadInverse = ((numCopies % 2) == 0)?true:false;
			
			// calculate what should be the 1st day to read from data in order to fill the entire days to write, 
			// given that we are copying and pasting rows from original data, in reverse order each time we make a copy.
			// for example, if we have input data with 3 days, but we need to write 6 days in output, 
			// then we need to write 2nd, 3rd, 2nd, 1st, 2nd, 3rd days (in this order); thus the first day to write is the 2nd day to read in data.
			int firstDayToReadInData = mod;
			if (mod == 0) {
				if (isReadInverse) {
					firstDayToReadInData = (daysInData.size() - 1);
				} // else {firstDayToReadInData = 0;}
			} else if (!isReadInverse) {
				firstDayToReadInData = (daysInData.size() - 1 - mod);
			}
			
			int globalDayId = BEGIN_DAY;
			for (int copy = 0 ; copy < numCopies; copy++) {
				// another condition to finish
				if (globalDayId > END_DAY) {
					throw new IllegalStateException("Premature end reached. This is probably due to data not supported by this program.");
				}
				
				for (int dayIndexInData = firstDayToReadInData; ; globalDayId++) {
					if (globalDayId >= ABORT_DAY) {
						Debug.println("Aborted at day " + globalDayId);
						break;
					}

					int type = (globalDayId >= BEGIN_TESTING_DAY)?2:1;	// 2 = testing data; 1 = training data.
					
					// check condition to end loop
					if (isReadInverse) {
						if (dayIndexInData < 0) {
							break;
						}
					} else {
						if (dayIndexInData >= daysInData.size()) {
							break;
						}
					}
					
					// extract the day in original data (this will be used with user id to estimate the row number)
					int dayInData = daysInData.get(dayIndexInData);
					
					for (int timeblock = 1; timeblock <= NUM_TIMEBLOCKS; timeblock++) {
						
						// extract the row (1st row of day of current user + timeblock)
						int rowInData = userToDayRowMap.get(userId).get(dayInData) + timeblock - 1;
						row = rows.get(rowInData);
						
						// make sure timeblock is consistent
						if (Integer.parseInt(row[timeIDColumn]) != timeblock) {
							throw new IOException("Expected timeblock " + timeblock + " at row " + rowInData
									+ ", but obtained " + row[timeIDColumn]);
						};
						// make sure user is consistent
						if (Integer.parseInt(row[userColumn]) != userId) {
							throw new IOException("Expected user " + userId + " at row " + rowInData
									+ ", but obtained " + row[userColumn]);
						};
						
						// print the current row, but changing day and type (training/testing)
						for (int column = 0; column < row.length; column++) {
							int intValue = -1;
							try {
								intValue = Integer.parseInt(row[column]);
							} catch (Exception e) {}
							if (column == dayColumn) {
								printer.print(globalDayId);
							} else if (column == typeColumn) {
								printer.print(type);
							} else if (isToFixZeroProxyDetectors && (intValue == 0) && proxyDetectorColumns.contains(column)) {
								// get respective download detector
								int downloadDetectorColumn = downloadDetectorColumns.get(proxyDetectorColumns.indexOf(column));
								if (Float.parseFloat(row[downloadDetectorColumn]) > 0) {
									printer.print("1");
								} else {
									printer.print(row[column]);
								}
							} else {
								printer.print(row[column]);
							}
							if ((column + 1) < row.length) {
								printer.print(",");
							}
						}
						printer.println();
					}	// end of for each time block
					
					Debug.println("Printed day " + dayInData + " for user " + userId);
					
					// update index
					if (isReadInverse) {
						dayIndexInData--;
					} else {
						dayIndexInData++;
					}
				}	// end of for dayIndexInData
				
				if (isReadInverse) {
					// next iteration reads from beginning
					isReadInverse = false;
					firstDayToReadInData = 1;	// day at index 0 was already written in previous loop, so start next from 1
				} else {
					
					// next iteration starts reading from the end
					isReadInverse = true;
					firstDayToReadInData = daysInData.size()-2; // last day was already written in previous loop, so start next loop from the day before
				}
				
			}	// end of for each copy of rows in data (of current user)
			
			
			// write the test days
			for (int dayIndexInData = 0; dayIndexInData < testDaysInData.size() ; dayIndexInData++, globalDayId++) {
				if (globalDayId >= ABORT_DAY) {
					Debug.println("Aborted at day " + globalDayId);
					break;
				}

				int type = (globalDayId >= BEGIN_TESTING_DAY)?2:1;	// 2 = testing data; 1 = training data.
				
				// extract the day in original data (this will be used with user id to estimate the row number)
				int dayInData = testDaysInData.get(dayIndexInData);
				
				for (int timeblock = 1; timeblock <= NUM_TIMEBLOCKS; timeblock++) {
					
					// extract the row (1st row of day of current user + timeblock)
					int rowInData = userToDayRowMap.get(userId).get(dayInData) + timeblock - 1;
					row = rows.get(rowInData);
					
					// make sure timeblock is consistent
					if (Integer.parseInt(row[timeIDColumn]) != timeblock) {
						throw new IOException("Expected timeblock " + timeblock + " at row " + rowInData
								+ ", but obtained " + row[timeIDColumn]);
					};
					// make sure user is consistent
					if (Integer.parseInt(row[userColumn]) != userId) {
						throw new IOException("Expected user " + userId + " at row " + rowInData
								+ ", but obtained " + row[userColumn]);
					};
					
					// print the current row, but changing day and type (training/testing)
					for (int column = 0; column < row.length; column++) {
						if (column == dayColumn) {
							printer.print(globalDayId);
						} else if (column == typeColumn) {
							printer.print(type);
						} else {
							printer.print(row[column]);
						}
						if ((column + 1) < row.length) {
							printer.print(",");
						}
					}
					printer.println();
				}	// end of for each time block
				
				Debug.println("Printed day " + dayInData + " for user " + userId);
				
			}	// end of for dayIndexInData
		
		}	// end of for each user in data
		
		printer.close();
	}

}
