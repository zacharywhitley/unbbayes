package unbbayes.io.umpst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

import unbbayes.model.umpst.project.UMPSTProject;

public class UbfSave {
	
	private String[][] tokens = { // tokens used for .ubf file construction
			{"CommentInitiator","%"},
			{"ArgumentSeparator",","},
			{"AttributionSeparator","="},
			{"Quote","\""},
			
			{"VersionDeclarator" , "Version"},
			{"PrOwlFileDeclarator" , "PrOwl"},
			
			{"MTheoryDeclarator" , "MTheory"},
			{"NextMFragDeclarator" , "NextMFrag"},
			{"NextResidentDeclarator" , "NextResident"},
			{"NextInputDeclarator" , "NextInput"},
			{"NextContextDeclarator" , "NextContext"},
			{"NextEntityDeclarator" , "NextEntity"},	
			
	};
	
	public void saveUbf() throws FileNotFoundException{
		
		File file = new File("images/file.txt");

		PrintStream printStream = new PrintStream(new FileOutputStream(file));
		
		Set<String> keys = UMPSTProject.getInstance().getMapGoal().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		for (String key : sortedKeys){
			printStream.println(UMPSTProject.getInstance().getMapGoal().get(key).getId());
		}
		
	}

}
