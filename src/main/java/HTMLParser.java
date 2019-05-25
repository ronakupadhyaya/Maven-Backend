import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLParser {
	File file;
	HashMap<String, Speaker> speakersMap;
	HashMap<String, Speaker> authorsMap;

	
	public HTMLParser(File file) {
		this.file = file;
		this.speakersMap = new HashMap<String, Speaker>();
		this.authorsMap = new HashMap<String, Speaker>();
	}
	
	public HashMap<String, HashSet<Talk>> getSelfMap(String name) {
		HashMap<String, HashSet<Talk>> result = new HashMap<String, HashSet<Talk>>();
		HashSet<Talk> resultSet = new HashSet<Talk>();
		for(String speakerString: speakersMap.keySet()) {
			if(isIdentical(name, speakerString)) {
				Speaker speaker = speakersMap.get(speakerString);
				HashSet<Talk> talks = speaker.talks;
				resultSet.addAll(talks);
				break;
			}
		}
		for(String authorString: authorsMap.keySet()) {
			if(isIdentical(name, authorString)) {
				Speaker author = authorsMap.get(authorString);
				HashSet<Talk> talks = author.talks;
				resultSet.addAll(talks);
				break;
			}
		}
		result.put(name, resultSet);
		return result;
	}
	
	public HashMap<String, HashSet<Talk>> getSpeakerMap(ArrayList<String> names) {
		HashMap<String, HashSet<Talk>> result = new HashMap<String, HashSet<Talk>>();
		for(int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			for(String speakerString: speakersMap.keySet()) {
				if(isIdentical(name, speakerString)) {
					Speaker speaker = speakersMap.get(speakerString);
					HashSet<Talk> talks = speaker.talks;
					result.put(speakerString, talks);
					break;
				}
			}
		}
		return result;
	}
	
	public HashMap<String, HashSet<Talk>> getAuthorMap(ArrayList<String> names) {
		HashMap<String, HashSet<Talk>> result = new HashMap<String, HashSet<Talk>>();
		for(int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			for(String authorString: authorsMap.keySet()) {
				if(isIdentical(name, authorString)) {
					Speaker author = authorsMap.get(authorString);
					HashSet<Talk> talks = author.talks;
					result.put(authorString, talks);
					break;
				}
			}
		}
		return result;
	}
	
	public boolean isIdentical(String name, String author) {
		String[] nameSplit = name.replace(".", "").split("\\s+");
		String[] authorSplit = author.replace(".", "").split("\\s+");
		if(nameSplit.length < authorSplit.length) {
			int j = 0;
			for(int i = 0; i < nameSplit.length; i++) {
				if(j == authorSplit.length) {
					return false;
				}
				if(!isIdenticalHelper(nameSplit[i], authorSplit[j])) {
					j++;
				}
				else {
					i++;
					j++;
				}
			}
			return j == authorSplit.length;
		}
		else if(authorSplit.length < nameSplit.length) {
			int j = 0;
			for(int i = 0; i < authorSplit.length; i++) {
				if(j == nameSplit.length) {
					return false;
				}
				if(!isIdenticalHelper(authorSplit[i], nameSplit[j])) {
					j++;
				}
				else {
					i++;
					j++;
				}
			}
			return j == nameSplit.length;
		}
		else {
			for(int i = 0; i < nameSplit.length; i++) {
				if(!isIdenticalHelper(nameSplit[i], authorSplit[i])) {
					return false;
				}
			}
			return true;
		}
	}

	private boolean isIdenticalHelper(String name, String author) {
		if(!((name.length() == 1 && author.startsWith(name)) || 
				(author.length() == 1 && name.startsWith(author)) ||
				 name.equals(author))) {
			 return false;
		 }
		return true;
	}
	
	public void parse() throws IOException {
		Document document = Jsoup.parse(file, "utf-8"); 
		PrintWriter parsedPrintWriter = new PrintWriter("parsed.txt", "UTF-8");
		Elements tables = document.select("table table");
		for(Element table: tables) {
			Elements rows = table.select("tr");
			Element index = rows.get(0).select("a").get(0);
			String titleString = rows.get(1).select("td").get(0).text();
			String datestampString = rows.get(0).select("td").get(1).text();
			String dateString = datestampString.split(",")[1].trim();
			String endTimeString = datestampString.split(",")[2].split("-")[1].trim();
			if(!index.text().trim().matches("\\d+") && !index.text().contains("!")) {
				continue;
			}
			ArrayList<Talk> talks = new ArrayList<Talk>();
			for(int i = 1; i < rows.size(); i++) {
				Element row = rows.get(i);
				if(row.text().contains(":") && (row.text().contains(" AM ") || row.text().contains(" PM "))) {
					String timeString = row.select("td").get(0).ownText();
					String talkString = row.select("td").get(1).text();
					String topicString = row.select("td").get(1).select("a").text();
					Talk talk = new Talk("TBA", topicString, timeString, dateString);
					talks.add(talk);

					int topicEndIndex = talkString.indexOf(topicString) + topicString.length();
					String authorsString = talkString.substring(topicEndIndex);
					String[] authorsArray = authorsString.split(";");
					for(int j = 0; j < authorsArray.length; j++) {
						String authorString = authorsArray[j].trim().split(",")[0];
						Speaker author;
						if(j == 0) {
							if(speakersMap.containsKey(authorString)) {
								author = speakersMap.get(authorString);
				    		}
				    		else {
				    			author = new Speaker(authorString);
				    			speakersMap.put(authorString, author);
				    		}
						}
						else {
							if(authorsMap.containsKey(authorString)) {
								author = authorsMap.get(authorString);					
				    		}
				    		else {
				    			author = new Speaker(authorString);
				    			authorsMap.put(authorString, author);
				    		}
						}	
						author.talks.add(talk);
					}										
				}
			}
			addEndTimes(talks, endTimeString);
		}		
		parsedPrintWriter.close();
	}
	
	private void addEndTimes(ArrayList<Talk> talks, String endTimeString) {
		for(int i = 0; i < talks.size(); i++) {
			Talk currentTalk = talks.get(i);
			if(i == talks.size() - 1) {
				currentTalk.endTime = endTimeString;
			}
			else {
				Talk nextTalk = talks.get(i + 1);
				currentTalk.endTime = nextTalk.startTime;
			}
		}
	}
}
