/*******************************************************************
  * For more information regarding api login and file url uploading
 * refer to the following site for specific details on what commands
 * are needed for proper processing on the website end
 * http://astrometry.net/doc/net/api.html
 * 
 *******************************************************************/


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import java.net.*;

import org.apache.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Comms {
	
	private String apiKey, session, subid, jobid;
	Scanner in = new Scanner(System.in);
	HttpURLConnection conn;
	
	//Function to set api for the entire program
	void setAPI(String tmp){
		apiKey = tmp;
	}
	
	//This function sets the jobid for an image after it's been submitted
	void getJobID(String sub) throws IOException, ParseException{
		URL url = new URL("http://nova.astrometry.net/api/submissions/"+sub);
		JSONObject subs = new JSONObject();
		subs.put("subid", sub);
		
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(url.toString());
		
		String enc = URLEncoder.encode(subs.toString(), "utf-8");
		StringEntity ent = new StringEntity("request-json="+enc);

		//The following lines build the entity required to login
		post.setEntity(EntityBuilder.create()
				.setText("request-json="+enc).
				setContentType(ContentType.APPLICATION_FORM_URLENCODED).build());
		
		CloseableHttpResponse resp = client.execute(post);

		BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		String inLine;
		StringBuffer repo = new StringBuffer();
		while((inLine = reader.readLine()) != null){
			repo.append(inLine);
		}
		reader.close();
		System.out.println(repo.toString());
		client.close();
		resp.close();
		
        JSONParser par = new JSONParser();
        JSONObject tm = (JSONObject)par.parse(repo.toString());	//parse string buffer into json form
        jobid = tm.get("jobs").toString();	//sets jobid variable that will be used for current connection
        this.jobid = jobid.substring(1, jobid.length()-1);	//parses job string to save numbers only
        System.out.println(jobid);
		
		/***********************************************************************************************
		conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestMethod("POST");
		
        String enc = URLEncoder.encode(subs.toString(),"UTF-8");
        
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write("request-json="+enc);
        wr.flush();
		
        StringBuilder sb = new StringBuilder();
        int HttpResult = conn.getResponseCode();
        if(HttpResult == HttpURLConnection.HTTP_OK){
        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        	String line = null;
        	while ((line = br.readLine()) != null){  
        		//sb.append(line + "\n");
        		sb.append(line);
        	}
        	
	        br.close();  //closes BufferedReader
	        JSONParser par = new JSONParser();
	        JSONObject tm = (JSONObject)par.parse(sb.toString());	//parses string builder into json form
	        jobid = tm.get("jobs").toString();	//sets session variable that will be used for current connection
	        this.jobid = jobid.substring(1, jobid.length()-1);	//parses job string to save numbers only
	        System.out.println(""+sb.toString());	//Displays output of result
	        System.out.println(jobid);
        }
        else{
            System.out.println(conn.getResponseMessage());  
        }
        ******************************************************************************************/
	}
	
	//Function downloads a specific file from the given link
	void fileDown(String tmp, String ext) throws IOException{
		try{
			System.out.println("Contacting site...");
			URL url = new URL("http://nova.astrometry.net/annotated_full/"+tmp);
			
			System.out.println("Saving image to buffer...");
			long start = System.currentTimeMillis();
			BufferedImage bi = ImageIO.read(url);	//saves image from url to some memory?
			long end = System.currentTimeMillis();
			long tme = end - start;
			double sec = tme / 1000.0;
			System.out.println(sec);
			File oFile = new File(tmp+"."+ext);		//creates an output file according to the name
			
			System.out.println("Saving image to disk...");
			start = System.currentTimeMillis();
			ImageIO.write(bi, ext, oFile);		//writes the image in buffer to the outfile
			end = System.currentTimeMillis();
			tme = end - start;
			sec = tme / 1000.0;
			System.out.println(sec);
		}
		catch(Exception ex){
			System.out.println("Failed to get image");
		}
	}
	
	//Function to upload file in JPG format to site
	void fileUp(String location) throws ClientProtocolException, IOException{	
		File img = new File(location);
		FileBody image = new FileBody(img);
		
		//Following is a sample json file outline
		/************************
		 *Sending json: {"publicly_visible": "y", 
		 *"allow_modifications": "d", 
		 *"session": "YYY", 
		 *"allow_commercial_use": "d"}
		 *************************/
		
		//the following builds the json for this function, more settings can be added here
		JSONObject up = new JSONObject();
		up.put("publicly_visible", "y");
		up.put("allow_modifications", "d");
		up.put("session", session);
		up.put("allow_commercial_use", "d");

		String enc = URLEncoder.encode(up.toString(), "utf-8");	//encodes json file
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		MultipartEntityBuilder enti;	//NOT BEING USED
		
		//HttpEntity ent = MultipartEntityBuilder.create()//.setContentType(ContentType.MULTIPART_FORM_DATA)
			//	.addTextBody("request-json=", enc)
				//.setContentType(ContentType.DEFAULT_TEXT)
				//.addBinaryBody("octet-stream", img)
				//.addTextBody("request", "request-json="+enc, ContentType.TEXT_PLAIN)
				//.addBinaryBody("octet-stream", img, ContentType.APPLICATION_OCTET_STREAM, location)
			//	.build();

		HttpPost post = new HttpPost("http://nova.astrometry.net/api/upload");
		post.setEntity(MultipartEntityBuilder.create()
				.addTextBody("request-json=", enc, ContentType.TEXT_PLAIN)
				.addBinaryBody("octet-stream", img, ContentType.APPLICATION_OCTET_STREAM, location)
				.build());
/*
		post.setEntity(EntityBuilder.create()
				.setText("request-json="+enc).
				setContentType(ContentType.APPLICATION_FORM_URLENCODED).build());
*/		
		//post.setEntity(ent);
		
		CloseableHttpResponse resp = client.execute(post);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		String inLine;
		StringBuffer repo = new StringBuffer();
		while((inLine = reader.readLine()) != null){
			repo.append(inLine);
		}
		
		System.out.println(repo.toString());
	}
	
	//This will not upload a file, it will send a JSON with all the proper data and a URL link to the pic for processing
	//DOES NOT WORK WITH DROPBOX LINKS, REASON IS UNKNOWN!!!!
	void fileUpLink(String link) throws IOException, ParseException{
		URL destination = new URL("http://nova.astrometry.net/api/url_upload");

		JSONObject up = new JSONObject();
		up.put("session", session);	//inserts session tag into JSON
		up.put("url", link);		//inserts image url into JSON
				

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(destination.toString());
		
		String enc = URLEncoder.encode(up.toString(), "utf-8");
		StringEntity ent = new StringEntity("request-json="+enc);

		//The following lines build the entity required to login
		post.setEntity(EntityBuilder.create()
				.setText("request-json="+enc).
				setContentType(ContentType.APPLICATION_FORM_URLENCODED).build());
		
		CloseableHttpResponse resp = client.execute(post);

		BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		String inLine;
		StringBuffer repo = new StringBuffer();
		while((inLine = reader.readLine()) != null){
			repo.append(inLine);
		}
		reader.close();
		System.out.println(repo.toString());
		client.close();
		resp.close();
		
        JSONParser par = new JSONParser();
        JSONObject tm = (JSONObject)par.parse(repo.toString());	//parse string buffer into json form
        this.subid = tm.get("subid").toString();	//sets session variable that will be used for current connection
        System.out.println(subid);
		
		
		
		/*******************************************************************************************************		
		
		//{"session": "o7tpxha2ef7b7eedh6vpc74w8sdbe2ut", 
		//"url": "http://apod.nasa.gov/apod/image/1206/ldn673s_block1123.jpg", 
		//"scale_units": "degwidth", 
		//"scale_lower": 0.5, 
		//"scale_upper": 1.0, 
		//"center_ra": 290, 
		//"center_dec": 11, 
		//"radius": 2.0 }
		
		
		JSONObject up = new JSONObject();
		up.put("session", session);	//inserts session tag into JSON
		up.put("url", link);		//inserts image url into JSON
		
		conn = (HttpURLConnection)destination.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestMethod("POST");
		
        String enc = URLEncoder.encode(up.toString(),"UTF-8");
        
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write("request-json="+enc);
        wr.flush();
		
        StringBuilder sb = new StringBuilder();
        int HttpResult = conn.getResponseCode();
        if(HttpResult == HttpURLConnection.HTTP_OK){
        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        	String line = null;
        	while ((line = br.readLine()) != null){  
        		//sb.append(line + "\n");
        		sb.append(line);
        	}
        	
	        br.close();  //closes BufferedReader
	        JSONParser par = new JSONParser();
	        JSONObject tm = (JSONObject)par.parse(sb.toString());	//parses string builder into json form
	        this.subid = tm.get("subid").toString();	//sets session variable that will be used for current connection
	        System.out.println(""+sb.toString());	//Displays output of result
	        System.out.println(subid);
        }
        else{
            System.out.println(conn.getResponseMessage());  
        }
        ******************************************************************************************************/
	}
	//function to log into site
	void login() throws IOException, ParseException{
		URL url = new URL("http://nova.astrometry.net/api/login");	//URL for login site

		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://nova.astrometry.net/api/login");
		
		JSONObject api = new JSONObject();
		api.put("apikey", apiKey);
		String enc = URLEncoder.encode(api.toString(), "utf-8");
		StringEntity ent = new StringEntity("request-json="+enc);

		//The following lines build the entity required to login
		post.setEntity(EntityBuilder.create()
				.setText("request-json="+enc).
				setContentType(ContentType.APPLICATION_FORM_URLENCODED).build());
		
		CloseableHttpResponse resp = client.execute(post);

		BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		String inLine;
		StringBuffer repo = new StringBuffer();
		while((inLine = reader.readLine()) != null){
			repo.append(inLine);
		}
		reader.close();
		System.out.println(repo.toString());
		client.close();
		resp.close();
		
        JSONParser par = new JSONParser();
        JSONObject tm = (JSONObject)par.parse(repo.toString());	//parse string buffer into json form
        this.session = tm.get("session").toString();	//sets session variable that will be used for current connection
        System.out.println(session);
		
		
		
		/**************************************************************************
        	
        	// * The following code works but it uses local libraries
        	// * the switch to Apache library was done in order to shorten code
        	// * and to aid in sending images from disk, that code is in another function
        
        //HttpURLConnection conn = (HttpURLConnection)url.openConnection();	

		conn = (HttpURLConnection)url.openConnection();
		
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Accept","application/json");
        conn.setRequestMethod("POST");
       
        JSONObject api = new JSONObject();	//Creates JSON object needed for login
        api.put("apikey",apiKey);			//Inserts required data to JSON file
        
        //Before POST, JSON must be encoded so the site will recognize it, VERY IMPORTANT!!!
        String enc = URLEncoder.encode(api.toString(),"UTF-8");
        
        //Creates stream writer to write string to site
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write("request-json="+enc);	//This sets the required format that the site asks for, anything else won't work!!!
        wr.flush();		//clears the stream writer
      
        StringBuilder sb = new StringBuilder();  
        int HttpResult = conn.getResponseCode(); 
        
        //Checks to see if the site accepted the POST, if OK code is returned then this is done
        if(HttpResult == HttpURLConnection.HTTP_OK){
        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));  
        	String line = null;  
        	while ((line = br.readLine()) != null){  
        		//sb.append(line + "\n");
        		sb.append(line);
        	}  

	        br.close();  //closes BufferedReader
	        JSONParser par = new JSONParser();
	        JSONObject tm = (JSONObject)par.parse(sb.toString());	//parses string builder into json form
	        this.session = tm.get("session").toString();	//sets session variable that will be used for current connection
	        System.out.println(""+sb.toString());	//Displays output of result
	        System.out.println(session);

        }
        else{
            System.out.println(conn.getResponseMessage());  
        }
        ********************************************************************/
	}
}