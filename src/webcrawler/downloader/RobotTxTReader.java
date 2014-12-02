package webcrawler.downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class RobotTxTReader {
	
	private String robotText = "";
	private int sleepTimeSeconds = 1;
	private String currentHost = "";
	private String baseString = "";
	private Set<String> disallowSet = new HashSet<String>();
	
	private static final String ROBOTS_TXT = "/robots.txt";
	private static final String ROBOT_USER_AGENT = "User-agent: ";
	private static final String CRAWL_DELAY = "Crawl-delay: ";
	private static final String DISALLOW_TEXT = "Disallow: ";
	
	public boolean isAllowed(URL url) throws IOException {
		readRobotTxt(url);
		return checkValidity(url);
	}
	
	private boolean checkValidity(URL urlObj) {
		String host = urlObj.getHost();
		String url = urlObj.toString();
		int baseURLIndex = url.indexOf(host);
		String baseURL = url.substring(0, baseURLIndex + host.length());
		String remainingURL = url.substring(baseURLIndex + host.length());
		String[] directories = remainingURL.split("/");
		if (disallowSet.contains(baseURL) || disallowSet.contains(baseURL+"/")) {
			return false;
		}
		for (String directory : directories) {
			if (directory != null && !directory.equals("")) {
				baseURL = baseURL + "/" + directory;
				if (disallowSet.contains(baseURL) || disallowSet.contains(baseURL+"/")) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int getSleepTimeSeconds(URL url) throws IOException {
		readRobotTxt(url);
		return sleepTimeSeconds;
	}
	
	private void readRobotTxt(URL url) throws IOException {
		String host = url.getHost();
		int dotAppearances = 0;
		for (int i=0;i<host.length();i++) {
			if (host.charAt(i) == '.') {
				dotAppearances++;
			}
		}
		if (dotAppearances == 2) {
			host = host.substring(host.indexOf(".")+1);
		}
		if(!(host == null || host.equals(currentHost))){
			currentHost = host;
			robotText = "";
			int hostIndex = url.toString().indexOf(host);
			baseString = url.toString().substring(0, hostIndex+host.length());
			String pathForRobotFile = baseString + ROBOTS_TXT;
			URL robotURL = null;
			try {
				robotURL = new URL(pathForRobotFile);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			Object robotURLContent = null;
			try {
				robotURLContent = robotURL.getContent();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(robotURLContent != null && (robotURLContent instanceof InputStream)){
				BufferedReader robotReader = new BufferedReader(new InputStreamReader((InputStream)robotURLContent));
				String line = null;
				while((line = robotReader.readLine()) != null){
					robotText += line + " ";
				}
				try {
					robotReader.close();
				} catch (IOException e) {
					System.out.println(" error in closing the stream ");
					e.printStackTrace();
				}
				updateDisallowSet();
			}
		}
	}
	
	private void updateDisallowSet() {
		disallowSet = new HashSet<String>();
		sleepTimeSeconds = 1;
		if (robotText == null || robotText.equals("")) {
			return;
		}
		String ourUserAgentText = "";
		int previousUserAgentTextIndex = robotText.indexOf(ROBOT_USER_AGENT);
		int sizeUserAgentString = ROBOT_USER_AGENT.length();
		while (true) {
			if (previousUserAgentTextIndex == -1) {
				break;
			}
			int nextUserAgentTextIndex = robotText.indexOf(ROBOT_USER_AGENT, previousUserAgentTextIndex + sizeUserAgentString);
			char userAgent = robotText.charAt(previousUserAgentTextIndex + sizeUserAgentString);
			if (userAgent == '*') {
				if (nextUserAgentTextIndex != -1) {
					ourUserAgentText += robotText.substring(previousUserAgentTextIndex + sizeUserAgentString, nextUserAgentTextIndex) + " ";
				} else {
					ourUserAgentText += robotText.substring(previousUserAgentTextIndex + sizeUserAgentString) + " ";
				}
			}
			previousUserAgentTextIndex = nextUserAgentTextIndex;
		}
		int indexCrawlDelay = ourUserAgentText.indexOf(CRAWL_DELAY);
		if (indexCrawlDelay != -1) {
			sleepTimeSeconds = Integer.parseInt(ourUserAgentText.substring(indexCrawlDelay + CRAWL_DELAY.length(), indexCrawlDelay + CRAWL_DELAY.length()+1));  
		}
		int currentDisallowIndex = ourUserAgentText.indexOf(DISALLOW_TEXT);
		while (true) {
			if (currentDisallowIndex == -1) {
				break;
			}
			int endIndex = ourUserAgentText.indexOf(" ", currentDisallowIndex+DISALLOW_TEXT.length());
			boolean breakAtEnd = false;
			if (endIndex == -1) {
				endIndex = ourUserAgentText.length();
				breakAtEnd = true;
			}
			disallowSet.add(baseString+ourUserAgentText.substring(currentDisallowIndex+DISALLOW_TEXT.length(), endIndex));
			if (breakAtEnd) {
				break;
			}
			currentDisallowIndex = ourUserAgentText.indexOf(DISALLOW_TEXT, currentDisallowIndex + DISALLOW_TEXT.length());
		}
	}
}
