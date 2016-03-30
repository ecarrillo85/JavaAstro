/*******************************************************************************
 	An API key is required to use the program, you can get one free
	from the site nova.astrometry.net/api all you need to do is
	sign in using any of the accounts available.
	Once you have the api key, just set it to the login 
	function and it will login from there. All functions work
	except for the fileUp function, which isn't able to upload
	images from disk yet.	 
*******************************************************************************/

import java.io.*;

import org.apache.http.HttpException;
import org.json.simple.parser.ParseException;

public class Astrometry {
    public static void main(String args[]) throws IOException, ParseException, HttpException{
    	
    	Comms com = new Comms();
    	com.setAPI();	//api key goes here as a string
    	com.login();
    	//com.fileUpLink("https://i.imgur.com/kNa5CZA.jpg");
    	//com.fileDown("1482113","png");
    	//com.getJobID("1008464");
    }
}
