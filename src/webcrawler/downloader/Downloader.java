package webcrawler.downloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import webcrawler.configuration.Configuration;
import webcrawler.crawlfrontier.CrawlFrontier;

public class Downloader implements Runnable {

	private CrawlFrontier crawlFrontier;
	private RobotTxTReader robottxtReader;
	private static final int IOEXCEPTION_SLEEP_TIME_SECONDS = 1;
	private String threadName;
	private String fileName;
	
	public Downloader(CrawlFrontier crawlFrontier, String threadName, String fileName) {
		robottxtReader = new RobotTxTReader();
		this.crawlFrontier = crawlFrontier;
		this.threadName = threadName;
		this.fileName = fileName;
	}
	
	@Override
	public void run() {
		System.out.println("Starting thread "+threadName);
		URL url = null;
		if (crawlFrontier == null) {
			return;
		}
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);
		while ((url = crawlFrontier.getURL()) != null) {
			int sleepTime = 1;
			boolean validURL = true;
			try {
				validURL = robottxtReader.isAllowed(url);
				sleepTime = robottxtReader.getSleepTimeSeconds(url);
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(IOEXCEPTION_SLEEP_TIME_SECONDS * 1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				try {
					validURL = robottxtReader.isAllowed(url);
					sleepTime = robottxtReader.getSleepTimeSeconds(url);
				} catch (IOException e1) {
					e1.printStackTrace();
					continue;
				}
			}
			if (validURL) {
				Document doc = null;
				try {
					doc = Jsoup.connect(url.toString()).get();
				} catch (IOException e) {
					e.printStackTrace();
					try {
						Thread.sleep(IOEXCEPTION_SLEEP_TIME_SECONDS * 1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					try {
						doc = Jsoup.connect(url.toString()).get();
					} catch (IOException e1) {
						e1.printStackTrace();
						continue;
					}
					
				}
				String htmlText = doc.body().text();
				boolean relevant = true;
				if (Configuration.TOPIC_CRAWL) {
					relevant = false;
					if (htmlText != null && !htmlText.isEmpty()) {
						relevant = htmlText.contains(Configuration.TOPIC_CRAWL_SEARCH_STRING);
					}
					System.out.println(url.toString() + " relevant =>"+relevant);
					System.out.println("writing to file "+fileName);
					try {
						bw.write(url.toString() + " relevant =>"+relevant+"\n");
						bw.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else {
					System.out.println(url.toString());
				}
		        Elements links = doc.select("a[href]");
		        Map<String, String> newURLs = new HashMap<String, String>();
		        if (links != null && !links.isEmpty()) {
		        	for (Element link : links) {
		        		String linkStr = link.attr("abs:href");
		        		if (linkStr != null && !linkStr.equals("")) {
		        			String linkRelevantText = link.parent().text();
		        			newURLs.put(linkStr, linkRelevantText);
		        		}
			        }
		        }
		        crawlFrontier.addURLs(newURLs);
		        try {
					Thread.sleep(sleepTime * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
