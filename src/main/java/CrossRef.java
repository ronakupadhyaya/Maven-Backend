import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


public class CrossRef {
	OkHttpClient client;
	String CrossRefAPI;
	
	public CrossRef(OkHttpClient client) {
		this.client = new OkHttpClient();
		CrossRefAPI = "https://api.crossref.org/works/";
	}
	
	public String information(String DOI) throws IOException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(CrossRefAPI + DOI).newBuilder();
		urlBuilder.addQueryParameter("mailto", "rdupadhy@usc.edu");
		String url = urlBuilder.build().toString();
		
		Request request = new Request.Builder()
                .url(url)
                .build();
		
		Response response = client.newCall(request).execute();
		return response.body().string();
	}
}

class CrossRefResponse {
	class Reference {
		String key;
		String DOI;
		@SerializedName("article-title")
		String articleTitle;
		String year;
		String author;
		@SerializedName("journal-title")
		String journalTitle;
	}
	class Author {
		String given;
		String family;
	}
	class Message {
		Reference[] reference;
		Author[] author;
	}
	Message message;
}