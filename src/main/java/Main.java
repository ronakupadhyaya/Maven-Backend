import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		ArrayList<String> citingAuthors = new ArrayList<String>();
		ArrayList<String> citedAuthors = new ArrayList<String>();
				
		
//		File file = new File("JSM2019-Online-Program.htm");
//		HTMLParser htmlparser = new HTMLParser(file);
//		htmlparser.parse();
		
//		HashMap<String, HashSet<Talk>> speakerMap = htmlparser.getSpeakerMap(citedAuthors);
//		HashMap<String, HashSet<Talk>> authorMap = htmlparser.getAuthorMap(citedAuthors);
//		GoogleCalendar googleCalendar = new GoogleCalendar(speakerMap, authorMap);
		
	}
	
	public static void writeInterestingTalks(HashMap<String, HashSet<Talk>> interestingTalks) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter printWriter = new PrintWriter("interestingTalks.txt", "UTF-8");
		for(String author: interestingTalks.keySet()) {
			printWriter.println(author + "\n");
			HashSet<Talk> talks = interestingTalks.get(author);
			for(int i = 0; i < talks.size(); i++) {
			}
			for(Talk talk: talks) {
				printWriter.println(talk.toString());
			}
		}
		printWriter.close();
	}
	
	public static String parseLine(String line) {
		String[] result = line.split(",");
		String name = result[0].split(":")[1].trim();
		return name;
	}
}
