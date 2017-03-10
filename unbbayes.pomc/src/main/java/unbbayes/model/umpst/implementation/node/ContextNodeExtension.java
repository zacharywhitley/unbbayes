package unbbayes.model.umpst.implementation.node;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import unbbayes.controller.umpst.MappingController;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTreeUMP;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;

public class ContextNodeExtension extends ContextNode {
	
	private NecessaryConditionVariableModel necessaryConditionModel;
	private MFragExtension mfragExtensionActive;

	
	/**
	 * {@link ContextNodeExtension} related to {@link NecessaryConditionVariableModel} present
	 * in {@link MultiEntityBayesianNetwork}.
	 * @param name
	 * @param mfrag
	 * @param ncModel
	 */
	public ContextNodeExtension(String name, MFragExtension mfrag, NecessaryConditionVariableModel ncModel) {
		super(name, mfrag);
		
		setMfragExtensionActive(mfrag);
		setNecessaryConditionModel(ncModel);
	}

	/**
	 * @return the necessaryConditionModel
	 */
	public NecessaryConditionVariableModel getNecessaryConditionModel() {
		return necessaryConditionModel;
	}

	/**
	 * @param necessaryConditionModel the necessaryConditionModel to set
	 */
	public void setNecessaryConditionModel(NecessaryConditionVariableModel necessaryConditionModel) {
		this.necessaryConditionModel = necessaryConditionModel;
	}

	/**
	 * @return the mfragExtensionActive
	 */
	public MFragExtension getMfragExtensionActive() {
		return mfragExtensionActive;
	}

	/**
	 * @param mfragExtensionActive the mfragExtensionActive to set
	 */
	public void setMfragExtensionActive(MFragExtension mfragExtensionActive) {
		this.mfragExtensionActive = mfragExtensionActive;
	}
}
