package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.util.*;

import unbbayes.controller.IconController;
import unbbayes.datamining.datamanipulation.*;

/**
 *  Class that implements a tree used to insert a new instance for classification.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class InferenceTree extends JTree{
  public static final int CHECK_YES = 1;
  public static final int CHECK_NO = -1;
  public static final int CHECK_EMPTY = 0;

  private ArrayMap objectsMap = new ArrayMap();
  private Attribute[] attributeVector;
  private int classIndex;
  private InferencePanel inferencePanel;
  protected IconController iconController = IconController.getInstance();

  protected InferenceTree(){
    setShowsRootHandles(true);
    setSelectionModel(null);      //null = nós não selecionaveis
    setRootVisible(true);         //raiz visivel?
    this.setAutoscrolls(true);
    setCellRenderer(new CnmTreeCellRenderer());
    addMouseListener(new MouseAdapter(){
      public void mouseClicked(java.awt.event.MouseEvent evt){
        cnmTreeMouseClicked(evt);
      }
    });
  }

  /**
   * Builds a new inference tree.
   *
   * @param attributeVector an array with all the attributes of the trainning set.
   * @param classIndex the index of the class attribute.
   */
  public InferenceTree(Attribute[] attributeVector, int classIndex){
    this();
    setAttributes(attributeVector, classIndex);
  }

  /**
   * Used to set the controller of this class.
   *
   * @param inferencePanel the controller.
   */
  public void setController(InferencePanel inferencePanel){
    this.inferencePanel = inferencePanel;
  }

  /**
   * Used to set the attributes of the trainning set.
   *
   * @param attributeVector an array with all the attributes of the trainning set.
   * @param classIndex the index of the class attribute.
   */
  public void setAttributes(Attribute[] attributeVector, int classIndex){
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
    if (attributeVector != null){
      if (!attributeVector.equals(this.attributeVector)){
        this.attributeVector = attributeVector;
        this.classIndex = classIndex;
        root.removeAllChildren();
        objectsMap.clear();
        DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        this.setModel(model);
        root = (DefaultMutableTreeNode) getModel().getRoot();
        int size = attributeVector.length;
        for (int i=0; i<size; i++){
          if(i!=classIndex){
            Attribute attribute = attributeVector[i];

            //definição de um nó atributo
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(attribute.getAttributeName());
            objectsMap.put(treeNode, attribute);
            root.add(treeNode);

            //definição dos nós dos valores dos atributos
            int numStates = attribute.numValues();
            for (short j=0; j<numStates; j++){
              DefaultMutableTreeNode stateNode = new DefaultMutableTreeNode(attribute.value(j));
              treeNode.add(stateNode);
              objectsMap.put(stateNode, new StateObject(attribute, j, CHECK_EMPTY));
            }
          }
        }
      }
    } else {
      this.attributeVector = null;
      root.removeAllChildren();
      objectsMap.clear();
    }
  }

  /**
   * Returns the instance inserted on the tree by the user.
   *
   * @return the instance inserted on the tree by the user.
   */
  public Instance getInstance(){
    ArrayList keys = objectsMap.getKeys();
    int keysSize = keys.size();
    Instance instance = new Instance(new short[attributeVector.length]);

    for(int i=0; i<attributeVector.length; i++){
      instance.setMissing(i);
    }

    for(int i=0; i<keysSize; i++){
      Object obj = objectsMap.get(keys.get(i));
      if(obj instanceof StateObject){
        StateObject state = (StateObject)obj;
        int check = state.getCheck();
        if(check == CHECK_YES){
          instance.setValue(state.getAttribute().getIndex(), state.getAttributeValue());
        }
      }
    }

    return instance;
  }

  private void cnmTreeMouseClicked(java.awt.event.MouseEvent evt) {
    TreePath clickedPath = getPathForLocation(evt.getX(), evt.getY());
    if (clickedPath != null) {
      DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)(clickedPath.getLastPathComponent());
      if (clickedNode != null && clickedNode.isLeaf()) {
        Object obj = objectsMap.get(clickedNode);
        if (obj instanceof StateObject) {
          DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)clickedNode.getParent();
          Enumeration childrenEnum = parentNode.children();
          StateObject yesChecked = null;
          ArrayList noCheckeds = new ArrayList(),
          emptyCheckeds = new ArrayList();
          while (childrenEnum.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)childrenEnum.nextElement();
            if (!child.equals(clickedNode)) {
              if (((StateObject)objectsMap.get(child)).getCheck() == CHECK_YES) {
                yesChecked = (StateObject)objectsMap.get(child);
              }
              else if (((StateObject)objectsMap.get(child)).getCheck() == CHECK_NO) {
                noCheckeds.add(objectsMap.get(child));
              }
              else {
                emptyCheckeds.add(objectsMap.get(child));
              }
            }
          }
          if (SwingUtilities.isLeftMouseButton(evt)) {
            if (((StateObject)obj).getCheck() == CHECK_YES) {
              ((StateObject)obj).setCheck(CHECK_EMPTY);
              for (int i = 0; i < noCheckeds.size(); i++) {
                ((StateObject)noCheckeds.get(i)).setCheck(CHECK_EMPTY);
              }
            }
            else {
              ((StateObject)obj).setCheck(CHECK_YES);
              if (yesChecked != null) {
                yesChecked.setCheck(CHECK_NO);
              }
              for (int i = 0; i < emptyCheckeds.size(); i++) {
                ((StateObject)emptyCheckeds.get(i)).setCheck(CHECK_NO);
              }
            }
          }
          if (SwingUtilities.isRightMouseButton(evt)) {
            if (((StateObject)obj).getCheck() == CHECK_NO) {
              ((StateObject)obj).setCheck(CHECK_EMPTY);
              if (yesChecked != null) {
                yesChecked.setCheck(CHECK_EMPTY);
              }
            }
            else if (noCheckeds.size() < (parentNode.getChildCount() - 1)) {
              ((StateObject)obj).setCheck(CHECK_NO);
              if (noCheckeds.size() == (parentNode.getChildCount() - 2)) {
                ((StateObject)emptyCheckeds.get(0)).setCheck(CHECK_YES);
              }
            }
          }
          repaint();
          inferencePanel.printRule(this);
        }
      }
    }
  }

  /**
   * Expand the tree nodes.
   *
   * @see JTree
   */
  public void expandTree(){
    for (int i = 0; i < getRowCount(); i++){
      expandRow(i);
    }
  }

  /**
   * Collapse the tree nodes.
   *
   * @see JTree
   */
  public void collapseTree(){
    for (int i = 0; i < getRowCount(); i++){
      collapseRow(i);
    }
  }

  private class StateObject{
    private Attribute attribute;
    private short attributeValue = -1;
    private int check = CHECK_EMPTY;

    public StateObject(Attribute attribute, short attributeValue, int check){
      this.attribute = attribute;
      this.attributeValue = attributeValue;
      this.check = check;
    }

    public void setAttributeValue(short attributeValue){
      this.attributeValue = attributeValue;
    }

    public void setCheck(int check){
      this.check = check;
    }

    public short getAttributeValue(){
      return attributeValue;
    }

    public int getCheck(){
      return check;
    }

    public Attribute getAttribute(){
      return attribute;
    }
  }

  private class CnmTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer{
    ImageIcon yesIcon = iconController.getYesStateIcon();
    ImageIcon noIcon = iconController.getNoStateIcon();
    ImageIcon emptyIcon = iconController.getEmptyStateIcon();
    ImageIcon evidenciasIcon = iconController.getMoreIcon();
    ImageIcon folderSmallIcon = iconController.getFolderSmallIcon();

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
      if (leaf){
        Object obj = objectsMap.get(treeNode);
        if (obj instanceof StateObject){
          StateObject stateObject = (StateObject)obj;
          int check = stateObject.getCheck();
          setIcon((check == CHECK_YES) ? yesIcon : ((check == CHECK_NO) ? noIcon : emptyIcon));
        }
      } else {
        Object obj = objectsMap.get(treeNode);
        if (obj instanceof Attribute){
          setIcon(evidenciasIcon);
        }
        this.setOpenIcon(folderSmallIcon);
        this.setClosedIcon(folderSmallIcon);
      }
      return this;
    }
  }
}