package BHI.ReportExtractor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import gate.AnnotationSet;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author nicholassunderland
 *
 * Unit test class for testing the extraction of echo data.
 *
 */
public class TestEchoExtraction 
{
	private static Data data;
	
	@BeforeAll
	public static void initTestEchoExtraction() {
		data = new Data();
		data.init("echocardiogram");
	}
	
	@Test
	public void testDataInitialisation() {
		//Does the data init using the @BeforeAll flag
		PrintColour.println("Completed 'Data' object init()", PrintColour.GREEN_BOLD_BRIGHT);
	}
	
	@Test
	public void testAnnotationRefinement() throws ResourceInstantiationException, ExecutionException {
		//Does the data init using the @BeforeAll flag
		
		String text = "5ft 3\" and cm/m"; //This should be 2 Numeric annotations and 4 Units annotations
		//--> after processing in ../general/refine_gaz_annotations & ../general/numerical & ../general/imperial_measurements
		//--> this should become 1 ImperialMeasurement and 1 Units (cm/m)
		int exp_unit_ann_size = 1;
		int exp_numeric_ann_size = 0;
		int exp_imperial_ann_size = 1;
		
		//Load the test string and process
		ArrayList<Record> single_record = new ArrayList<Record>(List.of(new Record("TestId", "2000-01-01 00:00:00", text)));
		Iterator<Record> record_it = single_record.iterator();
		data.initCsvDataIterator(record_it);
    	
    	//Get the annotations and check
    	AnnotationSet all_annots = data.extractAllDocumentAnnotations();
    	AnnotationSet unit_annots = all_annots.get("Units");
    	AnnotationSet numeric_annots = all_annots.get("Numeric");
    	AnnotationSet imperial_measure_annots = all_annots.get("ImperialMeasurement");
		
    	String message = String.format(	"\n" +
                						"Text:               %s \n" + 
                						"Expected value:     Units x %s, Numeric x %s, ImperialMeasurement x %s \n" +
                						"Actual value:       Units x %s, Numeric x %s, ImperialMeasurement x %s",
                						text, 
                						Integer.toString(exp_unit_ann_size), Integer.toString(exp_numeric_ann_size), Integer.toString(exp_imperial_ann_size), 
                						Integer.toString(unit_annots.size()), Integer.toString(numeric_annots.size()), Integer.toString(imperial_measure_annots.size()));

		PrintColour.println("Testing annotation refinement:       ", PrintColour.WHITE_BOLD);
		PrintColour.print  ("Text:           ", PrintColour.WHITE_BOLD);
		PrintColour.println(text, PrintColour.WHITE);
		PrintColour.print  ("Expected value: ", PrintColour.WHITE_BOLD);
		PrintColour.println(String.format("Units x %s, Numeric x %s, ImperialMeasurement x %s",
						    Integer.toString(exp_unit_ann_size), Integer.toString(exp_numeric_ann_size), Integer.toString(exp_imperial_ann_size)), PrintColour.WHITE);
		PrintColour.print  ("Actual value:   ", PrintColour.WHITE_BOLD);
		PrintColour.println(String.format("Units x %s, Numeric x %s, ImperialMeasurement x %s",
			    		    Integer.toString(unit_annots.size()), Integer.toString(numeric_annots.size()), Integer.toString(imperial_measure_annots.size())), PrintColour.WHITE);
		
		if(exp_unit_ann_size == unit_annots.size() & 
				exp_numeric_ann_size == numeric_annots.size() &
						exp_imperial_ann_size == imperial_measure_annots.size()) {
		PrintColour.println("** PASS **\n", PrintColour.GREEN_BOLD_BRIGHT);
		}else {
		PrintColour.println("** FAIL **\n", PrintColour.RED_BOLD_BRIGHT);
		}
		
		assertTrue(exp_unit_ann_size == unit_annots.size() & 
				   exp_numeric_ann_size == numeric_annots.size() &
				   exp_imperial_ann_size == imperial_measure_annots.size(), message);	
		
	}
	
	// The base test for the JAPE grammars
	public void baseTest(String var_name, String text, String expected_value, String expected_units) {
		
		ArrayList<Record> single_record = new ArrayList<Record>(List.of(new Record("TestId", "2000-01-01 00:00:00", text)));
		Iterator<Record> record_it = single_record.iterator();

		data.initCsvDataIterator(record_it);
    	data.extractDataTest();
    	
    	ResultUnitPair<Object, String> actual_result   = new ResultUnitPair<Object, String>(data.getResults(var_name).getValue(), data.getResults(var_name).getUnits());
    	ResultUnitPair<Object, String> expected_result = new ResultUnitPair<Object, String>(expected_value, expected_units);
    	data.clearResults();
    	
    	// Make sure rounding is the same if it's numerical; skip if expecting null
    	if(expected_result.getValue()!=null) {
    		
//    		PrintColour.println("Expected result: " + expected_result.toString(), PrintColour.YELLOW_BOLD_BRIGHT);
//    		PrintColour.println("Actual   result: " + actual_result.toString(), PrintColour.YELLOW_BOLD_BRIGHT);
			try{
				int decimals = 0;
				if(expected_result.getValueAsString().contains(".")) {
					String[] splitter = expected_result.getValueAsString().split("\\.");
					decimals = splitter[1].length();   // After  Decimal Count
				}
				
				String decimals_format = "%." + decimals + "f"; 
				Double act_result = Double.parseDouble(actual_result.getValueAsString());
				act_result = Double.parseDouble(String.format(decimals_format, act_result));
				actual_result.setValue(String.format(decimals_format, act_result));	

			} catch (NumberFormatException | NullPointerException e) {
				
			}
    	}
    	
    	String message = String.format(	 "\n" +
    	                                 "Variable:           %s \n" +
    									 "Text:               %s \n" + 
						                 "Expected value:     %s %s \n" +
								         "Actual value:       %s %s \n",
										 var_name, 
										 text.substring(0, text.length()>50?50:text.length()) + "...", 
						                 expected_value, expected_units, 
						                 actual_result.getValueAsString(), actual_result.getUnits());
    	
		PrintColour.print  ("Variable:       ", PrintColour.WHITE_BOLD);
		PrintColour.println(var_name, PrintColour.WHITE);
		PrintColour.print  ("Text:           ", PrintColour.WHITE_BOLD);
		PrintColour.println(text.substring(0, text.length()>50?50:text.length()) + "...", PrintColour.WHITE);
		PrintColour.print  ("Expected value: ", PrintColour.WHITE_BOLD);
		PrintColour.println(expected_value + " " + expected_units, PrintColour.WHITE);
		PrintColour.print  ("Actual value:   ", PrintColour.WHITE_BOLD);
		PrintColour.println(actual_result.getValueAsString() + " " + actual_result.getUnits(), PrintColour.WHITE);
		
    	if(expected_result.equals(actual_result)) {
    		PrintColour.println("** PASS **\n", PrintColour.GREEN_BOLD_BRIGHT);
    	}else {
    		PrintColour.print  ("Full text:      ", PrintColour.WHITE_BOLD);
    		PrintColour.println(text, PrintColour.WHITE);
    		PrintColour.println("** FAIL **\n", PrintColour.RED_BOLD_BRIGHT);
    	}
 
        assertTrue(expected_result.equals(actual_result), message);		
	}
	
    @ParameterizedTest(name = "testHeight row: {index}")
    @CsvFileSource(resources = "/general/height.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testHeight(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testWeight row: {index}")
    @CsvFileSource(resources = "/general/weight.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testWeight(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testDiameterAorticRoot row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_root_diam.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testDiameterAorticRoot(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testDiameterSinusOfValsalva row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_sov_diam.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testDiameterSinusOfValsalva(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testDiameterSinotubularJunction row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_stj_diam.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testDiameterSinotubularJunction(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testDiameterAscendingAorta row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_asc_diam.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testDiameterAscendingAorta(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testHeightIndexedAorticRootDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_root_diam_ht_idx.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testHeightIndexedAorticRootDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testHeightIndexedSinusOfValsalvaDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_sov_diam_ht_idx.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testHeightIndexedSinusOfValsalvaDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testHeightIndexedSinotubularJunctionDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_stj_diam_ht_idx.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testHeightIndexedSinotubularJunctionDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testHeightIndexedAscendingAortaDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/linear_measurements/ao_asc_diam_ht_idx.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testHeightIndexedAscendingAortaDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testCategoricalSinusOfValsalvaDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/categorical_size/ao_sov_cat.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testCategoricalSinusOfValsalvaDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testCategoricalSinotubularJunctionDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/categorical_size/ao_stj_cat.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testCategoricalSinotubularJunctionDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testCategoricalAscendingAortaDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/categorical_size/ao_asc_cat.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testCategoricalAscendingAortaDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testCategoricalAortaRootDiameter row: {index}")
    @CsvFileSource(resources = "/echo/vessels/categorical_size/ao_root_cat.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
    public void testCategoricalAortaRootDiameter(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testLvEjectionFractionVisual row: {index}")
    @CsvFileSource(resources = "/echo/left_ventricle/function/lv_ef_visual.csv", numLinesToSkip = 1)
    public void testLvEjectionFractionVisual(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testLvEjectionFractionSimpsons row: {index}")
    @CsvFileSource(resources = "/echo/left_ventricle/function/lv_ef_simpsons.csv", numLinesToSkip = 1)
    public void testLvEjectionFractionSimpsons(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testLvEjectionFractionAuto row: {index}")
    @CsvFileSource(resources = "/echo/left_ventricle/function/lv_ef_auto.csv", numLinesToSkip = 1)
    public void testLvEjectionFractionAuto(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testLvEjectionFractionTeicholz row: {index}")
    @CsvFileSource(resources = "/echo/left_ventricle/function/lv_ef_teicholz.csv", numLinesToSkip = 1)
    public void testLvEjectionFractionTeicholz(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
    @ParameterizedTest(name = "testLvEjectionFractionCategorical row: {index}")
    @CsvFileSource(resources = "/echo/left_ventricle/function/lv_sysfunc_cat.csv", numLinesToSkip = 1)
    public void testLvEjectionFractionCategorical(String var_name, String text, String expected_value, String expected_units) {
    	baseTest(var_name, text, expected_value, expected_units);
    }
    
//  @ParameterizedTest(name = "testLvInternalDiameterDiastole row: {index}")
//  @CsvFileSource(resources = "/echo/left_ventricle/linear_measurements/internal_diameter_diastole.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
//  public void testLvInternalDiameterDiastole(String var_name, String text, String expected_value, String expected_units) {
//  	baseTest(var_name, text, expected_value, expected_units);
//  }
//  
//  @ParameterizedTest(name = "testLvInternalDiameterSystole row: {index}")
//  @CsvFileSource(resources = "/echo/left_ventricle/linear_measurements/internal_diameter_systole.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
//  public void testLvInternalDiameterSystole(String var_name, String text, String expected_value, String expected_units) {
//  	baseTest(var_name, text, expected_value, expected_units);
//  }
//  
//  @ParameterizedTest(name = "testLvInterventricularSeptalDiameter row: {index}")
//  @CsvFileSource(resources = "/echo/left_ventricle/linear_measurements/interventricular_septal_diameter.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
//  public void testLvInterventricularSeptalDiameter(String var_name, String text, String expected_value, String expected_units) {
//  	baseTest(var_name, text, expected_value, expected_units);
//  }
//  
//  @ParameterizedTest(name = "testLvPosteriorWallDiameter row: {index}")
//  @CsvFileSource(resources = "/echo/left_ventricle/linear_measurements/posterior_wall_diameter.csv", numLinesToSkip = 1, maxCharsPerColumn=10000)
//  public void testLvPosteriorWallDiameter(String var_name, String text, String expected_value, String expected_units) {
//  	baseTest(var_name, text, expected_value, expected_units);
//  }

}
