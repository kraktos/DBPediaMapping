/**
 * 
 */
package com.mapper.indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;

import com.mapper.pojo.Fact;
import com.mapper.utility.Hasher;
import com.mapper.utility.Utilities;

/**
 * This class indexes a given data set consisting of millions of facts with
 * confidence values
 * 
 * 
 * @author Arnab Dutta
 * 
 */
public class DataIndexerImpl implements IDataIndexer {

	/**
	 * Collection to store the the DBPedia entities and literals mapped to their
	 * hash values
	 */
	public static Map<Long, String> MAP_DBPEDIA_LITERALS = new FastHashMap();//new FastHashMap();
		
	/**
	 * List of S -> PO, P-> SO, O-> SP
	 */
	private static List<Long> LIST_DBPEDIA_ENTITIES = null;
	
	public static Map<Long, List<Long>> MAP_DBPEDIA_SUB_LITERALS = new FastHashMap();//new FastHashMap();
	public static Map<Long, List<Long>> MAP_DBPEDIA_PRED_LITERALS = new FastHashMap();//new FastHashMap();
	public static Map<Long, List<Long>> MAP_DBPEDIA_OBJ_LITERALS = new FastHashMap();//new FastHashMap();

	private static final String DELIMIT = "\","; 

	private static final long MEGABYTE = 1024L * 1024L;

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

	private String dataFileToIndex = null;

	private Map<Long, Fact> factsMap = new HashMap<Long, Fact>();

	/**
	 * @param dataFileToIndex
	 */
	public DataIndexerImpl(final String dataFileToIndex) {
		this.dataFileToIndex = dataFileToIndex;
	}

	@Override
	public void readData() {

		String strLine = null;
		String[] st = null;
		String subject;
		String predicate;
		String object;

		try {
			// create BufferedReader to read csv file
			BufferedReader br = new BufferedReader(new FileReader(
					dataFileToIndex));

			// Fact fact = null;
			// read each facts
			while ((strLine = br.readLine()) != null) {
				// logger.info("\n" + strLine);
				// lineNumber++;

				// fact = new Fact();
				// break comma separated line using one or more tabs
				// strLine = strLine.replaceAll(", ", ",");
				// st = new StringTokenizer(strLine, DELIMIT);
				st = strLine.split(DELIMIT);

				// while(st.hasMoreTokens()){
				subject = st[0].contains("\"") ? st[0].replaceAll("\"", "") : st[0];
				predicate = st[1].contains("\"") ? st[1].replaceAll("\"", "") : st[1];
				object = st[2].contains("\"") ? st[2].replaceAll("\"", "") : st[2];

				/*System.out.print(nextToken + " " + Hasher.hash64(nextToken)
						+ "  ");*/
				// }
				//System.out.println("\n");

				createIndex(subject, predicate, object);
				
			}
			System.out.println("Sub Map = " + MAP_DBPEDIA_SUB_LITERALS.size());
			System.out.println("Pred Map = " + MAP_DBPEDIA_PRED_LITERALS.size());
			System.out.println("Obj Map = " + MAP_DBPEDIA_OBJ_LITERALS.size());

			//System.out.println(MAP_DBPEDIA_PRED_LITERALS.get(new Long(3965841705612063023L)).size());
			
			Utilities.printMap(MAP_DBPEDIA_LITERALS); 
			
			
			// Get the Java runtime
			Runtime runtime = Runtime.getRuntime();
			// Run the garbage collector
			runtime.gc();
			// Calculate the used memory
			long memory = runtime.totalMemory() - runtime.freeMemory();
			logger.info("Used memory is Megabytes: " + bytesToMegabytes(memory)
					+ " for " + factsMap.size() + " elements");

		} catch (Exception e) {
			logger.error("Exception while reading csv file: " + e);
		}

	}

	

	private void createIndex(String subject, String predicate, String object) {
		
		long subjectHash = Hasher.hash64(subject);
		long predicateHash = Hasher.hash64(predicate);
		long objectHash = Hasher.hash64(object);
		List<Long> tempList;
		
		
		if(MAP_DBPEDIA_SUB_LITERALS.containsKey(subjectHash)){
			tempList = MAP_DBPEDIA_SUB_LITERALS.get(subjectHash);
			tempList.add(objectHash);
			tempList.add(predicateHash);
			MAP_DBPEDIA_SUB_LITERALS.put(subjectHash, tempList);			
		}else{
			LIST_DBPEDIA_ENTITIES = new ArrayList<Long>();
			LIST_DBPEDIA_ENTITIES.add(objectHash);
			LIST_DBPEDIA_ENTITIES.add(predicateHash);			
			MAP_DBPEDIA_SUB_LITERALS.put(subjectHash, LIST_DBPEDIA_ENTITIES);
		}
		
		if(MAP_DBPEDIA_PRED_LITERALS.containsKey(predicateHash)){
			tempList = MAP_DBPEDIA_PRED_LITERALS.get(predicateHash);
			tempList.add(subjectHash);
			tempList.add(objectHash);
			MAP_DBPEDIA_PRED_LITERALS.put(predicateHash, tempList);			
		}else{
			LIST_DBPEDIA_ENTITIES = new ArrayList<Long>();
			LIST_DBPEDIA_ENTITIES.add(subjectHash);
			LIST_DBPEDIA_ENTITIES.add(objectHash);			
			MAP_DBPEDIA_PRED_LITERALS.put(predicateHash, LIST_DBPEDIA_ENTITIES);
		}
		
		if(MAP_DBPEDIA_OBJ_LITERALS.containsKey(objectHash)){
			tempList = MAP_DBPEDIA_OBJ_LITERALS.get(objectHash);
			tempList.add(subjectHash);
			tempList.add(predicateHash);
			MAP_DBPEDIA_OBJ_LITERALS.put(objectHash, tempList);			
		}else{
			LIST_DBPEDIA_ENTITIES = new ArrayList<Long>();
			LIST_DBPEDIA_ENTITIES.add(subjectHash);
			LIST_DBPEDIA_ENTITIES.add(predicateHash);			
			MAP_DBPEDIA_OBJ_LITERALS.put(objectHash, LIST_DBPEDIA_ENTITIES);
		}
		
		
		
		MAP_DBPEDIA_LITERALS.put(subjectHash, subject);
		MAP_DBPEDIA_LITERALS.put(predicateHash, predicate);
		MAP_DBPEDIA_LITERALS.put(objectHash, object);
		
		
	}

	@Override
	public void indexData() {
		// TODO Auto-generated method stub

	}

}
