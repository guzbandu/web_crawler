package web_crawler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class LocalDataCollectorController {
    private static final Logger logger =
            LoggerFactory.getLogger(LocalDataCollectorController.class);
    
    private static MongoClient dbClient;
    private static MongoDatabase database;
    private static String db_url = "localhost";
    private static Integer db_port = 27017;
    private static String db_name = "crawler";
    private static String db_collection = "graphs";
    private static MongoCollection<Document> collection;

    //bodycontenthandler is the main class to use re: SAX parser with Tika
    //ensure that we enable binary content in the crawler when we start using Tika

	public static void main(String[] args) throws Exception {
        String rootFolder = "/Users/jen/crawler/data/example/";
        int numberOfCrawlers = 1;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(rootFolder);
        config.setMaxPagesToFetch(10);
        config.setPolitenessDelay(1000);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://sikaman.dyndns.org/index.php");
        controller.start(LocalDataCollectorCrawler.class, numberOfCrawlers);

        List<Object> crawlersLocalData = controller.getCrawlersLocalData();
        long totalLinks = 0;
        long totalTextSize = 0;
        int totalProcessedPages = 0;
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
        dbClient = new MongoClient(db_url, db_port);
        database = dbClient.getDatabase(db_name);
		MongoCollection<Document> exists = database.getCollection(db_collection);
		if(exists==null) {
			database.createCollection(db_collection, null);
		}
		collection = database.getCollection(db_collection);
		
		Iterator<String> iter = LocalDataCollectorCrawler.graph.vertexSet().iterator();
		while(iter.hasNext()) {
			String vertex = iter.next();
			Document writeVertex = new Document("url", vertex);
			Document writeEdges = new Document("url", vertex);
			Set<DefaultEdge> edges = LocalDataCollectorCrawler.graph.edgesOf(vertex);
    		ArrayList<String> edgeTargets = new ArrayList<String>();
    		for (DefaultEdge edge: edges) {
    			String target = (String) LocalDataCollectorCrawler.graph.getEdgeTarget(edge);
    			edgeTargets.add(target);
    		}
			writeEdges.append("edges", edgeTargets);
			collection.replaceOne(writeVertex, writeEdges, new UpdateOptions().upsert( true ));
		}
    }

}
