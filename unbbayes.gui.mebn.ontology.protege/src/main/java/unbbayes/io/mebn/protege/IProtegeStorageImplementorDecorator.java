package unbbayes.io.mebn.protege;

import org.protege.editor.owl.OWLEditorKit;

import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;

/**
 * It extends {@link IOWLAPIStorageImplementorDecorator} in order to
 * carry protege's {@link OWLEditorKit} (a reference to OWL workspace and its contents) as well
 * @author Shou Matsumoto
 *
 */
public interface IProtegeStorageImplementorDecorator extends IOWLAPIStorageImplementorDecorator {

	/**
	 * @return the owlEditorKit
	 */
	public abstract OWLEditorKit getOWLEditorKit();

	/**
	 * @param owlEditorKit the owlEditorKit to set
	 */
	public abstract void setOWLEditorKit(OWLEditorKit owlEditorKit);

}