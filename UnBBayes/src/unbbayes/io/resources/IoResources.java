package unbbayes.io.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.io package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 02/05/2002
 */

public class IoResources extends ListResourceBundle {

    /**
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * The resources
	 */
	static final Object[][] contents =
	{	{"logHeader","This description is made in the net's compilation process.\n" +
                     "It has the information of how a subjacent junction tree was\n" +
                     "created based on the junction tree technique with the minimum\n" +
                     "weight heuristic.\n\n"},
		{"cliqueHeader","******************* Cliques ******************\n"},
		{"cliqueName","Clique "},
		{"cliqueLabel"," Clique: "},
		{"potentialTableName","\nPotential Table\n"},
		{"utilityTableName","\nUtility Table\n"},
		{"separatorHeader","**************** Separators *****************\n"},
		{"separatorName","Separator "},
		{"betweenName","between "},
		{"andName"," and "},
		{"nodeName","Node(s): "},		
		{"potentialAssociatedHeader","************ Potentials associatedes to cliques **************\n"},
		{"errorNet","This file do not conform with the NET specification."},
		{"LoadException"," Missing 'net'"},
		{"LoadException2",": Unknown statement: "},
		{"LoadException3",": Missing '{'"},
		{"LoadException4",": Decision variable cannot have a table"},
		{"LoadException5",": Missing 'data'"},
		{"FileNotFoundException","It was not possible to load the file!"},
		{"IsNotDirectoryException", "The specified path must be a directory"}
	};
}