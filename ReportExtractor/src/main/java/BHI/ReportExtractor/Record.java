package BHI.ReportExtractor;

import com.opencsv.bean.CsvBindByName;

/**
 * @author nicholassunderland
 *
 * Class that holds record / text reports and associated details
 *
 */
public class Record{
	
	@CsvBindByName(column = "id", required = true)
	private String id;
	@CsvBindByName(column = "datetime", required = true)
	private String datetime;
	@CsvBindByName(column = "report_text", required = true)
	private String report_text;
	
	// Constructor to load via .csv file
	public Record(){
	}
	
	// Constructor to manually load
	public Record(String id, String datetime, String report_text){
		this.id = id;
		this.datetime = datetime;
		this.report_text = report_text; 
	}
	
	public String getId() {
		return id;
	}
	
	public String getDatetime() {
		return datetime;
	}
	
	public String getReportText() {
		return report_text;
	}
	
	public void print(){
		PrintColour.println("-----------------------", PrintColour.CYAN);
		PrintColour.print  ("ID:     ", PrintColour.WHITE_BOLD);
		PrintColour.println(this.id==null?"":this.id, PrintColour.WHITE);
		PrintColour.print  ("Date:   ", PrintColour.WHITE_BOLD);
		PrintColour.println(this.datetime==null?"":this.datetime.toString(), PrintColour.WHITE);
		PrintColour.print  ("Report: ", PrintColour.WHITE_BOLD);
		PrintColour.println(this.report_text==null?"":this.report_text, PrintColour.WHITE);
	}
	
	
}  