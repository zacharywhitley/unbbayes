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

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.filechooser.FileFilter;

/**
 *  Classe que extende <code>FileFilter</code>, respons�vel por filtrar
 *  os tipos de arquivo a mostrar.
 *
 *@author     Rommel N. Carvalho, Michael S. Onishi
 *@created    27 de Junho de 2001
 *@see        FileFilter
 */
public class SimpleFileFilter extends FileFilter {

    private String[] extensions;
    private String description;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");


    /**
     *  Constr�i um <code>FileFilter</code> com a extens�o desejada.
     *
     *@param  ext  extens�o (<code>String</code>) dos arquivos a mostrar
     *@see         FileFilter
     */
    public SimpleFileFilter(String ext) {
        this(new String[]{ext}, null);
    }


    /**
     *  Constr�i um <code>FileFilter</code> com as extens�es desejadas e
     *  descri��o dessas extens�es.
     *
     *@param  exts   um array de <code>String</code> (extens�es desejadas)
     *@param  descr  descri��o das extens�es (<code>String</code>)
     *
     *@see           String
     */
    public SimpleFileFilter(String[] exts, String descr) {
        //clona e coloca em lowercase as extens�es
        extensions = new String[exts.length];
        for (int i = exts.length - 1; i >= 0; i--) {
            extensions[i] = exts[i].toLowerCase();
        }
        //verificar se temos uma descri��o v�lida
        description = (descr == null ? exts[0] + resource.getString("filesText") : descr);
    }


    /**
     *  Retorna a descri��o geral.
     *
     *@return    descri��o (<code>String</code>) geral
     *@see       String
     */
    public String getDescription() {
        return description;
    }


    /**
     *  Verifica se o arquivo desejado possui a extens�o que a classe filtra.
     *
     *@param  f  arquivo (<code>File</code>) a verificar
     *@return    true se possuir a extens�o correta e false caso contr�rio
     */
    public boolean accept(File f) {
        //sempre permitimos diret�rios independente da extensao
        if (f.isDirectory()) {
            return true;
        }

        //checar a extens�o
        String name = f.getName().toLowerCase();
        for (int i = extensions.length - 1; i >= 0; i--) {
            if (name.endsWith(extensions[i])) {
                return true;
            }
        }
        return false;
    }
}

