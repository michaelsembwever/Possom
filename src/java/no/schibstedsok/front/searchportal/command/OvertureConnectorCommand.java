/**
 * 
 */
package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.OvertureSearchResultElement;
import no.schibstedsok.front.searchportal.response.SearchResponseImpl;
import no.schibstedsok.front.searchportal.response.SearchResultElement;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Lars Johansson
 *
 */
public class OvertureConnectorCommand implements ConnectorCommand {

    /** Logger for this class. */
    private Logger log = Logger.getLogger(this.getClass());

    private CommandResponse response;
    private String queryString;
    private String directive;

    private int startSearchAt = 1;      // default starting postion in search
    
    private int maxResults;             // default starting postion in search
	
	private final static SearchConfiguration configuration = null;

    /**
     * 
     */
    public OvertureConnectorCommand() {
        super();
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#getDirective()
     */
    public String getDirective() {
        return this.directive;
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#setDirective(java.lang.String)
     */
    public void setDirective(String directive) {
        this.directive = directive;

    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#getQueryString()
     */
    public String getQueryString() {
       return queryString;
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#setQueryString(java.lang.String)
     */
    public void setQueryString(String query) {
        this.queryString = query;
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#getResponse()
     */
    public CommandResponse getResponse() {
        return this.response;
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#setResponse(com.schibstedsok.portal.search.connectors.interfaces.CommandResponse)
     */
    public void setResponse(CommandResponse response) {
        this.response = response;

    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.command.ConnectorCommand#execute()
     */
    public void execute() {
    
        if("".equals(getQueryString()))
            return;
        
        long timer = System.currentTimeMillis(); //used for timing search
        
        // Create a Yahoo Search object, set our authorization key
        HttpClient overtureSearcClient = new HttpClient();
        HttpMethod searchMethod = new GetMethod("http://xml.se.overture.com/d/search/p/standard/eu/xml/multi/");
        searchMethod.setFollowRedirects(true);
        searchMethod.setQueryString("adultFilter=clean&Partner=schibsted_xml_se_searchbox_test&os=1&on=5&is=1&in=5");
        searchMethod.setQueryString(searchMethod.getQueryString() + "&mkt=se");
        searchMethod.setQueryString(searchMethod.getQueryString() + "&Keywords=" + getQueryString());
        try {

            overtureSearcClient.executeMethod(searchMethod);

            //parse the xml response
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            org.w3c.dom.Document doc = factory.newDocumentBuilder().parse(searchMethod.getResponseBodyAsStream());
            NodeList listing = doc.getElementsByTagName("Listing");
            Node node= null;
            ArrayList results = new ArrayList();
            
            response = new SearchResponseImpl();
            
            for(int i = 0; i< listing.getLength();i++){
                SearchResultElement result = new OvertureSearchResultElement();
                node = (Node)listing.item(i);
                NamedNodeMap attrs = node.getAttributes();
                for (int j=0; j <attrs.getLength(); j++) {
                    Attr attr = (Attr)attrs.item(j);
                    if(attr.getNodeName().equals("title")){
                        result.setTitle(attr.getNodeValue());
                    } else if(attr.getNodeName().equals("description")){
                        result.setSummary(attr.getNodeValue());
                    } else if(attr.getNodeName().equals("siteHost")){
                        result.setUrl(attr.getNodeValue());
                        result.setClickUrl(attr.getNodeValue());    //default if no content in overture
                    }
                }

                //TODO: do more efficient XML parsing here
                /**
                 * <Listing rank="4" title="Schibsted - Wikipedia" description="Från Wikipedia, den fria encyklopedin. Extern länk... Från encyklopedin. Schibsted är en norsk mediekoncern med huvudkontor i Oslo som bl.a ..." siteHost="sv.wikipedia.org">
                 *    <ClickUrl type="inktomi">http://www6.overture.com/d/sr/?xargs=15KPjg10VSs4K9k7PyMPiIRvydgApOwZyqq5U1S84kRssNry9yArwmPfjO3tB7ORRT0E2W07XNp%5F5WeaeiyeXVUFmRAWKyBoCshNWWjs9oYrqjX5gnso4sxOqYmPRLTXtyamnDCqe69tz8EY%5FsPApP94kWzxvK6PIaw862zOR7Ivt47WVt%2DVnRVPhnlcBU1f37W%2DYONqdXet7q1GDeQK00l4c%5F3d3WYhcvfT6hkCpI%5FEeIMTJ547vOCNMxh%2DHkmfvfLqT%5FlNlDKAKG9vI9lCqm%5FguUwfFmTniAjsoolFtlKtzpYjyenQhbqauetLarHe12vDbyHe7hFXjzZUIIe1BzVOuxHWXJJGI05R%2DHdJ03Arrf%5FFC8FsLeTi0mYFAJ7te1Mc8O4xuLm2ssWD7hMpjfuCZNNJRrpGzqpWfw%5FUx6etoEna6CedxD67G5mEPGVXBTzAil0I21Bsv1xQYXoXIm%5FvAGH99N0pASoiWnt1mbhaKU%2DIZO7JaDesuU0WV2n6C3cVMrarTCeLdeAI8sPzOZQPWvZJ7dYmI8OVNEdziBmoAprTzu9Nt2Mf%5FNx0gi9wQ1e3UzPR4o8LbB7O6SoC%5FIPnCn6DWYCbuhqiw%2E&amp;yargs=sv.wikipedia.org</ClickUrl>
                 * </Listing>
                 */
                NodeList clickUrls = node.getChildNodes();
                for(int k = 0; k< clickUrls.getLength();k++){
                    Node clickUrl = (Node)clickUrls.item(i);
                    if(clickUrl != null && clickUrl.getNodeName().equals("ClickUrl")) {
                        result.setClickUrl(clickUrl.getFirstChild().getNodeValue());
                    }
                }
                results.add(result);
            }

            response = new SearchResponseImpl();
            response.setFetchTime(System.currentTimeMillis() - timer);
            response.setDocumentsReturned(results.size());
            //TODO: get the total documents from xml as well
            // response.setTotalDocumentsAvailable();
            //TODO: get the starting positon for new search
            //response.setConsequtiveSearchStartsAt(postions)
            response.setResults(results);
            
            
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    

    }

    public int getMaxResultsToReturn() {
        return maxResults;
    }
    

    public void setMaxResultsToReturn(int maxResults) {
        this.maxResults = maxResults;
    }
    

    public int getStartSearchAt() {
        return startSearchAt;
    }
    

    public void setStartSearchAt(int startSearchAt) {
        this.startSearchAt = startSearchAt;
    }

	public void setConfiguration(SearchConfiguration config) {
		// DO NOTHING for Overture
		
	}
    

}
