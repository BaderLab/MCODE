package org.cytoscape.mcode.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * Tests CyRest commands provided by MCODE.
 * Cytoscape must be running with MCODE installed for this to work. You also need to set a network as current first.
 */
public class TestCyRest {

	private static final String URL = "http://localhost:1234/v1/commands/mcode/";
	private static final TestCyRest test = new TestCyRest();
	
	private final HttpClient client = HttpClient.newHttpClient();
	
	public static void main(String[] args) throws Exception {
		test.testClusterCommand();
		test.testViewCommand();
	}

	/**
	 * Run the clustering algorithm.
	 */
	private void testClusterCommand() throws Exception {
		var request = HttpRequest.newBuilder()
				.uri(URI.create(URL + "cluster"))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(
		    		  "{\n" + 
		    		  "  \"degreeCutoff\": \"2\",\n" + 
		    		  "  \"fluff\": \"true\",\n" + 
		    		  "  \"fluffNodeDensityCutoff\": \"0.1\",\n" + 
		    		  "  \"haircut\": \"true\",\n" + 
		    		  "  \"includeLoops\": \"false\",\n" + 
		    		  "  \"kCore\": \"2\",\n" + 
		    		  "  \"maxDepthFromStart\": \"100\",\n" + 
		    		  "  \"network\": \"current\",\n" + 
		    		  "  \"nodeScoreCutoff\": \"0.2\",\n" + 
		    		  "  \"scope\": \"NETWORK\"\n" + 
		    		  "}"))
				.build();
		client.sendAsync(request, BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenAccept(System.out::println)
				.join();
	}
	
	/**
	 * Creates a network and view from a cluster. 
	 */
	private void testViewCommand() throws Exception {
		var request = HttpRequest.newBuilder()
				.uri(URI.create(URL + "view"))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(
			    		  "{\n" + 
			    		  "  \"id\": \"1\",\n" +  // result id
			    		  "  \"rank\": \"2\"\n" + // cluster number
			    		  "}"))
				.build();
		client.sendAsync(request, BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenAccept(System.out::println)
				.join();
	}
}
