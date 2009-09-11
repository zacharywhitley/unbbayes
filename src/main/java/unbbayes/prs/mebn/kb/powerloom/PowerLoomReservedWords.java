package unbbayes.prs.mebn.kb.powerloom;

import java.util.ArrayList;
import java.util.List;

import unbbayes.controller.ReservedWordsList;

public class PowerLoomReservedWords implements ReservedWordsList{

	public List<String> getReservedWordsList() {
		List<String> reservedWords = new ArrayList<String>(); 
		
		//TODO list the reserved words of powerloom. 
		reservedWords.add("exists"); 
		
		return reservedWords;
	}

}
