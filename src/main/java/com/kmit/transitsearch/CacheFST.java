package com.kmit.transitsearch;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.fst.CharSequenceOutputs;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Outputs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CacheFST {

	public static final int CACHEEXPIRY = 60;
	private static String FSTLOAD_DIR = "../projectData/fst"; // directory to persist FST
	private static String BLUEDARTAIR = "BD_Air" + ".bin";
	private static String UPSROAD = "UPS_Road" + ".bin";
	private LoadingCache<String, FST<CharsRef>> transitTimeCache;

	public CacheFST() {
		// TODO 
	}
	
	/*public void buildFST(String cacheKey) throws ExecutionException
	{
		transitTimeCache.put(cacheKey, createFST(cacheKey));	
	}*/
	
	public void createFST(String cacheKey) throws ExecutionException {

		transitTimeCache = CacheBuilder.newBuilder().maximumSize(100) // maximum
																											// 100
																											// records

				.expireAfterAccess(CACHEEXPIRY, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
				.recordStats().build(new CacheLoader<String, FST<CharsRef>>() { // build the cacheloader

					@Override
					public FST<CharsRef> load(String cacheKey) throws Exception {
						// make the expensive call
						return getFST(cacheKey);
					}
				});
		return;
	}

	private static FST<CharsRef> getFST(String cacheKey) throws IOException {

		String S3fileName = getS3Key(cacheKey);
		FST<CharsRef> fst;
		Map<String, FST<CharsRef>> database = new HashMap<String, FST<CharsRef>>();
		Path p = FileSystems.getDefault().getPath(FSTLOAD_DIR);
		Directory dir = FSDirectory.open(p);

		Outputs<CharsRef> output = CharSequenceOutputs.getSingleton();

		IndexInput in = dir.openInput(getS3Key(cacheKey), null);
		try {
			fst = new FST<CharsRef>(in, output);
			database.put(cacheKey, fst);
		} finally {
			in.close();
		}
		
		return database.get(cacheKey);
	}

	private static String getS3Key(String cacheKey) {
		// TODO Someone has to write code to pull this from S3 based on some dynamoDB
		// query...
		// Assuming the file is magically available and moving on :)
		if (cacheKey.equals("BDAir"))
			return BLUEDARTAIR;
		else if (cacheKey.equals("UPSRoad"))
			return UPSROAD;
		else
			return null;
	}
	
	public void setCache(LoadingCache<String, FST<CharsRef>> transitTimeCacheParam)
	{
		this.transitTimeCache = transitTimeCacheParam;
	}
	
	public LoadingCache<String, FST<CharsRef>>  getCache()
	{
		return transitTimeCache;
	}

}