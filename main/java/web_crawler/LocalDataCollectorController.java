package web_crawler;

import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class LocalDataCollectorController {
    private static final Logger logger =
            LoggerFactory.getLogger(LocalDataCollectorController.class);
    
    //bodycontenthandler is the main class to use re: SAX parser with Tika
    //ensure that we enable binary content in the crawler when we start using Tika

	public static void main(String[] args) throws Exception {
        String rootFolder = "/Users/jen/crawler/data/example/";
        int numberOfCrawlers = 2;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(rootFolder);
        config.setMaxPagesToFetch(10);
        config.setPolitenessDelay(10000);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://sikaman.dyndns.org/index.php");
        controller.addSeed("https://sikaman.dyndns.org/courses/4601/index.html");
        controller.start(LocalDataCollectorCrawler.class, numberOfCrawlers);

        List<Object> crawlersLocalData = controller.getCrawlersLocalData();
        long totalLinks = 0;
        long totalTextSize = 0;
        int totalProcessedPages = 0;
        
        Document myDoc = new Document("graph", Marshaller.serializeObject(LocalDataCollectorCrawler.myGraph));
        
        MyDatabase.getInstance().updateInCollection("graphs", myDoc, myDoc);
		
        for (Object localData : crawlersLocalData) {
            CrawlStat stat = (CrawlStat) localData;
            totalLinks += stat.getTotalLinks();
            totalTextSize += stat.getTotalTextSize();
            totalProcessedPages += stat.getTotalProcessedPages();
            
        }
        
        logger.info("Aggregated Statistics:");
        logger.info("\tProcessed Pages: {}", totalProcessedPages);
        logger.info("\tTotal Links found: {}", totalLinks);
        logger.info("\tTotal Text Size: {}", totalTextSize);
    }

}
