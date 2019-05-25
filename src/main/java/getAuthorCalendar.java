import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

import net.fortuna.ical4j.model.*;

/**
 * Servlet implementation class getAuthorCalendar
 */
@WebServlet("/getAuthorCalendar")
public class getAuthorCalendar extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public getAuthorCalendar() {
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
		ArrayList<Event> authorEvents = CustomCalendar.getEvents(authorMap);

		Calendar authorCalendar = new Calendar();
		try {
			authorCalendar = CustomCalendar.getICal(authorEvents);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		response.setContentType("text/calendar");
        response.setHeader("Content-disposition","attachment; filename=authorcalendar.ics");
        System.out.println(authorCalendar.toString());
        OutputStream outputStream = response.getOutputStream();
        InputStream inputStream = new ByteArrayInputStream(authorCalendar.toString().getBytes());
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0){
           outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.flush();
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
