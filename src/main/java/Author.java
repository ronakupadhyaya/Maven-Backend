import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

public class Author {
	String name;
	ArrayList<Paper> papers;
	OkHttpClient client;
	ArrayList<Result> citedAuthors;
	ArrayList<Result> citingAuthors;
	int refereeCount;
	
	public Author(String name) throws IOException {
		this.name = name;
		this.papers = new ArrayList<Paper>();
		citedAuthors = new ArrayList<Result>();
		citingAuthors = new ArrayList<Result>();
		
		refereeCount = 0;
		client = new OkHttpClient();
		setPapers();
		setReferences();
		setReferees();
	}
	
	public void setPapers() throws IOException {
		String expr = "Composite(AA.AuN=='" + name.toLowerCase() + "')";
		String attributes = "Ti,Y,E.DOI";
		
		MicrosoftAcademicGraph microsoftAcademicGraph = new MicrosoftAcademicGraph(client);
		String jsonData = microsoftAcademicGraph.evaluate(expr, attributes);		
		
		Gson gson = new Gson();
		MicrosoftAcademicGraphResponse microsoftAcademicGraphResponse = gson.fromJson(jsonData, MicrosoftAcademicGraphResponse.class);
		if(microsoftAcademicGraphResponse != null && microsoftAcademicGraphResponse.entities != null) {
			for(int i = 0; i < microsoftAcademicGraphResponse.entities.length; i++) {
				System.out.println("setpapers: " + i);
				String title = microsoftAcademicGraphResponse.entities[i].Ti;
				String year = microsoftAcademicGraphResponse.entities[i].Y;
				String DOI = microsoftAcademicGraphResponse.entities[i].DOI;
				Paper paper = new Paper(title, year, DOI);
				papers.add(paper);
			}
		}
	}
	
	public void setReferences() throws IOException {
		CrossRef crossRef = new CrossRef(client);
		for(int i = 0; i < papers.size(); i++) {
			System.out.println("setreferences: " + i);
			Paper paper = papers.get(i);
			String paperDOI = paper.DOI;
			if(paperDOI != null) {
				deserializeReferences(crossRef, paper, false);
				ArrayList<Paper> references = paper.references;
				for(int j = 0; j < references.size(); j++) {
					Paper reference = references.get(j);
					String referenceDOI = reference.DOI;
					if(referenceDOI != null) {
						deserializeReferences(crossRef, reference, true);
					}
				}
			}
		}
		setCitedAuthors();
	}
	
	public void deserializeReferences(CrossRef crossRef, Paper paper, boolean referencePaper) throws IOException {
		ArrayList<Paper> references = new ArrayList<Paper>();
		ArrayList<String> authors = new ArrayList<String>();
		
		String jsonData = crossRef.information(paper.DOI);
		Gson gson = new Gson();
		CrossRefResponse crossRefResponse = gson.fromJson(jsonData, CrossRefResponse.class);
		
		if(!referencePaper) {
			if(crossRefResponse != null && crossRefResponse.message != null && crossRefResponse.message.reference != null) {
				for(int i = 0; i < crossRefResponse.message.reference.length; i++) {
					String title = crossRefResponse.message.reference[i].articleTitle;
					String year = crossRefResponse.message.reference[i].year;
					String DOI = crossRefResponse.message.reference[i].DOI;
					Paper reference = new Paper(title, year, DOI);
					references.add(reference);
				}
			}
			paper.references = references;
		}
		
		if(crossRefResponse != null && crossRefResponse.message != null && crossRefResponse.message.author != null) {
			for(int i = 0; i < crossRefResponse.message.author.length; i++) {
				String name = crossRefResponse.message.author[i].given + " " + crossRefResponse.message.author[i].family;
				authors.add(name);
			}
		}
		paper.authors = authors;
	}
	
	public void setCitedAuthors() {
		ArrayList<Result> citedAuthors = new ArrayList<Result>();
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		for(int i = 0; i < papers.size(); i++) {
			Paper paper = papers.get(i);
			ArrayList<Paper> references = paper.references;
			for(int j = 0; j < references.size(); j++) {
				Paper reference = references.get(j);
				ArrayList<String> authors = reference.authors;
				for(int k = 0; k < authors.size(); k++) {
					String author = authors.get(k);
					if(counts.containsKey(author)) {
						counts.put(author, counts.get(author) + 1);
					}
					else {
						counts.put(author, 1);
					}
				}
			}
		}
		
		for(String author: counts.keySet()) {
			Result citedAuthor = new Result(author, counts.get(author));
			citedAuthors.add(citedAuthor);
		}
		
		Collections.sort(citedAuthors, new CitedAuthorComparator());
		citedAuthors = mergeAuthors(citedAuthors);
		this.citedAuthors = citedAuthors;
	}
	
	public void setReferees() throws IOException {
		OpenCitations opencitations = new OpenCitations(client);
		CrossRef crossRef = new CrossRef(client);
		for(int i = 0; i < papers.size(); i++) {
			System.out.println("setreferees: " + i);
			Paper paper = papers.get(i);
			String paperDOI = paper.DOI;
			if(paperDOI != null) {	
				deserializeReferees(opencitations, paper);
				ArrayList<Paper> referees = paper.referees;
				for(int j = 0; j < referees.size(); j++) {
					Paper referee = referees.get(j);
					String refereeDOI = referee.DOI;
					if(refereeDOI != null) {
						deserializeReferences(crossRef, referee, true);
					}
				}
			}
		}
		setCitingAuthors();
	}
	
	public void deserializeReferees(OpenCitations opencitations, Paper paper) throws IOException {
		if(refereeCount > 30) {
			return;
		}
		ArrayList<Paper> referees = new ArrayList<Paper>();
		
		String jsonData = opencitations.information(paper.DOI);
		Gson gson = new Gson();
		OpenCitationsResponse[] openCitationsResponses = gson.fromJson(jsonData, OpenCitationsResponse[].class);
		
		if(openCitationsResponses != null) {
			for(int i = 0; i < openCitationsResponses.length; i++) {
				refereeCount++;
				if(refereeCount > 30) {
					break;
				}
				Paper referee = new Paper(openCitationsResponses[i].citing);
				referees.add(referee);
			}
		}
		paper.referees = referees;
	}
	
	public void setCitingAuthors() {
		ArrayList<Result> citingAuthors = new ArrayList<Result>();
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		for(int i = 0; i < papers.size(); i++) {
			Paper paper = papers.get(i);
			ArrayList<Paper> referees = paper.referees;
			for(int j = 0; j < referees.size(); j++) {
				Paper referee = referees.get(j);
				ArrayList<String> authors = referee.authors;
				for(int k = 0; k < authors.size(); k++) {
					String author = authors.get(k);
					if(counts.containsKey(author)) {
						counts.put(author, counts.get(author) + 1);
					}
					else {
						counts.put(author, 1);
					}
				}
			}
		}
		
		for(String author: counts.keySet()) {
			Result citingAuthor = new Result(author, counts.get(author));
			citingAuthors.add(citingAuthor);
		}
		
		Collections.sort(citingAuthors, new CitedAuthorComparator());
		citingAuthors = mergeAuthors(citingAuthors);
		this.citingAuthors = citingAuthors;
	}
	
	public static ArrayList<Result> mergeAuthors(ArrayList<Result> authors) {
		for(int i = 0; i < authors.size(); i++) {
			Result author = authors.get(i);
			String authorName = author.name;
			for(int j = 0; j < i; j++) {
				Result check = authors.get(j);
				String checkName = check.name;
				String merge = merge(authorName, checkName);
				if(merge != null) {
					check.name = merge;
					check.count += author.count;
					authors.remove(i);
					i--;
					break;
				}
			}
		}
		return authors;
	}
	
	private static String merge(String authorName, String checkName) {
		 String[] authorSplit = authorName.replace(".", "").split("\\s+");
		 String[] checkSplit = checkName.replace(".", "").split("\\s+");
		 StringBuilder stringBuilder = new StringBuilder();
		 String toInsert;
		 
		 if(authorSplit.length > checkSplit.length) {
			 int j = 0;
			 for(int i = 0; i < checkSplit.length;) {
				 if(j == authorSplit.length) {
					 return null;
				 }
				 if(!isIdentical(authorSplit[j], checkSplit[i])) {
					 toInsert = authorSplit[j];
					 j++;
				 }
				 else {
					 toInsert = authorSplit[j].length() >= checkSplit[i].length() ? authorSplit[j] : checkSplit[i];
					 i++;
					 j++;
				 }
				 stringBuilder.append(toInsert);
				 stringBuilder.append(toInsert.length() == 1 ? ". " : " ");
			 }
			 return j == authorSplit.length ? stringBuilder.toString() : null;
		 }
		 
		 if(checkSplit.length > authorSplit.length) {
			 int j = 0;
			 for(int i = 0; i < authorSplit.length;) {
				 if(j == checkSplit.length) {
					 return null;
				 }
				 if(!isIdentical(checkSplit[j], authorSplit[i])) {
					 toInsert = checkSplit[j];
					 j++;
				 }
				 else {
					 toInsert = checkSplit[j].length() >= authorSplit[i].length() ? checkSplit[j] : authorSplit[i];
					 i++;
					 j++;
				 }
				 stringBuilder.append(toInsert);
				 stringBuilder.append(toInsert.length() == 1 ? ". " : " ");
			 }
			 return j == checkSplit.length ? stringBuilder.toString() : null;
		 }
		 
		 for(int i = 0; i < authorSplit.length; i++) {
			 if(!isIdentical(checkSplit[i], authorSplit[i])) {
				 return null;
			 }
			 else {
				 toInsert = checkSplit[i].length() >= authorSplit[i].length() ? checkSplit[i]: authorSplit[i];
				 stringBuilder.append(toInsert);
				 stringBuilder.append(toInsert.length() == 1 ? ". " : " ");
			 }
		 }
		 
		 return stringBuilder.toString().trim();
	}
	
	private static boolean isIdentical(String checkWord, String authorWord) {
		if(!((checkWord.length() == 1 && authorWord.startsWith(checkWord)) || 
				(authorWord.length() == 1 && checkWord.startsWith(authorWord)) ||
				 checkWord.equals(authorWord))) {
			 return false;
		 }
		return true;
	}
		
	
	/*
	 * Print functions
	 */
	
	public void printPapers() {
		for(int i = 0; i < papers.size(); i++) {
			Paper paper = papers.get(i);
			printPaper(paper);
		}
	}
	
	public void printReferences(Paper paper) {
		ArrayList<Paper> references = paper.references;
		for(int i = 0; i < references.size(); i++) {
			Paper reference = references.get(i);
			printPaper(reference);
		}
	}
	
	public void printAuthors(Paper paper) {
		ArrayList<String> authors = paper.authors;
		for(int i = 0; i < authors.size(); i++) {
			String author = authors.get(i);
			System.out.println(author);
		}
	}
	
	public void printPaper(Paper paper) {
		System.out.println("Title: " + paper.title + ", Year: " + paper.year + ", DOI: " + paper.DOI);
	}
	
	public void printCitedAuthors() {
		for(int i = 0; i < citedAuthors.size(); i++) {
			Result citedAuthor = citedAuthors.get(i);
			System.out.println("Author: " + citedAuthor.name + ", Count: " + citedAuthor.count);
		}
	}
	
	public void printCitingAuthors() {
		for(int i = 0; i < citingAuthors.size(); i++) {
			Result citingAuthor = citingAuthors.get(i);
			System.out.println("Author: " + citingAuthor.name + ", Count: " + citingAuthor.count);
		}
	}
	
	public void writeCitedAuthors() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter printWriter = new PrintWriter("citedauthors.txt", "UTF-8");
		for(int i = 0; i < citedAuthors.size(); i++) {
			Result citedAuthor = citedAuthors.get(i);
			printWriter.println("Author: " + citedAuthor.name + ", Count: " + citedAuthor.count);
		}
		printWriter.close();
	}
	
	public void writeCitingAuthors() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter printWriter = new PrintWriter("citingauthors.txt", "UTF-8");
		for(int i = 0; i < citingAuthors.size(); i++) {
			Result citingAuthor = citingAuthors.get(i);
			printWriter.println("Author: " + citingAuthor.name + ", Count: " + citingAuthor.count);
		}
		printWriter.close();
	}
}

