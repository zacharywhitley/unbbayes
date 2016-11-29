/**
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class PeerGroupSizeCSVReader implements IPeerGroupReader {

	private List<String> peerGroupSizes;
	private List<Integer> peerGroupCounts;
	/**
	 * 
	 */
	public PeerGroupSizeCSVReader() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see io.IPeerGroupReader#load(java.io.InputStream)
	 */
	public void load(InputStream input) throws IOException {
		// read file and fill cache
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(input)));
		List<String[]> csvRows = reader.readAll();
		reader.close();
		
		peerGroupSizes = new ArrayList<String>(csvRows.size());
		peerGroupCounts = new ArrayList<Integer>(csvRows.size());
		for (int row = 1; row < csvRows.size(); row++) {
			String[] csvRow = csvRows.get(row);
			
			peerGroupSizes.add(csvRow[1]);
			peerGroupCounts.add(Math.round(Float.parseFloat(csvRow[2].trim())));
		}
		
	}
	
	

	/**
	 * @return the peerGroupSizes
	 */
	public List<String> getPeerGroupSizes() {
		return peerGroupSizes;
	}

	/**
	 * @param peerGroupSizes the peerGroupSizes to set
	 */
	public void setPeerGroupSizes(List<String> peerGroupSizes) {
		this.peerGroupSizes = peerGroupSizes;
	}

	/**
	 * @return the peerGroupCounts
	 */
	public List<Integer> getPeerGroupCounts() {
		return peerGroupCounts;
	}

	/**
	 * @param peerGroupCounts the peerGroupCounts to set
	 */
	public void setPeerGroupCounts(List<Integer> peerGroupCounts) {
		this.peerGroupCounts = peerGroupCounts;
	}

	

}
