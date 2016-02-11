package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.umpst.FormulaTreeController;
import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.TableButton;
import unbbayes.model.umpst.entity.EntityModel;
import unbbayes.model.umpst.entity.RelationshipModel;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EventType;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.util.ArrayMap;

public class RVTreeForReplaceInFormula extends JTree{
	
	private final int SIZE_COLUMNS_ICON = 25;
	private final int SIZE_COLUMNS_TEXT = 300;
	
	private ArrayMap<Object, Object> randomVariableMap = new ArrayMap<Object, Object>();
	
	private static final long serialVersionUID = 1L;
	private IconController iconController;
	private RuleModel rule;
	private UMPSTProject umpstProject;
	
	private FormulaTreeController formulaTreeController;
	private FormulaEditionPane formulaEditionPane;
	
	private DefaultMutableTreeNode root;
	private DefaultMutableTreeNode causes;
	private DefaultMutableTreeNode effects;
	private DefaultMutableTreeNode others;
	private DefaultTreeModel model;
	
	
	public RVTreeForReplaceInFormula(UMPSTProject umpstProject, RuleModel rule, FormulaEditionPane formulaEditionPane) {
		super();
		this.umpstProject = umpstProject;
		this.rule = rule;
		this.formulaEditionPane = formulaEditionPane;
		this.setLayout(new BorderLayout());
		
		formulaTreeController = formulaEditionPane.getFormulaTreeController();
		randomVariableMap = new ArrayMap<Object, Object>();
		
		root = new DefaultMutableTreeNode("RV");
//		causes = new DefaultMutableTreeNode("Causes");
		effects = new DefaultMutableTreeNode("Effects");
		// All relationship that are not in cause or effect event
		others = new DefaultMutableTreeNode("Others");
//		root.add(causes);
//		root.add(effects);
		root.add(others);
		
//		createCausesTree();
//		createEffectsTree();
		createOthersTree();
		
		model = new DefaultTreeModel(root);
		setModel(model);
		this.setRootVisible(false); 
		
		addListeners();
		super.treeDidChange();
	
	}
	
	public void addListeners(){
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e) {				
				int selRow = getRowForLocation(e.getX(), e.getY());
				if (selRow == -1) {
					return;
				}				
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
				.getLastPathComponent();
				
				if (node.isLeaf()) {					
					Object nodeLeaf = randomVariableMap.get(node); 
//					objectSelected = nodeLeaf;

//					RandomVariableObjectModel tst = (RandomVariableObjectModel)nodeLeaf;					
					if (nodeLeaf instanceof EventVariableObjectModel){
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {							
							
						} else if (e.getClickCount() == 2
								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							formulaTreeController.addNode((EventVariableObjectModel)nodeLeaf);						
							formulaTreeController.updateFormulaActiveContextNode();
						} else if (e.getClickCount() == 1) {							
						}
					}
				}
			}
//					else{
//						if(nodeLeaf instanceof StateLink){
//							if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
//								
//							} else if (e.getClickCount() == 2
//									&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
//								//TODO at final version use LinkState in the tree
//								formulaTreeController.addEntity((Entity)((StateLink)nodeLeaf).getState()); 
//								controller.updateFormulaActiveContextNode(); 
//							} else if (e.getClickCount() == 1) {
//											
//							}	
//						}
//					}
//				} 
//				else { //Not is a leaf 
//					Object nodeLeaf = randomVariableMap.get(node); 
////					objectSelected = nodeLeaf; 
//					
//					if (nodeLeaf instanceof Node){
//						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
//						} else if (e.getClickCount() == 2
//								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
//							formulaTreeController.addNode((RandomVariableObjectModel)nodeLeaf); 
////							controller.updateFormulaActiveContextNode(); 
//							
//						} else if (e.getClickCount() == 1) {
//						}
//					}
//					else{
//					}
//				}
//			}
		});
	}
	
	public JScrollPane createRVDataTable(List<CauseVariableModel> causeVariableList) {
		Object[] columnNames = {"ID", "Cause Variable", ""};
		Object[][] data = new Object[causeVariableList.size()][3];
		
		String sentence = null;
		for (int i = 0; i < causeVariableList.size(); i++) {
			if (causeVariableList.get(i).getRelationship() != null) {
				sentence = causeVariableList.get(i).getRelationship() + "(";
				for (int j = 0; j < causeVariableList.get(i).getArgumentList().size(); j++) {				
					sentence = sentence + causeVariableList.get(i).getArgumentList().get(j) + ", ";
				}
				int index = sentence.lastIndexOf(", ");
				sentence = sentence.substring(0, index);
				sentence = sentence + ")";
			}
			
			String causeVariableId = causeVariableList.get(i).getId();
			data[i][0] = causeVariableId;
			data[i][1] = sentence;
			data[i][2] = "";
		}
		return createRVTable(data, columnNames);
	}
	
	public JScrollPane createRVTable(final Object[][] data, Object[] columnNames) {		
		
		// Ordinary variable table
		DefaultTableModel tbModel = new DefaultTableModel(data, columnNames);
		JTable rvTable = new JTable(tbModel);
		rvTable.setTableHeader(null);
		rvTable.setPreferredSize(new Dimension(400, 150));
		
		TableButton buttonAdd = new TableButton(new TableButton.TableButtonCustomizer() {			
			public void customize(JButton button, int row, int column) {
//				button.setIcon(iconController.getAddFolderIcon());				
			}
		});
		
		TableColumn columnAdd = rvTable.getColumnModel().getColumn(
				columnNames.length-1);
		columnAdd.setMaxWidth(SIZE_COLUMNS_ICON);
		columnAdd.setCellRenderer(buttonAdd);
		columnAdd.setCellEditor(buttonAdd);
		
		buttonAdd.addHandler(new TableButton.TableButtonPressedHandler() {			
			public void onButtonPress(int row, int column) {
				String rvRow = data[row][0].toString();
				System.out.println(rvRow);
			}
		});
		
		rvTable.setPreferredScrollableViewportSize(rvTable.getPreferredSize());
		rvTable.updateUI();
		rvTable.repaint();
		
		JScrollPane scrollpane = new JScrollPane(rvTable);
		scrollpane.setPreferredSize(new Dimension(80, 80));
				
		return scrollpane;
	}
	
	/**
	 * All relationship from umpstProject that have the same entities
	 * present in rule ordinaryVariableList.
	 */
	public void createOthersTree() {
		others.removeAllChildren();

		Map<String, RelationshipModel> relationshipMap = new HashMap<String, RelationshipModel>(); 
		relationshipMap = umpstProject.getMapRelationship();
				
		List<EventVariableObjectModel> othersEventVariableList = new ArrayList<EventVariableObjectModel>();
		
		Set<String> keys = relationshipMap.keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key : sortedKeys) {
			
			if (canParticipate(relationshipMap.get(key))) {
				String id = relationshipMap.get(key).getId();
				String name = relationshipMap.get(key).getName();
			
				EventVariableObjectModel event = new EventVariableObjectModel(id, EventType.OTHER);
				event.setRelationship(name);
				event.setRelationshipModel(relationshipMap.get(key));
				
				othersEventVariableList.add(event);
				
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
				others.add(node);
				randomVariableMap.put(node, event);
			}
		}
		updateTree();
	}
	
	/**
	 * This function evaluates if relationshipModel can be eventVariableObjectModel. 
	 * @param relationship
	 * @return
	 */
	public boolean canParticipate(RelationshipModel relationship) {		
		
		List<OrdinaryVariableModel> ordinaryVariableList = new ArrayList<OrdinaryVariableModel>();
		ordinaryVariableList = rule.getOrdinaryVariableList();
		
		int flag = 0;
		
		for (int i = 0; i < relationship.getEntityList().size(); i++) {
			EntityModel entity = relationship.getEntityList().get(i);

			for (int j = 0; j < rule.getOrdinaryVariableList().size(); j++) {			
				if (entity.getId().equals(rule.getOrdinaryVariableList().get(j).getEntityObject().getId())) {
					flag++;
				}
			}
		}
		if (flag >= relationship.getEntityList().size()) {
			return true;
		}		
		return true;
	}
	
	public void createEffectsTree() {		
		effects.removeAllChildren();
		
		List<RuleModel> ruleChildren;
		List<EffectVariableModel> effectVariableList = new ArrayList<EffectVariableModel>();		
		
		// All cause variables present in the rule and its children
		effectVariableList = rule.getEffectVariableList();
//		if (hasChildren()) {
//			for (int i = 0; i < rule.getChildrenRuleList().size(); i++) {
//				ruleChildren = new ArrayList<RuleModel>();
//				ruleChildren.add(rule.getChildrenRuleList().get(i));
//				for (int j = 0; j < ruleChildren.get(0).getCauseVariableList().size(); j++) {
//					causeVariableList.add(ruleChildren.get(0).getCauseVariableList().get(j));
//				}
//			}
//		}getSentenceMap
		
//		String sentence = null;
		String relationshipName = null;
		for (int i = 0; i < effectVariableList.size(); i++) {
			if (effectVariableList.get(i).getRelationship() != null) {
//				sentence = effectVariableList.get(i).getRelationship() + "(";
//				for (int j = 0; j < effectVariableList.get(i).getArgumentList().size(); j++) {				
//					sentence = sentence + effectVariableList.get(i).getArgumentList().get(j) + ", ";
//				}
//				int index = sentence.lastIndexOf(", ");
//				sentence = sentence.substring(0, index);
//				sentence = sentence + ")";
				
				relationshipName = effectVariableList.get(i).getRelationship();
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(relationshipName);
				effects.add(node);
				randomVariableMap.put(node, effectVariableList.get(i));
			}
		}
		updateTree();
//		return causeVariableList;
//		return root;
	}
	
//	public List<CauseVariableModel> populateCauseVariableList(List<CauseVariableModel> causeVariableList) {
	public void createCausesTree() {		
		causes.removeAllChildren();
		
		List<RuleModel> ruleChildren;
		List<CauseVariableModel> causeVariableList = new ArrayList<CauseVariableModel>();		
		
		// All cause variables present in the rule and its children
		causeVariableList = rule.getCauseVariableList();
//		if (hasChildren()) {
//			for (int i = 0; i < rule.getChildrenRuleList().size(); i++) {
//				ruleChildren = new ArrayList<RuleModel>();
//				ruleChildren.add(rule.getChildrenRuleList().get(i));
//				for (int j = 0; j < ruleChildren.get(0).getCauseVariableList().size(); j++) {
//					causeVariableList.add(ruleChildren.get(0).getCauseVariableList().get(j));
//				}
//			}
//		}
		
//		String sentence = null;
		String relationshipName = null;
		for (int i = 0; i < causeVariableList.size(); i++) {
			if (causeVariableList.get(i).getRelationship() != null) {
//				sentence = causeVariableList.get(i).getRelationship() + "(";
//				for (int j = 0; j < causeVariableList.get(i).getArgumentList().size(); j++) {				
//					sentence = sentence + causeVariableList.get(i).getArgumentList().get(j) + ", ";
//				}
//				int index = sentence.lastIndexOf(", ");
//				sentence = sentence.substring(0, index);
//				sentence = sentence + ")";
				
				relationshipName = causeVariableList.get(i).getRelationship();
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(relationshipName);
				causes.add(node);
				randomVariableMap.put(node, causeVariableList.get(i));
			}
		}
		updateTree();
//		return causeVariableList;
//		return root;
	}
	
	/**
	 * Rebuild the tree.
	 */
	public void updateTree() {		
		for (int i = 0; i < getRowCount(); i++){
			expandRow(i);
		}
	}
	
	public boolean hasChildren() {
		if (rule.getChildrenRuleList().size() > 0) {
			return true;
		} else {
			return false;
		}
	}
//
//	/**
//	 * @return the randomVariableMap
//	 */
//	public ArrayMap<Object, String> getRandomVariableMap() {
//		return randomVariableMap;
//	}
//
//	/**
//	 * @param randomVariableMap the randomVariableMap to set
//	 */
//	public void setRandomVariableMap(ArrayMap<Object, String> randomVariableMap) {
//		this.randomVariableMap = randomVariableMap;
//	}	
}
