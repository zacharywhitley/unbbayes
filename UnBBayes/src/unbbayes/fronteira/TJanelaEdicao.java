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

package unbbayes.fronteira;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import unbbayes.jprs.jbn.Edge;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @version 1.0
 */
public class TJanelaEdicao extends JDialog {

    private JToolBar jtb;
    private JViewport view;
    private TEditaRede rede;
    private JButton insereArco;
    private JButton reaprende;
    private JScrollPane jspView;
    private ProbabilisticNetwork net;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.fronteira.resources.FronteiraResources");

    public TJanelaEdicao(ProbabilisticNetwork _net) {
		super(new Frame(), resource.getString("aprendizagemTitle"), true);
        Container contentPane = getContentPane();
        net = _net;

        setSize(550, 470);
        setResizable(true);

        insereArco = new JButton(new ImageIcon("icones/ferraarco.gif"));
        reaprende  = new JButton(new ImageIcon("icones/aprende.gif"));
        view       = new JViewport();
        jtb        = new JToolBar();
        rede       = new TEditaRede(this, net);
        jspView    = new JScrollPane(view);

        view.setView(rede);

        insereArco.setToolTipText(resource.getString("arcToolTip"));
        reaprende.setToolTipText(resource.getString("calculateProbabilitiesFromLearningToEditMode"));

        //setar defaults para jspDesenho
        jspView.setHorizontalScrollBar(jspView.createHorizontalScrollBar());
        jspView.setVerticalScrollBar(jspView.createVerticalScrollBar());
        jspView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jspView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //ao clicar no botão reaprende, mostra-se o menu para escolha do arquivo para o aprendizado.
        reaprende.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {            	
            	List edgeList = net.getArcos();
            	Edge edge;
            	boolean close = true;
            	for(int i = 0; i < edgeList.size() && close; i++){
                     edge = (Edge)edgeList.get(i);
                    if(!edge.hasDirection()){
                    	JOptionPane.showMessageDialog(null,"Todos os arcos tem que ter uma direção.","ERROR",JOptionPane.ERROR_MESSAGE);
                    	close = false;

                    }
               	}
               	if(close){               		
                    setVisible(false);
                    dispose();
               	}
            }
        });

        insereArco.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                rede.setbArco(true);
            }
        });

        jtb.add(insereArco);
        jtb.add(reaprende);
        contentPane.add(jtb, BorderLayout.NORTH);
        contentPane.add(jspView, BorderLayout.CENTER);

        this.setVisible(true);
        this.addWindowListener(closeFunction);

    }

    /**
     *  Retorna o painel da tela de edição.
     *
     *@return    retorna o jspView (<code>JScrollPane</code>)
     *@see       JScrollPane
     */
    public JScrollPane getJspView() {
        return this.jspView;
    }

    /**
     *  Retorna o painel o view.
     *
     *@return    retorna o view (<code>JViewport</code>)
     *@see       JViewport
     */
    public JViewport getView() {
        return this.view;
    }
    
    WindowAdapter closeFunction = new WindowAdapter(){
        public void windowClosing(WindowEvent we){}   	    	
    };
}