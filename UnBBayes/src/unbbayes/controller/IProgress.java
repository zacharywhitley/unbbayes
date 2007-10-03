/*
 * Created on 09/04/2003
 *
 */
package unbbayes.controller;

/**
 * @author Mário Henrique
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IProgress {

	public boolean next();
	public void cancel();
	public int maxCount();
}
