/*
 * Channels.java
 *
 * Created on 29 May 2006, 12:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.DocumentContext;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class that holds channels keys and names
 *
 * @author andersjj
 */
public class Channels {
    
    public interface Context extends SiteContext, DocumentContext { };
    
    private static final Map<Site,Channels> instances = new HashMap<Site,Channels>();
    
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();
    
    private static final Logger LOG = Logger.getLogger(Channels.class);
    
    private Context context;
    private final DocumentBuilder db;

    /** Resource filename */
    private static final String CHANNELS_RESOURCE = "channels.xml";
    
    /** Channel elementname */
    private static final String CHANNELS_TAG = "channel";
    
    /** Name of element holding channel id */
    private static final String CHANNEL_ID = "id";
    
    /** Name of element holding channel name */
    private static final String CHANNEL_NAME = "name";
    
    /** Name of element holding channel priority */
    private static final String CHANNEL_PRIORITY = "priority";

    /** Map of channelId and channel object pairs */
    private final Map<String, Channel> channels = new HashMap<String,Channel>();

    /** Channel comparator */
    private final Comparator<Modifier> CHANNEL_COMPARATOR;
            
    /** Creates a new instance of Channels */
    private Channels(final Context cxt) throws ParserConfigurationException {
        try{
            INSTANCES_LOCK.writeLock().lock();
            context = cxt;
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            loadChannels();
            CHANNEL_COMPARATOR = new Comparator<Modifier>() {
            
                public final Map<String, Channel> getChannels() {
                    return channels;
                }
                
                public int compare(final Modifier m1, final Modifier m2) {
                    final Map<String, Channel> channels = getChannels();
                    if (channels.containsKey(m1.getName()) == false || channels.containsKey(m2.getName()) == false) {
                        return 0;
                    }
    
                    final Channel c1 = channels.get(m1.getName());
                    final Channel c2 = channels.get(m2.getName());
                    return c1.compareTo(c2);
                }
            };

            instances.put(cxt.getSite(), this);
        }finally{
            INSTANCES_LOCK.writeLock().unlock();
        }
    }

    private final void loadChannels() {
        final DocumentLoader loader =
                context.newDocumentLoader(context, CHANNELS_RESOURCE, db);
        loader.abut();
        Document doc = loader.getDocument();
        NodeList nodeList = doc.getElementsByTagName(CHANNELS_TAG);
        for(int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            String id = null;
            String name = null;
            Integer priority = -1;
            NodeList childNodes = node.getChildNodes();
            for (int n = 0; n < childNodes.getLength(); n++) {
                Node childNode = childNodes.item(n);
                if (CHANNEL_ID.equals(childNode.getNodeName())){
                    id = childNode.getTextContent();
                } else if (CHANNEL_NAME.equals(childNode.getNodeName())) {
                    name = childNode.getTextContent();
                } else if (CHANNEL_PRIORITY.equals(childNode.getNodeName())) {
                    priority = Integer.valueOf(childNode.getTextContent());
                }
            }
            final Channel channel = Channel.newInstance(id, name, priority);
            addChannel(channel);
        }
    }
    
    public static final Channels valueOf(final Context cxt) {
        final Site site = cxt.getSite();
        INSTANCES_LOCK.readLock().lock();
        Channels instance = instances.get(site);
        INSTANCES_LOCK.readLock().unlock();
        
        if (instance == null) {
            try {
                instance = new Channels(cxt);
            }
            catch (ParserConfigurationException ex) {
                LOG.fatal(ex.getMessage(), ex);
            }
        }
        return instance;
    }

    /**
     * Add channel to channel list.
     *
     * @param channel Channel object to add
     */
    private final void addChannel(final Channel channel) {
        this.channels.put(channel.getId(), channel);
    }
    
    /**
     * Get a channel object.
     *
     * @param channelId
     * @return Object representing the channel or null on error
     */
    public final Channel getChannel(final String channelId) {
        return channels.get(channelId);
    }
    
    /**
     * Get channel name from channel id.
     *
     * @param channelId Channel id
     * @return Channel name or null on error
     */
    public final String getMessage(final String channelId) {
        return hasChannel(channelId) ? ((Channel)channels.get(channelId)).getName() : null;
    }
    
    /** @deprecated */
    public String getText(final String channelId) {
        return getMessage(channelId);
    }
    
    /** 
     * Check if channel id exists.
     *
     * @param channelId Channel id
     * @return true if channel exists, false otherwise
     */
    public boolean hasChannel(final String channelId) {
        return channels.containsKey(channelId);
    }
    
    /** 
     * Getter for channel comparator
     * 
     * @return CHANNEL_COMPARATOR
     */
     public Comparator<Modifier> getComparator() {
         return CHANNEL_COMPARATOR;
     }
}
