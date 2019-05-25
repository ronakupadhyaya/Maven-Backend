import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
 * Servlet implementation class getSchedule
 */
@WebServlet("/getSchedule")
public class getSchedule extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public getSchedule() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		StringBuilder buffer = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        buffer.append(line);
	    }
	    String data = buffer.toString();
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
		ArrayList<Event> speakerEvents = CustomCalendar.getEvents(speakerMap);
		ArrayList<Event> authorEvents = CustomCalendar.getEvents(authorMap);
		ArrayList<Event> selfEvents = CustomCalendar.getEvents(selfMap);
		
		HashMap<String, ArrayList<Event>> schedule = new HashMap<String, ArrayList<Event>>();
		schedule.put("Speaker", speakerEvents);
		schedule.put("Author", authorEvents);
		schedule.put("Self", selfEvents);
		
		String json = new Gson().toJson(schedule);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(json);
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

class GetScheduleBody {
	ArrayList<String> authors;
}
