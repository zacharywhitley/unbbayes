package unbbayes.gui.umpst.implementation;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

public class ImplementationMainPropertiesEditionPane extends IUMPSTPanel {
	
	private static final long serialVersionUID = 1L;
	private UMPSTProject umpstProject;
	private RuleModel rule;
	private CauseVariableModel cvTested;
	private EffectVariableModel efTested;
	
	
	public ImplementationMainPropertiesEditionPane(UmpstModule janelaPai, UMPSTProject umpstProject,
			RuleModel rule) {
		super(janelaPai);
		this.setUmpstProject(umpstProject);
		this.umpstProject = umpstProject;
		this.setRule(rule);
//		this.setLayout(new GridLayout(1,1));
	}
	
	public JPanel createTitleLabel(String name) {
		JLabel titleLabel = new JLabel();
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titleLabel.setText(name);
		
		JPanel panelTitle = new JPanel(new FlowLayout());
		panelTitle.setLayout(new GridBagLayout());
		panelTitle.add(titleLabel);
		
		return panelTitle;
	}
	
	/**
	 * If variable edited or added is duplicated then return true else return false.
	 * This method receives EffectVariableModel as argument and returns boolean value.
	 * @param vCompared
	 * @return boolean value
	 */
	public boolean isVariableDuplicated(EffectVariableModel vCompared) {		
		for (int i = rule.getEffectVariableList().size()-1; i > -1; i--) {
			EffectVariableModel evTested = rule.getEffectVariableList().get(i);	
		
			if ((vCompared.getRelationship() != null) && (vCompared.getAttribute() == null) &&
					(evTested.getRelationship() != null) && (evTested.getAttribute() == null)) {				
				if (evTested.getRelationship().equals(vCompared.getRelationship())) {
					if (evTested.getArgumentList().size() == vCompared.getArgumentList().size()) {
						if (evTested.getArgumentList().size() == 1) {
							if (evTested.getArgumentList().get(0).equals(vCompared.getArgumentList().get(0))) {
								return true;
							} else {
								return false;
							}
						} else if (evTested.getArgumentList().size() == 2) {
							if (evTested.getArgumentList().get(0).equals(vCompared.getArgumentList().get(0)) &&
									evTested.getArgumentList().get(1).equals(vCompared.getArgumentList().get(1))) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
			} else if ((vCompared.getRelationship() == null) && (vCompared.getAttribute() != null) && 
					(evTested.getRelationship() == null) && (evTested.getAttribute() != null)) {
				if (evTested.getAttribute().equals(vCompared.getAttribute())) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;		
	}
	
	/**
	 * If variable edited or added is duplicated then return true else return false.
	 * This method receives EffectVariableModel as argument and returns boolean value.
	 * @param vCompared
	 * @return boolean
	 */
	public boolean isVariableDuplicated(CauseVariableModel vCompared) {		
		for (int i = rule.getCauseVariableList().size()-1; i > -1; i--) {
			CauseVariableModel cvTested = rule.getCauseVariableList().get(i);
		
			if ((vCompared.getRelationship() != null) && (vCompared.getAttribute() == null) &&
					(cvTested.getRelationship() != null) && (cvTested.getAttribute() == null)) {				
				if (cvTested.getRelationship().equals(vCompared.getRelationship())) {
					if (cvTested.getArgumentList().size() == vCompared.getArgumentList().size()) {
						if (cvTested.getArgumentList().size() == 1) {
							if (cvTested.getArgumentList().get(0).equals(vCompared.getArgumentList().get(0))) {
								return true;
							} else {
								return false;
							}
						} else if (cvTested.getArgumentList().size() == 2) {
							if (cvTested.getArgumentList().get(0).equals(vCompared.getArgumentList().get(0)) &&
									cvTested.getArgumentList().get(1).equals(vCompared.getArgumentList().get(1))) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
			} else if ((vCompared.getRelationship() == null) && (vCompared.getAttribute() != null) && 
					(cvTested.getRelationship() == null) && (cvTested.getAttribute() != null)) {
				if (cvTested.getAttribute().equals(vCompared.getAttribute())) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;		
	}

	/**
	 * @return the rule
	 */
	public RuleModel getRule() {
		return rule;
	}

	/**
	 * @param rule the rule to set
	 */
	public void setRule(RuleModel rule) {
		this.rule = rule;
	}			
}

