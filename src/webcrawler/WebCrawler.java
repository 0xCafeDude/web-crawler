package webcrawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import webcrawler.configuration.Configuration;
import webcrawler.crawlfrontier.CrawlFrontier;
import webcrawler.downloader.Downloader;

public class WebCrawler {
	public static void main(String args[]) throws IOException {
		System.out.println("Web Crawler Started");
		Document doc = Jsoup.connect("http://jhu.edu/".toString()).get();
		Elements links = doc.select("a[href]");
		int count=0;
		Map<Integer, Map<String, String>> newURLMapMap = new HashMap<Integer, Map<String,String>>();
		for (count = 0 ; count < Configuration.NO_OF_THREADS; count++) {
			newURLMapMap.put(count, new HashMap<String, String>());
		}
		count = 0;
		if (links != null && !links.isEmpty()) {
        	for (Element link : links) {
        		if (count == Configuration.NO_OF_THREADS) {
    				count = 0;
    			}
    			Map<String, String> newURLs = newURLMapMap.get(count++);
        		String linkStr = link.attr("abs:href");
        		if (linkStr != null && !linkStr.equals("")) {
        			String linkRelevantText = link.parent().text();
        			newURLs.put(linkStr, linkRelevantText);
        		}
	        }
        }
		for (count = 0 ; count < Configuration.NO_OF_THREADS; count++) {
			CrawlFrontier crawlFrontier = new CrawlFrontier();
			crawlFrontier.addURLs(newURLMapMap.get(count));
			Thread thread = new Thread(new Downloader(crawlFrontier, Integer.toString(count), "FILE"+Integer.toString(count)));
			thread.start();
		}
	}
}
