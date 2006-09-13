/**
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

package unbbayes.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.AttributedString;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JViewport;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

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
public class TEditaRede extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
// TODO REMOVER ESSA CLASSE!!!
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private List arco;
    private Node noAtual;
    private Node noMover;
    private Object selecionado;
    private Graphics2D view;
    private Point2D.Double arcoInicioAtual;
    private Point2D.Double arcoFimAtual;
    //private Point2D.Double pontoSelecaoInicial;
    //private Point2D.Double pontoSelecaoFinal;
    private Line2D.Double arcoAtual;
    private boolean bArco;
    private boolean bArrastouNo;
    private boolean bMoverArco;
    private boolean bMoverNo;
    //private boolean bScroll;
    //private boolean bPrimeiraVez;
    private Color corNo;
    private Color corArco;
    private Color corSelecao;
    private Color corFundo;
    private double raio;
    //private int scrollX;
    //private int scrollY;
    private JViewport desenho;
    private Dimension tamanhoVisivel;
    private Dimension tamanhoRede;
    private TJanelaEdicao janela;
	private ProbabilisticNetwork net;
	
	// se 0 e 1 mudar a direção do arco e se 2 deixar sem direção    
    private int direction;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");


    /**
     *  O construtor é responsável por iniciar todas as variáveis que serão
     *  utilizadas por essa classe para que se possa desenhar a rede Bayesiana.
     *
     */
    public TEditaRede(TJanelaEdicao janel, ProbabilisticNetwork net) {
        super();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        this.setRequestFocusEnabled(true);
        this.janela = janel;
		this.net = net;
        this.desenho = janela.getView();
        this.setSize(800, 600);

        arcoInicioAtual = new Point2D.Double();
        arcoFimAtual = new Point2D.Double();
        arcoAtual = new Line2D.Double();
        //pontoSelecaoFinal = new Point2D.Double();
        //pontoSelecaoInicial = new Point2D.Double();
        bArco = false;
        bArrastouNo = false;
        bMoverArco = false;
        bMoverNo = false;
        //bScroll = false;
        //bPrimeiraVez = true;
        corNo = Color.yellow;
        corArco = Color.black;
        corSelecao = Color.red;
        corFundo = Color.white;
        raio = 18;
        //scrollX = 0;
        //scrollY = 0;
        tamanhoRede = new Dimension(1500, 1500);
        tamanhoVisivel = new Dimension(0, 0);

		arco = net.getEdges();
        Node noAux;
        int linha = 1;
        for (int i = 0; i < net.getNodeCount(); i++) {
            noAux = net.getNodeAt(i);
            if ((i + 1) * raio >= this.getSize().getWidth()) {
                linha++;
            }
            noAux.setPosition((i + 1) * 3 * raio, 3 * raio * linha);
        }
    }

    /**
     *  Esse método seta o valor do bArco (valor booleano para desenhar o arco)
     *
     *@param  b  O booleano true ou false
     */
    public void setbArco(boolean b) {
        bArco = b;
        if (b) {
          setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
        else {
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }


    /**
     *  Pega o nó que se encontra na posição x,y
     *
     *@param  x  A posição x (double)
     *@param  y  A posição y (double)
     *@return    O nó encontrado (<code>Node</code>)
     *@see Node
     */
    public Node getNo(double x, double y) {
        double x1;
        double y1;

        for (int i = 0; i < net.getNodeCount(); i++) {
            Node noPegar = net.getNodeAt(i);
            x1 = noPegar.getPosition().getX();
            y1 = noPegar.getPosition().getY();

            if ((x >= x1 - raio) && (x <= x1 + raio) && (y >= y1 - raio) && (y <= y1 + raio)) {
                return noPegar;
            }
        }
        return null;
    }


    /**
     *  Pega o atributo focusTransversable do objeto da classe TDesenhaRede
     *
     *@return    True como valor do focusTransversable (método necessário para que se possa tratar evento de tecla)
     */
    public boolean isFocusable() {
        return true;
    }

    /**
     *  Pega o arco que se encontra na posição x,y
     *
     *@param  x  A posição x (double)
     *@param  y  A posição y (double)
     *@return    O arco encontrado (<code>TArco</code>)
     *@see TArco
     */
    public Edge getArco(double x, double y) {
        double x1;
        double y1;
        double x2;
        double y2;

        for (int i = 0; i < arco.size(); i++) {
            Edge arcoPegar = (Edge) arco.get(i);
            x1 = arcoPegar.getOriginNode().getPosition().getX();
            y1 = arcoPegar.getOriginNode().getPosition().getY();
            x2 = arcoPegar.getDestinationNode().getPosition().getX();
            y2 = arcoPegar.getDestinationNode().getPosition().getY();

            double yTeste = ((y2 - y1) / (x2 - x1)) * x + (y1 - x1 * ((y2 - y1) / (x2 - x1)));
            double xTeste = (y - (y1 - x1 * ((y2 - y1) / (x2 - x1)))) / ((y2 - y1) / (x2 - x1));

            Node no1 = arcoPegar.getOriginNode();
            Node no2 = arcoPegar.getDestinationNode();

            Point2D.Double ponto1 = getPonto(no1.getPosition(), no2.getPosition(), raio);
            Point2D.Double ponto2 = getPonto(no2.getPosition(), no1.getPosition(), raio);

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

    /**
     *  Método para achar o ponto do arco (<code>Point2D.Double</code>) na circunferência do nó em
     *  relação ao ponto1 (<code>Point2D.Double</code>)
     *
     *@param  ponto1  Centro da circunferência do nó de origem
     *@param  ponto2  Centro da circunferência do nó de destino
     *@param  r       O raio da circunferência
     *@return         O ponto do arco na circunferência
     *@see Point2D.Double
     */
    public Point2D.Double getPonto(Point2D.Double ponto1, Point2D.Double ponto2, double r) {
        double x = 0;
        double y = 0;
        double x1 = ponto1.getX();
        double y1 = ponto1.getY();
        double x2 = ponto2.getX();
        double y2 = ponto2.getY();

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
     *  Pega o tamanho necessário para repintar a tela (<code>Rectangle</code>)
     *
     *@return    O tamanho necessário para repintar a tela
     *@see Rectangle
     */
    public Rectangle getTamanhoRepintar() {
        double maiorX;
        double maiorY;
        double menorX;
        double menorY;

        if (bMoverNo)
        {
            Node noAux = (Node) selecionado;
            maiorX = noAux.getPosition().getX();
            menorX = noAux.getPosition().getX();
            maiorY = noAux.getPosition().getY();
            menorY = noAux.getPosition().getY();

            Node noAux2;
            for (int i = 0; i < noAux.getParents().size(); i++) {
                noAux2 = (Node) noAux.getParents().get(i);

                if (maiorX < noAux2.getPosition().getX()) {
                    maiorX = noAux2.getPosition().getX();
                }
                else {
                    if (menorX > noAux2.getPosition().getX()) {
                        menorX = noAux2.getPosition().getX();
                    }
                }

                if (maiorY < noAux2.getPosition().getY()) {
                    maiorY = noAux2.getPosition().getY();
                }
                else {
                    if (menorY > noAux2.getPosition().getY()) {
                        menorY = noAux2.getPosition().getY();
                    }
                }
            }

            for (int i = 0; i < noAux.getChildren().size(); i++) {
                noAux2 = (Node) noAux.getChildren().get(i);

                if (maiorX < noAux2.getPosition().getX()) {
                    maiorX = noAux2.getPosition().getX();
                }
                else {
                    if (menorX > noAux2.getPosition().getX()) {
                        menorX = noAux2.getPosition().getX();
                    }
                }

                if (maiorY < noAux2.getPosition().getY()) {
                    maiorY = noAux2.getPosition().getY();
                }
                else {
                    if (menorY > noAux2.getPosition().getY()) {
                        menorY = noAux2.getPosition().getY();
                    }
                }
            }
            return new Rectangle((int) (menorX - 6 * raio), (int) (menorY - 6 * raio), (int) (maiorX - menorX + 12 * raio), (int) (maiorY - menorY + 12 * raio));
        } else {
            return new Rectangle((int) janela.getJspView().getHorizontalScrollBar().getValue(), (int) janela.getJspView().getVerticalScrollBar().getValue(), (int) tamanhoVisivel.getWidth(), (int) tamanhoVisivel.getHeight());
        }
    }


    /**
     *  Método responsável por repintar a rede Bayesiana
     */
    public void repintar() {
        this.repaint(getTamanhoRepintar());
    }

    /**
     *  Não faz nada quando uma tecla é pressionada e em seguida solta.
     *
     * @param  e  um <code>KeyEvent</code> que será passado pelo <code>KeyListener
     *      </code>
     * @since
     * @see       KeyEvent
     * @see       KeyListener
     */
    public void keyTyped(KeyEvent e) { }


    /**
     *  Apaga o objeto selecionado da rede quando a tecla del(KeyEvent.VK_DELETE) é
     *  pressionada, copia um pedaço da rede quando a tecla c(KeyEvent.VK_C) é pressionada
     *  e cola um pedaço da rede quando a tecla p(KeyEvent.VK_P) é pressionada.
     *
     * @param  e  um <code>KeyEvent</code> que será passado pelo <code>KeyListener
     *      </code>
     * @since
     * @see       KeyEvent
     * @see       KeyListener
     */
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            apagaArco(selecionado);
        }
        this.repintar();
    }

    /**
     *  Não faz nada quando uma tecla é solta.
     *
     * @param  e  um <code>KeyEvent</code> que será passado pelo <code>KeyListener
     *      </code>
     * @since
     * @see       KeyEvent
     * @see       KeyListener
     */
    public void keyReleased(KeyEvent e) { }



    /**
     *  Método responsável por tratar o evento de botão de mouse pressionado
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mousePressed(MouseEvent e) {
        //setar o melhor scrollMode para desenhar e mexer na rede
        desenho.setScrollMode(JViewport.BLIT_SCROLL_MODE);

        if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
            Node no = getNo(e.getX(), e.getY());

            if (bArco) {
                if (no != null) {
                    bMoverArco = true;
                    //seto o ponto origem para o arco
                    arcoInicioAtual.setLocation(no.getPosition().getX(), no.getPosition().getY());
                    arcoAtual.setLine(no.getPosition().getX(), no.getPosition().getY(), e.getX(), e.getY());
                }
            }

            if ((!bArco) && (no != null)) {
                if (!no.isSelected()) {
                    selecionaNo(no);
                }
            }

            Edge arco = getArco(e.getX(), e.getY());

            if (arco != null) {
                selecionaArco(arco);
                if (bArco) {
                    bArco = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            if (!bArco) {
                if ((no != null) && (no.isSelected())) {
                    noMover = no;
                    bMoverNo = true;
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                }
            }

        }

        this.repaint((int) janela.getJspView().getHorizontalScrollBar().getValue(), (int) janela.getJspView().getVerticalScrollBar().getValue(), (int) tamanhoVisivel.getWidth(), (int) tamanhoVisivel.getHeight());
    }


    /**
     *  Método responsável por tratar o evento de clique no botão do mouse
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseClicked(MouseEvent e) { 
    	Edge edge = getArc(e.getX(), e.getY());
	    if ((edge != null) && (e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)) {
	    	if ((direction == 0) || (direction == 1)) {
	    		direction++;
	    		edge.setDirection(true);
	    		edge.changeDirection();
	    	} else if (direction == 2) {
	    		direction = 0;
	    		edge.setDirection(false);
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
        Node noDestino = getNo(e.getX(), e.getY());
        if ((bArco) && (e.getModifiers() == MouseEvent.BUTTON1_MASK)) {
//            Node noOrigem = getNo(arcoInicioAtual.getX(), arcoInicioAtual.getY());
            if ((noDestino != null)/* && (controlador.getRede().existeArco(noOrigem, noDestino) == -1)*/) {
                insereArco(arcoInicioAtual.getX(), arcoInicioAtual.getY(), arcoFimAtual.getX(), arcoFimAtual.getY());
            }

            //deixa a arco atual como um ponto em 0,0
            arcoAtual.setLine(0, 0, 0, 0);
        }

        Edge arco = getArco(e.getX(), e.getY());

        if ((!bArco) && (noDestino == null) && (arco == null) && (e.getModifiers() == MouseEvent.BUTTON1_MASK)) {
            deselecionaNo();
            deselecionaArco();
            selecionado = null;
        }

        if (bMoverArco) {
            //seta para false para dizer que acabou o movimento
            bMoverArco = false;
        }

        if (!bArco) {
            bMoverNo = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if ((e.getModifiers() == MouseEvent.BUTTON3_MASK) && (bArco)) {
            //seta o booleano do arco e selecionar para false
            bArco = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if (bArrastouNo) {
            //retorna o valor de bArrastouNo para false para futura comparação
            bArrastouNo = false;
        }

        repintar();
    }


    /**
     *  Método responsável por tratar o evento do mouse entrar nesse componente (objeto da classe TDesenhaRede)
     *
     *@param  e  O <code>MouseEvent</code>
     *@see MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        if ((!bMoverNo) && (!bMoverArco)) {
            //setar o tamanho visivel da rede como o tamanho o jspDesenho - raio
            tamanhoVisivel = new Dimension((int) (janela.getJspView().getSize().getWidth()), (int) (janela.getJspView().getSize().getHeight()));

			desenho.setOpaque(true);
			desenho.scrollRectToVisible(new Rectangle(tamanhoRede));

            desenho.setPreferredSize(tamanhoRede);
            desenho.revalidate();

            //receber o focus para poder tratar o evento de tecla
            this.requestFocus();
        }
    }


    /**
     *  Método responsável por tratar o evento do mouse sair desse componente (objeto da classe TDesenhaRede)
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

        //mover o scroll junto com a seta e/ou nó
        if ((e.getX() < tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight()) && (e.getX() + 2 * raio > tamanhoVisivel.getWidth() + janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() + 2 * raio > tamanhoVisivel.getHeight() + janela.getJspView().getVerticalScrollBar().getValue())) {
            if (bMoverNo && noMover != null) {
                janela.getJspView().getHorizontalScrollBar().setValue((int) (noMover.getPosition().getX() + 2 * raio - tamanhoVisivel.getWidth()));
                janela.getJspView().getVerticalScrollBar().setValue((int) (noMover.getPosition().getY() + 2 * raio - tamanhoVisivel.getHeight()));
            }
            else {
                janela.getJspView().getHorizontalScrollBar().setValue((int) (e.getX() + 2 * raio - tamanhoVisivel.getWidth()));
                janela.getJspView().getVerticalScrollBar().setValue((int) (e.getY() + 2 * raio - tamanhoVisivel.getHeight()));
            }
        }
        else {
            if ((e.getX() < tamanhoRede.getWidth()) && (e.getX() + 2 * raio > tamanhoVisivel.getWidth() + janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() + 2 * raio <= tamanhoVisivel.getHeight() + janela.getJspView().getVerticalScrollBar().getValue())) {
                if (bMoverNo && noMover != null) {
                    janela.getJspView().getHorizontalScrollBar().setValue((int) (noMover.getPosition().getX() + 2 * raio - tamanhoVisivel.getWidth()));
                }
                else {
                    janela.getJspView().getHorizontalScrollBar().setValue((int) (e.getX() + 2 * raio - tamanhoVisivel.getWidth()));
                }
            }
            else {
                if ((e.getY() < tamanhoRede.getHeight()) && (e.getX() + 2 * raio <= tamanhoVisivel.getWidth() + janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() + 2 * raio > tamanhoVisivel.getHeight() + janela.getJspView().getVerticalScrollBar().getValue())) {
                    if (bMoverNo && noMover != null) {
                        janela.getJspView().getVerticalScrollBar().setValue((int) (noMover.getPosition().getY() + 2 * raio - tamanhoVisivel.getHeight()));
                    }
                    else {
                        janela.getJspView().getVerticalScrollBar().setValue((int) (e.getY() + 2 * raio - tamanhoVisivel.getHeight()));
                    }
                }
                else {
                    if ((e.getX() - raio > janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() - raio > janela.getJspView().getVerticalScrollBar().getValue())) {
                    }
                    else {

                        if ((e.getX() > 0) && (e.getY() > 0) && (e.getX() - raio < janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() - raio < janela.getJspView().getVerticalScrollBar().getValue())) {
                            if (bMoverNo && noMover != null) {
                                janela.getJspView().getHorizontalScrollBar().setValue((int) (noMover.getPosition().getX() - raio));
                                janela.getJspView().getVerticalScrollBar().setValue((int) (noMover.getPosition().getY() - raio));
                            }
                            else {
                                janela.getJspView().getHorizontalScrollBar().setValue((int) (e.getX() - raio));
                                janela.getJspView().getVerticalScrollBar().setValue((int) (e.getY() - raio));
                            }
                        }
                        else {
                            if ((e.getY() > 0) && (e.getX() - raio >= janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() - raio < janela.getJspView().getVerticalScrollBar().getValue())) {
                                if (bMoverNo && noMover != null) {
                                    janela.getJspView().getVerticalScrollBar().setValue((int) (noMover.getPosition().getY() - raio));
                                }
                                else {
                                    janela.getJspView().getVerticalScrollBar().setValue((int) (e.getY() - raio));
                                }
                            }
                            else {

                                if ((e.getX() > 0) && (e.getX() - raio < janela.getJspView().getHorizontalScrollBar().getValue()) && (e.getY() - raio >= janela.getJspView().getVerticalScrollBar().getValue())) {
                                    if (bMoverNo && noMover != null) {
                                        janela.getJspView().getHorizontalScrollBar().setValue((int) (noMover.getPosition().getX() - raio));
                                    }
                                    else {
                                        janela.getJspView().getHorizontalScrollBar().setValue((int) (e.getX() - raio));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
            //move nó somente se este menos o raio for menor que (0,0)
            if ((bMoverNo) && (e.getX() > raio) && (e.getY() > raio) && (e.getX() < tamanhoRede.getWidth() - raio) && (e.getY() < tamanhoRede.getHeight() - raio)) {
                noAtual = noMover;
                atualizaNoAtual(e.getX(), e.getY());
                //seta a variável bArrastouNo para true para futura comparação
                bArrastouNo = true;
            }
            else {
                //atualiza a posicão da arco que está sendo desenhada, somente se este for maior que (0,0)
                if ((bArco) && (bMoverArco) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() < tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight())) {
                    arcoFimAtual.setLocation(e.getX(), e.getY());
                    atualizaArcoAtual(e.getX(), e.getY());
                }
                else {
                    if ((bArco) && (bMoverArco) && (e.getX() <= 0) && (e.getY() > 0) && (e.getX() < tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight())) {
                        arcoFimAtual.setLocation(e.getX(), e.getY());
                        atualizaArcoAtual(0, e.getY());
                    }
                    else {
                        if ((bArco) && (bMoverArco) && (e.getX() > 0) && (e.getY() <= 0) && (e.getX() < tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight())) {
                            arcoFimAtual.setLocation(e.getX(), e.getY());
                            atualizaArcoAtual(e.getX(), 0);
                        }
                        else {
                            if ((bArco) && (bMoverArco) && (e.getX() <= 0) && (e.getY() <= 0) && (e.getX() < tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight())) {
                                arcoFimAtual.setLocation(e.getX(), e.getY());
                                atualizaArcoAtual(0, 0);
                            }
                            else {
                                if ((bArco) && (bMoverArco) && (e.getX() <= 0) && (e.getY() > 0) && (e.getX() < tamanhoRede.getWidth()) && (e.getY() >= tamanhoRede.getHeight())) {
                                    arcoFimAtual.setLocation(e.getX(), e.getY());
                                    atualizaArcoAtual(0, tamanhoRede.getHeight());
                                }
                                else {
                                    if ((bArco) && (bMoverArco) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() < tamanhoRede.getWidth()) && (e.getY() >= tamanhoRede.getHeight())) {
                                        arcoFimAtual.setLocation(e.getX(), e.getY());
                                        atualizaArcoAtual(e.getX(), tamanhoRede.getHeight());
                                    }
                                    else {
                                        if ((bArco) && (bMoverArco) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() >= tamanhoRede.getWidth()) && (e.getY() >= tamanhoRede.getHeight())) {
                                            arcoFimAtual.setLocation(e.getX(), e.getY());
                                            atualizaArcoAtual(tamanhoRede.getWidth(), tamanhoRede.getHeight());
                                        }
                                        else {
                                            if ((bArco) && (bMoverArco) && (e.getX() > 0) && (e.getY() > 0) && (e.getX() >= tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight())) {
                                                arcoFimAtual.setLocation(e.getX(), e.getY());
                                                atualizaArcoAtual(tamanhoRede.getWidth(), e.getY());
                                            }
                                            else {
                                                if ((bArco) && (bMoverArco) && (e.getX() > 0) && (e.getY() <= 0) && (e.getX() >= tamanhoRede.getWidth()) && (e.getY() < tamanhoRede.getHeight())) {
                                                    arcoFimAtual.setLocation(e.getX(), e.getY());
                                                    atualizaArcoAtual(tamanhoRede.getWidth(), 0);
                                                }
                                                else {
                                                    if ((bMoverNo) && (e.getX() <= raio) && (e.getY() > raio) && (e.getX() < tamanhoRede.getWidth() - raio) && (e.getY() < tamanhoRede.getHeight() - raio)) {
                                                        noAtual = noMover;
                                                        atualizaNoAtual(raio, e.getY());
                                                        //seta a variável bArrastouNo para true para futura comparação
                                                        bArrastouNo = true;
                                                    }
                                                    else {
                                                        if ((bMoverNo) && (e.getX() > raio) && (e.getY() <= raio) && (e.getX() < tamanhoRede.getWidth() - raio) && (e.getY() < tamanhoRede.getHeight() - raio)) {
                                                            noAtual = noMover;
                                                            atualizaNoAtual(e.getX(), raio);
                                                            //seta a variável bArrastouNo para true para futura comparação
                                                            bArrastouNo = true;
                                                        }
                                                        else {
                                                            if ((bMoverNo) && (e.getX() <= raio) && (e.getY() <= raio) && (e.getX() < tamanhoRede.getWidth() - raio) && (e.getY() < tamanhoRede.getHeight() - raio)) {
                                                                noAtual = noMover;
                                                                atualizaNoAtual(raio, raio);
                                                                //seta a variável bArrastouNo para true para futura comparação
                                                                bArrastouNo = true;
                                                            }
                                                            else {
                                                                if ((bMoverNo) && (e.getX() <= raio) && (e.getY() > raio) && (e.getX() < tamanhoRede.getWidth() - raio) && (e.getY() >= tamanhoRede.getHeight() - raio)) {
                                                                    noAtual = noMover;
                                                                    atualizaNoAtual(raio, tamanhoRede.getHeight() - raio);
                                                                    //seta a variável bArrastouNo para true para futura comparação
                                                                    bArrastouNo = true;
                                                                }
                                                                else {
                                                                    if ((bMoverNo) && (e.getX() > raio) && (e.getY() > raio) && (e.getX() < tamanhoRede.getWidth() - raio) && (e.getY() >= tamanhoRede.getHeight() - raio)) {
                                                                        noAtual = noMover;
                                                                        atualizaNoAtual(e.getX(), tamanhoRede.getHeight() - raio);
                                                                        //seta a variável bArrastouNo para true para futura comparação
                                                                        bArrastouNo = true;
                                                                    }
                                                                    else {
                                                                        if ((bMoverNo) && (e.getX() > raio) && (e.getY() > raio) && (e.getX() >= tamanhoRede.getWidth() - raio) && (e.getY() >= tamanhoRede.getHeight() - raio)) {
                                                                            noAtual = noMover;
                                                                            atualizaNoAtual(tamanhoRede.getWidth() - raio, tamanhoRede.getHeight() - raio);
                                                                            //seta a variável bArrastouNo para true para futura comparação
                                                                            bArrastouNo = true;
                                                                        }
                                                                        else {
                                                                            if ((bMoverNo) && (e.getX() > raio) && (e.getY() > raio) && (e.getX() >= tamanhoRede.getWidth() - raio) && (e.getY() < tamanhoRede.getHeight() - raio)) {
                                                                                noAtual = noMover;
                                                                                atualizaNoAtual(tamanhoRede.getWidth() - raio, e.getY());
                                                                                //seta a variável bArrastouNo para true para futura comparação
                                                                                bArrastouNo = true;
                                                                            }
                                                                            else {
                                                                                if ((bMoverNo) && (e.getX() > raio) && (e.getY() <= raio) && (e.getX() >= tamanhoRede.getWidth() - raio) && (e.getY() < tamanhoRede.getHeight() - raio)) {
                                                                                    noAtual = noMover;
                                                                                    atualizaNoAtual(tamanhoRede.getWidth() - raio, raio);
                                                                                    //seta a variável bArrastouNo para true para futura comparação
                                                                                    bArrastouNo = true;
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
     *  Seta o atributo selecionado (boolean) do arco (<code>TArco</code>) para falso, caso exista selecionado
     *
     * @see TArco
     */
    public void deselecionaArco() {
        if (selecionado instanceof Edge) {
            ((Edge) selecionado).setSelected(false);
        }
    }


    /**
     *  Seta o atributo selecionado (boolean) do nó (<code>Node</code>) para falso, caso exista selecionado
     *
     * @see Node
     */
    public void deselecionaNo() {
        if (selecionado instanceof Node) {
            ((Node) selecionado).setSelected(false);
        }
    }


    /**
     *  Seta o atributo selecionado (boolean) do nó (<code>Node</code>) desejado para true
     *
     *@param  no  O nó desejado
     *@see Node
     */
    public void selecionaNo(Node no) {
        //deseleciona nó ou arco, caso algum esteja selecionado
        deselecionaNo();
        deselecionaArco();

        //seleciona o nó escolhido
        no.setSelected(true);
        selecionado = no;
    }


    /**
     *  Seta o atributo selecionado (boolean) do arco (<code>TArco</code>) desejado para true
     *
     *@param  arco  O arco desejado
     */
    public void selecionaArco(Edge arco) {
        //deseleciona nó ou arco, caso algum esteja selecionado
        deselecionaNo();
        deselecionaArco();

        //seleciona o arco escolhido
        arco.setSelected(true);
        selecionado = arco;
    }

    /**
     *  Método responsável por atualizar o arco (<code>TArco</code>) atual ao se mover um arco
     *
     *@param  x  Posição x (double) da ponta do arco
     *@param  y  Posição y (double) da ponta do arco
     */
    public void atualizaArcoAtual(double x, double y) {
        Point2D.Double ponto = new Point2D.Double();
        ponto = getPonto(arcoInicioAtual, arcoFimAtual, raio);

        arcoAtual.setLine(ponto.getX(), ponto.getY(), x, y);
        arcoFimAtual.setLocation(x, y);

        repintar();
    }


    /**
     *  Método responsável por atualizar o nó (<code>Node</code>) atual ao se mover um nó
     *
     *@param  x  Posição x (double) do centro do nó
     *@param  y  Posição y (double) do centro do nó
     *@see Node
     */
    public void atualizaNoAtual(double x, double y) {
        noAtual.setPosition(x, y);
        repintar();
    }


    /**
     *  Inere um arco (<code>TArco</code>) com origem na posição (x1,y1) e destino na posição (x2,y2)
     *
     *@param  x1  Posição x1 (double) da origem do arco
     *@param  y1  Posição y1 (double) da origem do arco
     *@param  x2  Posição x2 (double) do destino do arco
     *@param  y2  Posição y2 (double) do destino do arco
     */
    public void insereArco(double x1, double y1, double x2, double y2) {
        Node no1 = getNo(x1, y1);
        Node no2 = getNo(x2, y2);
        Edge arcoInserir = new Edge(no1, no2);
        net.addEdge(arcoInserir);
/*
		arcoInserir.getOriginNode().getChildren().add(arcoInserir.getDestinationNode());
        arcoInserir.getDestinationNode().getParents().add(arcoInserir.getOriginNode());
        arco.add(arcoInserir);

		if (arcoInserir.getDestinationNode() instanceof ITabledVariable) {
            ITabledVariable v2 = (ITabledVariable)arcoInserir.getDestinationNode();
            PotentialTable auxTab = v2.getPotentialTable();
            auxTab.addVariable(arcoInserir.getOriginNode());
        }
*/
        repintar();
    }


    /**
     *  Método responsável por desenhar um arco a (<code>TArco</code>) desejado
     *
     *@param  a  O arco que se deseja desenhar
     *@return    A reta do arco (<code>Line2D.Double</code>)
     *@see Line2D.Double
     */
    public Line2D.Double desenhaArco(Edge a) {
        Point2D.Double ponto1 = new Point2D.Double();
        Point2D.Double ponto2 = new Point2D.Double();

        Node no1;
        Node no2;
        no1 = a.getOriginNode();
        no2 = a.getDestinationNode();

        ponto1 = getPonto(no1.getPosition(), no2.getPosition(), raio);
        ponto2 = getPonto(no2.getPosition(), no1.getPosition(), raio);

        return new Line2D.Double(ponto1, ponto2);
    }


    /**
     *  Método responsável por desenhar a ponta da seta do arco (<code>TArco</code>) desejado
     *
     *@param  a            O arco que se deseja desenhar a ponta da seta
     *@param  bExisteArco  True se a seta já existe e false se ela está sendo inserida
     *@return              A ponta de uma seta (<code>GeneralPath</code>)
     *@see GeneralPath
     */
    public GeneralPath desenhaSeta(Edge a, boolean bExisteArco) {
        Point2D.Double ponto1 = new Point2D.Double();
        Point2D.Double ponto2 = new Point2D.Double();
        GeneralPath seta = new GeneralPath();
        Node no1;
        Node no2;
        no1 = a.getOriginNode();
        no2 = a.getDestinationNode();

        double x1 = no1.getPosition().getX();
        double y1 = no1.getPosition().getY();
        double x2;
        double y2;
        double x3;
        double y3;
        double x4;
        double y4;

        //ponta da seta = ponto correspondente na circunferência do nó, caso a seta já esteja inserida - base da seta deslecada de 10 do centro do nó
        if (bExisteArco) {
            ponto1 = getPonto(no2.getPosition(), no1.getPosition(), raio + 10);
            ponto2 = getPonto(no2.getPosition(), no1.getPosition(), raio);
            x2 = ponto2.getX();
            y2 = ponto2.getY();
        }
        //ponta do seta = ponto na ponta do mouse - base da seta deslecada de 10 da ponta do mouse
        else {
            ponto1 = getPonto(no2.getPosition(), no1.getPosition(), 10);
            x2 = no2.getPosition().getX();
            y2 = no2.getPosition().getY();
        }

        //se for no segundo ou quarto quadrante usamos as primeiras 4 equações, senão, usammos as outras 4
        if (((x1 > x2) && (y1 > y2)) || ((x1 < x2) && (y1 < y2))) {
            x3 = ponto1.getX() + 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
            y3 = ponto1.getY() - 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
            x4 = ponto1.getX() - 5 * Math.abs(Math.cos(Math.atan((x2 - x1) / (y1 - y2))));
            y4 = ponto1.getY() + 5 * Math.abs(Math.sin(Math.atan((x2 - x1) / (y1 - y2))));
        }
        else {
            x3 = ponto1.getX() + 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
            y3 = ponto1.getY() - 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
            x4 = ponto1.getX() - 5 * (Math.cos(Math.atan((x1 - x2) / (y1 - y2))));
            y4 = ponto1.getY() + 5 * (Math.sin(Math.atan((x1 - x2) / (y1 - y2))));
        }

        //montar a seta com os pontos obtidos
        seta.moveTo((float) (x3), (float) (y3));
        seta.lineTo((float) (x2), (float) (y2));
        seta.lineTo((float) (x4), (float) (y4));
        seta.lineTo((float) (x3), (float) (y3));

        return seta;
    }

    /**
     *  Método responsável por pintar a rede Bayesiana, ou seja, o objeto da classe TDesenhaRede
     *
     *@param  g  O <code>Graphics</code>
     *@see Graphics
     */
    public void paint(Graphics g) {
        view = (Graphics2D) g;
        view.setBackground(corFundo);
        view.clearRect((int) janela.getJspView().getHorizontalScrollBar().getValue(), (int) janela.getJspView().getVerticalScrollBar().getValue(), (int) (janela.getJspView().getSize().getWidth()), (int) (janela.getJspView().getSize().getHeight()));
        view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        view.setColor(corNo);
        view.setStroke(new BasicStroke(1));

        //desenha todos os nós
        for (int i = 0; i < net.getNodeCount(); i++) {
            Node noAux = (Node) net.getNodeAt(i);
            view.fill(new Ellipse2D.Double(noAux.getPosition().x - raio, noAux.getPosition().y - raio, raio * 2, raio * 2));
            if (noAux.getName() == null) {
                noAux.setName(resource.getString("nodeGraphName") + i);
            }
            //desenha a sigla do nó
            AttributedString as = new AttributedString(noAux.getName());
            Font serifFont = new Font("Serif", Font.PLAIN, 12);
            FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            double alt = serifFont.getStringBounds(noAux.getName(), frc).getHeight();
            double lar = serifFont.getStringBounds(noAux.getName(), frc).getWidth();
            as.addAttribute(TextAttribute.FONT, serifFont);
            as.addAttribute(TextAttribute.FOREGROUND, Color.black);
            view.drawString(as.getIterator(), (int) (noAux.getPosition().getX() - lar/2), (int) (noAux.getPosition().getY() + alt/2));
        }

        view.setColor(corArco);

        //desenha o arco atual se booleano for true
        if (bArco) {
            view.draw(arcoAtual);
            //chama o método que desenha a ponta da seta e desenha na tela
            Node noAux = new ProbabilisticNode();
            noAux.setPosition(arcoAtual.getX1(), arcoAtual.getY1());
            Node noAux2 = new ProbabilisticNode();
            noAux2.setPosition(arcoAtual.getX2(), arcoAtual.getY2());
            Edge arcoAux = new Edge(noAux, noAux2);
            view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            view.fill(desenhaSeta(arcoAux, false));
            view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        }

        //desenha todos os arcos
        for (int i = 0; i < arco.size(); i++) {
            Edge arcoAux = (Edge) arco.get(i);
            if (arcoAux.isSelected()) {
                view.setColor(corSelecao);
                view.setStroke(new BasicStroke(2));
            }
            else {
                view.setColor(corArco);
            }


            if (arcoAux.getOriginNode().getPosition().getX() == arcoAux.getDestinationNode().getPosition().getX() ||
                arcoAux.getOriginNode().getPosition().getY() == arcoAux.getDestinationNode().getPosition().getY()) {
               view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            //chama o método que cria um Line2D.Double e desenha o mesmo
            view.draw(desenhaArco(arcoAux));
            
            if (arcoAux.hasDirection()) {
            	//chama o método que desenha a ponta da seta e desenha na tela
            	view.fill(desenhaSeta(arcoAux, true));
            }
            
            view.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            view.setStroke(new BasicStroke(1));
        }

        //desenha o nó atual se for para mover o nó selecionado
        if ((bMoverNo) && (noAtual != null) && (noAtual.getPosition().getX() != 0) && (noAtual.getPosition().getY() != 0) ) {
            view.setColor(corNo);
            view.draw(new Ellipse2D.Double(noAtual.getPosition().getX() - raio, noAtual.getPosition().getY() - raio, raio * 2, raio * 2));
        }

        for (int i = 0; i < net.getNodeCount(); i++) {
            Node noAux = (Node) net.getNodeAt(i);
            if (noAux.isSelected()) {
                view.setColor(corSelecao);
                view.setStroke(new BasicStroke(2));
            }
            else {
                view.setColor(corArco);
            }
            view.draw(new Ellipse2D.Double(noAux.getPosition().x - raio, noAux.getPosition().y - raio, raio * 2, raio * 2));
            view.setStroke(new BasicStroke(1));
        }
    }

    /**
     *  Apaga o arco desejado
     *
     *@param  o  O arco desejado (<code>Object</code>)
     *@see Object
     */
    public void apagaArco(Object o) {
        Edge aux = (Edge)o;
        Node no1 = aux.getOriginNode();
        Node no2 = aux.getDestinationNode();
        no2.getParents().remove(no1);
        no1.getChildren().remove(no2);
        arco.remove(o);
        repintar();
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

        for (int i = 0; i < arco.size(); i++) {
            Edge arcoPegar = (Edge) arco.get(i);
            x1 = arcoPegar.getOriginNode().getPosition().getX();
            y1 = arcoPegar.getOriginNode().getPosition().getY();
            x2 = arcoPegar.getDestinationNode().getPosition().getX();
            y2 = arcoPegar.getDestinationNode().getPosition().getY();

            double yTeste = ((y2 - y1) / (x2 - x1)) * x + (y1 - x1 * ((y2 - y1) / (x2 - x1)));
            double xTeste = (y - (y1 - x1 * ((y2 - y1) / (x2 - x1)))) / ((y2 - y1) / (x2 - x1));

            Node no1 = arcoPegar.getOriginNode();
            Node no2 = arcoPegar.getDestinationNode();

            Point2D.Double ponto1 = getPoint(no1.getPosition(), no2.getPosition(), raio);
            Point2D.Double ponto2 = getPoint(no2.getPosition(), no1.getPosition(), raio);

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
}