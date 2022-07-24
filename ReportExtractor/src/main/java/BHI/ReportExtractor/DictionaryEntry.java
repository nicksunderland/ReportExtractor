package BHI.ReportExtractor;

import com.opencsv.bean.CsvBindByName;

/**
 * @author nicholassunderland
 *
 */
public class DictionaryEntry {
	
	@CsvBindByName(column = "examination", required = true)
	private String examination_type;
	@CsvBindByName(column = "variable_name", required = true)
	private String variable_name;
	@CsvBindByName(column = "unit_type", required = true)
	private String unit_type;
	@CsvBindByName(column = "units", required = true)
	private String units;
	@CsvBindByName(column = "min", required = true)
	private String min;
	@CsvBindByName(column = "max", required = true)
	private String max;
	@CsvBindByName(column = "attempt_adjust", required = true)
	private String attempt_adjust;
	
	// Constructor to load via .csv file
	public DictionaryEntry(){
	}
	
	public String getExaminationType() {
		return examination_type;
	}
	
	public String getVariableName() {
		return variable_name;
	}
	
	public String getUnitType() {
		return unit_type;
	}
	
	public String getUnits() {
		return units;
	}
	
	public Double getMin() {
		try{
			return Double.parseDouble(this.min);
		}catch(NullPointerException | NumberFormatException e) {
			return null;
		}
	}
	
	public Double getMax() {
		try{
			return Double.parseDouble(this.max);
		}catch(NullPointerException | NumberFormatException e) {
			return null;
		}
	}
	
	public boolean attemptAdjust() {
		return Boolean.parseBoolean(attempt_adjust);
	}	
	
}  