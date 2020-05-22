package com.kmit.transitsearch;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.fst.CharSequenceOutputs;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.Util;
import org.apache.lucene.util.fst.Outputs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

public class CacheFST {
	public static void main(String args[]) throws IOException {

		LoadingCache<String, TransitTime_FST> transitTimeCache = CacheBuilder.newBuilder().maximumSize(100) // maximum
																											// 100
																											// records
																											// can be
																											// cached
				.expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
				.recordStats().build(new CacheLoader<String, TransitTime_FST>() { // build the cacheloader

					@Override
					public TransitTime_FST load(String S3Key) throws Exception {
						// make the expensive call
						S3Key = "43c50cbf-6c83-4aa9-8057-8b3fafc1bfcb" + ".bin"; // bad but do it
						return getFSTFromS3(S3Key);
					}
				});

		try {
			// on first invocation, cache will be populated with corresponding
			// employee record
			/*
			 * System.out.println("Invocation #1");
			 * System.out.println(transitTimeCache.get("100"));
			 * System.out.println(transitTimeCache.get("103"));
			 * System.out.println(transitTimeCache.get("110"));
			 */

			TransitTime_FST ttFst = transitTimeCache.get("43c50cbf-6c83-4aa9-8057-8b3fafc1bfcb" + ".bin");
			FST<CharsRef> f = ttFst.getfst();
			CharsRef value = Util.get(f, new BytesRef("360001360026"));
			System.out.println(value);

			value = Util.get(f, new BytesRef("560004574114"));
			System.out.println(value);

			value = Util.get(f, new BytesRef("591111574279"));
			System.out.println(value);

			value = Util.get(f, new BytesRef("3600013600251A"));
			System.out.println(value);

			// second invocation, data will be returned from cache
			value = Util.get(f, new BytesRef("360001360026"));
			System.out.println(value);

			value = Util.get(f, new BytesRef("560004574114"));
			System.out.println(value);

			value = Util.get(f, new BytesRef("591111574279"));
			System.out.println(value);

			value = Util.get(f, new BytesRef("3600013600251A"));
			System.out.println(value);

			CacheStats stats = transitTimeCache.stats();
			System.out.println("Request Count:" + stats.requestCount());
			System.out.println("Hit Count:" + stats.hitCount());
			System.out.println("Miss Count:" + stats.missCount());

		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private static TransitTime_FST getFSTFromS3(String S3Key) throws IOException {

		// TODO at some point, write code to get from S3, now load it from file

		FST<CharsRef> fst;
		final String FSTLOAD_DIR = "../projectData/fst"; // directory to persist FST
		Path p = FileSystems.getDefault().getPath(FSTLOAD_DIR);
		Directory dir = FSDirectory.open(p);

		Map <String, TransitTime_FST> database = new HashMap<String, TransitTime_FST>();

		Outputs<CharsRef> output = CharSequenceOutputs.getSingleton();
		
		IndexInput in = dir.openInput(S3Key, null);
		try {
			fst = new FST<CharsRef>(in, output);
			TransitTime_FST ttFST = new TransitTime_FST(fst);
			database.put(S3Key, ttFST);
		} finally {
			in.close();
		}

		return database.get(S3Key);
	}
}

class TransitTime_FST {
	FST<CharsRef> fst;

	public TransitTime_FST(FST<CharsRef> fstParam) {
		this.fst = fstParam;
	}

	public FST<CharsRef> getfst() {
		return fst;
	}

	public void setfst(FST<CharsRef> fstParam) {
		this.fst = fstParam;
	}

	/*
	 * @Override public String toString() { return
	 * MoreObjects.toStringHelper(Employee.class) .add("Name", name)
	 * .add("Department", dept) .add("Emp Id", emplD).toString(); }
	 */
}