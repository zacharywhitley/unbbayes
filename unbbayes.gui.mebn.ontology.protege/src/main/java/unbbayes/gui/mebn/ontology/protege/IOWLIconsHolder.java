/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import javax.swing.Icon;

import org.protege.editor.owl.ui.OWLIcons;

/**
 * This interface contains icons to be used by GUI classes using icons related to
 * OWL entities.
 * @author Shou Matsumoto
 *
 */
public interface IOWLIconsHolder {
	/** Protege-like icon for object properties */
	public static final Icon OBJECT_PROPERTY_ICON = OWLIcons.getIcon("property.object.png");
	/** Protege-like icon for data properties */
    public static final Icon DATA_PROPERTY_ICON = OWLIcons.getIcon("property.data.png");
	/** Protege-like icon for property usage*/
    public static final Icon PROPERTY_USAGE_ICON = OWLIcons.getIcon("property.usage.png");
}
