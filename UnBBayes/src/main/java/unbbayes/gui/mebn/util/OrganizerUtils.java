package unbbayes.gui.mebn.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;

/**
 * This utility class have methods for organize the mebn elements for 
 * presentation in the diverses painels of the user. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class OrganizerUtils {

	/**
	 * Build a list with all the resident nodes of the MEBN ordered by name. 
	 */
	public static List<ResidentNode> createOrderedResidentNodeList(MultiEntityBayesianNetwork mebn){

		List<ResidentNode> listResident = new ArrayList<ResidentNode>(); 
		
		for(MFrag mfrag: mebn.getMFragList()){
			
			for(ResidentNode node: mfrag.getResidentNodeList()){
				listResident.add(node);
			}
			Collections.sort(listResident); 	
		}
		
		return listResident; 
	}
	
}
