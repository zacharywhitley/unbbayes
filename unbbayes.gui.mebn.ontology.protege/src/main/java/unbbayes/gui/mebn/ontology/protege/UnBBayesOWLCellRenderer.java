/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.tree.TreeCellRenderer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.OWLEntityColorProvider;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This class rewrites {@link org.protege.editor.owl.ui.renderer.OWLCellRenderer} because
 * the original class was trying to load OSGI plug-ins and
 * crushes, because no OSGI plug-ins can be loaded in non-OSGI complaint
 * applications (this is a Bug in Protege 4.1, because it should let
 * applications to use Protege as a ordinal library as well as OSGI bundles).
 * Some additional getters/setters were added to make it easier to extend.
 * "As is" usage or simple inheritance from {@link org.protege.editor.owl.ui.renderer.OWLCellRenderer}
 * could not be used because the original class was written in a manner that OSGI plug-in
 * loader was mandatory (thus, causing a crush no matter what we did).
 * @author Shou Matsumoto
 *
 */
public class UnBBayesOWLCellRenderer implements TableCellRenderer, TreeCellRenderer, ListCellRenderer {

    private boolean forceReadOnlyRendering;

    private OWLEditorKit owlEditorKit;

    private boolean renderIcon;

    private boolean renderExpression;

    private boolean strikeThrough;

    private OWLOntology ontology;

    private Set<OWLObject> equivalentObjects;

    private LinkedObjectComponent linkedObjectComponent;

    private Font plainFont;

    private Font boldFont;

    public static final Color SELECTION_BACKGROUND = UIManager.getDefaults().getColor("List.selectionBackground");

    public static final Color SELECTION_FOREGROUND = UIManager.getDefaults().getColor("List.selectionForeground");

    public static final Color FOREGROUND = UIManager.getDefaults().getColor("List.foreground");

    private boolean gettingCellBounds;

    private List<OWLEntityColorProvider> entityColorProviders;

    // The object that determines which icon should be displayed.
    private OWLObject iconObject;

    private int leftMargin = 0;

    private int rightMargin = 40;

    private JComponent componentBeingRendered;

    private JPanel renderingComponent;

    private JLabel iconLabel;

    private JTextPane textPane;

    private int preferredWidth;

    private int minTextHeight;

    private OWLEntity focusedEntity;

    private boolean commentedOut;

    private boolean inferred;

    private boolean highlightKeywords;

    private boolean wrap = true;

    private boolean highlightUnsatisfiableClasses = true;

    private boolean highlightUnsatisfiableProperties = true;

    private Set<OWLEntity> crossedOutEntities;

    private Set<String> unsatisfiableNames;

    private Set<String> boxedNames;

    private int plainFontHeight;

    private boolean opaque = false;

    /**
     * The default constructor is made visible to subclasses in order to allow inheritance
     * @deprecated use {@link #getInstance(OWLEditorKit)} or {@link #getInstance(OWLEditorKit, boolean, boolean)} instead
     */
    protected UnBBayesOWLCellRenderer() {
    	super();
    }
    
    /**
     * Construction method using fields
     * @param owlEditorKit
     * @return a new instance
     */
    public static UnBBayesOWLCellRenderer getInstance(OWLEditorKit owlEditorKit) {
    	return UnBBayesOWLCellRenderer.getInstance(owlEditorKit, true, true);
    }

    /**
     * Construction method using fields
     * @param owlEditorKit
     * @param renderExpression
     * @param renderIcon
     * @return a new instance
     */
    public static UnBBayesOWLCellRenderer getInstance(OWLEditorKit owlEditorKit, boolean renderExpression, boolean renderIcon) {
    	UnBBayesOWLCellRenderer ret =  new UnBBayesOWLCellRenderer();
    	
    	ret.setOwlEditorKit(owlEditorKit);
    	ret.setRenderExpression(renderExpression);
    	ret.setRenderIcon(renderIcon);
    	ret.setEquivalentObjects(new HashSet<OWLObject>());
    	
    	ret.setIconLabel(new JLabel(""));
    	ret.getIconLabel().setOpaque(false);
    	ret.getIconLabel().setVerticalAlignment(SwingConstants.CENTER);
    	
    	ret.setTextPane(new JTextPane());
    	ret.getTextPane().setOpaque(false);
    	
    	ret.setRenderingComponent(new JPanel(ret.new OWLCellRendererLayoutManager()));
    	ret.getRenderingComponent().add(ret.getIconLabel());
    	ret.getRenderingComponent().add(ret.getTextPane());
    	
    	ret.setEntityColorProviders(new ArrayList<OWLEntityColorProvider>());
    	
    	ret.loadColorProviderPlugins(ret);
    	
    	ret.setCrossedOutEntities(new HashSet<OWLEntity>());
    	ret.setUnsatisfiableNames(new HashSet<String>());
    	ret.setBoxedNames(new HashSet<String>());
    	ret.prepareStyles();
    	ret.setupFont();
    	
		return ret;
    }


    /**
     * Overwrite this method if you want to load {@link org.protege.editor.owl.ui.renderer.OWLEntityColorProviderPlugin}
     * @param hostRenderer : the renderer where the plugins will be loaded. Usually, you'll use "this" as parameter.
     */
    protected void loadColorProviderPlugins(UnBBayesOWLCellRenderer hostRenderer) {
    	// stop loading OSGI plugins (actually, the followingin code is not a problem - the problem is when loader.getPlugins() tries to load extensions and does not check plug-in existency)
//		try {
//			OWLEntityColorProviderPluginLoader loader = new OWLEntityColorProviderPluginLoader(ret.getOWLModelManager());
//			for (OWLEntityColorProviderPlugin plugin : loader.getPlugins()) {
//				try {
//					OWLEntityColorProvider prov = plugin.newInstance();
//					prov.initialise();
//					entityColorProviders.add(prov);
//				} catch (Exception e) {
//					logger.error(e);
//				}
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
	}

	public void setForceReadOnlyRendering(boolean forceReadOnlyRendering) {
        this.forceReadOnlyRendering = forceReadOnlyRendering;
    }


    public void setOpaque(boolean opaque){
        this.opaque = opaque;
    }


    public void setUnsatisfiableNames(Set<String> unsatisfiableNames) {
        this.unsatisfiableNames.clear();
        this.unsatisfiableNames.addAll(unsatisfiableNames);
    }


    public void setHighlightKeywords(boolean hightlighKeywords) {
        this.highlightKeywords = hightlighKeywords;
    }


    public void setHighlightUnsatisfiableClasses(boolean highlightUnsatisfiableClasses) {
        this.highlightUnsatisfiableClasses = highlightUnsatisfiableClasses;
    }


    public void setHighlightUnsatisfiableProperties(boolean highlightUnsatisfiableProperties) {
        this.highlightUnsatisfiableProperties = highlightUnsatisfiableProperties;
    }


    public void setOntology(OWLOntology ont) {
        forceReadOnlyRendering = false;
        this.ontology = ont;
    }


    public void setIconObject(OWLObject object) {
        iconObject = object;
    }

    public void setCrossedOutEntities(Set<OWLEntity> entities) {
        crossedOutEntities.addAll(entities);
    }

    public void addBoxedName(String name) {
        boxedNames.add(name);
    }

    public boolean isBoxedName(String name) {
        return boxedNames.contains(name);
    }

    public void reset() {
        iconObject = null;
        rightMargin = 0;
        ontology = null;
        focusedEntity = null;
        commentedOut = false;
        inferred = false;
        strikeThrough = false;
        highlightUnsatisfiableClasses = true;
        highlightUnsatisfiableProperties = true;
        crossedOutEntities.clear();
        unsatisfiableNames.clear();
        boxedNames.clear();
    }


    public void setFocusedEntity(OWLEntity entity) {
        focusedEntity = entity;
    }


    /**
     * Sets equivalent objects for the object being rendered.  For example,
     * if the object being rendered is A, and B and C are equivalent to A, then
     * setting the equivalent objects to {B, C} will cause the rendering to
     * have (= B = C) appended to it
     * @param objects The objects that are equivalent to the object
     *                being rendered
     */
    public void setEquivalentObjects(Set<OWLObject> objects) {
        equivalentObjects.clear();
        equivalentObjects.addAll(objects);
    }


    /**
     * Specifies whether or not this row displays inferred information (the
     * default value is false)
     */
    public void setInferred(boolean inferred) {
        this.inferred = inferred;
    }


    public void setStrikeThrough(boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
    }


    public int getPreferredWidth() {
        return preferredWidth;
    }


    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }


    public int getRightMargin() {
        return rightMargin;
    }


    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    /**
     * The constructor calls this method to initialize font
     */
    protected void setupFont() {
        plainFont = OWLRendererPreferences.getInstance().getFont();
        plainFontHeight = iconLabel.getFontMetrics(plainFont).getHeight();
        boldFont = plainFont.deriveFont(Font.BOLD);
        textPane.setFont(plainFont);
    }

    protected int getFontSize() {
        return OWLRendererPreferences.getInstance().getFontSize();
    }


    public boolean isRenderExpression() {
        return renderExpression;
    }


    public boolean isRenderIcon() {
        return renderIcon;
    }


    public void setCommentedOut(boolean commentedOut) {
        this.commentedOut = commentedOut;
    }


    public boolean isWrap() {
        return wrap;
    }


    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //
    // Implementation of renderer interfaces
    //
    ////////////////////////////////////////////////////////////////////////////////////////

    private boolean renderLinks;


    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setupLinkedObjectComponent(table, table.getCellRect(row, column, true));
        preferredWidth = table.getParent().getWidth();
        componentBeingRendered = table;
        // Set the size of the table cell
//        setPreferredWidth(tabl)e.getColumnModel().getColumn(column).getWidth());
        return prepareRenderer(value, isSelected, hasFocus);

//        // This is a bit messy - the row height doesn't get reset if it is larger than the
//        // desired row height.
//        // Reset the row height if the text has been wrapped
//        int desiredRowHeight = getPrefSize(table, table.getGraphics(), c.getText()).height;
//        if (desiredRowHeight < table.getRowHeight()) {
//            desiredRowHeight = table.getRowHeight();
//        }
//        else if (desiredRowHeight > table.getRowHeight(row)) {
//            // Add a bit of a margin, because wrapped lines
//            // tend to merge with adjacent lines too much
//            desiredRowHeight += 4;
//        }
//        if (table.getEditingRow() != row) {
//            if (table.getRowHeight(row) < desiredRowHeight) {
//                table.setRowHeight(row, desiredRowHeight);
//            }
//        }
//        reset();
//        return c;
    }


    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        componentBeingRendered = tree;
        Rectangle cellBounds = new Rectangle();
        if (!gettingCellBounds) {
            gettingCellBounds = true;
            cellBounds = tree.getRowBounds(row);
            gettingCellBounds = false;
        }
        setupLinkedObjectComponent(tree, cellBounds);
        preferredWidth = -1;
        minTextHeight = 12;
//        textPane.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2 + rightMargin));
        tree.setToolTipText(value != null ? value.toString() : "");
        Component c = prepareRenderer(value, selected, hasFocus);
        reset();
        return c;
    }


    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        componentBeingRendered = list;
        Rectangle cellBounds = new Rectangle();
        // We need to prevent infinite recursion here!
        if (!gettingCellBounds) {
            gettingCellBounds = true;
            cellBounds = list.getCellBounds(index, index);
            gettingCellBounds = false;
        }
        minTextHeight = 12;
        if (list.getParent() != null) {
            preferredWidth = list.getParent().getWidth();
        }
//        preferredWidth = -1;
//        textPane.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2 + rightMargin));
        setupLinkedObjectComponent(list, cellBounds);
        Component c = prepareRenderer(value, isSelected, cellHasFocus);
        reset();
        return c;
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param component
     * @param cellRect
     */
    protected void setupLinkedObjectComponent(JComponent component, Rectangle cellRect) {
        renderLinks = false;
        linkedObjectComponent = null;
        if (cellRect == null) {
            return;
        }
        if (component instanceof LinkedObjectComponent && OWLRendererPreferences.getInstance().isRenderHyperlinks()) {
            linkedObjectComponent = (LinkedObjectComponent) component;
            Point mouseLoc = component.getMousePosition(true);
            if (mouseLoc == null) {
                linkedObjectComponent.setLinkedObject(null);
                return;
            }
            renderLinks = cellRect.contains(mouseLoc);
        }
    }

    /**
     * 
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @author Shou Matsumoto
     *
     */
    protected class ActiveEntityVisitor implements OWLEntityVisitor {

        public void visit(OWLClass cls) {
            if (!getOWLModelManager().getActiveOntology().getAxioms(cls).isEmpty()) {
                ontology = getOWLModelManager().getActiveOntology();
            }
        }


        public void visit(OWLDatatype dataType) {
            if (!getOWLModelManager().getActiveOntology().getAxioms(dataType).isEmpty()) {
                ontology = getOWLModelManager().getActiveOntology();
            }
        }


        public void visit(OWLNamedIndividual individual) {
            if (!getOWLModelManager().getActiveOntology().getAxioms(individual).isEmpty()) {
                ontology = getOWLModelManager().getActiveOntology();
            }
        }


        public void visit(OWLDataProperty property) {
            if (!getOWLModelManager().getActiveOntology().getAxioms(property).isEmpty()) {
                ontology = getOWLModelManager().getActiveOntology();
            }
        }


        public void visit(OWLObjectProperty property) {
            if (!getOWLModelManager().getActiveOntology().getAxioms(property).isEmpty()) {
                ontology = getOWLModelManager().getActiveOntology();
            }
        }


        public void visit(OWLAnnotationProperty property) {
            if (!getOWLModelManager().getActiveOntology().getAxioms(property).isEmpty()) {
                ontology = getOWLModelManager().getActiveOntology();
            }
        }
    }


    private ActiveEntityVisitor activeEntityVisitor = new ActiveEntityVisitor();

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param value
     * @param isSelected
     * @param hasFocus
     * @return
     */
    protected Component prepareRenderer(Object value, boolean isSelected, boolean hasFocus) {
        renderingComponent.setOpaque(isSelected || opaque);

        if (value instanceof OWLEntity) {
            OWLEntity entity = (OWLEntity) value;
            OWLDeclarationAxiom declAx = getOWLModelManager().getOWLDataFactory().getOWLDeclarationAxiom(entity);
            if (getOWLModelManager().getActiveOntology().containsAxiom(declAx)) {
                ontology = getOWLModelManager().getActiveOntology();
            }
            entity.accept(activeEntityVisitor);
        }


        prepareTextPane(getRendering(value), isSelected);

        if (isSelected) {
            renderingComponent.setBackground(SELECTION_BACKGROUND);
            textPane.setForeground(SELECTION_FOREGROUND);
        }
        else {
            renderingComponent.setBackground(componentBeingRendered.getBackground());
            textPane.setForeground(componentBeingRendered.getForeground());
        }

        final Icon icon = getIcon(value);
        iconLabel.setIcon(icon);
        if (icon != null){
            iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), plainFontHeight));
        }
        renderingComponent.revalidate();
        return renderingComponent;
    }


    protected String getRendering(Object object) {
        if (object instanceof OWLObject) {
            String rendering = getOWLModelManager().getRendering(((OWLObject) object));
            for (OWLObject eqObj : equivalentObjects) {
                // Add in the equivalent class symbol
                rendering += " \u2261 " + getOWLModelManager().getRendering(eqObj);
            }
            return rendering;
        }
        else {
            if (object != null) {
                return object.toString();
            }
            else {
                return "";
            }
        }
    }


    protected Icon getIcon(Object object) {
        if(!renderIcon) {
            return null;
        }
        if (iconObject != null) {
            return owlEditorKit.getWorkspace().getOWLIconProvider().getIcon(iconObject);
        }
        if (object instanceof OWLObject) {
            return owlEditorKit.getWorkspace().getOWLIconProvider().getIcon((OWLObject) object);
        }
        else {
            return null;
        }
    }


    private Composite disabledComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    /**
     * Extracts the owl manager from {@link #getOwlEditorKit()}
     * @return
     */
    protected OWLModelManager getOWLModelManager() {
        return this.getOwlEditorKit().getModelManager();
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.ui.renderer.OWLCellRenderer#getColor(org.semanticweb.owlapi.model.OWLEntity, java.awt.Color)
     */
    protected Color getColor(OWLEntity entity, Color defaultColor) {
        for (OWLEntityColorProvider prov : this.getEntityColorProviders()) {
            Color c = prov.getColor(entity);
            if (c != null) {
                return c;
            }
        }
        return defaultColor;
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.ui.renderer.OWLCellRenderer#activeOntologyContainsAxioms(org.semanticweb.owlapi.model.OWLEntity)
     */
    protected boolean activeOntologyContainsAxioms(OWLEntity owlEntity) {
        return !getOWLModelManager().getActiveOntology().getReferencingAxioms(owlEntity).isEmpty();
    }


    private Style plainStyle;

    private Style boldStyle;

    private Style nonBoldStyle;

    private Style selectionForeground;

    private Style foreground;

    private Style linkStyle;

    private Style inconsistentClassStyle;

    private Style focusedEntityStyle;

    private Style ontologyURIStyle;

    private Style commentedOutStyle;

    private Style strikeOutStyle;

    private Style fontSizeStyle;
    
    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     */
    protected void prepareStyles() {
        StyledDocument doc = textPane.getStyledDocument();
        Map<String, Color> keyWordColorMap = owlEditorKit.getWorkspace().getKeyWordColorMap();
        for (String keyWord : keyWordColorMap.keySet()) {
            Style s = doc.addStyle(keyWord, null);
            Color color = keyWordColorMap.get(keyWord);
            StyleConstants.setForeground(s, color);
            StyleConstants.setBold(s, true);
        }
        plainStyle = doc.addStyle("PLAIN_STYLE", null);
//        StyleConstants.setForeground(plainStyle, Color.BLACK);
        StyleConstants.setItalic(plainStyle, false);
        StyleConstants.setSpaceAbove(plainStyle, 0);
//        StyleConstants.setFontFamily(plainStyle, textPane.getFont().getFamily());

        boldStyle = doc.addStyle("BOLD_STYLE", null);
        StyleConstants.setBold(boldStyle, true);


        nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
        StyleConstants.setBold(nonBoldStyle, false);

        selectionForeground = doc.addStyle("SEL_FG_STYPE", null);
        StyleConstants.setForeground(selectionForeground, SELECTION_FOREGROUND);

        foreground = doc.addStyle("FG_STYLE", null);
        StyleConstants.setForeground(foreground, FOREGROUND);

        linkStyle = doc.addStyle("LINK_STYLE", null);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);

        inconsistentClassStyle = doc.addStyle("INCONSISTENT_CLASS_STYLE", null);
        StyleConstants.setForeground(inconsistentClassStyle, Color.RED);

        focusedEntityStyle = doc.addStyle("FOCUSED_ENTITY_STYLE", null);
        StyleConstants.setForeground(focusedEntityStyle, Color.BLACK);
        StyleConstants.setBackground(focusedEntityStyle, new Color(220, 220, 250));

        ontologyURIStyle = doc.addStyle("ONTOLOGY_URI_STYLE", null);
        StyleConstants.setForeground(ontologyURIStyle, Color.GRAY);

        commentedOutStyle = doc.addStyle("COMMENTED_OUT_STYLE", null);
        StyleConstants.setForeground(commentedOutStyle, Color.GRAY);
        StyleConstants.setItalic(commentedOutStyle, true);

        strikeOutStyle = doc.addStyle("STRIKE_OUT", null);
        StyleConstants.setStrikeThrough(strikeOutStyle, true);
        StyleConstants.setBold(strikeOutStyle, false);

        fontSizeStyle = doc.addStyle("FONT_SIZE", null);
        StyleConstants.setFontSize(fontSizeStyle, 40);
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param value
     * @param selected
     */
    protected void prepareTextPane(Object value, boolean selected) {

        textPane.setBorder(null);
        String theVal = value.toString();
        if (!wrap) {
            theVal = theVal.replace('\n', ' ');
            theVal = theVal.replaceAll(" [ ]+", " ");
        }
        textPane.setText(theVal);
        if (commentedOut) {
            textPane.setText("// " + textPane.getText());
        }
//        textPane.setSize(textPane.getPreferredSize());
        StyledDocument doc = textPane.getStyledDocument();
//        doc.setParagraphAttributes(0, doc.getLength(), linespacingStyle, false);
        resetStyles(doc);

        if (selected) {
            doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
        }
        else {
            doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
        }

        if (commentedOut) {
            doc.setParagraphAttributes(0, doc.getLength(), commentedOutStyle, false);
            return;
        }
        else if (inferred) {

        }

        if (strikeThrough) {
            doc.setParagraphAttributes(0, doc.getLength(), strikeOutStyle, false);
        }

        if (ontology != null) {
            if (OWLRendererPreferences.getInstance().isHighlightActiveOntologyStatements() &&
                getOWLModelManager().getActiveOntology().equals(ontology)) {
                doc.setParagraphAttributes(0, doc.getLength(), boldStyle, false);
            }
            else {
                doc.setParagraphAttributes(0, doc.getLength(), nonBoldStyle, false);
            }
        }
        else {
            textPane.setFont(plainFont);
        }

        // Set the writable status
        if (ontology != null) {
            if (getOWLModelManager().isMutable(ontology)) {
                textPane.setEnabled(!forceReadOnlyRendering);
            }
            else {
                // Not editable - set readonly
                textPane.setEnabled(false);
            }
        }
        else {
            // Ontology is null.  If the object is an entity then the font
            // should be bold if there are statements about it
            if (value instanceof OWLEntity) {
                if (activeOntologyContainsAxioms((OWLEntity) value)) {
                    textPane.setFont(boldFont);
                }
            }
        }

        highlightText(doc);
    }


    protected void highlightText(StyledDocument doc) {
        // Highlight text
        StringTokenizer tokenizer = new StringTokenizer(textPane.getText(), " []{}(),\n\t'", true);
        linkRendered = false;
        annotURIRendered = false;
        int tokenStartIndex = 0;
        while (tokenizer.hasMoreTokens()) {
            // Get the token and determine if it is a keyword or
            // entity (or delimeter)
            String curToken = tokenizer.nextToken();
            if (curToken.equals("'")) {
                while (tokenizer.hasMoreTokens()) {
                    String s = tokenizer.nextToken();
                    curToken += s;
                    if (s.equals("'")) {
                        break;
                    }
                }
            }
            renderToken(curToken, tokenStartIndex, doc);

            tokenStartIndex += curToken.length();
        }
        if (renderLinks && !linkRendered) {
            linkedObjectComponent.setLinkedObject(null);
        }
    }


    private boolean annotURIRendered = false;
    private boolean linkRendered = false;
    private boolean parenthesisRendered = false;

    protected void renderToken(final String curToken, final int tokenStartIndex, final StyledDocument doc) {

        boolean enclosedByBracket = false;
        if (parenthesisRendered){
            parenthesisRendered = false;
            enclosedByBracket = true;
        }

        OWLRendererPreferences prefs = OWLRendererPreferences.getInstance();

        final int tokenLength = curToken.length();
        Color c = owlEditorKit.getWorkspace().getKeyWordColorMap().get(curToken);
        if (c != null && prefs.isHighlightKeyWords() && highlightKeywords) {
            Style s = doc.getStyle(curToken);
            doc.setCharacterAttributes(tokenStartIndex, tokenLength, s, true);
        }
        else {
            // Not a keyword, so might be an entity (or delim)
            final OWLEntity curEntity = getOWLModelManager().getOWLEntityFinder().getOWLEntity(curToken);
            if (curEntity != null) {
                if (focusedEntity != null) {
                    if (curEntity.equals(focusedEntity)) {
                        doc.setCharacterAttributes(tokenStartIndex, tokenLength, focusedEntityStyle, true);
                    }
                }
                else if (highlightUnsatisfiableClasses && curEntity instanceof OWLClass) {
                    // If it is a class then paint the word red if the class
                    // is inconsistent
                    getOWLModelManager().getReasonerPreferences().executeTask(OptionalInferenceTask.SHOW_CLASS_UNSATISFIABILITY,
                                                                              new Runnable() {
                        public void run() {
                            if (!getOWLModelManager().getReasoner().isSatisfiable((OWLClass) curEntity)) {
                                // Paint red because of inconsistency
                                doc.setCharacterAttributes(tokenStartIndex, tokenLength, inconsistentClassStyle, true);
                            }
                        }
                    });

                }
                else if (highlightUnsatisfiableProperties && curEntity instanceof OWLObjectProperty) {
                    highlightPropertyIfUnsatisfiable(curEntity, doc, tokenStartIndex, tokenLength);
                }
                strikeoutEntityIfCrossedOut(curEntity, doc, tokenStartIndex, tokenLength);

                if (renderLinks) {
                    renderHyperlink(curEntity, tokenStartIndex, tokenLength, doc);
                }
            }
            else {
                if (highlightUnsatisfiableClasses && unsatisfiableNames.contains(curToken)) {
                    // Paint red because of inconsistency
                    doc.setCharacterAttributes(tokenStartIndex, tokenLength, inconsistentClassStyle, true);
                }
                else if (isOntologyURI(curToken)){
                    fadeOntologyURI(doc, tokenStartIndex, tokenLength, enclosedByBracket);
                }
                else if (curToken.equals("(")){
                    parenthesisRendered = true;
                }
            }
        }
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param curEntity
     * @param tokenStartIndex
     * @param tokenLength
     * @param doc
     */
    protected void renderHyperlink(OWLEntity curEntity, int tokenStartIndex, int tokenLength, StyledDocument doc) {
        try {
            Rectangle startRect = textPane.modelToView(tokenStartIndex);
            Rectangle endRect = textPane.modelToView(tokenStartIndex + tokenLength);
            if (startRect != null && endRect != null) {
                int width = endRect.x - startRect.x;
                int heght = startRect.height;

                Rectangle tokenRect = new Rectangle(startRect.x, startRect.y, width, heght);
                tokenRect.grow(0, -2);
                if (linkedObjectComponent.getMouseCellLocation() != null) {
                    Point mouseCellLocation = linkedObjectComponent.getMouseCellLocation();
                    if (mouseCellLocation != null) {
                        mouseCellLocation = SwingUtilities.convertPoint(renderingComponent,
                                                                        mouseCellLocation,
                                                                        textPane);
                        if (tokenRect.contains(mouseCellLocation)) {
                            doc.setCharacterAttributes(tokenStartIndex, tokenLength, linkStyle, false);
                            linkedObjectComponent.setLinkedObject(curEntity);
                            linkRendered = true;
                        }
                    }
                }
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param token
     * @return
     */
    protected boolean isOntologyURI(String token) {
        try {
            final URI uri = new URI(token);
            if (uri.isAbsolute()){
                IRI iri = IRI.create(uri);
                OWLOntology ont = getOWLModelManager().getOWLOntologyManager().getOntology(iri);
                if (getOWLModelManager().getActiveOntologies().contains(ont)){
                    return true;
                }
            }
        }
        catch (URISyntaxException e) {
            // just dropthough
        }
        return false;
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param doc
     * @param tokenStartIndex
     * @param tokenLength
     * @param enclosedByBracket
     */
    protected void fadeOntologyURI(StyledDocument doc, int tokenStartIndex, int tokenLength, boolean enclosedByBracket) {
        // if surrounded by brackets, also render them in grey
        int start = tokenStartIndex;
        int length = tokenLength;
        if (enclosedByBracket){
            start--;
            length = length+2;
        }
        doc.setCharacterAttributes(start, length, ontologyURIStyle, true);
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param entity
     * @param doc
     * @param tokenStartIndex
     * @param tokenLength
     */
    protected void strikeoutEntityIfCrossedOut(OWLEntity entity, StyledDocument doc, int tokenStartIndex,
                                             int tokenLength) {
        if(crossedOutEntities.contains(entity)) {
            doc.setCharacterAttributes(tokenStartIndex, tokenLength, strikeOutStyle, false);
        }
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param entity
     * @param doc
     * @param tokenStartIndex
     * @param tokenLength
     */
    protected void highlightPropertyIfUnsatisfiable(final OWLEntity entity, final StyledDocument doc, final int tokenStartIndex, final int tokenLength) {
        getOWLModelManager().getReasonerPreferences().executeTask(OptionalInferenceTask.SHOW_OBJECT_PROPERTY_UNSATISFIABILITY, 
                                                                  new Runnable() {
            public void run() {
                OWLObjectProperty prop = (OWLObjectProperty) entity;
                if(getOWLModelManager().getReasoner().getBottomObjectPropertyNode().contains(prop)) {
                    doc.setCharacterAttributes(tokenStartIndex, tokenLength, inconsistentClassStyle, true);
                }
            }
        });
    }

    /**
     * This is just to extend visibility of the superclass' method (they were private, but now they are protected)
     * @param doc
     */
    protected void resetStyles(StyledDocument doc) {
        doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);
        StyleConstants.setFontSize(fontSizeStyle, getFontSize());
        Font f = OWLRendererPreferences.getInstance().getFont();
        StyleConstants.setFontFamily(fontSizeStyle, f.getFamily());
        doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, false);
        setupFont();
    }


    protected class OWLCellRendererLayoutManager implements LayoutManager2 {


        /**
         * Adds the specified component to the layout, using the specified
         * constraint object.
         * @param comp        the component to be added
         * @param constraints where/how the component is added to the layout.
         */
        public void addLayoutComponent(Component comp, Object constraints) {
            // We only have two components the label that holds the icon
            // and the text area
        }


        /**
         * Calculates the maximum size dimensions for the specified container,
         * given the components it contains.
         * @see java.awt.Component#getMaximumSize
         * @see java.awt.LayoutManager
         */
        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }


        /**
         * Returns the alignment along the x axis.  This specifies how
         * the component would like to be aligned relative to other
         * components.  The value should be a number between 0 and 1
         * where 0 represents alignment along the origin, 1 is aligned
         * the furthest away from the origin, 0.5 is centered, etc.
         */
        public float getLayoutAlignmentX(Container target) {
            return 0;
        }


        /**
         * Returns the alignment along the y axis.  This specifies how
         * the component would like to be aligned relative to other
         * components.  The value should be a number between 0 and 1
         * where 0 represents alignment along the origin, 1 is aligned
         * the furthest away from the origin, 0.5 is centered, etc.
         */
        public float getLayoutAlignmentY(Container target) {
            return 0;
        }


        /**
         * Invalidates the layout, indicating that if the layout manager
         * has cached information it should be discarded.
         */
        public void invalidateLayout(Container target) {
        }


        /**
         * If the layout manager uses a per-component string,
         * adds the component <code>comp</code> to the layout,
         * associating it
         * with the string specified by <code>name</code>.
         * @param name the string to be associated with the component
         * @param comp the component to be added
         */
        public void addLayoutComponent(String name, Component comp) {
        }


        /**
         * Removes the specified component from the layout.
         * @param comp the component to be removed
         */
        public void removeLayoutComponent(Component comp) {
        }


        /**
         * Calculates the preferred size dimensions for the specified
         * container, given the components it contains.
         * @param parent the container to be laid out
         * @see #minimumLayoutSize
         */
        public Dimension preferredLayoutSize(Container parent) {
            if (componentBeingRendered instanceof JList) {
                JList list = (JList) componentBeingRendered;
                if (list.getFixedCellHeight() != -1) {
                    return new Dimension(list.getWidth(), list.getHeight());
                }
            }
            int iconWidth;
            int iconHeight;
            int textWidth;
            int textHeight;
            int width;
            int height;
            iconWidth = iconLabel.getPreferredSize().width;
            iconHeight = iconLabel.getPreferredSize().height;
            Insets insets = parent.getInsets();
            Insets rcInsets = renderingComponent.getInsets();

            if (preferredWidth != -1) {
                textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right;
                View v = textPane.getUI().getRootView(textPane);
                v.setSize(textWidth, Integer.MAX_VALUE);
                textHeight = (int) v.getMinimumSpan(View.Y_AXIS);
                width = preferredWidth;
            }
            else {
                textWidth = textPane.getPreferredSize().width;
                textHeight = textPane.getPreferredSize().height;
                width = textWidth + iconWidth;
            }
            if (textHeight < iconHeight) {
                height = iconHeight;
            }
            else {
                height = textHeight;
            }
            int minHeight = minTextHeight;
            if (height < minHeight) {
                height = minHeight;
            }
            int totalWidth = width + rcInsets.left + rcInsets.right;
            int totalHeight = height + rcInsets.top + rcInsets.bottom;
            return new Dimension(totalWidth, totalHeight);
        }

        /**
         * Lays out the specified container.
         * @param parent the container to be laid out
         */
        public void layoutContainer(Container parent) {
            int iconWidth;
            int iconHeight;
            int textWidth;
            int textHeight;
            Insets rcInsets = renderingComponent.getInsets();

            iconWidth = iconLabel.getPreferredSize().width;
            iconHeight = iconLabel.getPreferredSize().height;
            if (preferredWidth != -1) {
                textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right;
                View v = textPane.getUI().getRootView(textPane);
                v.setSize(textWidth, Integer.MAX_VALUE);
                textHeight = (int) v.getMinimumSpan(View.Y_AXIS);
            }
            else {
                textWidth = textPane.getPreferredSize().width;
                textHeight = textPane.getPreferredSize().height;
                if (textHeight < minTextHeight) {
                    textHeight = minTextHeight;
                }
            }
            int leftOffset = rcInsets.left;
            int topOffset = rcInsets.top;
            iconLabel.setBounds(leftOffset, topOffset, iconWidth, iconHeight);
            textPane.setBounds(leftOffset + iconWidth, topOffset, textWidth, textHeight);
        }

        /**
         * Calculates the minimum size dimensions for the specified
         * container, given the components it contains.
         * @param parent the component to be laid out
         * @see #preferredLayoutSize
         */
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }



    }


	/**
	 * @return the owlEditorKit
	 */
	public OWLEditorKit getOwlEditorKit() {
		return owlEditorKit;
	}

	/**
	 * @param owlEditorKit the owlEditorKit to set
	 */
	public void setOwlEditorKit(OWLEditorKit owlEditorKit) {
		this.owlEditorKit = owlEditorKit;
	}

	/**
	 * @return the linkedObjectComponent
	 */
	public LinkedObjectComponent getLinkedObjectComponent() {
		return linkedObjectComponent;
	}

	/**
	 * @param linkedObjectComponent the linkedObjectComponent to set
	 */
	public void setLinkedObjectComponent(LinkedObjectComponent linkedObjectComponent) {
		this.linkedObjectComponent = linkedObjectComponent;
	}

	/**
	 * @return the plainFont
	 */
	public Font getPlainFont() {
		return plainFont;
	}

	/**
	 * @param plainFont the plainFont to set
	 */
	public void setPlainFont(Font plainFont) {
		this.plainFont = plainFont;
	}

	/**
	 * @return the boldFont
	 */
	public Font getBoldFont() {
		return boldFont;
	}

	/**
	 * @param boldFont the boldFont to set
	 */
	public void setBoldFont(Font boldFont) {
		this.boldFont = boldFont;
	}

	/**
	 * @return the gettingCellBounds
	 */
	public boolean isGettingCellBounds() {
		return gettingCellBounds;
	}

	/**
	 * @param gettingCellBounds the gettingCellBounds to set
	 */
	public void setGettingCellBounds(boolean gettingCellBounds) {
		this.gettingCellBounds = gettingCellBounds;
	}

	/**
	 * @return the entityColorProviders
	 */
	public List<OWLEntityColorProvider> getEntityColorProviders() {
		return entityColorProviders;
	}

	/**
	 * @param entityColorProviders the entityColorProviders to set
	 */
	public void setEntityColorProviders(
			List<OWLEntityColorProvider> entityColorProviders) {
		this.entityColorProviders = entityColorProviders;
	}

	/**
	 * @return the leftMargin
	 */
	public int getLeftMargin() {
		return leftMargin;
	}

	/**
	 * @param leftMargin the leftMargin to set
	 */
	public void setLeftMargin(int leftMargin) {
		this.leftMargin = leftMargin;
	}

	/**
	 * @return the componentBeingRendered
	 */
	public JComponent getComponentBeingRendered() {
		return componentBeingRendered;
	}

	/**
	 * @param componentBeingRendered the componentBeingRendered to set
	 */
	public void setComponentBeingRendered(JComponent componentBeingRendered) {
		this.componentBeingRendered = componentBeingRendered;
	}

	/**
	 * @return the renderingComponent
	 */
	public JPanel getRenderingComponent() {
		return renderingComponent;
	}

	/**
	 * @param renderingComponent the renderingComponent to set
	 */
	public void setRenderingComponent(JPanel renderingComponent) {
		this.renderingComponent = renderingComponent;
	}

	/**
	 * @return the iconLabel
	 */
	public JLabel getIconLabel() {
		return iconLabel;
	}

	/**
	 * @param iconLabel the iconLabel to set
	 */
	public void setIconLabel(JLabel iconLabel) {
		this.iconLabel = iconLabel;
	}

	/**
	 * @return the textPane
	 */
	public JTextPane getTextPane() {
		return textPane;
	}

	/**
	 * @param textPane the textPane to set
	 */
	public void setTextPane(JTextPane textPane) {
		this.textPane = textPane;
	}

	/**
	 * @return the minTextHeight
	 */
	public int getMinTextHeight() {
		return minTextHeight;
	}

	/**
	 * @param minTextHeight the minTextHeight to set
	 */
	public void setMinTextHeight(int minTextHeight) {
		this.minTextHeight = minTextHeight;
	}

	/**
	 * @return the boxedNames
	 */
	public Set<String> getBoxedNames() {
		return boxedNames;
	}

	/**
	 * @param boxedNames the boxedNames to set
	 */
	public void setBoxedNames(Set<String> boxedNames) {
		this.boxedNames = boxedNames;
	}

	/**
	 * @return the plainFontHeight
	 */
	public int getPlainFontHeight() {
		return plainFontHeight;
	}

	/**
	 * @param plainFontHeight the plainFontHeight to set
	 */
	public void setPlainFontHeight(int plainFontHeight) {
		this.plainFontHeight = plainFontHeight;
	}

	/**
	 * @return the renderLinks
	 */
	public boolean isRenderLinks() {
		return renderLinks;
	}

	/**
	 * @param renderLinks the renderLinks to set
	 */
	public void setRenderLinks(boolean renderLinks) {
		this.renderLinks = renderLinks;
	}

	/**
	 * @return the activeEntityVisitor
	 */
	public ActiveEntityVisitor getActiveEntityVisitor() {
		return activeEntityVisitor;
	}

	/**
	 * @param activeEntityVisitor the activeEntityVisitor to set
	 */
	public void setActiveEntityVisitor(ActiveEntityVisitor activeEntityVisitor) {
		this.activeEntityVisitor = activeEntityVisitor;
	}

	/**
	 * @return the disabledComposite
	 */
	public Composite getDisabledComposite() {
		return disabledComposite;
	}

	/**
	 * @param disabledComposite the disabledComposite to set
	 */
	public void setDisabledComposite(Composite disabledComposite) {
		this.disabledComposite = disabledComposite;
	}

	/**
	 * @return the plainStyle
	 */
	public Style getPlainStyle() {
		return plainStyle;
	}

	/**
	 * @param plainStyle the plainStyle to set
	 */
	public void setPlainStyle(Style plainStyle) {
		this.plainStyle = plainStyle;
	}

	/**
	 * @return the boldStyle
	 */
	public Style getBoldStyle() {
		return boldStyle;
	}

	/**
	 * @param boldStyle the boldStyle to set
	 */
	public void setBoldStyle(Style boldStyle) {
		this.boldStyle = boldStyle;
	}

	/**
	 * @return the nonBoldStyle
	 */
	public Style getNonBoldStyle() {
		return nonBoldStyle;
	}

	/**
	 * @param nonBoldStyle the nonBoldStyle to set
	 */
	public void setNonBoldStyle(Style nonBoldStyle) {
		this.nonBoldStyle = nonBoldStyle;
	}

	/**
	 * @return the selectionForeground
	 */
	public Style getSelectionForeground() {
		return selectionForeground;
	}

	/**
	 * @param selectionForeground the selectionForeground to set
	 */
	public void setSelectionForeground(Style selectionForeground) {
		this.selectionForeground = selectionForeground;
	}

	/**
	 * @return the foreground
	 */
	public Style getForeground() {
		return foreground;
	}

	/**
	 * @param foreground the foreground to set
	 */
	public void setForeground(Style foreground) {
		this.foreground = foreground;
	}

	/**
	 * @return the linkStyle
	 */
	public Style getLinkStyle() {
		return linkStyle;
	}

	/**
	 * @param linkStyle the linkStyle to set
	 */
	public void setLinkStyle(Style linkStyle) {
		this.linkStyle = linkStyle;
	}

	/**
	 * @return the inconsistentClassStyle
	 */
	public Style getInconsistentClassStyle() {
		return inconsistentClassStyle;
	}

	/**
	 * @param inconsistentClassStyle the inconsistentClassStyle to set
	 */
	public void setInconsistentClassStyle(Style inconsistentClassStyle) {
		this.inconsistentClassStyle = inconsistentClassStyle;
	}

	/**
	 * @return the focusedEntityStyle
	 */
	public Style getFocusedEntityStyle() {
		return focusedEntityStyle;
	}

	/**
	 * @param focusedEntityStyle the focusedEntityStyle to set
	 */
	public void setFocusedEntityStyle(Style focusedEntityStyle) {
		this.focusedEntityStyle = focusedEntityStyle;
	}

	/**
	 * @return the ontologyURIStyle
	 */
	public Style getOntologyURIStyle() {
		return ontologyURIStyle;
	}

	/**
	 * @param ontologyURIStyle the ontologyURIStyle to set
	 */
	public void setOntologyURIStyle(Style ontologyURIStyle) {
		this.ontologyURIStyle = ontologyURIStyle;
	}

	/**
	 * @return the commentedOutStyle
	 */
	public Style getCommentedOutStyle() {
		return commentedOutStyle;
	}

	/**
	 * @param commentedOutStyle the commentedOutStyle to set
	 */
	public void setCommentedOutStyle(Style commentedOutStyle) {
		this.commentedOutStyle = commentedOutStyle;
	}

	/**
	 * @return the strikeOutStyle
	 */
	public Style getStrikeOutStyle() {
		return strikeOutStyle;
	}

	/**
	 * @param strikeOutStyle the strikeOutStyle to set
	 */
	public void setStrikeOutStyle(Style strikeOutStyle) {
		this.strikeOutStyle = strikeOutStyle;
	}

	/**
	 * @return the fontSizeStyle
	 */
	public Style getFontSizeStyle() {
		return fontSizeStyle;
	}

	/**
	 * @param fontSizeStyle the fontSizeStyle to set
	 */
	public void setFontSizeStyle(Style fontSizeStyle) {
		this.fontSizeStyle = fontSizeStyle;
	}

	/**
	 * @return the annotURIRendered
	 */
	public boolean isAnnotURIRendered() {
		return annotURIRendered;
	}

	/**
	 * @param annotURIRendered the annotURIRendered to set
	 */
	public void setAnnotURIRendered(boolean annotURIRendered) {
		this.annotURIRendered = annotURIRendered;
	}

	/**
	 * @return the linkRendered
	 */
	public boolean isLinkRendered() {
		return linkRendered;
	}

	/**
	 * @param linkRendered the linkRendered to set
	 */
	public void setLinkRendered(boolean linkRendered) {
		this.linkRendered = linkRendered;
	}

	/**
	 * @return the parenthesisRendered
	 */
	public boolean isParenthesisRendered() {
		return parenthesisRendered;
	}

	/**
	 * @param parenthesisRendered the parenthesisRendered to set
	 */
	public void setParenthesisRendered(boolean parenthesisRendered) {
		this.parenthesisRendered = parenthesisRendered;
	}

	/**
	 * @return the forceReadOnlyRendering
	 */
	public boolean isForceReadOnlyRendering() {
		return forceReadOnlyRendering;
	}

	/**
	 * @return the strikeThrough
	 */
	public boolean isStrikeThrough() {
		return strikeThrough;
	}

	/**
	 * @return the ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * @return the equivalentObjects
	 */
	public Set<OWLObject> getEquivalentObjects() {
		return equivalentObjects;
	}

	/**
	 * @return the iconObject
	 */
	public OWLObject getIconObject() {
		return iconObject;
	}

	/**
	 * @return the focusedEntity
	 */
	public OWLEntity getFocusedEntity() {
		return focusedEntity;
	}

	/**
	 * @return the commentedOut
	 */
	public boolean isCommentedOut() {
		return commentedOut;
	}

	/**
	 * @return the inferred
	 */
	public boolean isInferred() {
		return inferred;
	}

	/**
	 * @return the highlightKeywords
	 */
	public boolean isHighlightKeywords() {
		return highlightKeywords;
	}

	/**
	 * @return the highlightUnsatisfiableClasses
	 */
	public boolean isHighlightUnsatisfiableClasses() {
		return highlightUnsatisfiableClasses;
	}

	/**
	 * @return the highlightUnsatisfiableProperties
	 */
	public boolean isHighlightUnsatisfiableProperties() {
		return highlightUnsatisfiableProperties;
	}

	/**
	 * @return the crossedOutEntities
	 */
	public Set<OWLEntity> getCrossedOutEntities() {
		return crossedOutEntities;
	}

	/**
	 * @return the unsatisfiableNames
	 */
	public Set<String> getUnsatisfiableNames() {
		return unsatisfiableNames;
	}

	/**
	 * @return the opaque
	 */
	public boolean isOpaque() {
		return opaque;
	}

	/**
	 * @param renderIcon the renderIcon to set
	 */
	public void setRenderIcon(boolean renderIcon) {
		this.renderIcon = renderIcon;
	}

	/**
	 * @param renderExpression the renderExpression to set
	 */
	public void setRenderExpression(boolean renderExpression) {
		this.renderExpression = renderExpression;
	}
}
