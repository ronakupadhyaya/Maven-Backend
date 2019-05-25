import java.io.IOException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class OpenCitations {
	OkHttpClient client;
	String OpenCitationsAPI;
	
	public OpenCitations(OkHttpClient client) {
		this.client = client;
		OpenCitationsAPI = "http://opencitations.net/index/coci/api/v1/citations/";
	}
	
	public String information(String DOI) throws IOException {
		String url = OpenCitationsAPI + DOI;
		
		Request request = new Request.Builder()
                .url(url)
                .build();

		Response response = client.newCall(request).execute();
		return response.body().string();
	}
}

class OpenCitationsResponse {
	String oci;
	String citing;
	String cited;
}
