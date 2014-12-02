package webcrawler.crawlfrontier;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class CrawlFrontier {
	private Queue<URL> urlsToTraverse;
	private VisitedURLsManager visitedURLsManager;
	private Filter urlFilterer;
	
	public CrawlFrontier() {
		urlsToTraverse = new LinkedList<URL>();
		visitedURLsManager = VisitedURLsManager.getInstance();
		urlFilterer = Filter.getInstance();
	}
	
	public void addURLs(Map<String, String> newURLs) {
		if (newURLs != null && !newURLs.isEmpty()) {
			Iterator<Entry<String, String>> it = newURLs.entrySet().iterator();
			Map<URL, Boolean> relevancyMap = new HashMap<URL, Boolean>();
			boolean overallRelevancy = false;
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String url = entry.getKey();
				String urlText = entry.getValue();
				String oldUrl = url;
				if (url.contains("#")) {
					continue;
				}
				url.trim();
				URL urlObj;
				try {
					urlObj = (new URI(url)).normalize().toURL();
				} catch (MalformedURLException e) {
					System.out.println(" old url "+oldUrl+" malformed url "+url);
					e.printStackTrace();
					continue;
				} catch (URISyntaxException e) {
					e.printStackTrace();
					continue;
				}
				boolean urlRelevancy = urlFilterer.isValid(urlObj, urlText);
				if (urlRelevancy) {
					overallRelevancy = true;
				}
				relevancyMap.put(urlObj, urlRelevancy);
			}
			if (relevancyMap != null && !relevancyMap.isEmpty()) {
				Iterator<Entry<URL, Boolean>> relevancyMapIt = relevancyMap.entrySet().iterator();
				while (relevancyMapIt.hasNext()) {
					Entry<URL, Boolean> relevancyMapEntry = relevancyMapIt.next();
					URL urlObj = relevancyMapEntry.getKey();
					boolean relevancy = relevancyMapEntry.getValue();
					if (overallRelevancy) {
						if (relevancy) {
							if (visitedURLsManager.addURL(urlObj.toString())) {
								urlsToTraverse.add(urlObj);
							}
						}
					} else {
						if (visitedURLsManager.addURL(urlObj.toString())) {
							urlsToTraverse.add(urlObj);
						}
					}
				}
			}
		}
	}
	
	public URL getURL() {
		if(urlsToTraverse.isEmpty()) {
			return null;
		}
		return urlsToTraverse.remove();
	}
}
