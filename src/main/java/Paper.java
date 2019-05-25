import java.util.ArrayList;

public class Paper {
	String title;
	String year;
	String DOI;
	ArrayList<Paper> references;
	ArrayList<String> authors;
	ArrayList<Paper> referees;
	
	public Paper(String DOI) {
		this.title = "";
		this.year = "";
		this.DOI = DOI;
		references = new ArrayList<Paper>(); 
		authors = new ArrayList<String>();
		referees = new ArrayList<Paper>();
	}
	
	public Paper(String title, String year, String DOI) {
		this.title = title;
		this.year = year;
		this.DOI = DOI;
		references = new ArrayList<Paper>(); 
		authors = new ArrayList<String>();
		referees = new ArrayList<Paper>();
	}
}
