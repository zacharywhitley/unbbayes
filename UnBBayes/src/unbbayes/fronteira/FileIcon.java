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

import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.metal.MetalIconFactory;
import java.util.ResourceBundle;

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

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.fronteira.resources.FronteiraResources");

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
         */
        String name = f.getName();
        if (name.endsWith(".avi")) {
            return new ImageIcon("icones/avi.gif");
        }
        if (name.toLowerCase().endsWith(".bat")) {
            return new ImageIcon("icones/bat.gif");
        }
        if (name.toLowerCase().endsWith(".bmp")) {
            return new ImageIcon("icones/bmp.gif");
        }
        if (name.toLowerCase().endsWith(".cdr")) {
            return new ImageIcon("icones/cdr.gif");
        }
        if (name.toLowerCase().endsWith(".dcx")) {
            return new ImageIcon("icones/dcx.gif");
        }
        if (name.toLowerCase().endsWith(".doc")) {
            return new ImageIcon("icones/doc.gif");
        }
        if (name.toLowerCase().equals("command.pif")) {
            return new ImageIcon("icones/dos.gif");
        }
        if (name.toLowerCase().endsWith(".dot")) {
            return new ImageIcon("icones/dot.gif");
        }
        if (name.toLowerCase().endsWith(".dun")) {
            return new ImageIcon("icones/dun.gif");
        }
        if (name.toLowerCase().endsWith(".exe")) {
            return new ImageIcon("icones/exe.gif");
        }
        if (name.toLowerCase().endsWith(".fon")) {
            return new ImageIcon("icones/fon.gif");
        }
        if (name.toLowerCase().endsWith(".gif")) {
            return new ImageIcon("icones/gif.gif");
        }
        if (name.toLowerCase().endsWith(".hlp")) {
            return new ImageIcon("icones/help.gif");
        }
        if ((name.toLowerCase().endsWith(".htm")) || (name.endsWith(".html"))) {
            return new ImageIcon("icones/html.gif");
        }
        if (name.toLowerCase().endsWith(".idf")) {
            return new ImageIcon("icones/idf.gif");
        }
        if (name.toLowerCase().endsWith(".mdb")) {
            return new ImageIcon("icones/mdb.gif");
        }
        if (name.toLowerCase().endsWith(".mde")) {
            return new ImageIcon("icones/mde.gif");
        }
        if (name.toLowerCase().endsWith(".mdn")) {
            return new ImageIcon("icones/mdn.gif");
        }
        if (name.toLowerCase().endsWith(".mdz")) {
            return new ImageIcon("icones/mdz.gif");
        }
        if (name.toLowerCase().endsWith(".mid")) {
            return new ImageIcon("icones/mid.gif");
        }
        if (name.toLowerCase().endsWith(".mpg")) {
            return new ImageIcon("icones/mpeg.gif");
        }
        if (name.toLowerCase().endsWith(".obt")) {
            return new ImageIcon("icones/obt.gif");
        }
        if (name.toLowerCase().endsWith(".obz")) {
            return new ImageIcon("icones/obz.gif");
        }
        if (name.toLowerCase().endsWith(".pot")) {
            return new ImageIcon("icones/pot.gif");
        }
        if (name.toLowerCase().endsWith(".pps")) {
            return new ImageIcon("icones/pps.gif");
        }
        if (name.toLowerCase().endsWith(".ppt")) {
            return new ImageIcon("icones/ppt.gif");
        }
        if (name.toLowerCase().endsWith(".pwz")) {
            return new ImageIcon("icones/pwz.gif");
        }
        if (name.toLowerCase().endsWith(".sch")) {
            return new ImageIcon("icones/sch.gif");
        }
        if (name.toLowerCase().endsWith(".pot")) {
            return new ImageIcon("icones/pot.gif");
        }
        if (name.toLowerCase().endsWith(".the")) {
            return new ImageIcon("icones/theme.gif");
        }
        if (name.toLowerCase().endsWith(".ttf")) {
            return new ImageIcon("icones/ttf.gif");
        }
        if (name.toLowerCase().endsWith(".txt")) {
            return new ImageIcon("icones/txt.gif");
        }
        if (name.toLowerCase().endsWith(".wav")) {
            return new ImageIcon("icones/wav.gif");
        }
        if (name.toLowerCase().endsWith(".wbk")) {
            return new ImageIcon("icones/wbk.gif");
        }
        if (name.toLowerCase().endsWith(".wiz")) {
            return new ImageIcon("icones/wiz.gif");
        }
        if (name.toLowerCase().endsWith(".wri")) {
            return new ImageIcon("icones/wri.gif");
        }
        if (name.toLowerCase().endsWith(".wri")) {
            return new ImageIcon("icones/wri.gif");
        }
        if (name.toLowerCase().endsWith(".xlk")) {
            return new ImageIcon("icones/xlk.gif");
        }
        if (name.toLowerCase().endsWith(".xls")) {
            return new ImageIcon("icones/xls.gif");
        }
        if (name.toLowerCase().endsWith(".xlt")) {
            return new ImageIcon("icones/xlt.gif");
        }
        if (name.toLowerCase().endsWith(".xlw")) {
            return new ImageIcon("icones/xlw.gif");
        }
        if (name.toLowerCase().endsWith(".zip")) {
            return new ImageIcon("icones/zip.gif");
        }
        if (name.toLowerCase().endsWith(".pdf")) {
            return new ImageIcon("icones/pdf.gif");
        }
        if (name.toLowerCase().endsWith(".class")) {
            return new ImageIcon("icones/class.gif");
        }
        if (name.toLowerCase().endsWith(".java")) {
            return new ImageIcon("icones/java.gif");
        }
        if (name.toLowerCase().endsWith(".net")) {
            return new ImageIcon("icones/net.gif");
        }
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

        if (name.endsWith(".jpg")) {
            return resource.getString("fileJPGType");
        }

        if (name.endsWith(".gif")) {
            return resource.getString("fileGIFType");
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

