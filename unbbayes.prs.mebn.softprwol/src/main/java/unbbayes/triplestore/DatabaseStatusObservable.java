package unbbayes.triplestore;

import unbbayes.gui.mebn.extension.kb.triplestore.DatabaseStatusObserver;

public interface DatabaseStatusObservable {

	public void atach(DatabaseStatusObserver observer); 
	
	public void detach(DatabaseStatusObserver observer); 
	
	public void notifyListeners(); 
	
}
