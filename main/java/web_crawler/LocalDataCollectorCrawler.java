package web_crawler;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class LocalDataCollectorCrawler extends WebCrawler {
    private static final Logger logger = LoggerFactory.getLogger(LocalDataCollectorCrawler.class);
    private long crawlTime;

    private static final Pattern FILTERS = Pattern.compile(
        ".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
        "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    CrawlStat myCrawlStat;
    public static GraphStruct myGraph = new GraphStruct();

    public LocalDataCollectorCrawler() {
        myCrawlStat = new CrawlStat();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        crawlTime = System.currentTimeMillis();
        return !FILTERS.matcher(href).matches() && href.startsWith("https://sikaman.dyndns.org/");
    }

    @Override
    public void visit(Page page) {
        logger.info("Visited: {}", page.getWebURL().getURL());
        crawlTime = System.currentTimeMillis() - crawlTime;
        this.getMyController().getConfig().setPolitenessDelay(new Long(10*crawlTime).intValue());
        myCrawlStat.incProcessedPages();

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData parseData = (HtmlParseData) page.getParseData();
            Document doc = Jsoup.parse(parseData.getHtml());
            Elements theLinks = doc.select("a[href]");
            String selector = "img[src~=(?i)\\.(png|jpe?g|gif)]";
            Elements images = doc.select(selector);
            Elements text = doc.getElementsByTag("p");
            text.addAll(doc.select("h1"));
            text.addAll(doc.select("h2"));
            text.addAll(doc.select("h3"));
            text.addAll(doc.select("h4"));
            org.bson.Document document = new org.bson.Document("links",theLinks.toString());
            document.append("images",images.toString());
            document.append("text",text.toString());
            document.append("crawlTime", new Long(crawlTime).toString());
            document.append("docId", page.getWebURL().getDocid());
            org.bson.Document searchDocument = new org.bson.Document("docId",page.getWebURL().getDocid());
            MyDatabase.getInstance().updateInCollection("pageContent", searchDocument, document);
            Set<WebURL> links = parseData.getOutgoingUrls();
            Vertex currentPage = new Vertex(new Integer(page.getWebURL().getDocid()).toString(), page.getWebURL().getURL());
            myGraph.getGraph().addVertex(currentPage);
            for(WebURL link : links) {
            	Vertex linkedpage = new Vertex(new Integer(link.getDocid()).toString(),link.getURL());
            	myGraph.addVertex(linkedpage);
            	myGraph.addEdge(currentPage, linkedpage);
            }
            myCrawlStat.incTotalLinks(links.size());
            try {
                myCrawlStat.incTotalTextSize(parseData.getText().getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException ignored) {
                // Do nothing
            }
        }
        // We dump this crawler statistics after processing every 50 pages
        if ((myCrawlStat.getTotalProcessedPages() % 50) == 0) {
            dumpMyData();
        }
    }

    /**
     * This function is called by controller to get the local data of this crawler when job is
     * finished
     */
    @Override
    public Object getMyLocalData() {
        return myCrawlStat;
    }

    /**
     * This function is called by controller before finishing the job.
     * You can put whatever stuff you need here.
     */
    @Override
    public void onBeforeExit() {
        dumpMyData();
    }

    public void dumpMyData() {
        int id = getMyId();
        // You can configure the log to output to file
        logger.info("Crawler {} > Processed Pages: {}", id, myCrawlStat.getTotalProcessedPages());
        logger.info("Crawler {} > Total Links Found: {}", id, myCrawlStat.getTotalLinks());
        logger.info("Crawler {} > Total Text Size: {}", id, myCrawlStat.getTotalTextSize());

    }
    
}
