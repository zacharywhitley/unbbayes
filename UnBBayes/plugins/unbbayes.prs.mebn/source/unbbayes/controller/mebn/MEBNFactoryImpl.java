package unbbayes.controller.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.UbfIOReservedWords;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomReservedWords;

public class MEBNFactoryImpl implements MEBNFactory{

	public List<String> getReservedWords() {
		
		List<String> wordList = new ArrayList<String>(); 
		
		wordList.addAll(new PowerLoomReservedWords().getReservedWordsList()); 
		wordList.addAll(new UbfIOReservedWords().getReservedWordsList()); 
		
		return wordList;
		
	}

	public KnowledgeBase getKnowlegeBase() {
		// TODO Auto-generated method stub
		return null;
	}

	public MebnIO getMebnIO() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
