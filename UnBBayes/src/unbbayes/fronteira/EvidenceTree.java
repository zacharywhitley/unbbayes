package unbbayes.fronteira;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * @author Mário Henrique Paes Vieira
 * @version 1.0
 */
public class EvidenceTree extends JTree
{   private ProbabilisticNetwork net;
    private NumberFormat nf;
    private boolean[] expandedNodes;

    public EvidenceTree()
    {   nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(4);
    }

        /**
     *  Adiciona uma evidencia no estado especificado.
     *
     * @param  caminho  caminho do estado a ser setado para 100%;
     * @see             TreePath
     */
    /*public void arvoreDuploClick(DefaultMutableTreeNode treeNode) {
        DefaultMutableTreeNode pai = ((DefaultMutableTreeNode) treeNode.getParent());
        TreeVariable node = (TreeVariable) pai.getUserObject();


        for (int i = 0; i < pai.getChildCount(); i++) {
            DefaultMutableTreeNode auxNode = (DefaultMutableTreeNode) pai.getChildAt(i);
            auxNode.setUserObject(node.getStateAt(i) + ": 0");
        }

        if (node instanceof ProbabilisticNode) {
            treeNode.setUserObject(node.getStateAt(pai.getIndex(treeNode)) + ": 100");
        } else {
            treeNode.setUserObject(node.getStateAt(pai.getIndex(treeNode)) + ": **");
        }
        node.addFinding(pai.getIndex(treeNode));
        ((DefaultTreeModel) tela.getEvidenceTree().getModel()).reload(pai);
    }

    /**
     *  Retrai todos os nós da árvore desejada.
     *
     * @param  arvore  uma <code>JTree</code> que representa a rede Bayesiana em
     *      forma de árvore.
     * @since
     * @see            JTree
     */
    public void collapseTree()
    {   for (int i = 0; i < getRowCount(); i++)
        {   collapseRow(i);
        }

        for (int i = 0; i < expandedNodes.length; i++)
        {   expandedNodes[i] = false;
        }
    }

    /**
     *  Expande todos os nós da árvore desejada.
     *
     * @param  arvore  uma <code>JTree</code> que representa a rede Bayesiana em
     *      forma de árvore.
     * @since
     * @see            JTree
     */
    public void expandTree()
    {   for (int i = 0; i < getRowCount(); i++)
        {   expandRow(i);
        }

        for (int i = 0; i < expandedNodes.length; i++)
        {   expandedNodes[i] = true;
        }
    }

    /**
     *  Atualiza as marginais na árvore desejada.
     *
     * @param  arvore  uma <code>JTree</code> que representa a árvore a ser
     *      atualizada
     * @since
     * @see            JTree
     */
    public void updateTree()
    {   int i,j;
        NodeList nodes = net.getCopiaNos();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();

        root.removeAllChildren();

        int nodeSize = nodes.size();
        for (j = 0; j < nodeSize; j++)
        {   Node node = (Node)nodes.get(j);
            TreeVariable treeVariable = (TreeVariable) node;
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);

            int statesSize = node.getStatesSize();
            for (i = 0; i < statesSize; i++)
            {   String label;
                if (treeVariable instanceof ProbabilisticNode)
                {   label = node.getStateAt(i) + ": " + nf.format(treeVariable.getMarginalAt(i) * 100.0);
                }
                else
                {   label = node.getStateAt(i) + ": " + nf.format(treeVariable.getMarginalAt(i));
                }
                treeNode.add(new DefaultMutableTreeNode(label));
            }
            root.add(treeNode);
        }

        ((DefaultTreeModel) getModel()).reload(root);
        j = 0;
        for (i = 0; i < expandedNodes.length; i++)
        {   if (expandedNodes[i])
            {   expandRow(j);
                Node node = (Node) nodes.get(i);
                j += node.getStatesSize();
            }
            j++;
        }

    }

    /**
     * Modifica o formato de números
     *
     * @param local localidade do formato de números.
     */
    public void setNumberFormat(Locale local)
    {   nf = NumberFormat.getInstance(local);
    }

    public void setProbabilisticNetwork(ProbabilisticNetwork net)
    {   this.net = net;
    	// deve pegar o copiaNos que possui todos os nos que aparecem na
    	// arvore, ou seja, todos menos os de Utilidade
        //expandedNodes = new boolean[net.getNos().size()];
        expandedNodes = new boolean[net.getCopiaNos().size()];
        for (int i = 0; i < expandedNodes.length; i++) {
            expandedNodes[i] = false;
        }
        updateTree();
    }

    public boolean[] getExpandedNodes()
    {   return expandedNodes;
    }
}