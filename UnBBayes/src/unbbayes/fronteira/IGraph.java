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

package unbbayes.fronteira;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import unbbayes.controlador.*;
import unbbayes.jprs.jbn.*;

/**
 *  Essa classe é responsável por desenhar a rede Bayesiana na tela. Ela extende a classe
 *  <code>JPanel</code> para ser inserida na <code>TDesenho</code>. Ela também implementa
 *  as interfaces MouseListener e MouseMotionListener, para poder tratar os eventos de
 *  mouse e desenhar a rede Bayesiana.
 *
 *@author     Michael S. Onishi
 *@author     Rommel N. Carvalho
 *@created    27 de Junho de 2001
 *@see        JPanel
 */
public class IGraph extends JPanel implements MouseListener, MouseMotionListener {

    private WindowController controller;
    private List arc;
    private List node;
    private List selectedGroup;
    private Node presentNode;
    private Node movingNode;
    private Object selected;
    private Graphics2D view;
    private Point2D.Double presentBeginArc;
    private Point2D.Double presentEndArc;
    private Point2D.Double beginSelectionPoint;
    private Point2D.Double endSelectionPoint;
    private Line2D.Double presentArc;
    private boolean bArc;
    private boolean bProbabilisticNode;
	private boolean bDecisionNode;
	private boolean bUtilityNode;
    private boolean bNodeMoved;
    private boolean bMoveArc;
    private boolean bMoveNode;
    private boolean bScroll;
    private boolean bSelect;
    private boolean bFirstTime;
    private Color arcColor;
    private Color selectionColor;
    private Color backColor;
    private double radius;
    private int scrollX;
    private int scrollY;
    private JViewport graphViewport;
    private Dimension visibleDimension;
    private Dimension graphDimension;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.fronteira.resources.FronteiraResources");


    /**
     *  O construtor é responsável por iniciar todas as variáveis que serão
     *  utilizadas por essa classe para que se possa desenhar a rede Bayesiana.
     *
     *@param  controlador  o controlador (<code>TControladorTelaPrincipal</code>)
     *@param  graphViewport a tela, (<code>TViewport</code>), onde será inserida essa classe
     */
    public IGraph(WindowController controlador, JViewport graphViewport) {
        super();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(controlador);
        this.controller = controlador;
        this.setRequestFocusEnabled(true);
        this.graphViewport = graphViewport;
        this.setSize(800, 600);

        arc = new ArrayList();
        node = new ArrayList();
        selectedGroup = new ArrayList();
        presentBeginArc = new Point2D.Double();
        presentEndArc = new Point2D.Double();
        presentArc = new Line2D.Double();
        endSelectionPoint = new Point2D.Double();
        beginSelectionPoint = new Point2D.Double();
        bArc = false;
        bProbabilisticNode = false;
		bDecisionNode = false;
		bUtilityNode = false;
        bNodeMoved = false;
        bMoveArc = false;
        bMoveNode = false;
        bScroll = false;
        bSelect = false;
        bFirstTime = true;
        arcColor = Color.black;
        selectionColor = Color.red;
        backColor = Color.white;
		if (controller.getRede().getRadius() > 40) {
            radius = 40;
		} else if (controller.getRede().getRadius() < 10) {
			radius = 10;
		} else {
		    radius = controller.getRede().getRadius();
		}
        scrollX = 0;
        scrollY = 0;
        graphDimension = new Dimension(1500, 1500);
        visibleDimension = new Dimension(0, 0);
    }


    /**
     *  Seta o atributo selecionado (<code>Node</code> ou <code>Edge</code>) do objeto da classe IGraph
     *
     *@param  selecionado  O novo valor para selecionado
     *@see    Node
     *@see    Edge
     */
     /*
    public void setSelecionado(Object selecionado) {
        this.selecionado = selecionado;
    }
    */


    /**
     *  Seta o atributo corArco (cor dos arcos) do objeto da classe IGraph
     *
     *@param  arcColor  A nova cor, <code>Color</code>, dos arcos
     *@see Color
     */
    public void setArcColor(Color arcColor) {
        this.arcColor = arcColor;
    }


    /**
     *  Seta o atributo corSelecao (cor de seleção) do objeto da classe IGraph
     *
     *@param  selectionColor  A nova cor, <code>Color</code>, de seleção
     *@see Color
     */
    public void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
    }


    /**
     *  Seta o atributo corFundo (cor de fundo) do objeto da classe IGraph
     *
     *@param  backColor  A nova cor, <code>Color</code>, de Fundo
     *@see Color
     */
    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }


    /**
     *  Seta o atributo raio (raio dos nós) do objeto da classe IGraph
     *
     *@param  radius  O novo valor para o raio, <code>double</code>, dos nós
     */
    public void setRadius(double radius) {
        this.radius = radius;
		// gato pro momento
		controller.getRede().setRadius(radius);
    }


    /**
     *  Seta o atributo graphDimension (tamanho da rede Bayesiana) do objeto da classe IGraph
     *
     *@param  graphDimension  O novo valor do tamanho da rede (<code>Dimension</code>)
     *@see Dimension
     */
    public void setGraphDimension(Dimension graphDimension) {
        this.graphDimension = graphDimension;
    }


    /**
     *  Seta o atributo no do objeto da classe IGraph
     *
     *@param  node  O novo valor para o no (<code>List</code>)
     *@see List
     */
    public void setNode(List node) {
        this.node = node;
    }


    /**
     *  Seta o atributo arco do objeto da classe IGraph
     *
     *@param  arc  O novo valor para o arco (<code>List</code>)
     *@see List
     */
    public void setArc(List arc) {
        this.arc = arc;
    }

    /**
     *  Seta o valor de bSelecionar (valor booleano para selecionar) do objeto da classe IGraph
     *
     *@param  b  O booleano true ou false
     */
    public void setbSelect(boolean b) {
        bSelect = b;
        if (b) {
          setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
        else {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     *  Esse método seta o valor do bPropabilisticNo (valor booleano para desenhar
	 *  o nó de probabilidade)
     *
     *@param  b  O booleano true ou false
     */
    public void setbProbabilisticNode(boolean b) {
        bProbabilisticNode = b;
        if (b) {
          setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        else {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

	/**
     *  Esse método seta o valor do bPropabilisticNo (valor booleano para desenhar
	 *  o nó de decisão)
     *
     *@param  b  O booleano true ou false
     */
    public void setbDecisionNode(boolean b) {
        bDecisionNode = b;
        if (b) {
          setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        else {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

	/**
     *  Esse método seta o valor do bPropabilisticNo (valor booleano para desenhar
	 *  o nó de utilidade)
     *
     *@param  b  O booleano true ou false
     */
    public void setbUtilityNode(boolean b) {
        bUtilityNode = b;
        if (b) {
          setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        else {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }


    /**
     *  Esse método seta o valor do bArco (valor booleano para desenhar o arco)
     *
     *@param  b  O booleano true ou false
     */
    public void setbArc(boolean b) {
        bArc = b;
        if (b) {
          setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
        else {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }


    /**
     *  Retorna a cor de fundo
     *
     *@return    a backColor (<code>Color</code>)
     *@see Color
     */
    public Color getBackColor() {
        return this.backColor;
    }


    /**
     *  Retorna a cor de seleção
     *
     *@return    a selectionColor (<code>Color</code>)
     *@see Color
     */
    public Color getSelectionColor() {
        return this.selectionColor;
    }

    /**
     *  Retorna a cor do arco
     *
     *@return    a arcColor (<code>Color</code>)
     *@see Color
     */
    public Color getArcColor() {
        return this.arcColor;
    }

    /**
     *  Retorna o objeto selecionado (<code>Object</code>), que pode ser um <code>Node</code> ou <code>Edge</code>
     *
     *@return    O valor do <code>Object</code> selected
     *@see Object
     *@see Node
     *@see Edge
     */
    public Object getSelected() {
        return this.selected;
    }

    /**
     *  Retorna uma lista de selecionados (<code>List</code>), que podem ser um <code>Node</code> e/ou <code>Edge</code>
     *
     *@return    O valor do <code>List</code> selectedGroup
     *@see List
     *@see Node
     *@see Edge
     */
    public List getSelectedGroup() {
        return this.selectedGroup;
    }


    /**
     *  Pega a <code>Dimension</code> tamanhoRede do objeto da classe IGraph
     *
     *@return    A <code>Dimension</code> graphDimension
     */
    public Dimension getGraphDimension() {
        return this.graphDimension;
    }


    /**
     *  Pega o nó que se encontra na posição x,y
     *
     *@param  x  A posição x (double)
     *@param  y  A posição y (double)
     *@return    O nó encontrado (<code>Node</code>)
     *@see Node
     */
    public Node getNode(double x, double y) {
        double x1;
        double y1;

        for (int i = 0; i < node.size(); i++) {
            Node nodeAux = (Node) node.get(i);
            x1 = nodeAux.getPosicao().getX();
            y1 = nodeAux.getPosicao().getY();

            if ((x >= x1 - radius) && (x <= x1 + radius) && (y >= y1 - radius) && (y <= y1 + radius)) {
                return nodeAux;
            }
        }
        return null;
    }

    /**
     *  Pega o atributo focusTransversable do objeto da classe IGraph
     *
     *@return    True como valor do focusTransversable (método necessário para que se possa tratar evento de tecla)
     */
    public boolean isFocusTraversable() {
        return true;
    }


    /**
     *  Pega o arco que se encontra na posição x,y
     *
     *@param  x  A posição x (double)
     *@param  y  A posição y (double)
     *@return    O arco encontrado (<code>Edge</code>)
     *@see Edge
     */
    public Edge getArc(double x, double y) {
        double x1;
        double y1;
        double x2;
        double y2;

        for (int i = 0; i < arc.size(); i++) {
            Edge arcoPegar = (Edge) arc.get(i);
            x1 = arcoPegar.getOriginNode().getPosicao().getX();
            y1 = arcoPegar.getOriginNode().getPosicao().getY();
            x2 = arcoPegar.getDestinationNode().getPosicao().getX();
            y2 = arcoPegar.getDestinationNode().getPosicao().getY();

            double yTeste = ((y2 - y1) / (x2 - x1)) * x + (y1 - x1 * ((y2 - y1) / (x2 - x1)));
            double xTeste = (y - (y1 - x1 * ((y2 - y1) / (x2 - x1)))) / ((y2 - y1) / (x2 - x1));

            Node no1 = arcoPegar.getOriginNode();
            Node no2 = arcoPegar.getDestinationNode();

            Point2D.Double ponto1 = getPoint(no1.getPosicao(), no2.getPosicao(), radius);
            Point2D.Double ponto2 = getPoint(no2.getPosicao(), no1.getPosicao(), radius);

            if (ponto1.getX() < ponto2.getX()) {
                if (((y <= yTeste + 5) && (y >= yTeste - 5)) || ((x <= xTeste + 5) && (x >= xTeste - 5))) {
                    if (ponto1.getY() < ponto2.getY()) {
                        if ((y >= ponto1.getY() - 5) && (y <= ponto2.getY() + 5) && (x >= ponto1.getX() - 5) && (x <= ponto2.getX() + 5)) {
                            return arcoPegar;
                        }
                    }
                    else {
                        if ((y >= ponto2.getY() - 5) && (y <= ponto1.getY() + 5) && (x >= ponto1.getX() - 5) && (x <= ponto2.getX() + 5)) {
                            return arcoPegar;
                        }
                    }
                }
            }
            else {
                if (((y <= yTeste + 5) && (y >= yTeste - 5)) || ((x <= xTeste + 5) && (x >= xTeste - 5))) {
                    if (ponto1.getY() < ponto2.getY()) {
                        if ((y >= ponto1.getY() - 5) && (y <= ponto2.getY() + 5) && (x >= ponto2.getX() - 5) && (x <= ponto1.getX() + 5)) {
                            return arcoPegar;
                        }
                    }
                    else {
                        if ((y >= ponto2.getY() - 5) && (y <= ponto1.getY() + 5) && (x >= ponto2.getX() - 5) && (x <= ponto1.getX() + 5)) {
                            return arcoPegar;
                        }
                    }
                }
            }
        }

        return null;
    }


    //
    /**
     *  Método para achar o ponto do arco (<code>Point2D.Double</code>) na circunferência do nó em
     *  relação ao ponto1 (<code>Point2D.Double</code>)
     *
     *@param  point1  Centro da circunferência do nó de origem
     *@param  point2  Centro da circunferência do nó de destino
     *@param  r       O raio da circunferência
     *@return         O ponto do arco na circunferência
     *@see Point2D.Double
     */
    public Point2D.Double getPoint(Point2D.Double point1, Point2D.Double point2, double r) {
        double x = 0;
        double y = 0;
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        if (x2 < x1) {
            x = Math.abs((r * Math.cos(Math.atan((y2 - y1) / (x2 - x1)))) - x1);
            y = Math.abs((r * Math.sin(Math.atan((y2 - y1) / (x2 - x1)))) - y1);
        }
        else {
            x = Math.abs((r * Math.cos(Math.atan((y2 - y1) / (x2 - x1)))) + x1);
            y = Math.abs((r * Math.sin(Math.atan((y2 - y1) / (x2 - x1)))) + y1);
        }
        return new Point2D.Double(x, y);
    }


    /**
     *  Pega o atributo raio (double) do objeto da classe IGraph
     *
     *@return    O raio do nó
     */
    public double getRadius() {
        return this.radius;
    }


    /**
     *  Pega o atributo graphViewport (<code>JViewport</code>) do objeto da classe IGraph
     *
     *@return    O graphViewport (container onde essa classe se encontra inserida)
     *@see JViewport
     */
    public JViewport getGraphViewport() {
        return this.graphViewport;
    }


    /**
     *  Pega o atributo tamanhoVisivel (<code>Dimension</code>) do objeto da classe IGraph
     *
     *@return    O tamanho visível dessa classe
     *@see Dimension
     *
     *//*
    public Dimension getTamanhoVisivel() {
        return this.tamanhoVisivel;
    }
    */


    /**
     *  Pega o maior ponto (<code>Point2D.Double</code>) do objeto da classe IGraph
     *
     *@return    O maior ponto (x,y) existente nessa rede Bayesiana
     *@see Point2D.Double
     */
    public Point2D.Double getBiggestPoint() {
        double maiorX = 0;
        double maiorY = 0;

        for (int i = 0; i < node.size(); i++) {
            Node noAux = (Node) node.get(i);
            if (maiorX < noAux.getPosicao().getX() + radius) {
                maiorX = noAux.getPosicao().getX() + radius;
            }

            if (maiorY < noAux.getPosicao().getY() + radius) {
                maiorY = noAux.getPosicao().getY() + radius;
            }
        }
        if (maiorX < visibleDimension.getWidth()) {
            maiorX = graphViewport.getViewSize().getWidth();
        }
        if (maiorY < visibleDimension.getHeight()) {
            maiorY = graphViewport.getViewSize().getHeight();
        }

        return new Point2D.Double(maiorX, maiorY);
    }


    /**
     *  Pega o tamanho necessário para repintar a tela (<code>Rectangle</code>)
     *
     *@return    O tamanho necessário para repintar a tela
     *@see Rectangle
     */
    public Rectangle getRectangleRepaint() {
        double maiorX;
        double maiorY;
        double menorX;
        double menorY;

        if (bMoveNode)
        {
            Node noAux = (Node) selected;
            maiorX = noAux.getPosicao().getX();
            menorX = noAux.getPosicao().getX();
            maiorY = noAux.getPosicao().getY();
            menorY = noAux.getPosicao().getY();

            Node noAux2;
            for (int i = 0; i < noAux.getParents().size(); i++) {
                noAux2 = (Node) noAux.getParents().get(i);

                if (maiorX < noAux2.getPosicao().getX()) {
                    maiorX = noAux2.getPosicao().getX();
                }
                else {
                    if (menorX > noAux2.getPosicao().getX()) {
                        menorX = noAux2.getPosicao().getX();
                    }
                }

                if (maiorY < noAux2.getPosicao().getY()) {
                    maiorY = noAux2.getPosicao().getY();
                }
                else {
                    if (menorY > noAux2.getPosicao().getY()) {
                        menorY = noAux2.getPosicao().getY();
                    }
                }
            }

            for (int i = 0; i < noAux.getChildren().size(); i++) {
                noAux2 = (Node) noAux.getChildren().get(i);

                if (maiorX < noAux2.getPosicao().getX()) {
                    maiorX = noAux2.getPosicao().getX();
                }
                else {
                    if (menorX > noAux2.getPosicao().getX()) {
                        menorX = noAux2.getPosicao().getX();
                    }
                }

                if (maiorY < noAux2.getPosicao().getY()) {
                    maiorY = noAux2.getPosicao().getY();
                }
                else {
                    if (menorY > noAux2.getPosicao().getY()) {
                        menorY = noAux2.getPosicao().getY();
                    }
                }
            }
            return new Rectangle((int) (menorX - 6 * radius), (int) (menorY - 6 * radius), (int) (maiorX - menorX + 12 * radius), (int) (maiorY - menorY + 12 * radius));
        } else {
            return new Rectangle((int) controller.getTela().getJspGraph().getHorizontalScrollBar().getValue(), (int) controller.getTela().getJspGraph().getVerticalScrollBar().getValue(), (int) visibleDimension.getWidth(), (int) visibleDimension.getHeight());
        }
    }


    /**
     *  Método responsável por repintar a rede Bayesiana
     */
    public void update() {
        this.repaint(getRectangleRepaint());
    }



    /**
     *  Método responsável por tratar o evento de botão de mouse pressionado
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mousePressed(MouseEvent e) {
        //setar o melhor scrollMode para desenhar e mexer na rede
        graphViewport.setScrollMode(JViewport.BLIT_SCROLL_MODE);

        if (e.getModifiers() == e.BUTTON1_MASK) {
            Node node = getNode(e.getX(), e.getY());

            if (bArc) {
                if (node != null) {
                    bMoveArc = true;
                    //seto o ponto origem para o arco
                    presentBeginArc.setLocation(node.getPosicao().getX(), node.getPosicao().getY());
                    presentArc.setLine(node.getPosicao().getX(), node.getPosicao().getY(), e.getX(), e.getY());
                }
            }

            if ((!bArc) && (node != null)) {
                if (!node.isSelecionado()) {
                    selectNode(node);
                    if (controller.getTela().isCompiled()) {
                        for (int i = 0; i < controller.getTela().getEvidenceTree().getRowCount(); i++) {
                            if (controller.getTela().getEvidenceTree().getPathForRow(i).getLastPathComponent().toString().equals(selected.toString())) {
                                controller.getTela().getEvidenceTree().setSelectionPath(controller.getTela().getEvidenceTree().getPathForRow(i));
                                break;
                            }
                        }
                    }
                }
                if ((bProbabilisticNode) || (bDecisionNode) || (bUtilityNode)) {
                    bProbabilisticNode = false;
					bDecisionNode = false;
					bUtilityNode = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            Edge arc = getArc(e.getX(), e.getY());

            if ((!bProbabilisticNode) && (!bDecisionNode) && (!bUtilityNode) && (arc != null)) {
                selectArc(arc);
                if (bArc) {
                    bArc = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            if ((!bProbabilisticNode) && (!bDecisionNode) && (!bUtilityNode) && (!bArc)) {
                if ((node != null) && (node.isSelecionado()) && (!bSelect) && (selectedGroup.size() == 0)) {
                    movingNode = node;
                    bMoveNode = true;
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                }
            }

            if (bProbabilisticNode) {
                if (node == null) {
                    controller.insertProbabilisticNode(e.getX(), e.getY());
                }
            }

			if (bDecisionNode) {
                if (node == null) {
                    controller.insertDecisionNode(e.getX(), e.getY());
                }
            }

			if (bUtilityNode) {
                if (node == null) {
                    controller.insertUtilityNode(e.getX(), e.getY());
                }
            }
        }

        if (bSelect) {
            beginSelectionPoint.setLocation(e.getX(), e.getY());
            endSelectionPoint.setLocation(e.getX(), e.getY());
        }

        this.repaint((int) controller.getTela().getJspGraph().getHorizontalScrollBar().getValue(), (int) controller.getTela().getJspGraph().getVerticalScrollBar().getValue(), (int) visibleDimension.getWidth(), (int) visibleDimension.getHeight());
    }


    /**
     *  Método responsável por tratar o evento de clique no botão do mouse
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseClicked(MouseEvent e) {

        Node node = getNode(e.getX(), e.getY());
        if ((node != null) && (e.getModifiers() == e.BUTTON1_MASK) && (e.getClickCount() == 2)) {
            controller.getTela().setTable(controller.retornarTabela(node));
            controller.getTela().setTableOwner(node);
            if (controller.getTela().isCompiled()) {
                for (int i = 0; i < controller.getTela().getEvidenceTree().getRowCount(); i++) {
                    if (controller.getTela().getEvidenceTree().getPathForRow(i).getLastPathComponent().toString().equals(selected.toString())) {
                        if (controller.getTela().getEvidenceTree().isExpanded(controller.getTela().getEvidenceTree().getPathForRow(i))) {
                            controller.getTela().getEvidenceTree().collapsePath(controller.getTela().getEvidenceTree().getPathForRow(i));
                        }
                        else {
                            controller.getTela().getEvidenceTree().expandPath(controller.getTela().getEvidenceTree().getPathForRow(i));
                        }
                        break;
                    }
                }
            }
        }
    }


    /**
     *  Método responsável por tratar o evento de botão de mouse soltado
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseReleased(MouseEvent e) {
        Node endNode = getNode(e.getX(), e.getY());
        if ((bArc) && (e.getModifiers() == e.BUTTON1_MASK)) {
            Node beginNode = getNode(presentBeginArc.getX(), presentBeginArc.getY());
            if ((endNode != null) && (controller.getRede().existeArco(beginNode, endNode) == -1)) {
                insertArc(presentBeginArc.getX(), presentBeginArc.getY(), presentEndArc.getX(), presentEndArc.getY());
            }

            //deixa a arco atual como um ponto em 0,0
            presentArc.setLine(0, 0, 0, 0);
        }

        Edge arc = getArc(e.getX(), e.getY());

        if ((!bProbabilisticNode) && (!bDecisionNode) && (!bUtilityNode) && (!bArc) && (endNode == null) && (arc == null) && (e.getModifiers() == e.BUTTON1_MASK)) {
            unselectNode();
            unselectArc();
            selected = null;
        }

        if (bMoveArc) {
            //seta para false para dizer que acabou o movimento
            bMoveArc = false;
        }

        if ((!bProbabilisticNode) && (!bDecisionNode) && (!bUtilityNode) && (!bArc) && (!bSelect)) {
            bMoveNode = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if ((e.getModifiers() == e.BUTTON3_MASK) && ((bProbabilisticNode) || (bDecisionNode) || (bUtilityNode) || (bArc) || (bSelect))) {
            //seta o booleano do arco e nó e selecionar para false
            bProbabilisticNode = false;
			bDecisionNode = false;
			bUtilityNode = false;
            bArc = false;
            bSelect = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if (bNodeMoved) {
            //retorna o valor de bArrastouNo para false para futura comparação
            bNodeMoved = false;
        }

        if (bSelect) {
            endSelectionPoint.setLocation(e.getX(), e.getY());
            setSelectedGroup(beginSelectionPoint, endSelectionPoint);
            //setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            beginSelectionPoint.setLocation(-1,-1);
            endSelectionPoint.setLocation(-1,-1);
        }

        update();
    }


    /**
     *  Método responsável por tratar o evento do mouse entrar nesse componente (objeto da classe IGraph)
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        if ((!bMoveNode) && (!bMoveArc)) {
            //setar o tamanho visivel da rede como o tamanho o jspDesenho - raio
            visibleDimension = new Dimension((int) (controller.getTela().getJspGraph().getSize().getWidth()), (int) (controller.getTela().getJspGraph().getSize().getHeight()));

            //setar o tamanho do JViewport desenho e de seu view
            graphViewport.setSize(graphDimension);
            graphViewport.setViewSize(graphDimension);

            //receber o focus para poder tratar o evento de tecla
            this.requestFocus();
        }
    }


    /**
     *  Método responsável por tratar o evento do mouse sair desse componente (objeto da classe IGraph)
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseExited(MouseEvent e) {

    }


    /**
     *  Método responsável por tratar o evento arrastar o mouse com o botão pressionado
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseDragged(MouseEvent e) {
        //ponto final para mostrar a area de selecao
        if (bSelect) {
            updateEndSelectionPoint(e.getX(), e.getY());
        }

        //mover o scroll junto com a seta e/ou nó
        if ((e.getX() < graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight()) && (e.getX() + 2 * radius > visibleDimension.getWidth() + controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() + 2 * radius > visibleDimension.getHeight() + controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
            if (bMoveNode) {
                controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (movingNode.getPosicao().getX() + 2 * radius - visibleDimension.getWidth()));
                controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (movingNode.getPosicao().getY() + 2 * radius - visibleDimension.getHeight()));
            }
            else {
                controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (e.getX() + 2 * radius - visibleDimension.getWidth()));
                controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (e.getY() + 2 * radius - visibleDimension.getHeight()));
            }
        }
        else {
            if ((e.getX() < graphDimension.getWidth()) && (e.getX() + 2 * radius > visibleDimension.getWidth() + controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() + 2 * radius <= visibleDimension.getHeight() + controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
                if (bMoveNode) {
                    controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (movingNode.getPosicao().getX() + 2 * radius - visibleDimension.getWidth()));
                }
                else {
                    controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (e.getX() + 2 * radius - visibleDimension.getWidth()));
                }
            }
            else {
                if ((e.getY() < graphDimension.getHeight()) && (e.getX() + 2 * radius <= visibleDimension.getWidth() + controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() + 2 * radius > visibleDimension.getHeight() + controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
                    if (bMoveNode) {
                        controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (movingNode.getPosicao().getY() + 2 * radius - visibleDimension.getHeight()));
                    }
                    else {
                        controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (e.getY() + 2 * radius - visibleDimension.getHeight()));
                    }
                }
                else {
                    if ((e.getX() - radius > controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() - radius > controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
                    }
                    else {

                        if ((e.getX() > 0) && (e.getY() > 0) && (e.getX() - radius < controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() - radius < controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
                            if (bMoveNode) {
                                controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (movingNode.getPosicao().getX() - radius));
                                controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (movingNode.getPosicao().getY() - radius));
                            }
                            else {
                                controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (e.getX() - radius));
                                controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (e.getY() - radius));
                            }
                        }
                        else {
                            if ((e.getY() > 0) && (e.getX() - radius >= controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() - radius < controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
                                if (bMoveNode) {
                                    controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (movingNode.getPosicao().getY() - radius));
                                }
                                else {
                                    controller.getTela().getJspGraph().getVerticalScrollBar().setValue((int) (e.getY() - radius));
                                }
                            }
                            else {

                                if ((e.getX() > 0) && (e.getX() - radius < controller.getTela().getJspGraph().getHorizontalScrollBar().getValue()) && (e.getY() - radius >= controller.getTela().getJspGraph().getVerticalScrollBar().getValue())) {
                                    if (bMoveNode) {
                                        controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (movingNode.getPosicao().getX() - radius));
                                    }
                                    else {
                                        controller.getTela().getJspGraph().getHorizontalScrollBar().setValue((int) (e.getX() - radius));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (e.getModifiers() == e.BUTTON1_MASK) {
            //move nó somente se este menos o raio for menor que (0,0)
            if ((bMoveNode) && (e.getX() > radius) && (e.getY() > radius) && (e.getX() < graphDimension.getWidth() - radius) && (e.getY() < graphDimension.getHeight() - radius)) {
                presentNode = movingNode;
                updatePresentNode(e.getX(), e.getY());
                //seta a variável bArrastouNo para true para futura comparação
                bNodeMoved = true;
            }
            else {
                //atualiza a posicão da arco que está sendo desenhada, somente se este for maior que (0,0)
                if ((bArc) && (bMoveArc) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() < graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight())) {
                    presentEndArc.setLocation(e.getX(), e.getY());
                    updatePresentArc(e.getX(), e.getY());
                }
                else {
                    if ((bArc) && (bMoveArc) && (e.getX() <= 0) && (e.getY() > 0) && (e.getX() < graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight())) {
                        presentEndArc.setLocation(e.getX(), e.getY());
                        updatePresentArc(0, e.getY());
                    }
                    else {
                        if ((bArc) && (bMoveArc) && (e.getX() > 0) && (e.getY() <= 0) && (e.getX() < graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight())) {
                            presentEndArc.setLocation(e.getX(), e.getY());
                            updatePresentArc(e.getX(), 0);
                        }
                        else {
                            if ((bArc) && (bMoveArc) && (e.getX() <= 0) && (e.getY() <= 0) && (e.getX() < graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight())) {
                                presentEndArc.setLocation(e.getX(), e.getY());
                                updatePresentArc(0, 0);
                            }
                            else {
                                if ((bArc) && (bMoveArc) && (e.getX() <= 0) && (e.getY() > 0) && (e.getX() < graphDimension.getWidth()) && (e.getY() >= graphDimension.getHeight())) {
                                    presentEndArc.setLocation(e.getX(), e.getY());
                                    updatePresentArc(0, graphDimension.getHeight());
                                }
                                else {
                                    if ((bArc) && (bMoveArc) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() < graphDimension.getWidth()) && (e.getY() >= graphDimension.getHeight())) {
                                        presentEndArc.setLocation(e.getX(), e.getY());
                                        updatePresentArc(e.getX(), graphDimension.getHeight());
                                    }
                                    else {
                                        if ((bArc) && (bMoveArc) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() >= graphDimension.getWidth()) && (e.getY() >= graphDimension.getHeight())) {
                                            presentEndArc.setLocation(e.getX(), e.getY());
                                            updatePresentArc(graphDimension.getWidth(), graphDimension.getHeight());
                                        }
                                        else {
                                            if ((bArc) && (bMoveArc) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() >= graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight())) {
                                                presentEndArc.setLocation(e.getX(), e.getY());
                                                updatePresentArc(graphDimension.getWidth(), e.getY());
                                            }
                                            else {
                                                if ((bArc) && (bMoveArc) && (e.getX() > 0) && (e.getY() <= 0) && (e.getX() >= graphDimension.getWidth()) && (e.getY() < graphDimension.getHeight())) {
                                                    presentEndArc.setLocation(e.getX(), e.getY());
                                                    updatePresentArc(graphDimension.getWidth(), 0);
                                                }
                                                else {
                                                    if ((bMoveNode) && (e.getX() <= radius) && (e.getY() > radius) && (e.getX() < graphDimension.getWidth() - radius) && (e.getY() < graphDimension.getHeight() - radius)) {
                                                        presentNode = movingNode;
                                                        updatePresentNode(radius, e.getY());
                                                        //seta a variável bArrastouNo para true para futura comparação
                                                        bNodeMoved = true;
                                                    }
                                                    else {
                                                        if ((bMoveNode) && (e.getX() > radius) && (e.getY() <= radius) && (e.getX() < graphDimension.getWidth() - radius) && (e.getY() < graphDimension.getHeight() - radius)) {
                                                            presentNode = movingNode;
                                                            updatePresentNode(e.getX(), radius);
                                                            //seta a variável bArrastouNo para true para futura comparação
                                                            bNodeMoved = true;
                                                        }
                                                        else {
                                                            if ((bMoveNode) && (e.getX() <= radius) && (e.getY() <= radius) && (e.getX() < graphDimension.getWidth() - radius) && (e.getY() < graphDimension.getHeight() - radius)) {
                                                                presentNode = movingNode;
                                                                updatePresentNode(radius, radius);
                                                                //seta a variável bArrastouNo para true para futura comparação
                                                                bNodeMoved = true;
                                                            }
                                                            else {
                                                                if ((bMoveNode) && (e.getX() <= radius) && (e.getY() > radius) && (e.getX() < graphDimension.getWidth() - radius) && (e.getY() >= graphDimension.getHeight() - radius)) {
                                                                    presentNode = movingNode;
                                                                    updatePresentNode(radius, graphDimension.getHeight() - radius);
                                                                    //seta a variável bArrastouNo para true para futura comparação
                                                                    bNodeMoved = true;
                                                                }
                                                                else {
                                                                    if ((bMoveNode) && (e.getX() > radius) && (e.getY() > radius) && (e.getX() < graphDimension.getWidth() - radius) && (e.getY() >= graphDimension.getHeight() - radius)) {
                                                                        presentNode = movingNode;
                                                                        updatePresentNode(e.getX(), graphDimension.getHeight() - radius);
                                                                        //seta a variável bArrastouNo para true para futura comparação
                                                                        bNodeMoved = true;
                                                                    }
                                                                    else {
                                                                        if ((bMoveNode) && (e.getX() > radius) && (e.getY() > radius) && (e.getX() >= graphDimension.getWidth() - radius) && (e.getY() >= graphDimension.getHeight() - radius)) {
                                                                            presentNode = movingNode;
                                                                            updatePresentNode(graphDimension.getWidth() - radius, graphDimension.getHeight() - radius);
                                                                            //seta a variável bArrastouNo para true para futura comparação
                                                                            bNodeMoved = true;
                                                                        }
                                                                        else {
                                                                            if ((bMoveNode) && (e.getX() > radius) && (e.getY() > radius) && (e.getX() >= graphDimension.getWidth() - radius) && (e.getY() < graphDimension.getHeight() - radius)) {
                                                                                presentNode = movingNode;
                                                                                updatePresentNode(graphDimension.getWidth() - radius, e.getY());
                                                                                //seta a variável bArrastouNo para true para futura comparação
                                                                                bNodeMoved = true;
                                                                            }
                                                                            else {
                                                                                if ((bMoveNode) && (e.getX() > radius) && (e.getY() <= radius) && (e.getX() >= graphDimension.getWidth() - radius) && (e.getY() < graphDimension.getHeight() - radius)) {
                                                                                    presentNode = movingNode;
                                                                                    updatePresentNode(graphDimension.getWidth() - radius, radius);
                                                                                    //seta a variável bArrastouNo para true para futura comparação
                                                                                    bNodeMoved = true;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     *  Método responsável por tratar o evento de mover o mouse
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseMoved(MouseEvent e) {

    }

    /**
     *  Seta o atributo selecionado (boolean) do arco (<code>Edge</code>) para falso, caso exista selecionado
     *
     * @see Edge
     */
    public void unselectArc() {
        if (selected instanceof Edge) {
            ((Edge) selected).setSelecionado(false);
        }
    }


    /**
     *  Seta o atributo selecionado (boolean) do nó (<code>Node</code>) para falso, caso exista selecionado
     *
     * @see Node
     */
    public void unselectNode() {
        if (selected instanceof Node) {
            ((Node) selected).setSelected(false);
        }
    }


    /**
     *  Seta o atributo selecionado (boolean) do nó (<code>Node</code>) desejado para true
     *
     *@param  node  O nó desejado
     *@see Node
     */
    public void selectNode(Node node) {
        //deseleciona nó ou arco, caso algum esteja selecionado
        unselectNode();
        unselectArc();
        unselectAll();

        //seleciona o nó escolhido
        node.setSelected(true);
        selected = node;
    }


    /**
     *  Seta o atributo selecionado (boolean) do arco (<code>Edge</code>) desejado para true
     *
     *@param  arc  O arco desejado
     *@see Edge
     */
    public void selectArc(Edge arc) {
        //deseleciona nó ou arco, caso algum esteja selecionado
        unselectNode();
        unselectArc();
        unselectAll();

        //seleciona o arco escolhido
        arc.setSelecionado(true);
        selected = arc;
    }




    /**
     *  Método responsável por atualizar o arco (<code>Edge</code>) atual ao se mover um arco
     *
     *@param  x  Posição x (double) da ponta do arco
     *@param  y  Posição y (double) da ponta do arco
     *@see Edge
     */
    public void updatePresentArc(double x, double y) {
        Point2D.Double point = new Point2D.Double();
        Node nodeAux = (Node) node.get(0);
        point = getPoint(presentBeginArc, presentEndArc, radius);

        presentArc.setLine(point.getX(), point.getY(), x, y);
        presentEndArc.setLocation(x, y);

        update();
    }


    /**
     *  Método responsável por atualizar o nó (<code>Node</code>) atual ao se mover um nó
     *
     *@param  x  Posição x (double) do centro do nó
     *@param  y  Posição y (double) do centro do nó
     *@see Node
     */
    public void updatePresentNode(double x, double y) {
        presentNode.setPosicao(x, y);
        update();
    }

    /**
     *  Método responsável por atualizar o ponto final de seleção ao se mover o mouse para seleção
     *
     *@param  x  Posição x (double)
     *@param  y  Posição y (double)
     */
    public void updateEndSelectionPoint(double x, double y) {
        endSelectionPoint.setLocation(x, y);
        update();
    }


    /**
     *  Inere um arco (<code>Edge</code>) com origem na posição (x1,y1) e destino na posição (x2,y2)
     *
     *@param  x1  Posição x1 (double) da origem do arco
     *@param  y1  Posição y1 (double) da origem do arco
     *@param  x2  Posição x2 (double) do destino do arco
     *@param  y2  Posição y2 (double) do destino do arco
     */
    public void insertArc(double x1, double y1, double x2, double y2) {
        Node node1     = getNode(x1, y1);
        Node node2     = getNode(x2, y2);
        Edge insertArc = new Edge(node1, node2);

        //chama o controlador para inserir o arco na rede TRP
        controller.inserirArco(insertArc);
        update();
    }


    /**
     *  Método responsável por desenhar um arco a (<code>Edge</code>) desejado
     *
     *@param  a  O arco que se deseja desenhar
     *@return    A reta do arco (<code>Line2D.Double</code>)
     *@see Line2D.Double
     */
    public Line2D.Double drawArc(Edge a) {
        Point2D.Double point1 = new Point2D.Double();
        Point2D.Double point2 = new Point2D.Double();

        Node node1;
        Node node2;
        node1 = a.getOriginNode();
        node2 = a.getDestinationNode();

        point1 = getPoint(node1.getPosicao(), node2.getPosicao(), radius);
        point2 = getPoint(node2.getPosicao(), node1.getPosicao(), radius);

        return new Line2D.Double(point1, point2);
    }


    /**
     *  Método responsável por desenhar a ponta da seta do arco (<code>Edge</code>) desejado
     *
     *@param  a            O arco que se deseja desenhar a ponta da seta
     *@param  existArc     True se a seta já existe e false se ela está sendo inserida
     *@return              A ponta de uma seta (<code>GeneralPath</code>)
     *@see GeneralPath
     */
    public GeneralPath drawArrow(Edge a, boolean existArc) {
        Point2D.Double point1 = new Point2D.Double();
        Point2D.Double point2 = new Point2D.Double();
        GeneralPath arrow     = new GeneralPath();
        Node node1            = a.getOriginNode();
        Node node2            = a.getDestinationNode();

        double x1 = node1.getPosicao().getX();
        double y1 = node1.getPosicao().getY();
        double x2;
        double y2;
        double x3;
        double y3;
        double x4;
        double y4;

        //ponta da seta = ponto correspondente na circunferência do nó, caso a seta já esteja inserida - base da seta deslecada de 10 do centro do nó
        if (existArc) {
            point1 = getPoint(node2.getPosicao(), node1.getPosicao(), radius + 10);
            point2 = getPoint(node2.getPosicao(), node1.getPosicao(), radius);
            x2 = point2.getX();
            y2 = point2.getY();
        }
        //ponta do seta = ponto na ponta do mouse - base da seta deslecada de 10 da ponta do mouse
        else {
            point1 = getPoint(node2.getPosicao(), node1.getPosicao(), 10);
            x2 = node2.getPosicao().getX();
            y2 = node2.getPosicao().getY();
        }

        //se for no segundo ou quarto quadrante usamos as primeiras 4 equações, senão, usammos as outras 4
        if (((x1 > x2) && (y1 > y2)) || ((x1 < x2) && (y1 < y2))) {
            x3 = point1.getX() + 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
            y3 = point1.getY() - 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
            x4 = point1.getX() - 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
            y4 = point1.getY() + 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
        }
        else {
            x3 = point1.getX() + 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
            y3 = point1.getY() - 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
            x4 = point1.getX() - 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
            y4 = point1.getY() + 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
        }

        //montar a seta com os pontos obtidos
        arrow.moveTo((float) (x3), (float) (y3));
        arrow.lineTo((float) (x2), (float) (y2));
        arrow.lineTo((float) (x4), (float) (y4));
        arrow.lineTo((float) (x3), (float) (y3));

        return arrow;
    }

    /**
     *  Método responsável por definir quais os obejos (<code>Node</code> e/ou <code>Edge</code>) foram selecionados
     *
     *@param  p1  Ponto p1 (Point2D.Double) do início do retângulo
     *@param  p2  Ponto p2 (Point2D.Double) do fim do retângulo
     *@see Node
     *@see Point2D.Double
     */
    public void setSelectedGroup(Point2D.Double p1, Point2D.Double p2) {
        unselectAll();
        for (int i = 0; i < node.size(); i++) {
            Node nodeAux = (Node)node.get(i);
            if ( ( ((p1.getX() <= p2.getX()) && (nodeAux.getPosicao().getX() >= p1.getX()) && (nodeAux.getPosicao().getX() <= p2.getX()))
               || ((p2.getX() < p1.getX()) && (nodeAux.getPosicao().getX() >= p2.getX()) && (nodeAux.getPosicao().getX() <= p1.getX())) )
               && ( ((p1.getY() <= p2.getY()) && (nodeAux.getPosicao().getY() >= p1.getY()) && (nodeAux.getPosicao().getY() <= p2.getY()))
               || ((p2.getY() < p1.getY()) && (nodeAux.getPosicao().getY() >= p2.getY()) && (nodeAux.getPosicao().getY() <= p1.getY())) ) ) {
                //if ((noAux.getPosicao().getX() >= p1.getX()) && (noAux.getPosicao().getX() <= p2.getX()) && (noAux.getPosicao().getY() >= p1.getY()) && (noAux.getPosicao().getY() <= p2.getY())) {
                    selectedGroup.add(nodeAux);
                    nodeAux.setSelected(true);
                }
        }
        for (int i = 0; i < arc.size(); i++) {
            Edge arcAux = (Edge)arc.get(i);
            Node nodeAux1 = arcAux.getOriginNode();
            Node nodeAux2 = arcAux.getDestinationNode();
            //comentamos uma parte do if abaixo, pois queremos manter a relação dos arcos com os pais originais ou os
            //novos que serão copiados. Para isso temos que selecionar todos os arcos que um nó selecionado possui.
            if ( /*( ( ((p1.getX() <= p2.getX()) && (noAux1.getPosicao().getX() >= p1.getX()) && (noAux1.getPosicao().getX() <= p2.getX()))
               || ((p2.getX() < p1.getX()) && (noAux1.getPosicao().getX() >= p2.getX()) && (noAux1.getPosicao().getX() <= p1.getX())) )
               && ( ((p1.getY() <= p2.getY()) && (noAux1.getPosicao().getY() >= p1.getY()) && (noAux1.getPosicao().getY() <= p2.getY()))
               || ((p2.getY() < p1.getY()) && (noAux1.getPosicao().getY() >= p2.getY()) && (noAux1.getPosicao().getY() <= p1.getY())) ) )
               && */( ( ((p1.getX() <= p2.getX()) && (nodeAux2.getPosicao().getX() >= p1.getX()) && (nodeAux2.getPosicao().getX() <= p2.getX()))
               || ((p2.getX() < p1.getX()) && (nodeAux2.getPosicao().getX() >= p2.getX()) && (nodeAux2.getPosicao().getX() <= p1.getX())) )
               && ( ((p1.getY() <= p2.getY()) && (nodeAux2.getPosicao().getY() >= p1.getY()) && (nodeAux2.getPosicao().getY() <= p2.getY()))
               || ((p2.getY() < p1.getY()) && (nodeAux2.getPosicao().getY() >= p2.getY()) && (nodeAux2.getPosicao().getY() <= p1.getY())) ) ) ) {
            //if (((noAux1.getPosicao().getX() >= p1.getX()) && (noAux1.getPosicao().getX() <= p2.getX()) && (noAux1.getPosicao().getY() >= p1.getY()) && (noAux1.getPosicao().getY() <= p2.getY())) && ((noAux2.getPosicao().getX() >= p1.getX()) && (noAux2.getPosicao().getX() <= p2.getX()) && (noAux2.getPosicao().getY() >= p1.getY()) && (noAux2.getPosicao().getY() <= p2.getY())))  {
                selectedGroup.add(arcAux);
                arcAux.setSelecionado(true);
            }
        }
    }

    /**
     *  Seta o atributo selecionado (boolean) do nó (<code>Node</code>) e/ou arco (<code>Edge</code>) para falso, caso exista selecionado(s)
     *
     * @see Node
     */
    public void unselectAll() {
        for (int i = 0; i < selectedGroup.size(); i++) {
            if (selectedGroup.get(i) instanceof Node) {
                Node nodeAux = (Node)selectedGroup.get(i);
                nodeAux.setSelected(false);
            } else {
                Edge arcAux = (Edge)selectedGroup.get(i);
                arcAux.setSelecionado(false);
            }
        }
        selectedGroup.clear();
    }


    private GeneralPath drawUtility(double x, double y) {
        GeneralPath utility = new GeneralPath();

        utility.moveTo((float)(x - radius), (float)(y));
        utility.lineTo((float)(x), (float)(y + radius));
        utility.lineTo((float)(x + radius), (float)(y));
        utility.lineTo((float)(x), (float)(y - radius));
        utility.lineTo((float)(x - radius), (float)(y));

        return utility;
    }


    /**
     *  Método responsável por pintar a rede Bayesiana, ou seja, o objeto da classe IGraph
     *
     *@param  g  O <code>Graphics</code>
     *@see Graphics
     */
    public void paint(Graphics g) {
        view = (Graphics2D) g;
        view.setBackground(backColor);
        view.clearRect((int) controller.getTela().getJspGraph().getHorizontalScrollBar().getValue(), (int) controller.getTela().getJspGraph().getVerticalScrollBar().getValue(), (int) (controller.getTela().getJspGraph().getSize().getWidth()), (int) (controller.getTela().getJspGraph().getSize().getHeight()));
        view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        //desenha area de selecao
        if (bSelect) {
            float [] dash = {10f, 10f};
            view.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 10f));
            view.setColor(Color.black);
            if ((beginSelectionPoint.getX() <= endSelectionPoint.getX()) && (beginSelectionPoint.getY() <= endSelectionPoint.getY())) {
                view.drawRect((int)beginSelectionPoint.getX(), (int)beginSelectionPoint.getY(), (int)(endSelectionPoint.getX()-beginSelectionPoint.getX()), (int)(endSelectionPoint.getY()-beginSelectionPoint.getY()));
            } else {
                if ((beginSelectionPoint.getX() > endSelectionPoint.getX()) && (beginSelectionPoint.getY() <= endSelectionPoint.getY())) {
                    view.drawRect((int)endSelectionPoint.getX(), (int)beginSelectionPoint.getY(), (int)(beginSelectionPoint.getX()-endSelectionPoint.getX()), (int)(endSelectionPoint.getY()-beginSelectionPoint.getY()));
                } else {
                    if ((beginSelectionPoint.getX() <= endSelectionPoint.getX()) && (beginSelectionPoint.getY() > endSelectionPoint.getY())) {
                        view.drawRect((int)beginSelectionPoint.getX(), (int)endSelectionPoint.getY(), (int)(endSelectionPoint.getX()-beginSelectionPoint.getX()), (int)(beginSelectionPoint.getY()-endSelectionPoint.getY()));
                    } else {
                        view.drawRect((int)endSelectionPoint.getX(), (int)endSelectionPoint.getY(), (int)(beginSelectionPoint.getX()-endSelectionPoint.getX()), (int)(beginSelectionPoint.getY()-endSelectionPoint.getY()));
                    }
                }
            }
        }

        view.setStroke(new BasicStroke(1));

        //desenha todos os nós
        for (int i = 0; i < node.size(); i++) {
            Node nodeAux = (Node) node.get(i);
            if (nodeAux instanceof ProbabilisticNode) {
                view.setColor(ProbabilisticNode.getColor());
                view.fill(new Ellipse2D.Double(nodeAux.getPosicao().x - radius, nodeAux.getPosicao().y - radius, radius * 2, radius * 2));
            } else if (nodeAux instanceof DecisionNode) {
                view.setColor(DecisionNode.getColor());
                view.fillRect((int)(nodeAux.getPosicao().x - radius), (int)(nodeAux.getPosicao().y - radius), (int)(2 * radius), (int)(2 * radius));
            } else {
                view.setColor(UtilityNode.getColor());
                view.fill(drawUtility(nodeAux.getPosicao().x, nodeAux.getPosicao().y));
            }
            if (nodeAux.getName() == null) {
                nodeAux.setName(resource.getString("nodeGraphName") + i);
            }
            //desenha a sigla do nó
            AttributedString as = new AttributedString(nodeAux.getName());
            Font serifFont = new Font("Serif", Font.PLAIN, 12);
            FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            double alt = serifFont.getStringBounds(nodeAux.getName(), frc).getHeight();
            double lar = serifFont.getStringBounds(nodeAux.getName(), frc).getWidth();
            as.addAttribute(TextAttribute.FONT, serifFont);
            as.addAttribute(TextAttribute.FOREGROUND, Color.black);
            view.drawString(as.getIterator(), (int) (nodeAux.getPosicao().getX() - lar/2), (int) (nodeAux.getPosicao().getY() + alt/2));
        }

        view.setColor(arcColor);

        //desenha o arco atual se booleano for true
        if (bArc) {
            view.draw(presentArc);
            //chama o método que desenha a ponta da seta e desenha na tela
            Node nodeAux1 = new ProbabilisticNode();
            nodeAux1.setPosicao(presentArc.getX1(), presentArc.getY1());
            Node noAux2 = new ProbabilisticNode();
            noAux2.setPosicao(presentArc.getX2(), presentArc.getY2());
            Edge arcoAux = new Edge(nodeAux1, noAux2);
            view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            view.fill(drawArrow(arcoAux, false));
            view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        }

        //desenha todos os arcos
        for (int i = 0; i < arc.size(); i++) {
            Edge arcAux = (Edge) arc.get(i);
            if (arcAux.isSelecionado()) {
                view.setColor(selectionColor);
                view.setStroke(new BasicStroke(2));
            }
            else {
                view.setColor(arcColor);
            }


            if (arcAux.getOriginNode().getPosicao().getX() == arcAux.getDestinationNode().getPosicao().getX() ||
                arcAux.getOriginNode().getPosicao().getY() == arcAux.getDestinationNode().getPosicao().getY()) {
               view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //chama o método que cria um Line2D.Double e desenha o mesmo
            view.draw(drawArc(arcAux));
            //chama o método que desenha a ponta da seta e desenha na tela


            view.fill(drawArrow(arcAux, true));
            view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            view.setStroke(new BasicStroke(1));
        }

        /*
        //desenha o nó atual se for para mover o nó selecionado
        if ((bMoveNode) && presentNode != null && (presentNode.getPosicao().getX() != 0) && (presentNode.getPosicao().getY() != 0) ) {
            view.setColor(nodeColor);
            view.draw(new Ellipse2D.Double(presentNode.getPosicao().getX() - radius, presentNode.getPosicao().getY() - radius, radius * 2, radius * 2));
        }
        */

        for (int i = 0; i < node.size(); i++) {
            Node nodeAux3 = (Node) node.get(i);
            if (nodeAux3.isSelecionado()) {
                view.setColor(selectionColor);
                view.setStroke(new BasicStroke(2));
            }
            else {
                view.setColor(arcColor);
            }
            if (nodeAux3 instanceof ProbabilisticNode) {
                view.draw(new Ellipse2D.Double(nodeAux3.getPosicao().x - radius, nodeAux3.getPosicao().y - radius, radius * 2, radius * 2));
            } else if (nodeAux3 instanceof DecisionNode) {
                view.drawRect((int)(nodeAux3.getPosicao().x - radius), (int)(nodeAux3.getPosicao().y - radius), (int)(2 * radius), (int)(2 * radius));
            } else {
                view.draw(drawUtility(nodeAux3.getPosicao().x, nodeAux3.getPosicao().y));
            }
            view.setStroke(new BasicStroke(1));
        }
    }

}
