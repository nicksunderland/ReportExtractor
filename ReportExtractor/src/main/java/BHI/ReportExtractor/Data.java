package BHI.ReportExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.BasicConfigurator;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CreoleRegister;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.annotation.AnnotationSetImpl;
import gate.creole.ExecutionException;
import gate.creole.Plugin;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;


/**
 * @author nicholassunderland
 *
 * This class handles the higher level processing of the documents through the SerialAnalyserController pipeline
 *
 */
public class Data {

	// Class fields
	public String examination_type; // the type of report we will be processing
	private VariableDictionary variable_dictionary; // the collection of variables for the examination type and associated information about them
	private Iterator<Record> records_it; // an iterator of the reports we are going to process
	private LinkedHashMap<String, ResultUnitPair<Object, String>> results; // a map of the variable names and results
	public SerialAnalyserController pipeline;
	private CSVWriter csv_writer;

	
	/**
	 * Data class constructor
	 */
	public Data() {
		
	}
	
	
	/**
	 * Init must be called with the appropriate examination type before doing anything else
	 * 
	 * @param examination_type
	 */
	public void init(String examination_type) {
		this.examination_type = examination_type;
		this.variable_dictionary = new VariableDictionary(examination_type);
		this.initialiseResultsMap();
		this.initialiseLanguageAnalyser();
	}
		
	

	
	/**
	 * @param examination_type	currently either "echocardiogram" or "cardiac_mri"
	 */
	private void initialiseResultsMap() {
		
		if(this.results!=null) { // i.e. already exists and we are just setting everything back to null

			this.results.forEach((key, entry) -> {
				this.results.put(key, entry.clear());
			});
			
	
		}else { // Populate the variables from the data dictionary; initialise the results to 'null'
			
			this.results = new LinkedHashMap<String, ResultUnitPair<Object, String>>();
			
			Iterator<String> var_names_it = this.variable_dictionary.getVariableNameSet().iterator();
			
			while (var_names_it.hasNext()) {
				this.results.put(var_names_it.next(), new ResultUnitPair<Object, String>(null, null));
			}
		}
	}
	
	
	/**
	 * Extract the echo data
	 */
	public void extractData() {
		this.extractData(true);
	}
	public void extractDataTest() {
		this.extractData(false);
	}
	public void extractData(boolean to_csv) {
		
		try {
			// Ensure there are records
			if(!this.records_it.hasNext()) {
				System.err.println("*** No records available for processing ***");
				return;
			}
			
			int counter=0;
			if(to_csv) {
				// Set the column names for the output .csv
				this.writeCsvHeader(this.results.keySet());
				PrintColour.print("Processing...", PrintColour.GREEN_BRIGHT);
			}
			
			// Cycle the records and extract the data to the hashmap
			while(this.records_it.hasNext()) {

				if(to_csv) {
					if((counter+1) % 20 == 1) {
						PrintColour.print("\nreport: ", PrintColour.WHITE);
						PrintColour.print(Integer.toString(++counter) + ", ", PrintColour.WHITE_BRIGHT);
					}else {
						PrintColour.print(Integer.toString(++counter) + ", ", PrintColour.WHITE_BRIGHT);
					}
				}
				
				//Clear the result hash map
				this.initialiseResultsMap();
				
				Record record = this.records_it.next();
				String report_text = "TEXT_START-->\n" + record.getReportText() + "\n<--TEXT_END"; //to make sure there are tokens at each end incase we need to mmatch Tokens prior or just after a pattern at the beginning or end 
					
				//Create the corpus and document
				Document doc = Factory.newDocument(report_text);
				Corpus corpus = Factory.newCorpus("BatchProcessApp Corpus");			
				corpus.add(doc);
				
				//Set the corpus into the pipeline and execute
				pipeline.setCorpus(corpus);
				pipeline.execute();
				
				//Get all of the annotations found, and then get only those variables in our variable dictionary (this dictionary will be different for different examination types)
				AnnotationSet annotation_set = doc.getAnnotations().get( variable_dictionary.getVariableNameSet() );
				List<Annotation> ordered_annots = annotation_set.inDocumentOrder();
				
				// Cycle each annotation
				for (Annotation anno : ordered_annots) {
					
					// Extract the variable type (e.g. lv_ivs), value (e.g. 1.2) and unit (e.g. cm)
					String var_name  = (String) anno.getType();
					String value     = (String) anno.getFeatures().get("value");
					String unit      = (String) anno.getFeatures().get("unit");
					
			    	ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(value, unit); 	
			    	
			    	// See if this annotation is a better fit, based on the variable dictionary settings
					if( this.variable_dictionary.isBetterMatch(var_name, this.results.get(var_name), result) ) {
						this.results.put(var_name, result);
					}
				}
			
				if(to_csv) {
					// write the result to the next line
					this.writeCsvResults(record.getId(), this.results.values());	
				}
				
				//Clear up so that we don't overun the memory
				corpus.clear();
				Factory.deleteResource(doc);
			}
			
			if(to_csv) {
				// Close the output
				this.csv_writer.close();
				PrintColour.println("\n... processing complete", PrintColour.GREEN_BRIGHT);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}	

	
	
	
	/**
	 * Function to initialise all of the GATE language processing settings
	 */
	public void initialiseLanguageAnalyser(){
		try {
			
			//Logging
			BasicConfigurator.configure();
			
			//Init GATE
			Gate.init();
			
			//Register the CREOLE plugins from the creole.xml file in /resources
			CreoleRegister creole_register = Gate.getCreoleRegister();
			File resources_directory = new File("src/main/resources/language_resources"); 
			creole_register.registerPlugin(new Plugin.Directory(resources_directory.toURI().toURL()));
			

			// Processing resources configs
			FeatureMap gazetteer_common_units_config = gate.Utils.featureMap("listsURL",      this.getClass().getClassLoader().getResource("language_resources/gazetteers/measurement_units/measurement_units_lists.def"), 
                                                                             "caseSensitive", "False");
			FeatureMap gazetteer_main_config         = gate.Utils.featureMap("listsURL",      this.getClass().getClassLoader().getResource("language_resources/gazetteers/" + this.examination_type + "/" + this.examination_type + "_lists.def"), 
																			 "caseSensitive", "True", 
																			 "longestMatchOnly", "False");
			FeatureMap jape_config                   = gate.Utils.featureMap("grammarURL",    this.getClass().getClassLoader().getResource("language_resources/jape/" + this.examination_type + "/main_" + this.examination_type + ".jape"));
			
			this.createNonSplitPatterns(); //this directly alters the text file at the nonSplitListURL (the result can be found in the "target" folder once you've build the project)
			FeatureMap regex_splitter_config = gate.Utils.featureMap("externalSplitListURL", this.getClass().getClassLoader().getResource("language_resources/regex_sentence_splitter/external-split-patterns.txt"), 
																	 "internalSplitListURL", this.getClass().getClassLoader().getResource("language_resources/regex_sentence_splitter/internal-split-patterns.txt"),
																	 "nonSplitListURL",      this.getClass().getClassLoader().getResource("language_resources/regex_sentence_splitter/non-split-patterns.txt"));
			
			// Processing resources
			LanguageAnalyser clean_up  = (LanguageAnalyser)Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR");
			LanguageAnalyser tokeniser = (LanguageAnalyser)Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
			LanguageAnalyser jape_plus = (LanguageAnalyser)Factory.createResource("gate.jape.plus.Transducer", jape_config);
			LanguageAnalyser regex_sent_splitter    = (LanguageAnalyser)Factory.createResource("gate.creole.splitter.RegexSentenceSplitter", regex_splitter_config);
			LanguageAnalyser gazetteer_common_units = (LanguageAnalyser)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", gazetteer_common_units_config);
			LanguageAnalyser gazetteer_specific     = (LanguageAnalyser)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", gazetteer_main_config);
			
			// Build the pipeline - this will be run over the document in this order
			pipeline = (SerialAnalyserController)Factory.createResource("gate.creole.SerialAnalyserController");
			pipeline.add(clean_up);
			pipeline.add(tokeniser);
			pipeline.add(gazetteer_common_units);
			pipeline.add(gazetteer_specific);
			pipeline.add(regex_sent_splitter);
			pipeline.add(jape_plus);	
			
				
		} catch (GateException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Function to import csv data into "Record" objects. This will throw an exception if the 
	 * .csv input column names don't match the "Record" constructor (e.g. id, datetime, report_text)
	 * 
	 * @param file_path
	 * @throws IOException
	 * @throws CsvValidationException
	 */
	public void initCsvDataIterator(String file_path) throws IOException, CsvValidationException {
		
        this.records_it = new CsvToBeanBuilder<Record>(new BufferedReader(new FileReader(file_path)))
                .withType(Record.class)
                .build()
                .iterator();
        
	}
	
	/**
	 * Function to init this data objects file writer
	 * 
	 * @param file_path
	 * @throws IOException
	 */
	public void initCsvWriter(String file_path) throws IOException {
		
        File file = new File(file_path);
        BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
        this.csv_writer = new CSVWriter(out);
	}
	
	/**
	 * Function to manually set the iterator
	 * 
	 * @param Record Iterator
	 */
	public void initCsvDataIterator(Iterator<Record> record_it) {
		
        this.records_it = record_it;
	}
	
	/**
	 * Function to write a line of data to this object's csv_writer
	 * 
	 * @param Collection object
	 * 
	 */
	private void writeCsvHeader(Collection<String> hashmap_keys) {
		
		String[] id = {"id"};
		this.csv_writer.writeNext(ArrayUtils.addAll(id, hashmap_keys.toArray(String[]::new)));

	}
	
	private void writeCsvResults(String record_id, Collection<ResultUnitPair<Object,String>> hashmap_values) {
		
		ArrayList<String> output_line = new ArrayList<String>();
		for(ResultUnitPair<Object,String> res : hashmap_values) {
			output_line.add(res.getValueAsString());
		}
		String[] id = {record_id};
		this.csv_writer.writeNext(ArrayUtils.addAll(id, output_line.toArray(String[]::new)));
	}
	
	/**
	 * Function to clear the results manually (only used for unit testing currently)
	 * separate function name just for clarity
	 */
	public void clearResults(){
		this.initialiseResultsMap();
	}
	
	/**
	 * Print this object's results to the console
	 */
	public void printResults(){
		System.out.println("\n------------------------------");
		for (Map.Entry<String, ResultUnitPair<Object, String>> entry : results.entrySet()){
			if(entry.getValue() != null) {
				PrintColour.white(entry.getKey() + " = " + entry.getValue().toString());
			}else {
				PrintColour.red(entry.getKey() + " = " + entry.getValue());
			}
		}
	}
	
	
	/**
	 * Gets the result(s)
	 * 
	 * @param result_key
	 * @return
	 */
	public ResultUnitPair<Object, String> getResults(String result_key){
		return(this.results.get(result_key));
	}
	
	
	/**
	 * Only used by the testAnnotationRefinement unit test
	 * Not for general use
	 * @return All of the annotations for the first document loaded into this Data object
	 * @throws ResourceInstantiationException
	 * @throws ExecutionException
	 */
	public AnnotationSet extractAllDocumentAnnotations() throws ResourceInstantiationException, ExecutionException {
		
		Record record = this.records_it.next();
		String report_text = "TEXT_START-->\n" + record.getReportText() + "\n<--TEXT_END"; //to make sure there are tokens at each end incase we need to mmatch Tokens prior or just after a pattern at the beginning or end 
				
		//Create the corpus and document
		Document doc = Factory.newDocument(report_text);
		Corpus corpus = Factory.newCorpus("BatchProcessApp Corpus");			
		corpus.add(doc);
		
		//Set the corpus into the pipeline and execute
		pipeline.setCorpus(corpus);
		pipeline.execute();
			
		return( doc.getAnnotations() );
	}	
	
	
	/**
	 * Function to update the NonSplitPatterns for the regex sentence splitter based on
	 * whatever is in the gazetteer lists. This looks for phrases that contain "." and 
	 * adds them to the ignore list, so that we don't get sentence splits on things such as
	 * "Asc. Ao"
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void createNonSplitPatterns() {		

		String gaz_folder_path_study   = this.getClass().getClassLoader().getResource("language_resources/gazetteers/" + this.examination_type).getPath();
		String gaz_folder_path_general = this.getClass().getClassLoader().getResource("language_resources/gazetteers/general").getPath();
		
		try{
			
			Stream<Path> walk = Stream.concat(Files.walk(Paths.get(gaz_folder_path_general)), 
											  Files.walk(Paths.get(gaz_folder_path_study)));
			
			List<Path> gazetteer_file_paths = walk.filter(Files::isRegularFile)
									              .filter(p -> p.getFileName().toString().endsWith(".lst"))
									              .collect(Collectors.toList());
			
			
			String non_splitter_text_file = 
					  "//These are patterns for things that might look like sentence splits but they\n"
					+ "//should not be used as such.\n"
					+ "//\n"
					+ "// Valentin Tablan, 24 Aug 2007\n"
					+ "//\n"
					+ "//\n"
					+ "// Lines starting with // are comments; empty lines are ignored\n"
					+ "\n"
					+ "//The Java RegEx matching machine is eager to return the first match\n"
					+ "//because of this, the explicit abbreviations need to appear before the\n"
					+ "//generic patterns so that for example \"a.m.\" is matched in preference to\n"
					+ "//\"a.m\" (which would match under the internet address rule). \n"
					+ "\n"
					+ "\n"
					+ "//known abbreviations\n"
					+ "\\bAsc\\.\n"
					+ "\\b\\.net\n"
					+ "\\b\\.NET\n"
					+ "\\b\\.Net\n"
					+ "\\bAG\\.\n"
					+ "\\bA\\.M\\.\n"
					+ "\\bAPR\\.\n"
					+ "\\bAUG\\.\n"
					+ "\\bAdm\\.\n"
					+ "\\bBrig\\.\n"
					+ "\\bCO\\.\n"
					+ "\\bCORP\\.\n"
					+ "\\bCapt\\.\n"
					+ "\\bCmdr\\.\n"
					+ "\\bCo\\.\n"
					+ "\\bCol\\.\n"
					+ "\\bComdr\\.\n"
					+ "\\bDEC\\.\n"
					+ "\\bDR\\.\n"
					+ "\\bDr\\.\n"
					+ "\\bFEB\\.\n"
					+ "\\bFig\\.\n"
					+ "\\bFRI\\.\n"
					+ "\\bGMBH\\.\n"
					+ "\\bGen\\.\n"
					+ "\\bGov\\.\n"
					+ "\\bINC\\.\n"
					+ "\\bJAN\\.\n"
					+ "\\bJUL\\.\n"
					+ "\\bJUN\\.\n"
					+ "\\bLTD\\.\n"
					+ "\\bLt\\.\n"
					+ "\\bLtd\\.\n"
					+ "\\bMAR\\.\n"
					+ "\\bMON\\.\n"
					+ "\\bMP\\.\n"
					+ "\\bMaj\\.\n"
					+ "\\bMr\\.\n"
					+ "\\bMrs\\.\n"
					+ "\\bMs\\.\n"
					+ "\\bNA\\.\n"
					+ "\\bNOV\\.\n"
					+ "\\bNV\\.\n"
					+ "\\bOCT\\.\n"
					+ "\\bOy\\.\n"
					+ "\\bPLC\\.\n"
					+ "\\bP\\.M\\.\n"
					+ "\\bProf\\.\n"
					+ "\\bRep\\.\n"
					+ "\\bSA\\.\n"
					+ "\\bSAT\\.\n"
					+ "\\bSEP\\.\n"
					+ "\\bSIR\\.\n"
					+ "\\bSR\\.\n"
					+ "\\bSUN\\.\n"
					+ "\\bSen\\.\n"
					+ "\\bSgt\\.\n"
					+ "\\bSpA\\.\n"
					+ "\\bSt\\.\n"
					+ "\\bTHU\\.\n"
					+ "\\bTHUR\\.\n"
					+ "\\bTUE\\.\n"
					+ "\\bVP\\.\n"
					+ "\\bWED\\.\n"
					+ "\\ba\\.m\\.\n"
					+ "\\bad\\.\n"
					+ "\\bal\\.\n"
					+ "\\bed\\.\n"
					+ "\\beds\\.\n"
					+ "\\beg\\.\n"
					+ "\\be\\.g\\.\n"
					+ "\\bet\\.\n"
					+ "\\betc\\.(?!\\s+\\p{Upper}) \n"
					+ "\\bfig\\.\n"
					+ "\\bie\\.\n"
					+ "\\bi\\.e\\.\n"
					+ "\\bp\\.\n"
					+ "\\bp\\.m\\.\n"
					+ "\\busu\\.\n"
					+ "\\bvs\\.\n"
					+ "\\byr\\.\n"
					+ "\\byrs\\.\n"
					+ "\n"
					+ "//four or more dots are ignored\n"
					+ "\\.{4,}\n"
					+ "\n"
					+ "//five or more ?,! are ignored\n"
					+ "(?:!|\\?){5,}\n"
					+ "\n"
					+ "//a sequence of single upper case letters followed by dot\n"
					+ "\\b(?:\\p{javaUpperCase}\\.)+\n"
					+ "\n"
					+ "//numbers with decimal part or IP addresses, or Internet addresses\n"
					+ "\\p{Alnum}+(?:\\.\\p{Alnum}+)+\n"
					+ "\n"
					+ "//java dotted names or Internet addresses\n"
					+ "\\p{Alpha}+(?:\\.\\p{Alpha}+)+\n"
					+ "\n"
					+ "//Appended patterns from the gazetteer lists\n";
			
			StringBuilder str_builder = new StringBuilder();
			str_builder.append(non_splitter_text_file);
			for (Path f : gazetteer_file_paths) {
				Files.lines(f).filter(line -> line.contains("."))
							  .forEach(line -> str_builder.append("\\b" + line.replace(".", "\\.") + "\n"));
			}
			
			Files.writeString(Paths.get(this.getClass().getClassLoader().getResource("language_resources/regex_sentence_splitter/non-split-patterns.txt").toURI()), 
					   str_builder.toString(), 
	                   StandardCharsets.UTF_8,
	                   StandardOpenOption.CREATE,
	                   StandardOpenOption.TRUNCATE_EXISTING);

			
		}catch(IOException | URISyntaxException e) {
			System.err.println(e);
			System.exit(-1);
		}
	}
	

}
