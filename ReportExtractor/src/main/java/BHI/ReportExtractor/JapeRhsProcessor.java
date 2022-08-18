package BHI.ReportExtractor;

import static gate.Utils.stringFor;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author nicholassunderland
 * 
 * This class moves all of the logic from the right hand side of the JAPE files into .java files; 
 * which makes debugging and writing reusable helper functions a lot easier.
 * 
 */
public class JapeRhsProcessor {
		
    /**
     * A map/dictionary of the variables we want to extract, their dimension, and expected unit
     * The dictionary is stored in ...resources/language_resources/jape/measurements_dictionary.csv
     */
    private static VariableDictionary variable_dictionary = null;
    
	/**
	 * @param examination_type	the type of report being processed; defined in the JAPE file; currently, either echocardiogram or cardiac_mri
	 * @param var_type			the variable being extracted; defined by the JAPE file; for example "lv_ivs" for left ventricular interventricular septum
	 * @param doc				the document being extracted from
	 * @param bindings			a Map of bindings to annotations; defined in the JAPE file
	 * @param outputAS			the output annotation set, the main point of this processing is to add appropriate features into this variable
	 */
	public JapeRhsProcessor(String examination_type,
		                    String var_name,
							Document doc, 
							Map<String, AnnotationSet> bindings, 
							AnnotationSet outputAS) {
		
		try {

			//We must have a single value annotation (if not something has gone very wrong in the JAPE grammars)
			String annot_value_type;
			if(bindings.get("value")==null) {
				throw new Exception("The the 'value' AnnotSet captured in JapeRhsProcessor for [" + var_name + "] is null...\nBindings:\n" + bindings.toString());
			}else if(bindings.get("value").size()>1) {
				throw new Exception("The the 'value' AnnotSet captured in JapeRhsProcessor for [" + var_name + "] contains more than one annotation...\nBinding:\n" + bindings.get("value"));
			}else {
				annot_value_type = bindings.get("value").iterator().next().getType();
			}
		
			// Initialise the variable dictionary once (static)
			if(variable_dictionary==null) {
				variable_dictionary = new VariableDictionary(examination_type);
			}
			
			// Collect the information about this variable (stored in the measurements_dictionary.csv)
			String value_type      = variable_dictionary.get(var_name).getUnitType();   // e.g. "linear" or "volume"
			String expected_units  = variable_dictionary.get(var_name).getUnits();      // e.g. "cm"     or "mls" 
			Double min_value       = variable_dictionary.get(var_name).getMin();        // the minimum value if the units are 'expected_units'
			Double max_value       = variable_dictionary.get(var_name).getMax();        // the maximum value if the units are 'expected_units'
			boolean attempt_adjust = variable_dictionary.get(var_name).attemptAdjust(); // whether to try to adjust the value if we are not given the units
	

	//		for(Annotation ann : outputAS) {
	//		PrintColour.println("annot type: " + ann.toString(), PrintColour.YELLOW);
	//	}
	//		if(var_name.contains("height")) {
	//			PrintColour.println("value annot type: " + value_annot.getType(), PrintColour.GREEN_BOLD);
	//			PrintColour.println("value annot features: " + value_annot.getFeatures().toString(), PrintColour.GREEN_BOLD);
	//			PrintColour.println("Context annots:\n" + context_str, PrintColour.GREEN_BOLD);
	//			PrintColour.println("Value annots:\n" + bindings.get("value"), PrintColour.GREEN_BOLD);
	//			PrintColour.println("Unit annots:\n" + bindings.get("unit"), PrintColour.GREEN_BOLD);
	//			//PrintColour.println("IM annots:\n " + outputAS.get("ImperialMeasurement"), PrintColour.GREEN_BOLD);
	//			//PrintColour.println("Binding annots:\n" + bindings, PrintColour.GREEN_BOLD);
	//			//PrintColour.println("Output annots:\n" + outputAS, PrintColour.GREEN_BOLD);
	//			//System.out.println(bindings.toString());
	//			//System.exit(0);
	//		}
			
			Annotation value_annot    = null;
			Annotation unit_annot     = null;
			Annotation modifier_annot = null;
			String value_str          = null;
			String units_str          = null; 
			String modifier_str       = null;
			
			switch(annot_value_type) {
				case "Numeric":
					value_annot = bindings.get("value").get("Numeric").iterator().next();
					value_str   = (String) value_annot.getFeatures().get("value");
					value_str   = value_str.replaceAll("\\s","").replace(',','.');//remove white space to deal with matches like '3. 8cm' and '3,4cm')
					unit_annot  = bindings.get("unit")==null ? null : bindings.get("unit").get("Units").iterator().next();
					units_str   = unit_annot==null ? null : (String) unit_annot.getFeatures().get("minorType"); // the units is the "minorType" of the Units annotations (see the Gazetteers for the definitions of the units)
					break;
				case "Categorical":
					value_annot    = bindings.get("value").get("Categorical").iterator().next();
					value_str      = (String) value_annot.getFeatures().get("minorType"); // this value may get altered when we parseCategorical as there is additional negative/positive assertion info carried in the units - e.g. value="dilated" & units="isn't" ==> value="nondilated"
					modifier_annot = bindings.get("modifier")==null ? null : bindings.get("modifier").get("Categorical").iterator().next();
					modifier_str   = modifier_annot==null ? null : (String) modifier_annot.getFeatures().get("minorType"); 
					break;
				case "ImperialMeasurement":
					switch(var_name) {
						case "height":
							value_annot   = bindings.get("value").get("ImperialMeasurement").iterator().next();
							String feet   = value_annot==null ? null : (String) value_annot.getFeatures().get("feet");
							String inches = value_annot==null ? null : (String) value_annot.getFeatures().get("inches");
							value_str     = JapeHelper.imperialLengthToCm(feet, inches);
							units_str     = value_str!=null ? "cm" : null;
							break;
						case "weight":
							value_annot   = bindings.get("value").get("ImperialMeasurement").iterator().next();
							String stone  = value_annot==null ? null : (String) value_annot.getFeatures().get("stone");
							String pounds = value_annot==null ? null : (String) value_annot.getFeatures().get("pounds");
							value_str     = JapeHelper.imperialWeightToKg(stone, pounds);
							units_str     = value_str!=null ? "kg" : null;
							break;
						default:
							value_str = null;
							units_str = null;
					}
					break;
				default:
					PrintColour.println("Value annotation was not of type [null, Numeric, ImperialMeasurement, or Categorical] - see JapeRHSProcessor()", PrintColour.RED_BOLD_BRIGHT);
					System.exit(-1);
			}
			
			
			// Parse the units and return as ResultUnitPair
			ResultUnitPair<Object, String> result = null;
			//PrintColour.println("value_type: " + value_type, PrintColour.YELLOW_BOLD);
			switch(value_type) {
				case "length":
					result = JapeHelper.parseLengthUnits(value_str, units_str, expected_units, min_value, max_value, attempt_adjust);
					break;
				case "volume":
					result = JapeHelper.parseVolumeUnits(value_str, units_str, expected_units);
					break;
				case "area":
					result = JapeHelper.parseAreaUnits(value_str, units_str, expected_units);
					break;
				case "gradient":
					result = JapeHelper.parseGradientUnits(value_str, units_str, expected_units);
					break;
				case "velocity":
					result = JapeHelper.parseVelocityUnits(value_str, units_str, expected_units);
					break;
				case "percentage":
					result = JapeHelper.parsePercentageUnits(value_str, units_str, expected_units);
					break;
				case "dimensionless":
					result = JapeHelper.parseDimensionless(value_str, units_str, expected_units);
					break;
				case "mass":
					result = JapeHelper.parseMassUnits(value_str, units_str, expected_units, min_value, max_value, attempt_adjust);
					break;
				case "categorical":
					result = JapeHelper.parseCategorical(value_str, modifier_str, expected_units);
					break;
				default:
					result = new ResultUnitPair<Object, String>(0.0, "error");
			}
			
			// A new features map to take the variable features
			FeatureMap var_features = Factory.newFeatureMap();
			var_features.put("context", (String)stringFor(doc, bindings.get("context"))); // Set the context feature with the extracted context string; e.g. "left ventricular ejection fraction..."
			var_features.put("value", result.getValueAsString()); // Set the value feature
			var_features.put("value_type", annot_value_type); // Set the value feature
			var_features.put("unit",  result.getUnits()); // Set the unit feature
			var_features.put("examination_type", examination_type); // Set the examination type e.g. echocardiogram or cardiac_mri
			
	//		PrintColour.println("Value_type: " + var_type + ", Units type: " + value_type + ", Context: " + context_str + ", Value: " + value_str + ", Units: " + units_str + ", Expected_units: " + expected_units + ", Min: " + min_value + ", Max: " + max_value, PrintColour.CYAN_BRIGHT);
			//PrintColour.println(value_annot.getType(), PrintColour.GREEN_BOLD);
	//		PrintColour.println("features: " + var_features.toString(), PrintColour.GREEN_BOLD);
	//		if(var_type.contains("ao_sov_diam")){
	//			System.exit(-1);
	//		}
			//PrintColour.println("bindings: " + bindings.toString(), PrintColour.GREEN);
			
			// Finally, set the type of examination this variable is from; currently, either echocardiogram or MRI
			outputAS.add(bindings.get("annot").firstNode(), 
						 bindings.get("annot").lastNode(), 
						 var_name, 
						 var_features);	
			
		}catch(Exception e) {
			PrintColour.println(e.getMessage(), PrintColour.RED_BACKGROUND_BRIGHT);
			System.exit(0);
		}
	}
}
