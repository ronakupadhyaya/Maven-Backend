

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet implementation class getText
 */
@WebServlet("/getText")
public class getText extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public getText() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		StringBuilder inputBuffer = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        inputBuffer.append(line);
	    }
	    String data = inputBuffer.toString();
	    JsonParser jsonParser = new JsonParser();
	    JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
	    JsonElement jsonElement = jsonObject.get("authors");
	    ArrayList<String> authors = new Gson().fromJson(jsonElement, ArrayList.class);
	    
	    ServletContext context = getServletContext();
		String fullPath = context.getRealPath("/WEB-INF/files/JSM2019-Online-Program.htm");
	    File file = new File(fullPath);
		HTMLParser htmlparser = new HTMLParser(file);
		htmlparser.parse();
		
		HashMap<String, HashSet<Talk>> speakerMap = htmlparser.getSpeakerMap(authors);
		HashMap<String, HashSet<Talk>> authorMap = htmlparser.getAuthorMap(authors);
		HashMap<String, HashSet<Talk>> selfMap = htmlparser.getSelfMap("Jacob Bien");
		cleanMaps(speakerMap, authorMap);
		
		ArrayList<Talk> allTalks = mergeMaps(speakerMap, authorMap);
		Collections.sort(allTalks, new TalkComparator());
		
		StringBuilder sb = new StringBuilder();
		writeToStringBuilder(sb, allTalks);
		
		response.setContentType("text/plain");
        response.setHeader("Content-disposition","attachment; filename=schedule.txt");
        
        OutputStream outputStream = response.getOutputStream();
        InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes());
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0){
           outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.flush();
	}
	
	public static ArrayList<Talk> mergeMaps(HashMap<String, HashSet<Talk>> speakerMap, HashMap<String, HashSet<Talk>> authorMap) {
		ArrayList<Talk> allTalks = new ArrayList<Talk>();
		for(String name: speakerMap.keySet()) {
			HashSet<Talk> talks = speakerMap.get(name);
			for(Talk talk: talks) {
				talk.name = name;
				talk.type = "Speaker";
				allTalks.add(talk);
			}
		}
		for(String name: authorMap.keySet()) {
			HashSet<Talk> talks = authorMap.get(name);
			for(Talk talk: talks) {
				talk.name = name;
				talk.type = "Author";
				allTalks.add(talk);
			}
		}
		return allTalks;
	}
	
	public static void writeToStringBuilder(StringBuilder sb, ArrayList<Talk> allTalks) {
		for(int i = 0; i < allTalks.size(); i++) {
			Talk talk = allTalks.get(i);
			sb.append(talk.date + "\n");
			if(talk.type.equals("Speaker")) {
				sb.append(talk.name + " - " + talk.topic + " (Speaker) \n");
			}
			else {
				sb.append(talk.name + " - " + talk.topic + "\n");
			}
			sb.append(talk.startTime + " - " + talk.endTime + "\n");
			sb.append("\n\n");
		}		
	}
	
	public static void cleanMaps(HashMap<String, HashSet<Talk>> speakerMap, HashMap<String, HashSet<Talk>> authorMap) {
		HashSet<Talk> allTalks = new HashSet<Talk>();
		for(String speaker: speakerMap.keySet()) {
			HashSet<Talk> talks = speakerMap.get(speaker);
			for(Talk talk: talks) {
				allTalks.add(talk);
			}
		}
		for(String author: authorMap.keySet()) {
			HashSet<Talk> talks = authorMap.get(author);
			HashSet<Talk> talksCopy = new HashSet<Talk>(talks);
			for(Talk talk: talksCopy) {
				if(allTalks.contains(talk)) {
					talks.remove(talk);
				}
				else {
					allTalks.add(talk);
				}
			}
		}
	}

}
