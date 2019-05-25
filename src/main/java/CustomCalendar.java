import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Version;

public class CustomCalendar {

	public static ArrayList<Event> getEvents(HashMap <String, HashSet<Talk>> map) {
		ArrayList<Event> events = new ArrayList<Event>();
		for(String author: map.keySet()) {
			HashSet<Talk> talks = map.get(author);
			for(Talk talk: talks) {
				Event event = getEvent(author, talk);
				events.add(event);
			}
		}
		return events;
	}

	public static String formatDate(String dateString, String timeString) {
		String[] dateArray = dateString.split("/");
		int month = Integer.parseInt(dateArray[0]);
		int day = Integer.parseInt(dateArray[1]);
		int year = Integer.parseInt(dateArray[2]);

		String time = timeString.split("\\s+")[0];
		String[] timeArray = time.split(":");
		int hours = Integer.parseInt(timeArray[0]);
		int minutes = Integer.parseInt(timeArray[1]);
		if (timeString.contains("PM") && hours != 12) {
			hours += 12;
		}

		GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day, hours, minutes);
		Date date = calendar.getTime();

		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		String dateAsISOString = df.format(date);
		return dateAsISOString;
	}
	
	private static Event getEvent(String author, Talk talk) {
		String date = talk.date;
		String startTime = talk.startTime;
		String endTime = talk.endTime;
		
		String title = author + " - " + talk.topic;		
		String start = formatDate(date, startTime);
		String end = formatDate(date, endTime);

		return new Event(title, start, end);
	}
	
	public static Calendar getICal(ArrayList<Event> events) throws ParseException, SocketException {
		Calendar calendar = new Calendar();
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		for(int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			System.out.println(i);

			DateTime start = new DateTime(event.start);
			DateTime end = new DateTime(event.end);
			String title = event.title;
			
			VEvent vEvent = new VEvent(start, end, title);
//			UidGenerator uidGenerator = new UidGenerator(Integer.toString(i));
//			vEvent.getProperties().add(uidGenerator.generateUid());
			calendar.getComponents().add(vEvent);
		}
		
		return calendar;
	}
	
	public static String getICalString(ArrayList<Event> events) {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN:VCALENDAR \r\n");
		sb.append("VERSION:1.0 \r\n");
		sb.append("CALSCALE:GREGORIAN \r\n");
		for(int i = 0; i < events.size(); i++) {
			sb.append("BEGIN:VEVENT \r\n");
			Event event = events.get(i);
			System.out.println(i);
			sb.append("DTSTAMP:20190515T222247Z \r\n");
			sb.append("DTSTART:" + event.start + " \r\n");
			sb.append("DTEND:" + event.start + " \r\n");
			sb.append("SUMMARY:" + event.title + " \r\n");
			sb.append("END:VEVENT \r\n");
		}
		sb.append("END:VCALENDAR \r\n");
		return sb.toString();
	}

}

class Event {
	String title;
	String start;
	String end;
	
	public Event(String title, String start, String end) {
		this.title = title;
		this.start = start;
		this.end = end;
	}
}