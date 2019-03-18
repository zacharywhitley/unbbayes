package unbbayes.datamining.gui.preprocessor;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.discretize.DiscretizationWrapper;
import unbbayes.datamining.discretize.IDiscretization;
import unbbayes.datamining.discretize.sample.ISampler;
import unbbayes.datamining.discretize.sample.TriangularDistributionSampler;
import unbbayes.datamining.discretize.sample.UniformDistributionSampler;

/**
 * @author Shou Matsumoto
 *
 */
public class SamplingPanel extends JPanel {

	private static final long serialVersionUID = 2919071747185755577L;
	
	@SuppressWarnings("rawtypes")
	private JComboBox discretizationTypeComboBox;
	private InstanceSet instanceSet;
	private JTextField prefixTextField;
	private JTextField splitterTextField;
	private JTextField suffixTextField;

	/**
	 * Default constructor initializing field
	 */
	public SamplingPanel(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		this.initComponents();
	}


	/**
	 * Initializes and populates components
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void initComponents() {
		
		this.setLayout(new GridLayout(4,2,5,5));
		
		
		JPanel samplerTypeLabelPanel = new JPanel(new BorderLayout());
		JLabel discretizationTypeLabel = new JLabel("Discretization Type :");
		samplerTypeLabelPanel.add(discretizationTypeLabel,BorderLayout.CENTER);
		
		JPanel samplerTypePanel = new JPanel(new BorderLayout());
		discretizationTypeComboBox = new JComboBox();
		samplerTypePanel.add(discretizationTypeComboBox, BorderLayout.CENTER);
		
		this.add(samplerTypeLabelPanel);
		this.add(samplerTypePanel);
		
		// add samplers
		TriangularDistributionSampler defaultSampler = new TriangularDistributionSampler(getInstanceSet());
		DiscretizationWrapper defaultSamplerWrapper = new DiscretizationWrapper(defaultSampler);
		discretizationTypeComboBox.addItem(defaultSamplerWrapper);
		
		// more samplers can be added like following line
		discretizationTypeComboBox.addItem(new DiscretizationWrapper(new UniformDistributionSampler(getInstanceSet())));
		
		
		
		discretizationTypeComboBox.setSelectedItem(defaultSamplerWrapper);

		JPanel prefixLabelPanel = new JPanel(new BorderLayout());
		prefixLabelPanel.add(new JLabel("Prefix of states :"), BorderLayout.CENTER);
		
		JPanel prefixPanel = new JPanel(new BorderLayout());
		prefixTextField = new JTextField(defaultSampler.getPrefix(), 5);
		prefixPanel.add(prefixTextField,BorderLayout.CENTER);
		
		this.add(prefixLabelPanel);
		this.add(prefixPanel);
		
		JPanel splitterLabelPanel = new JPanel(new BorderLayout());
		splitterLabelPanel.add(new JLabel("Splitter of states :"), BorderLayout.CENTER);
		
		JPanel splitterPanel = new JPanel(new BorderLayout());
		splitterTextField = new JTextField(defaultSampler.getSplitter(), 5);
		splitterPanel.add(splitterTextField,BorderLayout.CENTER);
		
		this.add(splitterLabelPanel);
		this.add(splitterPanel);
		
		JPanel suffixLabelPanel = new JPanel(new BorderLayout());
		suffixLabelPanel.add(new JLabel("Suffix of states :"), BorderLayout.CENTER);
		
		JPanel suffixPanel = new JPanel(new BorderLayout());
		suffixTextField = new JTextField(defaultSampler.getSuffix(), 5);
		suffixPanel.add(suffixTextField,BorderLayout.CENTER);
		
		this.add(suffixLabelPanel);
		this.add(suffixPanel);
		
	}
	
	/**
	 * @return
	 * The sampler selected by this panel.
	 * Sampler uses same interface of {@link ISampler}
	 */
	public ISampler getSelectedSampler() {
		Object item = discretizationTypeComboBox.getSelectedItem();
		if (item == null 
				|| !(item instanceof DiscretizationWrapper) ) {
			throw new IllegalStateException("Invalid option: " + item);
		}
		DiscretizationWrapper wrapper = (DiscretizationWrapper) item;
		IDiscretization wrapped = wrapper.getWrapped();
		if (wrapped == null
				|| !(wrapped instanceof ISampler) ) {
			throw new IllegalStateException("Not a sampler: " + wrapped);
		}
		return (ISampler) wrapped;
	}


	/**
	 * @return the discretizationTypeComboBox
	 */
	public JComboBox getDiscretizationTypeComboBox() {
		return discretizationTypeComboBox;
	}


	/**
	 * @param discretizationTypeComboBox the discretizationTypeComboBox to set
	 */
	public void setDiscretizationTypeComboBox(JComboBox discretizationTypeComboBox) {
		this.discretizationTypeComboBox = discretizationTypeComboBox;
	}


	/**
	 * @return the instanceSet
	 */
	public InstanceSet getInstanceSet() {
		return instanceSet;
	}


	/**
	 * @param instanceSet the instanceSet to set
	 */
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}


	/**
	 * @return the prefixTextField
	 */
	public JTextField getPrefixTextField() {
		return prefixTextField;
	}


	/**
	 * @param prefixTextField the prefixTextField to set
	 */
	public void setPrefixTextField(JTextField prefixTextField) {
		this.prefixTextField = prefixTextField;
	}


	/**
	 * @return the splitterTextField
	 */
	public JTextField getSplitterTextField() {
		return splitterTextField;
	}


	/**
	 * @param splitterTextField the splitterTextField to set
	 */
	public void setSplitterTextField(JTextField splitterTextField) {
		this.splitterTextField = splitterTextField;
	}


	/**
	 * @return the suffixTextField
	 */
	public JTextField getSuffixTextField() {
		return suffixTextField;
	}


	/**
	 * @param suffixTextField the suffixTextField to set
	 */
	public void setSuffixTextField(JTextField suffixTextField) {
		this.suffixTextField = suffixTextField;
	}
}
