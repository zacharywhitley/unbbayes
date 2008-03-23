package unbbayes.gui.mebn.cpt;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public interface StyleTable {

	public StyledDocument getStyledDocument(); 
	
	public Style getIfStyle();
	
	public Style getAnyStyle();
	
	public Style getArgumentStyle();
	
	public Style getFunctionStyle();
	
	public Style getNumberStyle();
	
	public Style getBooleanStyle();
	
	public Style getFatherStyle();
	
	public Style getStateNodeStyle();
	
	public Style getStateFatherStyle();
	
	public Style getDefaultStype();
	
	public Style getDescriptionStyle();
	
}
