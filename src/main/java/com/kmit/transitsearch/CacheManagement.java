package com.kmit.transitsearch;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Util;


public class CacheManagement {
    private static final int WAITING = 0;
    private static final int OPTIONSELECTED = 1;
    private static final int LOADFST = 2;
    private static final int SEARCHTT = 3;
    FST<CharsRef> transitTimeFST = null;

    private int state = WAITING;
    
    public String processInput(String theInput) throws IOException {
        String theOutput = null;
        
        if (state == WAITING) {
            theOutput = "Enter 1 to load an FST. Enter 2 to search for transit time. 3 to exit";
            state = OPTIONSELECTED;
        } else if (state == OPTIONSELECTED) {
            if (theInput.equalsIgnoreCase("1")) {
                theOutput = "Enter the Carrier Name comma Transit Type, example BD,Air";
                state = LOADFST;
            } else if (theInput.equalsIgnoreCase("2")) {
            	theOutput = "Enter From and To Zip code, example 500001,700002";
            	state = SEARCHTT;
            }
            else if (theInput.equalsIgnoreCase("3")) {
            	theOutput = "Bye";
            }
            else {
            	theOutput = "Select an option " +
			    "Try again";
            	state = WAITING;
            }
        } else if(state == LOADFST)
        {
        	String car_type [] = theInput.split(",");
        	loadFST (car_type[0], car_type[1]);
        	theOutput = "FST loaded successfully";
        	state = WAITING;
        	
        } else if(state == SEARCHTT)
        {
        	String zip_from_to [] = theInput.split(",");
        	if (transitTimeFST == null) 
        		theOutput = "Enter 1 to load an FST before you perform a search";
			CharsRef value = Util.get(transitTimeFST, new BytesRef(zip_from_to[0] + zip_from_to[1]));
			//System.out.println(value);
        	theOutput = "TT Value is:" + value;
        	state = WAITING;
        	
        } 
        return theOutput;
    }
    
    private void loadFST(String carrier, String transitType)
    {
    	CacheFST cache = new CacheFST(carrier, transitType);
    	try {
			transitTimeFST = cache.constructFST();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}