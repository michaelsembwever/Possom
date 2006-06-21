package no.schibstedsok.front.searchportal.http;

import no.schibstedsok.front.searchportal.InfrastructureException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class HTTPClient {
    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(HttpClient.class);
    private static final String DEBUG_ADDING_CONF =
            "Adding configuration ";
    
    private final static Map hostConfigurations = new HashMap();

    private ConnectionManagerWithoutKeepAlive cMgr;
    private HttpClient commonsHttpClient;

    private final static HTTPClient client = new HTTPClient();


    private HTTPClient() {
        cMgr = new ConnectionManagerWithoutKeepAlive();
        cMgr.setConnectionStaleCheckingEnabled(true);
        cMgr.setMaxConnectionsPerHost(1);
        cMgr.setMaxTotalConnections(100);
        commonsHttpClient = new HttpClient(cMgr);
    }

    public static HTTPClient instance(String id, String host, int port) {
        if (hostConfigurations.containsKey(id)) {
            return client;
        }
        addHostConfiguration(id, host, port);
        return client;
    }

    public HttpMethod executeGet(String id, String path) throws IOException {
        HostConfiguration conf = (HostConfiguration) hostConfigurations.get(id);
        HttpMethod method = new GetMethod(path);
        commonsHttpClient.executeMethod(conf, method);
        return method;
    }

    public Document getXmlDocument(String id, String path) throws IOException, SAXException {
        HttpMethod method = null;
        try {
            method = executeGet(id, path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                return builder.parse(method.getResponseBodyAsStream());
            } catch (ParserConfigurationException e) {
                throw new InfrastructureException(e);
            }
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    public void release(HttpMethod method) {
        method.releaseConnection();
    }

    private synchronized static void addHostConfiguration(String id, String host, int port) {
        if (! hostConfigurations.containsKey(id)) {
            HostConfiguration conf = new HostConfiguration();
            LOG.debug(DEBUG_ADDING_CONF + host + ":" + port);
            conf.setHost(host, port, "http");
            hostConfigurations.put(id, conf);
        }
    }
}
