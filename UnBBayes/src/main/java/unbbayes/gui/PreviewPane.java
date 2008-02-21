/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.AttributedString;
import java.util.ResourceBundle;

import javax.swing.JPanel;


/**
 *  Classe respons�vel por mostrar como as op��es globais v�o ficar. Ela extende
 *  a classe <code>JPanel</code>
 *
 *@author     Rommel N. Carvalho, Michael S. Onishi
 *@created    27 de Junho de 2001
 *@see JPanel
 */
public class PreviewPane extends JPanel {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private GlobalOptionsDialog og;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");


    /**
     *  Constr�i o painel setando a op��o global pai (<code>TOp��esGlobais</code>)
     *
     *@param  og  a op��o global pai (<code>TOp��esGlobais</code>)
     */
    public PreviewPane(GlobalOptionsDialog og) {
        this.og = og;
    }

    /**
     *  M�todo para achar o ponto do arco na circunfer�ncia do n� em rela��o ao ponto1
     *
     *@param  x1    posi��o x do ponto 1
     *@param  y1    posi��o y do ponto 1
     *@param  x2    posi��o x do ponto 2
     *@param  y2    posi��o x do ponto 2
     *@param  raio  raio do n�
     *@return       ponto de destino (<code>Point2D.Double</code>)
     *@see Point2D.Double
     */
    public Point2D.Double getPoint(double x1, double y1, double x2, double y2, double radius) {
        double x = 0;
        double y = 0;

        if (x2 < x1) {
            x = Math.abs((radius * Math.cos(Math.atan((y2 - y1) / (x2 - x1)))) - x1);
            y = Math.abs((radius * Math.sin(Math.atan((y2 - y1) / (x2 - x1)))) - y1);
        }
        else {
            x = Math.abs((radius * Math.cos(Math.atan((y2 - y1) / (x2 - x1)))) + x1);
            y = Math.abs((radius * Math.sin(Math.atan((y2 - y1) / (x2 - x1)))) + y1);
        }
        return new Point2D.Double(x, y);
    }


    /**
     *  Desenha a ponta da seta (<code>GeneralPath</code>)
     *
     *@param  x   posi��o x de origem
     *@param  y   posi��o y de origem
     *@param  x1  posi��o x de destino
     *@param  y1  posi��o y de destino
     *@return     a ponta da seta (<code>GeneralPath</code>)
     *@see GeneralPath
     */
    public GeneralPath drawArrow(double x, double y, double x1, double y1) {
        Point2D.Double point1 = new Point2D.Double();
        Point2D.Double point2 = new Point2D.Double();
        GeneralPath arrow = new GeneralPath();

        double x2;
        double y2;
        double x3;
        double y3;
        double x4;
        double y4;
		double radius = og.getRadius();

        point1 = getPoint(x1 + radius, y1 + radius, x + radius, y + radius, radius + 10);
        point2 = getPoint(x1 + radius, y1 + radius, x + radius, y + radius, radius);
        x2 = point2.getX();
        y2 = point2.getY();

        //se for no segundo ou quarto quadrante usamos as primeiras 4 equa��es, sen�o, usammos as outras 4
        if (((x > x1) && (y > y1)) || ((x < x1) && (y < y1))) {
            x3 = point1.getX() + 5 * Math.abs(Math.cos(Math.atan((x1 - x) / (y - y1))));
            y3 = point1.getY() - 5 * Math.abs(Math.sin(Math.atan((x1 - x) / (y - y1))));
            x4 = point1.getX() - 5 * Math.abs(Math.cos(Math.atan((x1 - x) / (y - y1))));
            y4 = point1.getY() + 5 * Math.abs(Math.sin(Math.atan((x1 - x) / (y - y1))));
        }
        else {
            x3 = point1.getX() + 5 * (Math.cos(Math.atan((x + radius - x2) / (y + radius - y2))));
            y3 = point1.getY() - 5 * (Math.sin(Math.atan((x + radius - x2) / (y + radius - y2))));
            x4 = point1.getX() - 5 * (Math.cos(Math.atan((x + radius - x2) / (y + radius - y2))));
            y4 = point1.getY() + 5 * (Math.sin(Math.atan((x + radius - x2) / (y + radius - y2))));
        }

        //montar a seta com os pontos obtidos
        arrow.moveTo((float) (x3), (float) (y3));
        arrow.lineTo((float) (x2), (float) (y2));
        arrow.lineTo((float) (x4), (float) (y4));
        arrow.lineTo((float) (x3), (float) (y3));

        return arrow;
    }

	private GeneralPath drawUtility(double x, double y) {
        GeneralPath utility = new GeneralPath();

		double radius = og.getRadius();

        utility.moveTo((float)(x - radius), (float)(y));
        utility.lineTo((float)(x), (float)(y + radius));
        utility.lineTo((float)(x + radius), (float)(y));
        utility.lineTo((float)(x), (float)(y - radius));
        utility.lineTo((float)(x - radius), (float)(y));

        return utility;
    }


    /**
     *  Desenha uma pequena amostra de como ficar� as op��es globais
     *
     *@param  g  um gr�fico (<code>Graphics</code>)
     *@see Graphics
     */
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(og.getBackgroundColor());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.clearRect(3, 17, getWidth() - 6, getHeight() - 20);
        //pontos do arco a inserir
        Point2D.Double point1 = null;
        Point2D.Double point2 = null;

		double radius = og.getRadius();

        //desenha os n�s na cor escolhida
        g2.setColor(og.getPropabilisticDescriptionNodeColor());
        g2.fill(new Ellipse2D.Double(getWidth() / 2 - radius, getHeight() / 5 - radius + 10, radius * 2, radius * 2));
        g2.fill(new Ellipse2D.Double(getWidth() / 4 - radius, getHeight() * 2 / 5 - radius + 10, radius * 2, radius * 2));
		g2.setColor(og.getUtilityNodeColor());
        g2.fill(drawUtility(getWidth() * 3 / 4, getHeight() * 2 / 5 + 10));

        //desenha as bordas dos n�s na cor do arco e com um n� selecionado
        g2.setColor(og.getArcColor());
        g2.draw(new Ellipse2D.Double(getWidth() / 2 - radius, getHeight() / 5 - radius + 10, radius * 2, radius * 2));
        g2.draw(new Ellipse2D.Double(getWidth() / 4 - radius, getHeight() * 2 / 5 - radius + 10, radius * 2, radius * 2));
        g2.setColor(og.getSelectionColor());
        g2.draw(drawUtility(getWidth() * 3 / 4, getHeight() * 2 / 5 + 10));

        //desenha os arcos na cor escolhida
        g2.setColor(og.getArcColor());
        point1 = getPoint(getWidth() / 2, getHeight() / 5 + 10, getWidth() / 4, getHeight() * 2 / 5 + 10, radius);
        point2 = getPoint(getWidth() / 4, getHeight() * 2 / 5 + 10, getWidth() / 2, getHeight() / 5 + 10, radius);
        g2.draw(new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
        g2.fill(drawArrow(getWidth() / 2 - radius, getHeight() / 5 - radius + 10, getWidth() / 4 - radius, getHeight() * 2 / 5 - radius + 10));
        point1 = getPoint(getWidth() / 2, getHeight() / 5 + 10, getWidth() * 3 / 4, getHeight() * 2 / 5 + 10, radius);
        point2 = getPoint(getWidth() * 3 / 4, getHeight() * 2 / 5 + 10, getWidth() / 2, getHeight() / 5 + 10, radius);
        g2.draw(new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
        g2.fill(drawArrow(getWidth() / 2 - radius, getHeight() / 5 - radius + 10, getWidth() * 3 / 4 - radius, getHeight() * 2 / 5 - radius + 10));

        //desenha os n�s na cor escolhida
        g2.setColor(og.getPropabilisticExplanationNodeColor());
        g2.fill(new Ellipse2D.Double(getWidth() / 2 - radius, getHeight() * 3 / 5 - radius, radius * 2, radius * 2));
		g2.setColor(og.getDecisionNodeColor());
        g2.fillRect((int)(getWidth() / 4 - radius), (int)(getHeight() * 4 / 5 - radius), (int)(radius * 2), (int)(radius * 2));
		g2.setColor(og.getPropabilisticDescriptionNodeColor());
        g2.fill(new Ellipse2D.Double(getWidth() * 3 / 4 - radius, getHeight() * 4 / 5 - radius, radius * 2, radius * 2));

        //desenha as bordas dos n�s na cor do arco
        g2.setColor(og.getArcColor());
        g2.draw(new Ellipse2D.Double(getWidth() / 2 - radius, getHeight() * 3 / 5 - radius, radius * 2, radius * 2));
        g2.drawRect((int)(getWidth() / 4 - radius), (int)(getHeight() * 4 / 5 - radius), (int)(radius * 2), (int)(radius * 2));
        g2.draw(new Ellipse2D.Double(getWidth() * 3 / 4 - radius, getHeight() * 4 / 5 - radius, radius * 2, radius * 2));

        //desenha os arcos na cor escolhida e com um arco selecionado
        point1 = getPoint(getWidth() / 2, getHeight() * 3 / 5, getWidth() / 4, getHeight() * 4 / 5, radius);
        point2 = getPoint(getWidth() / 4, getHeight() * 4 / 5, getWidth() / 2, getHeight() * 3 / 5, radius);
        g2.draw(new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
        g2.fill(drawArrow(getWidth() / 2 - radius, getHeight() * 3 / 5 - radius, getWidth() / 4 - radius, getHeight() * 4 / 5 - radius));
        g2.setColor(og.getSelectionColor());
        point1 = getPoint(getWidth() / 2, getHeight() * 3 / 5, getWidth() * 3 / 4, getHeight() * 4 / 5, radius);
        point2 = getPoint(getWidth() * 3 / 4, getHeight() * 4 / 5, getWidth() / 2, getHeight() * 3 / 5, radius);
        g2.draw(new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
        g2.fill(drawArrow(getWidth() / 2 - radius, getHeight() * 3 / 5 - radius, getWidth() * 3 / 4 - radius, getHeight() * 4 / 5 - radius));

        //desenha o t�tulo "Preview"
        AttributedString as = new AttributedString(resource.getString("previewTitle"));
        Font serifFont = new Font("Serif", Font.PLAIN, 14);
        as.addAttribute(TextAttribute.FONT, serifFont);
        as.addAttribute(TextAttribute.FOREGROUND, Color.blue);
        g2.drawString(as.getIterator(), 15, 18);

        //desenha borda do Preview
        g2.setColor(Color.blue);
        g2.draw(new Line2D.Double(3, 17, 8, 17));
        g2.draw(new Line2D.Double(3, 17, 3, getHeight() - 3));
        g2.draw(new Line2D.Double(3, getHeight() - 3, getWidth() - 3, getHeight() - 3));
        g2.draw(new Line2D.Double(getWidth() - 3, getHeight() - 3, getWidth() - 3, 17));
		if (resource.getString("previewTitle").equals("Preview")) {
		   g2.draw(new Line2D.Double(getWidth() - 3, 17, 65, 17));
		} else {
		   g2.draw(new Line2D.Double(getWidth() - 3, 17, 123, 17));
		}

    }
}

