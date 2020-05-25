package com.kmit.transitsearch;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Util;

import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;


public class CacheManagement {
    private static final int WAITING = 0;
    private static final int OPTIONSELECTED = 1;
    private static final int LOADFST = 2;
    private static final int SEARCHTTRANSITTIME = 3;
   private static final int CACHESTATS = 3;
    CacheFST cache = new CacheFST();

    private int state = WAITING;
    
    public String processInput(String theInput) throws IOException, ExecutionException {
        String theOutput = null;
        
        if (state == WAITING) {
            theOutput = "Enter 1 to load an FST. Enter 2 to search for transit time. x to exit";
            state = OPTIONSELECTED;
        } else if (state == OPTIONSELECTED) {
            if (theInput.equalsIgnoreCase("1")) {
                theOutput = "Enter the Carrier Name comma Transit Type, example BD,Air";
                state = LOADFST;
            } else if (theInput.equalsIgnoreCase("2")) {
            	theOutput = "Enter Business, Transit Type, From and To Zip code, example BA, Air, 500001,700002";
            	state = SEARCHTTRANSITTIME;
            }
            else if (theInput.equalsIgnoreCase("x")) {
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
        	if (car_type.length != 2)
        		theOutput = "Invalid Input, enter carrier, transit type, example BD, Air";
        	else {
        		String cacheKey = getCacheKey(car_type[0], car_type[1]);
        		cache.createFST(cacheKey);
	        	theOutput = "FST loaded successfully";
        	}
        	state = WAITING;
        	
        } else if(state == SEARCHTTRANSITTIME)
        {
        	String zip_from_to [] = theInput.split(",");
        	if (zip_from_to.length == 4) {
        	LoadingCache<String, FST<CharsRef>>  localCache = cache.getCache();
        	String cacheKey = zip_from_to[0] + zip_from_to [1];
        	FST<CharsRef> transitTimeFST = localCache.get(cacheKey);
        	if (transitTimeFST == null) {
        		theOutput = "Enter 1 to load an FST before you perform a search";
        	}
        	else {
        		CharsRef value = Util.get(transitTimeFST, new BytesRef(zip_from_to[2] + zip_from_to[3]));
    			//System.out.println(value);
            	theOutput = "TT Value for : " + zip_from_to [2] + " and " + zip_from_to [3] + " is: " + value;
            	state = WAITING;
        		//theOutput = "Issue loading FST from cache";
        		//state = WAITING;
        	}
        	}
        	else {
        		theOutput = "Enter Business, Transit Type, From and To Zip code, example BA, Air, 500001,700002";
        		state = WAITING;
        	}
        	
        } 
        else if (state == CACHESTATS)
        {
        	LoadingCache<String, FST<CharsRef>>  guavaCache = cache.getCache();
        	CacheStats stats = guavaCache.stats();
        	theOutput = "Hit Count:" +
        	stats.hitCount() +
        	" Load Count:" + 
        	stats.loadCount() +
        	" Miss Count:" +
        	stats.missCount();
        }
        return theOutput;
    }
    
    private String getCacheKey(String carrier, String transitType)
    {
    	//TODO decide how to store the cache keys here, for now cache key = carrier + transitType
    	return carrier.concat(transitType);
    }
}