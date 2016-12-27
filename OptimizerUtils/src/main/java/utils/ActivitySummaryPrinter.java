/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class ActivitySummaryPrinter {

	/** A constant indicating maximum number of iterations to try to find users */
	public static final int MAX_ITERATION = 100000;

	/** This number of users with system alerts will be picked at most */
	public static int maxNumAlertUsers = 48;
	
	/** If detector alert days is greater than or equal to this value, then there is a system alert */
	public static int systemAlertThreshold = 8;
	

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
		
		// prepare seed and random number generator to be used in this program
		long seed = System.currentTimeMillis();
		System.out.println("Seed = " + seed);
		Random rand = new Random(seed );
		
		// prepare input files and output stream
		
		String activityFileName = "userActivity.csv";
		if (args.length > 0) {
			activityFileName = args[0];
		}
		
		String alertFileName = "detectorsDays.csv";
		if (args.length > 1) {
			alertFileName = args[1];
		}
		
		PrintStream printer = System.out;
		if (args.length > 2) {
			printer = new PrintStream(new FileOutputStream(args[2], false));
		}
		
		// read alert file
		CSVReader reader = new CSVReader(new FileReader(new File(alertFileName)));
		List<String[]> alertFileRows = reader.readAll();
		reader.close();
		reader = null;	// let garbage collector eventually free the memory space of reader
		
		// this object will be used to convert name of CSV columns to a normalized
		UniformNameConverter nameConverter = new UniformNameConverter();
		
		// prepare a list that identifies which are the columns of each detector
		List<Integer> detectorColumns = new ArrayList<Integer>();
		for (int column = 0; column < alertFileRows.get(0).length; column++) {	// read column names from 1st row (1st row is row 0)
			String columnName = nameConverter.convertToName(alertFileRows.get(0)[column]);
			if (columnName.startsWith("Detector")) {
				// This is a detector column. Name of column is DetectorX. Extract value of X
				int detectorIndex = Integer.parseInt(columnName.substring(8)) - 1;	// subtract 1 because Detector1 is the 1st detector, but list starts from index 0.
				
				// make sure the size of detector columns list is fine
				while (detectorColumns.size() <= detectorIndex) {
					detectorColumns.add(-1);	// initialize with invalid (negative) column number
				}
				detectorColumns.set(detectorIndex, column);	// store which column in csv file the current detector was.
				
			}	// ignore columns that are not detector values
		}
		
		// prepare a set of rows so that we can later randomly choose a row, without substitution
		List<Integer> rowsToConsider = new ArrayList<Integer>();
		int totalNumUsersAlert = 0;	// also count how many users with system alerts we had
		for (int rowNumber = 1; rowNumber < alertFileRows.size(); rowNumber++) {// skip row 0, which is header
			
			rowsToConsider.add(rowNumber);	// indicate that this row needs to be considered when picking rows in random
			
			// also check if user of current row had system alert
			String[] row = alertFileRows.get(rowNumber);
			// if any detector had detector alert days greater than or equal to a threshold, then user had system alert
			for (Integer detectorColumn : detectorColumns) {	// iterate only on columns that represent detector alert day values
				int detectorValue = Integer.parseInt(row[detectorColumn]);	// extract value of current cell
				if (detectorValue >= systemAlertThreshold) {
					totalNumUsersAlert++;
					break;
				}
			}
		}
		// only consider max number of users (if total number of users with system alert was greater than the value we should consider)
		if (totalNumUsersAlert > maxNumAlertUsers) {
			totalNumUsersAlert = maxNumAlertUsers;
		}
		
		// prepare a set of detectors so that we can later randomly choose a row, without substitution
		List<Integer> detectorsToConsider = new ArrayList<Integer>();
		for (int detector = 0; detector < detectorColumns.size(); detector++) {// size of detectorColumns is the number of detectors we have
			// detector "0" is actually Detector1. We use 0 as 1st detector so that it's synchronized with indexes of lists
			detectorsToConsider.add(detector);
		}

		
		// key of this map will be a subset of user ids with system alerts. Value keeps track of which detector triggered system alert of such user.
		Map<Integer,Integer> alertUserToDetectorMap = new HashMap<Integer,Integer>();	
		Map<Integer, Integer> userToRowMap = new HashMap<Integer, Integer>();	// map from user id to row in CSV related to such user
		while(alertUserToDetectorMap.size() < totalNumUsersAlert) {		// fill alertUserToDetectorMap 
			
			// clone list of rows to consider, so that we can randomly pick a row and remove the picked one from the list (but without changing original list)
			List<Integer> randomRowsToPick = new ArrayList<Integer>(rowsToConsider);
			// also clone list of detectors to consider, for same reason of above list (not to change original list when picking/removing in random)
			List<Integer> randomDetectorsToPick = new ArrayList<Integer>(detectorsToConsider);
			
			// pick users uniformly from each detector
			while (randomDetectorsToPick.size() > 0) {
				
				// pick a detector randomly (without substitution). 
				int pickedDetectorIndex = randomDetectorsToPick.remove(rand.nextInt(randomDetectorsToPick.size()));
				// this is the column in csv file associated with the detector we just picked
				int detectorColumn = detectorColumns.get(pickedDetectorIndex);
				
				// read rows. Since each row is 1 user, picking a row is equivalent to picking some user
				while (randomRowsToPick.size() > 0) {
					// pick a row randomly (without substitution).
					int rowNumber = randomRowsToPick.remove(rand.nextInt(randomRowsToPick.size()));
					String[] row = alertFileRows.get(rowNumber);	// content of the row we picked
					
					// extract the user id of current row
					int userId = Integer.parseInt(row[0]);
					// ignore user ids that we already included in set of users with alerts
					if (alertUserToDetectorMap.containsKey(userId)) {
						continue;
					}
					
					// associate user to current row
					userToRowMap.put(userId, rowNumber);	// just overwrite the row related to this user if mapping is already present
					
					// pick user if user has system alert caused by current detector
					int detectorValue = Integer.parseInt(row[detectorColumn]);	// extract value of current cell
					if (detectorValue >= systemAlertThreshold) {
						alertUserToDetectorMap.put(userId, pickedDetectorIndex);
						// only pick 1 user for each detector in this iteration (so that number of users is near uniform for each detector)
						break;	// end iteration on rows/users and go to next detector
					}
				}	// end of loop which reads rows
				
				//  break loop when we reached desired number of users with alerts
				if (alertUserToDetectorMap.size() >= totalNumUsersAlert) {
					break;	// end iteration on detectors
				}
				
			}	// end of loop for each detectors
			
		}	// end of loop which fills the set "alertUsers"
		
		
		// create mappings between user ids and peer groups

		// mapping of user to peer groups
		Map<Integer,Integer> userToPeerGroupMap = new HashMap<Integer,Integer>();

		// Read peer groups from activity file
		reader = new CSVReader(new FileReader(new File(activityFileName)));
		// skip first row (header of activity file)
		String[] row = reader.readNext();
		// load 1st "data"  (data row starts from 2nd row in csv)
		row = reader.readNext();
		while (row != null) {
			// read user id and peer group id
			int userid = Integer.parseInt(row[3]);
			int grpid = Integer.parseInt(row[2]);
			
			// go to the next (closest to current) row of different user
			while (row != null) {
				row = reader.readNext();
				if (row == null) {
					break;
				}
				// check if current row has a different user compared to previous row
				int currentUserId = Integer.parseInt(row[3]);
				int currentGrpId = Integer.parseInt(row[2]);
				if (currentUserId != userid
						|| currentGrpId != grpid) {
					break;
				}
			}
			
			userToPeerGroupMap.put(userid, grpid);	// fill the mapping
			
		}
		reader.close();
		reader = null;	// let garbage collector eventually free the memory space of reader
		
		// choose redacted subset of user ids without system alerts;
		
		// create a inverse mapping of userToPeerGroupMap (i.e. a mapping from peer group to set of users)
		Map<Integer, Set<Integer>> peerGroupToUserMap = new HashMap<Integer, Set<Integer>>();
		for (Entry<Integer, Integer> userToPeerGroup : userToPeerGroupMap.entrySet()) {
			Set<Integer> users = peerGroupToUserMap.get(userToPeerGroup.getValue());
			if (users == null) {
				users = new HashSet<Integer>();
			}
			users.add(userToPeerGroup.getKey());
			peerGroupToUserMap.put(userToPeerGroup.getValue(), users);
		}
		
		// for each peer group of user with system alert, pick another user with low detector alert days, and another with high alert days (but with no system alert);
		Set<Integer> nonAlertUsers = new HashSet<Integer>();
		for (Entry<Integer, Integer> entry : alertUserToDetectorMap.entrySet()) {
			// extract the user id
			int userId = entry.getKey();
			
			// extract the column in csv file associated with detector that caused system alert for such user id
			int detectorColumn = detectorColumns.get(entry.getValue());
			
			// extract the peer group id of user;
			Integer groupId = userToPeerGroupMap.get(userId);
			
			// extract the users in same peer group, but with no system alert
			List<Integer> usersInSameGroup = new ArrayList<Integer>(peerGroupToUserMap.get(groupId));
			usersInSameGroup.remove(new Integer(userId));	// do not consider the current user itself
			// peer groups supposedly have size larger than 3 (actually, smallest peer group should have size around 7), 
			// so it should contain at least 2 users other than current user 
			if (usersInSameGroup.size() < 2) {
				throw new IllegalArgumentException("Peer group " + groupId + " does not have enough number of users: " + (usersInSameGroup.size() + 1));
			}
			// avoid users with alert (i.e. only consider non-alert users)
			for (Integer alertUser : alertUserToDetectorMap.keySet()) {
				if (usersInSameGroup.size() <= 2) {	
					// make sure we leave at least 2 users there
					break;
				}
				usersInSameGroup.remove(alertUser);	
			}
			
			// at this point, usersInSameGroup should have at least 2 users different from userId
			
			// find users in same peer group with largest and smallest detector value
			Integer smallestDetectorValueUser = null;
			Integer largestDetectorValueUser = null;
			int smallestDetectorValue = Integer.MAX_VALUE;
			int largestDetectorValue = Integer.MIN_VALUE;
			
			// iterate on usersInSameGroup in random order
			while (usersInSameGroup.size() > 0) {
				int userInGroup = usersInSameGroup.remove(rand.nextInt(usersInSameGroup.size()));
				
				// extract row in csv related to current user
				int rowNumber = userToRowMap.get(userInGroup);
				row = alertFileRows.get(rowNumber);
				
				// get the detector value of current user
				int detectorValue = Integer.parseInt(row[detectorColumn]);
				
				if (detectorValue < smallestDetectorValue) {
					smallestDetectorValueUser = userInGroup;
					smallestDetectorValue = detectorValue;
				} 
				if (detectorValue >= largestDetectorValue) {	
					// using ">=" here so that if multiple users have same detector values, 
					// the user of largest detector value is picked from later users.
					largestDetectorValueUser = userInGroup;
					largestDetectorValue = detectorValue;
				} 
			}	// end of iterate on usersInSameGroup in random order
			
			
			// add the 2 users we found
			nonAlertUsers.add(largestDetectorValueUser);
			nonAlertUsers.add(smallestDetectorValueUser);
			
		}	// end of for each alert user
		
		
		// create a union (set) of users we picked with system alerts and without system alerts
		Set<Integer> allUserIdsToConsider = new HashSet<Integer>(nonAlertUsers);
		allUserIdsToConsider.addAll(alertUserToDetectorMap.keySet());
			
		
		// start printing
		
		// Print header "userid","grpid"
		printer.println("\"userid\",\"grpid\"");
		
		// print the user ids and respective group ids
		for (Integer userid : allUserIdsToConsider) {
			int grpid = userToPeerGroupMap.get(userid);	// extract the peer group of current user
			printer.println(userid + "," + grpid);
		}
		
		System.out.println("Total users: " + allUserIdsToConsider.size());
		System.out.println("Users with system alert: " + alertUserToDetectorMap.size());
		
	}

}
