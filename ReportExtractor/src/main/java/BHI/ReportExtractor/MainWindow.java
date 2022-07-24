package BHI.ReportExtractor;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import com.opencsv.exceptions.CsvValidationException;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;  

/**
 * @author nicholassunderland
 *
 * GUI class
 *
 */
public class MainWindow {

	/**
	 * Variables
	 */
	private JFrame frame;
	private JTextField lineEdit_file_path;
	private File selectedFile;
	private Data data;

	/**
	 * Create the application.
	 */
	public MainWindow(Data data) {
		this.data = data;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		
		JToolBar toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.NORTH, toolBar, 0, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, toolBar, 450, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(toolBar);
		
		JLabel label_report = new JLabel("Report");
		springLayout.putConstraint(SpringLayout.NORTH, label_report, 6, SpringLayout.SOUTH, toolBar);
		springLayout.putConstraint(SpringLayout.WEST, label_report, 10, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(label_report);
		
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 28, SpringLayout.SOUTH, toolBar);
		springLayout.putConstraint(SpringLayout.WEST, panel, 199, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel, -10, SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, -104, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(panel);
		
		JSpinner spinBox_report_counter = new JSpinner();
		
		JButton pushButton_file_dialog = new JButton("...");
		pushButton_file_dialog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("src/main/resources/data"));
				int result = fileChooser.showOpenDialog(null);
				
				if (result == JFileChooser.APPROVE_OPTION) {
					
					// Try to init an iterator for the input .csv file
				    try {
				    	
				    	// Eventually move this to the load up screen where we decide what kind of report it is
				    	data.init("echocardiogram");
				    	//
				    	
						data.initCsvDataIterator(fileChooser.getSelectedFile().getAbsolutePath());
						selectedFile = fileChooser.getSelectedFile();
					    lineEdit_file_path.setText(selectedFile.getAbsolutePath());
					} catch (Exception e1) {
						int response = JOptionPane.showConfirmDialog(
				    			null,
				    			"Could not parse input .csv file. \nPlease check it is in the correct format", 
				    			"Error...",
				    			JOptionPane.DEFAULT_OPTION);
						e1.printStackTrace();
						return;
					}
				}
			}
		}); // see below for lineEdit_file_path instatiation and testing set path....
		
		springLayout.putConstraint(SpringLayout.EAST, pushButton_file_dialog, -10, SpringLayout.EAST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, spinBox_report_counter, 0, SpringLayout.WEST, pushButton_file_dialog);
		
		JTextPane textPane_report_text = new JTextPane();
		springLayout.putConstraint(SpringLayout.NORTH, textPane_report_text, 6, SpringLayout.SOUTH, label_report);
		springLayout.putConstraint(SpringLayout.WEST, textPane_report_text, 16, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, textPane_report_text, 0, SpringLayout.SOUTH, panel);
		
		lineEdit_file_path = new JTextField();
		lineEdit_file_path.setColumns(10);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel.add(spinBox_report_counter);
		panel.add(pushButton_file_dialog);
		panel.add(lineEdit_file_path);
		
		// just for testing
		//selectedFile = new File("src/main/resources/data/test_report_doc.csv");
		//lineEdit_file_path.setText("src/main/resources/data/test_report_doc.csv");
		// just for testing
		
		JButton pushButton_load_reports = new JButton("Load");
		pushButton_load_reports.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    
			    //Create a results file and ask if you want to overwrite if it already exists
			    String output_filepath = FilenameUtils.removeExtension(selectedFile.getAbsolutePath()) + "_results.csv";
			    File f = new File(output_filepath);
			    if(f.exists() && !f.isDirectory()) { 
			    	int response = JOptionPane.showConfirmDialog(
			    			null,
			    			"Overwrite existing results?", 
			    			"Select an option...",
			    			JOptionPane.YES_NO_CANCEL_OPTION);
			    	
			    	if (response != JOptionPane.YES_OPTION){
				    	return;
				    }
			    }
			    
			    // Create / overwrite the file
			    try {
					data.initCsvWriter(output_filepath);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				// Extract the data to the results file
				data.extractData();
				
			}
		});
		panel.add(pushButton_load_reports);
		
		JLabel label = new JLabel("");
		panel.add(label);
		springLayout.putConstraint(SpringLayout.EAST, textPane_report_text, 120, SpringLayout.EAST, label_report);
		frame.getContentPane().add(textPane_report_text);
		frame.setVisible(true);
	}
}
