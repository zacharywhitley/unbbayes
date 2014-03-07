package unbbayes.gui.umpst;

import unbbayes.model.umpst.project.UMPSTProject;

public class TableObject  extends IUMPSTPanel {

	private static final long serialVersionUID = 1L;
	
	public static final int SIZE_COLUMN_BUTTON = 28; 
	public static final int SIZE_COLUMN_INDEX = 50; 
	
	/**private constructors make class extension almost impossible,
	that's why this is protected*/
public TableObject(UmpstModule janelaPai, UMPSTProject umpstProject) {

	super(janelaPai);

	this.setUmpstProject(umpstProject);

}

}
