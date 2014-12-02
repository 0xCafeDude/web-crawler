package webcrawler.crawlfrontier;

import com.skjegstad.utils.BloomFilter;

/**
 * @author shivam
 * not connected to database yet.
 * will go out of memory if kept 
 * running for long.
 */
public class VisitedURLsManager {
	
	private BloomFilter<String> visitedURLs;
	private static final VisitedURLsManager instance = new VisitedURLsManager();
	
	private VisitedURLsManager() {
		double falsePositiveProbability = 0.1;
		int expectedNumberOfURLs = 100000;
		visitedURLs = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfURLs);
	}
	
	public static VisitedURLsManager getInstance() {
		return instance;
	}
	
	public synchronized boolean addURL(String url) {
		boolean visited = visitedURLs.contains(url);
		if (!visited) {
			visitedURLs.add(url);
		}
		return visited;
	}
}
	