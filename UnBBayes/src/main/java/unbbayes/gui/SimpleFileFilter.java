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

import javax.swing.filechooser.*;
import java.io.File;
import java.util.ResourceBundle;

/**
 *  Classe que extende <code>FileFilter</code>, responsável por filtrar
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
     *  Constrói um <code>FileFilter</code> com a extensão desejada.
     *
     *@param  ext  extensão (<code>String</code>) dos arquivos a mostrar
     *@see         FileFilter
     */
    public SimpleFileFilter(String ext) {
        this(new String[]{ext}, null);
    }


    /**
     *  Constrói um <code>FileFilter</code> com as extensões desejadas e
     *  descrição dessas extensões.
     *
     *@param  exts   um array de <code>String</code> (extensões desejadas)
     *@param  descr  descrição das extensões (<code>String</code>)
     *
     *@see           String
     */
    public SimpleFileFilter(String[] exts, String descr) {
        //clona e coloca em lowercase as extensões
        extensions = new String[exts.length];
        for (int i = exts.length - 1; i >= 0; i--) {
            extensions[i] = exts[i].toLowerCase();
        }
        //verificar se temos uma descrição válida
        description = (descr == null ? exts[0] + resource.getString("filesText") : descr);
    }


    /**
     *  Retorna a descrição geral.
     *
     *@return    descrição (<code>String</code>) geral
     *@see       String
     */
    public String getDescription() {
        return description;
    }


    /**
     *  Verifica se o arquivo desejado possui a extensão que a classe filtra.
     *
     *@param  f  arquivo (<code>File</code>) a verificar
     *@return    true se possuir a extensão correta e false caso contrário
     */
    public boolean accept(File f) {
        //sempre permitimos diretórios independente da extensao
        if (f.isDirectory()) {
            return true;
        }

        //checar a extensão
        String name = f.getName().toLowerCase();
        for (int i = extensions.length - 1; i >= 0; i--) {
            if (name.endsWith(extensions[i])) {
                return true;
            }
        }
        return false;
    }
}

