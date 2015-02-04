package org.orderofthebee.workflow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.xml.security.utils.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.orderofthebee.workflow.meta.TestMeta;

public class WebScriptHelper {

	private static Logger log = Logger.getLogger(WebScriptHelper.class);

	private CloseableHttpClient client;

	public WebScriptHelper() {
	}

	public String genUniqueId() {
		return UUID.randomUUID().toString();
	}
	
	private String makeUnique(String url) {
		String uniq = "";
		
		if(url.contains("?")){
			uniq = url + "&uniqueId=" + genUniqueId();
		} else {
			uniq = url + "?uniqueId=" + genUniqueId();
		}
		
		return uniq;
	}

	public WebScriptHelper(final String username, final String password) {

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(TestMeta.HOST,
				TestMeta.PORT, null), new UsernamePasswordCredentials(username,
				password));

		client = HttpClients.custom()
				.setDefaultCredentialsProvider(credsProvider).build();

	}

	// http://stackoverflow.com/a/4308662/370191
	public String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	// http://stackoverflow.com/a/4308662/370191
	public JSONObject readJsonFromUrl(String url) throws IOException,
			JSONException {

		url = makeUnique(url);
		log.warn("readJsonFromUrl " + url);
		
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		InputStream is = response.getEntity().getContent();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public JSONObject readJsonFromInputStream(InputStream is)
			throws IOException, JSONException {
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public InputStream post(String url) throws IOException {
		return this.post(url, null);
	}

	public InputStream post(String url, Map<String, String> bodyParameters)
			throws IOException {
		
		url = makeUnique(url);
		log.warn("post " + url);
		
		HttpPost post = new HttpPost(url);

		List<NameValuePair> data = new ArrayList<NameValuePair>();
		if (bodyParameters != null) {
			Iterator<String> it = bodyParameters.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				data.add(new BasicNameValuePair(key, bodyParameters.get(key)));
			}

		}
		post.setEntity(new UrlEncodedFormEntity(data));

		HttpResponse response = client.execute(post);
		log.info("Response Code : " + response.getStatusLine().getStatusCode());

		return response.getEntity() == null ? null : response.getEntity()
				.getContent();
	}

	public String get(String url) throws IOException {
		
		url = makeUnique(url);
		log.warn("get " + url);
		
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		String text = null;
		InputStream is = response.getEntity().getContent();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			text = readAll(rd);
		} finally {
			is.close();
		}
		return text;
	}



	public JSONObject readJsonFromUrlPost(String url) throws IOException,
			JSONException {
		return readJsonFromInputStream(post(url));
	}

}