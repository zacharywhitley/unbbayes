/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package unbbayes.prs.bn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ResourceBundle;

import unbbayes.draw.DrawEllipse;
import unbbayes.prs.Node;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Representa variável probabilística.
 *
 *@author Michael Onishi
 *@author Rommel Carvalho
 */
public class ProbabilisticNode extends TreeVariable implements ITabledVariable, java.io.Serializable {
			
    /**
	 * 
	 */
	private static final long serialVersionUID = -8362313890037632119L;
	
	private ProbabilisticTable tabelaPot;
    private static Color descriptionColor = Color.yellow;
    private static Color explanationColor = Color.green;
    private DrawEllipse drawEllipse;

    /** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.bn.resources.BnResources");

    /**
     * Constructs a ProbabilisticNode with an initialized table and 
     * an incremented DrawElement.
     */
    public ProbabilisticNode() {
        tabelaPot = new ProbabilisticTable();
        // Here it is defined how this node is going to be drawn.
        // In the superclass, Node, it was already definied to draw text, here
        // we add the draw ellipse.
        drawEllipse = new DrawEllipse(position, size);
        drawElement.add(drawEllipse);
    }


    public int getType() {
    	return PROBABILISTIC_NODE_TYPE;
    }

    /**
     *  Copia as características principais para o nó desejado
     *@param raio raio do nó.
     *@return cópia do nó
     */
    public ProbabilisticNode clone(double raio) {
    	// TODO Rever esse método para não precisar do raio.
        ProbabilisticNode no = new ProbabilisticNode();

        for (int i = 0; i < getStatesSize(); i++) {
            no.appendState(getStateAt(i));
        }
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode.getExplanationColor().getRGB());
        no.setPosition(this.getPosition().getX() + 1.3 * raio, this.getPosition().getY() + 1.3 * raio);
        no.setName(resource.getString("copyName") + this.getName());
        no.setDescription(resource.getString("copyName") + this.getDescription());
        no.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
        return no;
    }
    
    public Object clone() {
    	ProbabilisticNode cloned = new ProbabilisticNode();
    	cloned.tabelaPot = (ProbabilisticTable)this.tabelaPot.clone();
		ProbabilisticNode.setDescriptionColor(ProbabilisticNode.getDescriptionColor().getRGB());
		ProbabilisticNode.setExplanationColor(ProbabilisticNode.getExplanationColor().getRGB());
		cloned.setDescription(this.getDescription());
		cloned.setName(this.getName());
		cloned.setPosition(this.getPosition().getX(), this.getPosition().getY());
		cloned.setParents(SetToolkit.clone(parents));
		cloned.setChildren(SetToolkit.clone(this.getChildren()));
		cloned.setStates(SetToolkit.clone(states));
		cloned.setAdjacents(SetToolkit.clone(this.getAdjacents()));
		cloned.setSelected(this.isSelected());
        cloned.setExplanationDescription(this.getExplanationDescription());
        cloned.setPhrasesMap(this.getPhrasesMap());
        cloned.setInformationType(this.getInformationType());
        float[] marginais = new float[super.marginais.length];
        System.arraycopy(super.marginais, 0, marginais, 0, marginais.length);
        cloned.marginais = marginais;
        
        return cloned;
    }


    /**
     *  Retorna a tabela de potencial desta variavel.
     *
     *@return    tabela de potencial
     */
    public PotentialTable getPotentialTable() {
        return tabelaPot;
    }


    /**
     * Calcula a marginal deste nó.
     */
    protected void marginal() {
        marginais = new float[getStatesSize()];
        PotentialTable auxTab = (PotentialTable) cliqueAssociado.getPotentialTable().clone();
        int index = auxTab.indexOfVariable(this);
        int size = cliqueAssociado.getPotentialTable().variableCount();
        for (int i = 0; i < size; i++) {
            if (i != index) {
                auxTab.removeVariable(cliqueAssociado.getPotentialTable().getVariableAt(i));
            }
        }

        int tableSize = auxTab.tableSize();
        for (int i = 0; i < tableSize; i++) {
            marginais[i] = auxTab.getValue(i);
        }
    }


    /**
     * Insere um novo estado e atualiza as tabelas afetadas.
     * Sobrescreve o método da superclasse Node.
     *
     * @param estado estado a ser adicionado
     */
    public void appendState(String estado) {
        updateState(estado, true);
    }

    /**
     *  Retira o estado criado mais recentemente e
     *  atualiza as tabelas afetadas. Sobrescreve o método
     *  da superclasse Node.
     */
    public void removeLastState() {
        if (states.size() > 1) {
//            super.removeLastState();
            updateState(null, false);
        }
    }

    /**
     *  Utilizado para atualizar as tabelas afetadas
     *  ao inserir e remover novos estados.
     *
     *@param  estado  estado a ser inserido / removido.
     *@param  insere  true se for para inserir e false se for para remover.
     */
    private void updateState(String estado, boolean insere) {
        int d = getStatesSize();
        if (d > 0) {
            while (d <= tabelaPot.tableSize()) {
                if (insere) {
                    tabelaPot.addValueAt(d++, 0);
                } else {
                    tabelaPot.removeValueAt(d);
                }
                d += getStatesSize();
            }
        }        
        
		NodeList clones[] = new NodeList[getChildren().size()];
		int indexes[] = new int[getChildren().size()];
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE) {
        		continue;
        	}
       		PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            clones[i] = auxTab.cloneVariables();
            indexes[i] = auxTab.indexOfVariable(this);     
        }
        
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE) {
        		continue;
        	}
            PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            int l = indexes[i];
            NodeList auxList = clones[i];            
            for (int k = auxList.size() - 1; k >= l; k--) {
                auxTab.removeVariable(auxList.get(k));
            }
        }
        
        if (insere) {
          	super.appendState(estado);
        } else {
        	super.removeLastState();        	
        }
       
        
        for (int i = 0; i < getChildren().size(); i++) {
        	if (getChildren().get(i).getType() == Node.DECISION_NODE_TYPE) {
        		continue;
        	}
            PotentialTable auxTab = ((ITabledVariable) getChildren().get(i)).getPotentialTable();
            int l = indexes[i];
            NodeList auxList = clones[i];         
            for (int k = l; k < auxList.size(); k++) {
                auxTab.addVariable(auxList.get(k));
            }
        }
    }

    /**
     *  Retorna a cor do nó.
     *
     * @return cor dos nós probabilísticos.
     */
    public static Color getDescriptionColor() {
        return descriptionColor;
    }

    /**
     *  Modifica a cor do nó de descrição.
     *
     *@param c O novo RGB da cor do nó.
     */
    public static void setDescriptionColor(int c) {
        descriptionColor = new Color(c);
    }
    
    /**
     *  Modifica a cor do nó de explanação.
     *
     *@param c O novo RGB da cor do nó.
     */
    public static void setExplanationColor(int c) {
        explanationColor = new Color(c);
    }
    

	/**
	 * Gets the explanationColor.
	 * @return Returns a Color
	 */
	public static Color getExplanationColor() {
		return explanationColor;
	}
	
	@Override
	public void setSelected(boolean b) {
		// Update the DrawEllipse selection state
		drawEllipse.setSelected(b);
		super.setSelected(b);
	}
	
	@Override
	public void paint(Graphics2D graphics) {
		if (getInformationType() == Node.DESCRIPTION_TYPE) {
			drawEllipse.setFillColor(getDescriptionColor());
    	} else if (getInformationType() == Node.EXPLANATION_TYPE) {
    		drawEllipse.setFillColor(getExplanationColor());
    	}
		super.paint(graphics);
	}

}
