package unbbayes.io.umpst.intermediatemtheory;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import unbbayes.io.umpst.implementation.FileBuildImplementationNode;
import unbbayes.model.umpst.entity.AttributeModel;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.goal.GoalModel;
import unbbayes.model.umpst.goal.HypothesisModel;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EnumSubType;
import unbbayes.model.umpst.implementation.EnumType;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTree;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.implementation.algorithm.MFragModel;
import unbbayes.model.umpst.implementation.algorithm.MTheoryModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Build hierarchy of nodes according to type node distribution
 * 
 * @author Diego Marques
 */

public class FileBuildIMMFrag {
	
	private Set<String> keys;
	private Set<String> subKeys, _subKeys;
	private TreeSet<String> sortedKeys;
	private TreeSet<String> subSortedKeys;
	
	private MTheoryModel mtheory;
	
	FileBuildIMMFragNode bn = new FileBuildIMMFragNode();
	FileBuildImplementationNode bin = new FileBuildImplementationNode();
	
	public void buildModel(Document doc, Element parent, UMPSTProject umpstProject) {
		Element node = null;
	
		if (umpstProject.getMtheory() != null) {
			mtheory = umpstProject.getMtheory();
			
			for (int i = 0; i < mtheory.getMFragList().size(); i++) {
				
				MFragModel mfragTest = mtheory.getMFragList().get(i);
				node = bn.buildMFrag(doc, parent, mfragTest);
				parent.appendChild(node);
			}
		}		
	}	
}