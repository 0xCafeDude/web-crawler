package webcrawler.crawlfrontier;

import java.net.URL;

import webcrawler.configuration.Configuration;

public class Filter {
	
	private static Filter filterInstance = new Filter();
	
	private Filter () {
		
	}
	
	public static Filter getInstance() {
		return filterInstance;
	}
	
	public boolean isValid(URL url, String urlText) {
		if (!(url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https"))) {
			return false;
		}
		if (Configuration.DOMAIN_CRAWL) {
			String host = url.getHost();
			if (!host.endsWith(Configuration.DOMAIN)) {
				return false;
			}
		}
		if (Configuration.TOPIC_CRAWL) {
			if (urlText.contains(Configuration.TOPIC_CRAWL_SEARCH_STRING)) {
				return true;
			}
			if (url.toString().contains(Configuration.TOPIC_CRAWL_SEARCH_STRING)) {
				return true;
			}
			return false;
		}
		return true;
	}
}
