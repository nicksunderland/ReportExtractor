/**
 * 
 */
package neoImage.v2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.opencsv.bean.CsvToBeanBuilder;

/**
 * @author nicholassunderland
 *
 */
public class VariableDictionary {
	
	private Map<String, DictionaryEntry> variable_dictionary;
	
	public VariableDictionary(String examination_type) {
		
		variable_dictionary = new LinkedHashMap<String, DictionaryEntry>();
		
		InputStream input_stream = JapeRhsProcessor.class.getClassLoader().getResourceAsStream("language_resources/jape/measurements_dictionary.csv");
		Iterator<DictionaryEntry> dict_it = new CsvToBeanBuilder<DictionaryEntry>(new InputStreamReader(input_stream))
				.withType(DictionaryEntry.class)
				.build()
				.iterator();
		
		while (dict_it.hasNext()) {
			
			DictionaryEntry dict_entry = dict_it.next();
			
			if(dict_entry.getExaminationType().contains(examination_type)) {
				variable_dictionary.put(dict_entry.getVariableName(), dict_entry);
			}
		}
		
		variable_dictionary = Collections.unmodifiableMap(variable_dictionary);
	}
	
	public DictionaryEntry get(String variable) {
		
		return( variable_dictionary.get(variable) );
		
	}
	
	public Set<String> getVariableNameSet() {
		
		return( variable_dictionary.keySet() );
		
	}
	
	public boolean isBetterMatch(String variable, ResultUnitPair<Object, String> old_result, ResultUnitPair<Object, String> new_result) {
		
		// The old result is null so don't need to compare
		if(old_result.isNull() & !new_result.isNull()) {
			
			return true;
			
		}else{
		
			try {
				Double old_value, new_value, min, max;
				
				old_value = Double.parseDouble(old_result.getValueAsString());
				new_value = Double.parseDouble(new_result.getValueAsString());
				min = this.variable_dictionary.get(variable).getMin();
				max = this.variable_dictionary.get(variable).getMax();
				
				// The old value is out of range AND the new value is in range
				if( (old_value > max | old_value < min) & (new_value <= max & new_value >= min) ){
					return true;
				}else{
					return false;
				}
			
			}catch(NullPointerException | NumberFormatException e) {
				return false;
			}
		}
	}
}

