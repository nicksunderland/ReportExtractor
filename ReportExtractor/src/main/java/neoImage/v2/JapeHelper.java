package neoImage.v2;

import java.util.Arrays;
import java.util.List;

/**
 * @author nicholassunderland
 *
 * This helper class provided static helper functions for use by the JAPE right hand side processor
 *
 */
public class JapeHelper {
	
	public static ResultUnitPair<Object, String> parseLengthUnits(String value_str, 
																  String units, 
																  String expected_units, 
																  Double min_value, 
																  Double max_value, 
																  boolean attempt_adjust) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (NumberFormatException | NullPointerException e) {
        	System.err.println("Exception thrown in parseLinearUnits() - unable to parse [\"" + value_str + "\"] to double\n" + e);
        	return(result);
        }
	
		// If no units, try to guess the units
		String found_units = null;
		String expec_units = expected_units.toLowerCase();
		try{
			found_units = units.toLowerCase(); 
		}
		catch(NullPointerException e){
			found_units = "no_units_found"; //just will send to the default switch case below
		}
		
		// Based on the units we found, alter the value in accordance with the units we actually want
		switch(found_units) {
		case "cm":		
			if(expec_units.contentEquals("cm")) {
				result.setValue(value_dbl);
				result.setUnits("cm");
				return(result);
			}else if(expec_units.contentEquals("mm")){
				result.setValue(value_dbl*10);
				result.setUnits("mm");
				return(result);
			}else if(expec_units.contentEquals("m")){
				result.setValue(value_dbl/100);
				result.setUnits("m");
				return(result);
			}else {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			}
		case "mm":
			if(expec_units.contentEquals("mm")) {
				result.setValue(value_dbl);
				result.setUnits("mm");
				return(result);
			}else if(expec_units.contentEquals("cm")){
				result.setValue(value_dbl/10);
				result.setUnits("cm");
				return(result);
			}else if(expec_units.contentEquals("m")){
				result.setValue(value_dbl/1000);
				result.setUnits("m");
				return(result);
			}else {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			}
		case "m":
			if(expec_units.contentEquals("m")) {
				result.setValue(value_dbl);
				result.setUnits("m");
				return(result);
			}else if(expec_units.contentEquals("cm")){
				result.setValue(value_dbl*100);
				result.setUnits("cm");
				return(result);
			}else if(expec_units.contentEquals("mm")){
				result.setValue(value_dbl*1000);
				result.setUnits("mm");
				return(result);
			}else {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			}
		case "cm/m":		
			if(expec_units.contentEquals("cm/m")) {
				result.setValue(value_dbl);
				result.setUnits("cm/m");
				return(result);
			}else if(expec_units.contentEquals("mm/m")){
				result.setValue(value_dbl*10);
				result.setUnits("mm/m");
				return(result);
			}else {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			}
		case "mm/m":
			if(expec_units.contentEquals("mm/m")) {
				result.setValue(value_dbl);
				result.setUnits("mm/m");
				return(result);
			}else if(expec_units.contentEquals("cm/m")){
				result.setValue(value_dbl/10);
				result.setUnits("cm/m");
				return(result);
			}else {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			}
		//if units null / not found in the text, then see if we can work out what the units are based on a sensible guess between the min-max range for the value
		default: 
			// if we don't want to attempt a guess just return the value and units==null 
			if(!attempt_adjust) {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			
			// attempt to adjust the double value to fit into the min-max range provided and based on the expected units provided
			}else { 
								
//				PrintColour.println("Attempting to adjust value [" + value_dbl + "] into range [" +  min_value + "-" + max_value + expec_units + "]", PrintColour.YELLOW);
//				System.exit(0);
				
				// Attempt the guess based on the expected units
				switch(expec_units) {
				case "mm":
					// Need mm : might have been given in mm
					if(value_dbl >= min_value & value_dbl <= max_value) {
						result.setValue(value_dbl);
						result.setUnits("mm");
						return(result);
					// Need mm : might have been given in cm
					}else if(value_dbl*10 >= min_value & value_dbl*10 <= max_value) {
						result.setValue(value_dbl*10);
						result.setUnits("mm");
						return(result);
					// Need mm : might have been given in m
					}else if(value_dbl*1000 >= min_value & value_dbl*1000 <= max_value) {
						result.setValue(value_dbl*1000);
						result.setUnits("mm");
						return(result);
					}else {
						result.setValue(null);
						result.setUnits(null);
						return(result);
					}
				case "cm":
					// Need cm : might have been given in cm
					if(value_dbl >= min_value & value_dbl <= max_value){
						result.setValue(value_dbl);
						result.setUnits("cm");
						return(result);
					// Need cm : might have been given in mm
					}else if(value_dbl/10 >= min_value & value_dbl/10 <= max_value) {
						result.setValue(value_dbl/10);
						result.setUnits("cm");
						return(result);
					// Need cm : might have been given in m
					}else if(value_dbl*100 >= min_value & value_dbl*100 <= max_value) {
						result.setValue(value_dbl*100);
						result.setUnits("cm");
						return(result);
					}else {
						result.setValue(null);
						result.setUnits(null);
						return(result);
					}
				case "m":
					// Need m : might have been given in m
					if(value_dbl >= min_value & value_dbl <= max_value) {
						result.setValue(value_dbl);
						result.setUnits("m");
						return(result);
					// Need m : might have been given in cm
					}else if(value_dbl/100 >= min_value & value_dbl/100 <= max_value) {
						result.setValue(value_dbl/100);
						result.setUnits("m");
						return(result);
					// Need m : might have been given in mm
					}else if(value_dbl/1000 >= min_value & value_dbl/1000 <= max_value) {
						result.setValue(value_dbl/1000);
						result.setUnits("m");
						return(result);
					}else {
						result.setValue(null);
						result.setUnits(null);
						return(result);
					}
				case "mm/m":
					// Need mm/m : might have been given in mm/m
					if(value_dbl >= min_value & value_dbl <= max_value) {
						result.setValue(value_dbl);
						result.setUnits("mm/m");
						return(result);
					// Need mm/m : might have been given in cm
					}else if(value_dbl*10 >= min_value & value_dbl*10 <= max_value) {
						result.setValue(value_dbl*10);
						result.setUnits("mm/m");
						return(result);
					// Need mm/m : might have been given in m
					}else if(value_dbl*1000 >= min_value & value_dbl*1000 <= max_value) {
						result.setValue(value_dbl*1000);
						result.setUnits("mm/m");
						return(result);
					}else {
						result.setValue(null);
						result.setUnits(null);
						return(result);
					}
				case "cm/m":
					// Need cm/m : might have been given in cm/m
					if(value_dbl >= min_value & value_dbl <= max_value) {
						result.setValue(value_dbl);
						result.setUnits("cm/m");
						return(result);
					// Need cm/m : might have been given in mm/m
					}else if(value_dbl/10 >= min_value & value_dbl/10 <= max_value) {
						result.setValue(value_dbl/10);
						result.setUnits("cm/m");
						return(result);				
					// Need cm/m : might have been given in m/m
					}else if(value_dbl*100 >= min_value & value_dbl*100 <= max_value) {
						result.setValue(value_dbl*100);
						result.setUnits("cm/m");
						return(result);
					}else {
						result.setValue(null);
						result.setUnits(null);
						return(result);
					}
				default:
					result.setValue("error - the units provided in the dictionary were not recognised");
					result.setUnits("error - the units provided in the dictionary were not recognised");
					return(result);
				}
			}
		}
	}
	
	public static ResultUnitPair<Object, String> parsePercentageUnits(String value_str, String units, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (Exception e) {
        	System.err.println("Exception thrown in parsePercentageUnits() - unable to parse [\"" + value_str + "\"] to double\n" + e);
        	return(result);
        }
	
		// If no units, return the value as a double, indicate no units found
		if(units == null) {
			result.setValue(value_dbl);
			result.setUnits("no units found");
			return(result);
			
		}else if(units.equals("%")){
			// Send back as %
			result.setValue(value_dbl);
			result.setUnits("%");
			return(result);
		
		}else{
			// Send back as error
			result.setValue(value_dbl);
			result.setUnits("error parsing " + units);
			return(result);
		}
	}
	
	public static ResultUnitPair<Object, String> parseDimensionless(String value_str, String units, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (Exception e) {
        	System.err.println("Exception thrown in parseDimensionless() - unable to parse [\"" + value_str + "\"] to double\n" + e);
        	return(result);
        }
	
		// If no units, return the value as a double, indicate no units found
		if(units == null) {
			result.setValue(value_dbl);
			result.setUnits("dimensionless");
			return(result);
		
		}else{
			// Send back as error
			result.setValue(value_dbl);
			result.setUnits("error parsing " + units);
			return(result);
		}
	}
	
	public static ResultUnitPair<Object, String> parseMassUnits(String value_str, 
																String units, 
																String expected_units, 
																Double min_value, 
																Double max_value, 
																boolean attempt_adjust) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (NumberFormatException | NullPointerException e) {
        	System.err.println("Exception thrown in parseMassUnits() - unable to parse [\"" + value_str + "\"] to double\n" + e);
        	return(result);
        }
	
		// If no units, try to guess the units
		String found_units = null;
		String expec_units = expected_units.toLowerCase();
		try{
			found_units = units.toLowerCase(); //will be unnecessary once I make the RHS processor do the work
		}
		catch(NullPointerException e){
			found_units = "no_units_found";
		}
		
//		PrintColour.println("in parsing...", PrintColour.YELLOW_BOLD_BRIGHT);
//		PrintColour.println("Result: " + value_dbl, PrintColour.YELLOW_BOLD_BRIGHT);
//		PrintColour.println("units: " + found_units, PrintColour.YELLOW_BOLD_BRIGHT);
//		PrintColour.println("exp units: " + expec_units, PrintColour.YELLOW_BOLD_BRIGHT);
//		PrintColour.println("min: " + min_value, PrintColour.YELLOW_BOLD_BRIGHT);
//		PrintColour.println("max: " + max_value, PrintColour.YELLOW_BOLD_BRIGHT);
		
		switch(found_units) {
		case "kg":
			result.setValue(value_dbl);
			result.setUnits("kg");
			return(result);
		case "g":
			result.setValue(value_dbl / 1000D);
			result.setUnits("kg");
			return(result);
		default: //if units null / not found in the text
			// if we don't want to attempt a guess just return the value and units==null 
			if(!attempt_adjust) {
				result.setValue(value_dbl);
				result.setUnits(null);
				return(result);
			}else {

				switch(expec_units) {
				case "kg":
					// Need kg : given in kg
					if(value_dbl >= min_value & value_dbl <= max_value) {
						result.setValue(value_dbl);
						result.setUnits("kg");
						return(result);
					// Need kg : given in g
					}else if(value_dbl/1000 >= min_value & value_dbl/1000 <= max_value) {
						result.setValue(value_dbl/1000);
						result.setUnits("kg");
						return(result);
					}else {
						result.setValue(value_dbl);
						result.setUnits(null);
						return(result);
					}
				case "g":
					// Need g : given in kg
					if(value_dbl*1000 >= min_value & value_dbl*1000 <= max_value) {
						result.setValue(value_dbl*1000);
						result.setUnits("g");
						return(result);
					// Need g : given in g
					}else if(value_dbl >= min_value & value_dbl <= max_value) {
						result.setValue(value_dbl);
						result.setUnits("g");
						return(result);
					}else {
						result.setValue(value_dbl);
						result.setUnits(null);
						return(result);
					}
				default:
					result.setValue(null);
					result.setUnits(null);
					return(result);
				}
			}
		}
	}
	
	public static ResultUnitPair<Object, String> parseVolumeUnits(String value_str, String units, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (Exception e) {
        	System.err.println("Exception thrown in parseVolumeUnits() - unable to parse [\"" + value_str + "\"] to double\n" + e);
        	return(result);
        }

		// If no units, return the value as a double, indicate no units found
		if(units == null) {
			result.setValue(value_dbl);
			result.setUnits("no units found");
			return(result);
			
		}else if(units.toLowerCase().equals("ml") | units.toLowerCase().equals("mls")){
			// Send back as mls
			result.setValue(value_dbl);
			result.setUnits("mls");
			return(result);
		
		}else{
			// Send back as error
			result.setValue(value_dbl);
			result.setUnits("error parsing " + units);
			return(result);
		}
	}
	
	public static ResultUnitPair<Object, String> parseAreaUnits(String value_str, String units, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (Exception e) {
        	System.err.println("Exception thrown in parseAreaUnits() - unable to parse [\"" + value_str + "\"] to double\n" + e);
        	return(result);
        }

		// If no units, return the value as a double, indicate no units found
		if(units == null) {
			result.setValue(value_dbl);
			result.setUnits("no units found");
			return(result);
			
		}else if(units.toLowerCase().equals("cm")){
			// Send back as cm2
			result.setValue(value_dbl);
			result.setUnits("cm2");
			return(result);
		
		}else{
			// Send back as error
			result.setValue(value_dbl);
			result.setUnits("error parsing " + units);
			return(result);
		}
	}
	
	public static ResultUnitPair<Object, String> parseGradientUnits(String value_str, String units, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (Exception e) {
        	System.err.println("Exception thrown in parseGradientUnits() - unable to parse" + value_str + " to double\n" + e);
        	return(result);
        }

		// If no units, return the value as a double, indicate no units found
		if(units == null) {
			result.setValue(value_dbl);
			result.setUnits("no units found");
			return(result);
			
		}else if(units.toLowerCase().equals("mmhg")){
			// Send back as mmHg
			result.setValue(value_dbl);
			result.setUnits("mmHg");
			return(result);
		
		}else{
			// Send back as error
			result.setValue(value_dbl);
			result.setUnits("error parsing " + units);
			return(result);
		}
	}
	
	public static ResultUnitPair<Object, String> parseVelocityUnits(String value_str, String units, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
		// Try to parse the value_str as a double
		Double value_dbl = Double.NaN;
		try {
			value_dbl = Double.parseDouble(value_str);
        }
        catch (Exception e) {
        	System.err.println("Exception thrown in parseVelocityUnits() - unable to parse " + value_str + " to double\n" + e);
        	return(result);
        }

		// If no units, return the value as a double, indicate no units found
		if(units == null) {
			result.setValue(value_dbl);
			result.setUnits("no units found");
			return(result);
			
		}else if(units.toLowerCase().equals("cm")){
			// Change to m/s
			result.setValue(value_dbl / 100D);
			result.setUnits("m/s");
			return(result);
			
		}else if(units.toLowerCase().equals("m")){
			// Send back as m/s
			result.setValue(value_dbl);
			result.setUnits("m/s");
			return(result);
		
		}else{
			// Send back as error
			result.setValue(value_dbl);
			result.setUnits("error parsing " + units);
			return(result);
		}
	}
	
	public static ResultUnitPair<Object, String> parseCategorical(String value_str, String modifier, String expected_units) {
		
		// The pair to take the parsed result
		ResultUnitPair<Object, String> result = new ResultUnitPair<Object, String>(null, null);
		
//		PrintColour.println("value_str:\t" + value_str + "\n" +
//				            "units_str:\t" + units + "\n" +
//				            "exp_units:\t" + expected_units, PrintColour.GREEN_BOLD);
		
		List<String> value_levels = Arrays.asList(expected_units.split(",")); 
		
		
		if(modifier==null | value_levels.size()>2) {
			result.setValue(value_str);
			result.setUnits(null);
			return(result);
		}else if(modifier.contentEquals("negative_modifier")) {
			int index_of_value = value_levels.indexOf(value_str);
			if(index_of_value==0) {
				result.setValue(value_levels.get(1));
				result.setUnits(null);
				return(result);
			}else if(index_of_value==1) {
				result.setValue(value_levels.get(0));
				result.setUnits(null);
				return(result);
			}
		}
		
		return(result);
	}
	
	public static String imperialLengthToCm(String feet, String inches) {
		
		Double feet_to_cm_dbl = null;
		Double inch_to_cm_dbl = null;
		
		// Try to convert the feet to cms
		try {
			Double feet_dbl = Double.parseDouble(feet);
			feet_to_cm_dbl  = feet_dbl * 30.48;
		}catch(Exception e) {
			//
		}
		
		// Try to convert the inches to cms
		try {
			Double inch_dbl = Double.parseDouble(inches);
			inch_to_cm_dbl  = inch_dbl * 2.54;
		}catch(Exception e) {
			//
		}
		
		// Return cm string or null
		if(feet_to_cm_dbl==null && inch_to_cm_dbl== null) {
			return(null);
		}else if(feet_to_cm_dbl!=null && inch_to_cm_dbl!= null) {
			return(Double.toString(feet_to_cm_dbl+inch_to_cm_dbl));
		}else if(feet_to_cm_dbl!=null && inch_to_cm_dbl== null) {
			return(Double.toString(feet_to_cm_dbl));
		}else if(feet_to_cm_dbl==null && inch_to_cm_dbl!= null) {
			return(Double.toString(inch_to_cm_dbl));
		}else {
			return("Something went wrong in JapeHelper::imperialLengthToCm()");
		}
	}
	
	public static String imperialWeightToKg(String stone, String lbs) {
		
		Double stone_to_kg_dbl = null;
		Double lbs_to_kg_dbl = null;
		
		// Try to convert the feet to cms
		try {
			Double stone_dbl = Double.parseDouble(stone);
			stone_to_kg_dbl  = stone_dbl * 6.35;
		}catch(Exception e) {
			//
		}
		
		// Try to convert the inches to cms
		try {
			Double lbs_dbl = Double.parseDouble(lbs);
			lbs_to_kg_dbl  = lbs_dbl * 0.45;
		}catch(Exception e) {
			//
		}
		
		// Return cm string or null
		if(stone_to_kg_dbl==null && lbs_to_kg_dbl== null) {
			return(null);
		}else if(stone_to_kg_dbl!=null && lbs_to_kg_dbl!= null) {
			return(Double.toString(stone_to_kg_dbl+lbs_to_kg_dbl));
		}else if(stone_to_kg_dbl!=null && lbs_to_kg_dbl== null) {
			return(Double.toString(stone_to_kg_dbl));
		}else if(stone_to_kg_dbl==null && lbs_to_kg_dbl!= null) {
			return(Double.toString(lbs_to_kg_dbl));
		}else {
			return("Something went wrong in JapeHelper::imperialWeightToKg()");
		}
	}

}
