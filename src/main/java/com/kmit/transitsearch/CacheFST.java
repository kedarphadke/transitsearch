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
	public String carrier;
	public String transitType;

	public CacheFST(String carrierParam, String transitTypeParam) {
		this.carrier = carrierParam;
		this.transitType = transitTypeParam;
	}

	public FST<CharsRef> constructFST() throws ExecutionException {

		try {

			LoadingCache<String, FST<CharsRef>> transitTimeCache = CacheBuilder.newBuilder().maximumSize(100) // maximum
																												// 100
																												// records

					.expireAfterAccess(CACHEEXPIRY, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
					.recordStats().build(new CacheLoader<String, FST<CharsRef>>() { // build the cacheloader

						@Override
						public FST<CharsRef> load(String key) throws Exception {
							// make the expensive call
							return getFST(carrier, transitType);
						}
					});
			return transitTimeCache.get(carrier + transitType);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static FST<CharsRef> getFST(String carrier,  String transitType) throws IOException {

		String S3fileName = getS3Key(carrier, transitType);
		
		String key = carrier + transitType;

		FST<CharsRef> fst;
		final String FSTLOAD_DIR = "../projectData/fst"; // directory to persist FST
		Path p = FileSystems.getDefault().getPath(FSTLOAD_DIR);
		Directory dir = FSDirectory.open(p);

		Map<String, FST<CharsRef>> database = new HashMap<String, FST<CharsRef>>();

		Outputs<CharsRef> output = CharSequenceOutputs.getSingleton();

		IndexInput in = dir.openInput(S3fileName, null);
		try {
			fst = new FST<CharsRef>(in, output);
			database.put(key, fst);
		} finally {
			in.close();
		}

		return database.get(key);
	}

	private static String getS3Key(String carrier, String transitType) {
		// TODO Somneone has to write code to pull this from S3 based on some dynamoDB
		// query...
		// Assuming the file is magically available and moving on :)

		return "43c50cbf-6c83-4aa9-8057-8b3fafc1bfcb" + ".bin";
	}

}