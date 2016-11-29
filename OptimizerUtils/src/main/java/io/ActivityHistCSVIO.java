/**
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class ActivityHistCSVIO implements IActivityHistIO {

	private List<String[]> cache = null;
	
	private List<List<Integer>> binOffset;
	private List<List<Integer>> binSize;
	private List<Integer> dayColumnIndex;
	
	private String detectorPrefix = "Detector";
	private String timeBlockPrefix = "Timeblock";
	private String binPrefix = "Bin";
	private String dayPrefix = "Day";


	/**
	 * Default constructor
	 */
	public ActivityHistCSVIO() {}

	/* (non-Javadoc)
	 * @see io.IActivityHistIO#load(java.io.InputStream)
	 */
	public void load(InputStream input) throws IOException {
		
		// read file and fill cache
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(input)));
		cache = reader.readAll();
		reader.close();
		
		if (cache == null || cache.isEmpty()) {
			throw new IOException("Empty csv file.");
		}
		
		// init indexes
		binOffset = new ArrayList<List<Integer>>();
		binSize = new ArrayList<List<Integer>>();
		dayColumnIndex = null;
		
		// read detector sections
		Integer detectorOffset = null;	// this will be used to let indexes of detectors to start from 0
		for (int row = 0; row < cache.size(); ) {
			
			String[] rowValue = cache.get(row);	// extract the values (text) of current row
			
			// we're expecting beginning of detector section
			if (rowValue.length <= 0 || !rowValue[0].startsWith(getDetectorPrefix())) {
				throw new IOException("Illegal format of csv file. Expected beginning of detector section at row " + row);
			}
			
			
			
			// read body of detector section
			
			int detectorNum = Integer.parseInt(rowValue[0].substring(rowValue[0].indexOf(getDetectorPrefix()) + getDetectorPrefix().length()).trim());
			
			// if detector "1" is the first detector, detectorOffset will subtract 1 so that detector "0" is the 1st detector
			if (detectorOffset == null) {
				detectorOffset = detectorNum;
			}
			
			while (binOffset.size() <= (detectorNum - detectorOffset)) {	
				binOffset.add(new ArrayList<Integer>());
			}
			while (binSize.size() <= (detectorNum - detectorOffset)) {
				binSize.add(new ArrayList<Integer>());
			}
			
			
			// read timeblock sections
			row++;
			Integer timeBlockOffset = null;	// this will be used to let indexes of timeblocks to start from 0
			while (row < cache.size()) {
				
				rowValue = cache.get(row);	// update value of row
				
				// beginning of another detector section is the end of timeblock sections of current detector
				if (rowValue.length > 0 && rowValue[0].startsWith(getDetectorPrefix())) {
					break;
				}
				
				if (rowValue.length <= 0 || !rowValue[0].startsWith(getTimeBlockPrefix())) {
					throw new IOException("Illegal format of csv file. Expected beginning of timeblock section at row " + row);
				}
				
				
				int timeBlockNum = Integer.parseInt(rowValue[0].substring(rowValue[0].indexOf(getTimeBlockPrefix()) + getTimeBlockPrefix().length()).trim());
				
				// if timeblock "1" is the first timeblock, timeBlockOffset will subtract 1 so that timeblock "0" is the 1st timeblock
				if (timeBlockOffset == null) {
					timeBlockOffset = timeBlockNum;
				}
				
				while (binOffset.get(detectorNum - detectorOffset).size() <= (timeBlockNum - timeBlockOffset)) {	
					binOffset.get(detectorNum - detectorOffset).add(-1);
				}
				while (binSize.get(detectorNum - detectorOffset).size() <= (timeBlockNum - timeBlockOffset)) {	
					binSize.get(detectorNum - detectorOffset).add(-1);
				}
				
				
				// read bin section (starting from a header containing "Bin", "Day1", "Day2", ...)
				row++;
				rowValue = cache.get(row);	// update value of row
				
				// expect beginning of bin section
				if (rowValue.length <= 0 || !rowValue[0].startsWith(getBinPrefix())) {
					throw new IOException("Illegal format of csv file. Expected header of bins (i.e. \"Bin\", \"Day1\", \"Day2\"...) at row " + row);
				}
				
				// read the days (1st column of current is the "Bin" cell, so read days from 2nd column)
				boolean isToFillColumnIndex = false;
				if (dayColumnIndex == null) {
					dayColumnIndex = new ArrayList<Integer>(164);
					isToFillColumnIndex = true;
				}
				for (int column = 1; column < rowValue.length; column++) {
					if (rowValue[column] == null || !rowValue[column].contains(getDayPrefix())) {
						throw new IOException("Illegal format of csv file. Expected a day in header at row " + row + ", column " + column);
					}
					if (isToFillColumnIndex) {
						dayColumnIndex.add(column);
					}
				}
				
				// go to the 1st row of data
				row++;
				rowValue = cache.get(row);	// update value of row
				if (rowValue == null || rowValue.length <= 0 || rowValue[0].trim().isEmpty()	// if current row is empty
						|| rowValue[0].startsWith(getDetectorPrefix())							// or current row starts a new section
						|| rowValue[0].startsWith(getTimeBlockPrefix())							// then csv format is wrong
						|| rowValue[0].startsWith(getBinPrefix())) {
					throw new IOException("Illegal format of csv file. Expected beginning of data at row " + row);
				}
				
				// set the offset of current detector and current timeblock to 1st row of data
				binOffset.get(detectorNum - detectorOffset).set((timeBlockNum - timeBlockOffset), row);
				
				// read until we find last bin (before empty row)
				int size = 1;	// this will check how many rows we had in data section
				for (row++; row < cache.size(); row++, size++) {
					rowValue = cache.get(row);
					// check for empty lines
					if (rowValue.length <= 0 || rowValue[0].trim().isEmpty()
							|| rowValue[0].startsWith(getTimeBlockPrefix())
							|| rowValue[0].startsWith(getDetectorPrefix())) {
						// end of timeblock section
						break;
					}
				}
				
				binSize.get(detectorNum - detectorOffset).set((timeBlockNum - timeBlockOffset), size);
				
				// go to the beginning of next block (detector or timeblock)
				while (row < cache.size()) {
					if (rowValue.length <= 0 || rowValue[0].trim().isEmpty()) {
						row++;
						rowValue = cache.get(row);
					} else {
						break;
					}
				}
				
			}	// end of for, time block section
		
		}	// end of for, detector section
		
	}

	/* (non-Javadoc)
	 * @see io.IActivityHistIO#getTotalNumDays()
	 */
	public int getTotalNumDays() {
		return getDayColumnIndex().size();
	}
	
	/* (non-Javadoc)
	 * @see io.IActivityHistIO#getTotalNumDetectors()
	 */
	public int getTotalNumDetectors() {
		return getBinSize().size();
	}

	/*
	 * (non-Javadoc)
	 * @see io.IActivityHistIO#getTotalNumTimeBlocks(int)
	 */
	public int getTotalNumTimeBlocks(int detector) {
		if (getBinSize().isEmpty()) {
			return 0;
		}
		return getBinSize().get(detector).size();
	}


	/* (non-Javadoc)
	 * @see io.IActivityHistIO#getNumBins(int, int)
	 */
	public int getNumBins(int detector, int timeBlock) {
		return getBinSize().get(detector).get(timeBlock);
	}
	
	/**
	 * 
	 * @param binInterval
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Entry<Float, Float> parseBinInterval(String binInterval) {
		if (binInterval.contains("-")) {
			String[] split = binInterval.trim().split("-");
			return new AbstractMap.SimpleEntry(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
		} else {
			// return the same element twice
			Float value = Float.parseFloat(binInterval.trim());
			return new AbstractMap.SimpleEntry(value, value);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.IActivityHistIO#getBinValueLower(int, int, int)
	 */
	public float getBinValueLower(int detector, int timeBlock, int bin) {
		if (bin >= getNumBins(detector, timeBlock)) {
			throw new ArrayIndexOutOfBoundsException(bin);
		}
		int row = getBinOffset().get(detector).get(timeBlock) + bin;
		return parseBinInterval(getCache().get(row)[0]).getKey();
	}

	/*
	 * (non-Javadoc)
	 * @see io.IActivityHistIO#getBinValueUpper(int, int, int)
	 */
	public float getBinValueUpper(int detector, int timeBlock, int bin) {
		int row = getBinOffset().get(detector).get(timeBlock) + bin;
		return parseBinInterval(getCache().get(row)[0]).getValue();
	}
	
	


	/*
	 * (non-Javadoc)
	 * @see io.IActivityHistIO#getNumUsers(int, int, int)
	 */
	public List<Integer> getNumUsers(int detector, int timeBlock, int day) {
		
		int rowStart = getBinOffset().get(detector).get(timeBlock);
		int numRowsToRead = getBinSize().get(detector).get(timeBlock);
		int column = getDayColumnIndex().get(day);
		
		List<Integer> ret = new ArrayList<Integer>(numRowsToRead);
		
		for (int i = 0; i < numRowsToRead; i++) {
			int row = rowStart + i;
			ret.add(Integer.parseInt(getCache().get(row)[column]));
		}
		
		return ret;
	}

	/**
	 * @return the cache
	 */
	public List<String[]> getCache() {
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(List<String[]> cache) {
		this.cache = cache;
	}


	/**
	 * @return the dayColumnIndex
	 */
	public List<Integer> getDayColumnIndex() {
		return dayColumnIndex;
	}

	/**
	 * @param dayColumnIndex the dayColumnIndex to set
	 */
	public void setDayColumnIndex(List<Integer> dayColumnIndex) {
		this.dayColumnIndex = dayColumnIndex;
	}



	/**
	 * @return the detectorPrefix
	 */
	public String getDetectorPrefix() {
		return detectorPrefix;
	}

	/**
	 * @param detectorPrefix the detectorPrefix to set
	 */
	public void setDetectorPrefix(String detectorPrefix) {
		this.detectorPrefix = detectorPrefix;
	}

	/**
	 * @return the timeBlockPrefix
	 */
	public String getTimeBlockPrefix() {
		return timeBlockPrefix;
	}

	/**
	 * @param timeBlockPrefix the timeBlockPrefix to set
	 */
	public void setTimeBlockPrefix(String timeBlockPrefix) {
		this.timeBlockPrefix = timeBlockPrefix;
	}

	/**
	 * @return the binOffset
	 */
	public List<List<Integer>> getBinOffset() {
		return this.binOffset;
	}

	/**
	 * @param binOffset the binOffset to set
	 */
	public void setBinOffset(List<List<Integer>> binOffset) {
		this.binOffset = binOffset;
	}

	/**
	 * @return the binSize
	 */
	public List<List<Integer>> getBinSize() {
		return this.binSize;
	}

	/**
	 * @param binSize the binSize to set
	 */
	public void setBinSize(List<List<Integer>> binSize) {
		this.binSize = binSize;
	}

	/**
	 * @return the binPrefix
	 */
	public String getBinPrefix() {
		return binPrefix;
	}

	/**
	 * @param binPrefix the binPrefix to set
	 */
	public void setBinPrefix(String binPrefix) {
		this.binPrefix = binPrefix;
	}

	/**
	 * @return the dayPrefix
	 */
	public String getDayPrefix() {
		return dayPrefix;
	}

	/**
	 * @param dayPrefix the dayPrefix to set
	 */
	public void setDayPrefix(String dayPrefix) {
		this.dayPrefix = dayPrefix;
	}

	/**
	 * This is just an example of how to use the methods in {@link ActivityHistCSVIO} class.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		IActivityHistIO io = new ActivityHistCSVIO();
		
		String fileName = "RCP8_ActivityHist.csv";	// default place to look for file
		if (args != null && args.length > 0) {
			fileName = args[0];	// extract name from argument
		}
		System.out.println("Loading file: " + fileName);
		
		io.load(new FileInputStream(new File(fileName)));
		
		int numDetectors = io.getTotalNumDetectors();
		System.out.println("Days:" + io.getTotalNumDays());
		System.out.println("Detectors: " + numDetectors);
		for (int detector = 0; detector < numDetectors; detector++) {
			int numTimeBlocks = io.getTotalNumTimeBlocks(detector);
			
			System.out.println("\n");
			System.out.println("Detector " + (detector + 1) + ", time blocks: " + numTimeBlocks);
			
			for (int timeBlock = 0; timeBlock < numTimeBlocks; timeBlock++) {
				System.out.println();
				System.out.println("\t Timeblock " + timeBlock);
				int numBins = io.getNumBins(detector, timeBlock);
				System.out.println("\t Num bins: " + numBins);
				for (int bin = 0; bin < numBins; bin++) {
					System.out.println("\t\tBin " + bin + ": [ " + io.getBinValueLower(detector, timeBlock, bin) + " - " + io.getBinValueUpper(detector, timeBlock, bin) + " ]");
				}
				System.out.println();
				for (int day = 0; day < io.getTotalNumDays(); day++) {
					List<Integer> users = io.getNumUsers(detector, timeBlock, day);
					System.out.print("\t Day " + day);
					System.out.println(": users = " + users);
				}
			}
		}
		
	}


}
