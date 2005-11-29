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

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;

import unbbayes.controller.IconController;
/**
 *  Essa classe extende o <code>FileView</code> que é o responsável por
 *  mostrar os ícones correspondentes para cada tipo de aquivo e pasta.
 *
 *@author     Rommel Novaes Carvalho, Michael S. Onishi
 *@created    27 de Junho de 2001
 *@see        FileView
 *@version    1.0 06/07/2001
 */
public class FileIcon extends FileView {

    private JFileChooser fc;
    private Component observer;
    protected IconController iconController = IconController.getInstance();

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

    /**
     *  Constroi um observer para desenhar ícones e um JFileChooser para pegar
     *  ícones (<code>Icon</code>) padrões.
     *
     *@param  c o componente (<code>Component</code>) que será o observer para desenhar ícones.
     *@see    Component
     *@see    Icon
     */
    public FileIcon(Component c) {
        //componente para criar ícones de arquivos
        observer = c;
        fc = new JFileChooser();
    }


    /**
     *  Retorna a descrição do arquivo desejado.
     *
     *@param     f o arquivo (<code>File</code>) ao qual se deseja a descrição
     *@return    a descrição do arquivo (<code>String</code>)
     *@see       File
     *@see       String
     */
    public String getDescription(File f) {
        return getTypeDescription(f);
    }


    /**
     *  Retorna o ícone (<code>Icon</code>) correnspondente ao arquivo desejado.
     *
     *@param     f o arquivo (<code>File</code>) que se deseja pegar o ícone
     *@return    o ícone correspondente ao arquivo f
     *@see       Icon
     *@see       File
     */
    public Icon getIcon(File f) {
        /*
         * if (f.isDirectory())
         * {
         * return directoryIcon;
         * }
         */ try{
        String name = f.getName();
        if (name.endsWith(".arff")) {
            return iconController.getArffFileIcon();
        }
        if (name.toLowerCase().endsWith(".txt")) {
            return iconController.getTxtFileIcon();
        }
        if (name.toLowerCase().endsWith(".net")) {
            return iconController.getNetFileIcon();
        }}
         catch (Exception ee){}
        return fc.getIcon(f);
    }


    /**
     *  Retorna o name do arquivo desejado.
     *
     *@param     f o arquivo (<code>File</code>) que se deseja receber o name
     *@return    o name do arquivo (<code>String</code>)
     *@see       String
     *@see       File
     */
    public String getName(File f) {
        String name = f.getName();
        return name.equals("") ? f.getPath() : name;
    }


    /**
     *  Retorna o tipo do arquivo.
     *
     *@param     f o arquivo (<code>File</code>) que se deseja descrição do tipo
     *@return    o tipo do arquivo (<code>String</code>)
     *@see       String
     *@see       File
     */
    public String getTypeDescription(File f) {
        String name = f.getName().toLowerCase();
        if (f.isDirectory()) {
            return resource.getString("fileDirectoryType");
        }

        if (name.endsWith(".arff")) {
            return resource.getString("fileARFFType");
        }

        if (name.endsWith(".txt")) {
            return resource.getString("fileTXTType");
        }

        if (name.endsWith(".net")) {
            return resource.getString("fileNETType");
        }
        return resource.getString("fileGenericType");
    }


    /**
     *  Retorna se é tranversable ou não.
     *
     *@param     f o arquivo (<code>File</code>) que se deseja saber se é tranversable
     *@return    true se for tranversable e falso caso contrário
     *@see       File
     */
    public boolean isTranversable(File f) {
        //todos diretórios serão transversable
        return f.isDirectory() ? true : false;
    }


    /**
     *  Classe que extende <code>ImageIcon</code> responsável por desenhar
     *  um ícone.
     *
     *@author     Rommel Novaes Carvalho, Michael S. Onishi
     *@created    27 de Junho de 2001
     *@see        ImageIcon
     */
    public class Icon16 extends ImageIcon {
    	/** Serialization runtime version number */
    	private static final long serialVersionUID = 0;

        /**
         *  Cria e desenha um ícone para o arquivo desejado.
         *
         *@param  f  o arquivo (<code>String</code>) que deseja-se criar um ícone
         *@see    String
         */
        public Icon16(String f) {
            super(f);
            Image i = observer.createImage(16, 16);
            i.getGraphics().drawImage(getImage(), 0, 0, 16, 16, observer);
            setImage(i);
        }


        /**
         *  Retorna altura do ícone
         *
         *@return    a altura do ícone (int)
         */
        public int getIconHeight() {
            return 16;
        }


        /**
         *  Retorna a largura do ícone
         *
         *@return    a altura do ícone (int)
         */
        public int getIconWidth() {
            return 16;
        }


        /**
         *  Desenha o ícone.
         *
         *@param  c  o componente (<code>Component</code>)
         *@param  g  o gráfico (<code>Graphics</code>)
         *@param  x  a largura do ícone (int)
         *@param  y  a altura do ícone (int)
         *@see    Component
         *@see    Graphics
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(getImage(), x, y, c);
        }
    }

}

