package com.anysoftkeyboard.dictionarycreator;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainForm extends JFrame implements UI {

	private ParserThread mThread;
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel2 = null;
	private JTextField jTextFieldSourceFilename = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JTextField jTextFieldSpellDictionaryFilename = null;
	private JTextField jTextFieldOutputfilename = null;
	private JButton jButtonStart = null;
	private JLabel jLabelProgress = null;
	private JLabel jLabel1 = null;
	private JTextField jTextFieldPossibleLetters = null;
	private JLabel jLabel5 = null;
	private JTextField jTextFieldInnerCharacters = null;
	private JLabel jLabelMaxWords = null;
	private JSlider jSliderWordsCount = null;
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.X_AXIS));
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridheight = 1;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints11.gridy = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.NORTH;
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.gridy = 0;
		}
		return jPanel;
	}

	/**
	 * This method initializes jTextFieldSourceFilename	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldSourceFilename() {
		if (jTextFieldSourceFilename == null) {
			jTextFieldSourceFilename = new JTextField();
		}
		return jTextFieldSourceFilename;
	}

	/**
	 * This method initializes jTextFieldSpellDictionaryFilename	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldSpellDictionaryFilename() {
		if (jTextFieldSpellDictionaryFilename == null) {
			jTextFieldSpellDictionaryFilename = new JTextField();
		}
		return jTextFieldSpellDictionaryFilename;
	}

	/**
	 * This method initializes jTextFieldOutputfilename	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldOutputfilename() {
		if (jTextFieldOutputfilename == null) {
			jTextFieldOutputfilename = new JTextField();
		}
		return jTextFieldOutputfilename;
	}

	/**
	 * This method initializes jButtonStart	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonStart() {
		if (jButtonStart == null) {
			jButtonStart = new JButton();
			jButtonStart.setText("Start parsing");
			jButtonStart.setEnabled(true);
			jButtonStart.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try
					{
						FileInputStream sourceFile = new FileInputStream(new File(jTextFieldSourceFilename.getText()));
						InputStreamReader input = new InputStreamReader(sourceFile, "UTF-8");
						
						FileInputStream dictFile = new FileInputStream(new File(jTextFieldSpellDictionaryFilename.getText()));
						InputStreamReader dictInput = new InputStreamReader(dictFile, "UTF-8");
						
						FileOutputStream outputFile = new FileOutputStream(new File(jTextFieldOutputfilename.getText()));
						OutputStreamWriter output = new OutputStreamWriter(outputFile, "UTF-8");
						mThread = new ParserThread(input, sourceFile.available(), dictInput, dictFile.available(), output, MainForm.this, jTextFieldPossibleLetters.getText(), jTextFieldInnerCharacters.getText(), 
								jSliderWordsCount.getValue());
						jButtonStart.setEnabled(false);
						jLabelProgress.setText("Starting parser thread");
						mThread.start();
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						showAlertDialog("Error", "Failed to start parser: "+ex.getMessage(), true);
					}
				}
			});
		}
		return jButtonStart;
	}
	
	private void showAlertDialog(String title, String message, boolean error)
	{
		JOptionPane.showMessageDialog(null, message, title, error? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * This method initializes jTextFieldPossibleLetters	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldPossibleLetters() {
		if (jTextFieldPossibleLetters == null) {
			jTextFieldPossibleLetters = new JTextField();
			jTextFieldPossibleLetters.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
		}
		return jTextFieldPossibleLetters;
	}

	/**
	 * This method initializes jTextFieldInnerCharacters	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldInnerCharacters() {
		if (jTextFieldInnerCharacters == null) {
			jTextFieldInnerCharacters = new JTextField();
			jTextFieldInnerCharacters.setText("'");
		}
		return jTextFieldInnerCharacters;
	}

	/**
	 * This method initializes jSliderWordsCount	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getJSliderWordsCount() {
		if (jSliderWordsCount == null) {
			jSliderWordsCount = new JSlider();
			jSliderWordsCount.setMinimum(5000);
			jSliderWordsCount.setMaximum(150000);
			jSliderWordsCount.setValue(50000);
			jSliderWordsCount.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					jLabelMaxWords.setText("Max words: ("+jSliderWordsCount.getValue()+")");
				}
			});
			jLabelMaxWords.setText("Max words: ("+jSliderWordsCount.getValue()+")");
		}
		return jSliderWordsCount;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainForm thisClass = new MainForm();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public MainForm() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(593, 388);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon_8_key.9.png")));
		this.setContentPane(getJContentPane());
		this.setTitle("AnySoftKeyboard Dictionary Creator");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabelMaxWords = new JLabel();
			jLabelMaxWords.setText("Maximum words");
			jLabel5 = new JLabel();
			jLabel5.setText("Additional characters which are possible only inside a word:");
			jLabel1 = new JLabel();
			jLabel1.setText("Possible characters (lower and upper cases):");
			jLabelProgress = new JLabel();
			jLabelProgress.setText(".");
			jLabel4 = new JLabel();
			jLabel4.setText("Select output file:");
			jLabel3 = new JLabel();
			jLabel3.setText("Select spell file:");
			jLabel2 = new JLabel();
			jLabel2.setText("Select source text file:");
			jLabel = new JLabel();
			jLabel.setText("Welcome to AnySoftKeyboard Dictionary Creator");
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
			jContentPane.add(getJPanel(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getJTextFieldSourceFilename(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getJTextFieldSpellDictionaryFilename(), null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(getJTextFieldOutputfilename(), null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJTextFieldPossibleLetters(), null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJTextFieldInnerCharacters(), null);
			jContentPane.add(jLabelMaxWords, null);
			jContentPane.add(getJSliderWordsCount(), null);
			jContentPane.add(getJButtonStart(), null);
			jContentPane.add(jLabelProgress, null);
		}
		return jContentPane;
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateProgressState(String message, int precentage) {
		jLabelProgress.setText(message+" "+precentage+"%");
	}

	@Override
	public void onEnded() {
		jButtonStart.setEnabled(true);
	}

}  //  @jve:decl-index=0:visual-constraint="64,28"
