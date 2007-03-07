// Copyright (2007) Schibsted Søk AS
/*
 * Channels.java
 *
 * Created on 29 May 2006, 12:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.util;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import no.schibstedsok.commons.ioc.ContextWrapper;
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
 * Class that holds channels keys and names.
 *
 * @author andersjj
 */
public final class Channels {
    
    public interface Context extends SiteContext, DocumentContext { };
    
    private static final Map<Site,Channels> instances = new HashMap<Site,Channels>();
    
    private static final ReentrantReadWriteLock INSTANCES_LOCK = new ReentrantReadWriteLock();
    
    private static final Logger LOG = Logger.getLogger(Channels.class);
    
    private final Context context;
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

    /** Name of element holding channel category */
    private static final String CHANNEL_CATEGORY = "category";
    
    /** Map of channelId and channel object pairs */
    private final Map<String, Channel> channels = new HashMap<String,Channel>();

    /** List of channel categories */
    private final Map<Channel.Category, List<Channel>> categoryLists = new HashMap<Channel.Category, List<Channel>>();
    
    /** Channel comparator */
    private final Comparator<Modifier> CHANNEL_COMPARATOR;
            
    /** Creates a new instance of Channels */
    private Channels(final Context cxt) throws ParserConfigurationException {
        
        try{
            INSTANCES_LOCK.writeLock().lock();
            context = cxt;
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            loadChannels(cxt);
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

    private final void loadChannels(final Context cxt) {
        
        final DocumentLoader loader = context.newDocumentLoader(cxt, CHANNELS_RESOURCE, db);
        loader.abut();
        
        final Document doc = loader.getDocument();
        
        if (doc.getDocumentElement() == null) {
            if (cxt.getSite().getParent() != null) {
                final Context parentContext = ContextWrapper.wrap(
                        Context.class,
                        new SiteContext() {
                            public Site getSite() {
                                return cxt.getSite().getParent();
                            }
                        },
                        cxt);
                loadChannels(parentContext);
            }
        } else {
            final NodeList channelNodes = doc.getElementsByTagName(CHANNELS_TAG);
            for(int i = 0; i < channelNodes.getLength(); i++) {
                final Node channelNode = channelNodes.item(i);

                String id = null;
                String name = null;
                int priority = -1;
                Channel.Category category = null;

                final NodeList channelChildNodes = channelNode.getChildNodes();
                for (int n = 0; n < channelChildNodes.getLength(); n++) {
                    final Node childNode = channelChildNodes.item(n);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        final NodeList textNodes = childNode.getChildNodes();
                        for (int m = 0; m < textNodes.getLength(); m++) {
                            final Node textNode = textNodes.item(m);
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                if (CHANNEL_ID.equals(childNode.getNodeName())) {
                                    id = textNode.getNodeValue();
                                } else if (CHANNEL_NAME.equals(childNode.getNodeName())) {
                                    name = textNode.getNodeValue();
                                } else if (CHANNEL_PRIORITY.equals(childNode.getNodeName())) {
                                    priority = Integer.parseInt(textNode.getNodeValue());
                                } else if (CHANNEL_CATEGORY.equals(childNode.getNodeName())) {
                                    category = Channel.Category.valueOf(textNode.getNodeValue().toUpperCase());
                                }
                            }
                        }
                    }
                }
                final Channel channel = Channel.newInstance(id, name, priority, category);
                addChannel(channel);
            }

            /* Sort channel lists */
            for (Channel.Category category : Channel.Category.values()) {
                Collections.sort(getChannelsByCategory(category));
            }
        }            
    }
    
    private void internalWriteDocument(Document d, Writer w) {
        DOMSource source = new DOMSource(d);
        StreamResult result = new StreamResult(w);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Xml Parser: " + e);
        } catch (TransformerException ignore) {
        }
    }
    
    public static final Channels valueOf(final Context cxt) {
        
        final Site site = cxt.getSite();
        Channels instance;
        try{
            INSTANCES_LOCK.readLock().lock();
            instance = instances.get(site);
        }finally{
            INSTANCES_LOCK.readLock().unlock();
        }
        
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
        if (this.categoryLists.get(channel.getCategory()) == null) {
            this.categoryLists.put(channel.getCategory(), new ArrayList<Channel>());
        }
        this.categoryLists.get(channel.getCategory()).add(channel);
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
     * Get list of channels in category.
     * 
     * @param category Category to fetch
     * @retun list of channels in category
     */
    public final List<Channel> getChannelsByCategory(final Channel.Category category) {
        
        return null != categoryLists.get(category)
                ? categoryLists.get(category)
                : Collections.EMPTY_LIST;
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
     * Getter for channel comparator.
     * 
     * @return CHANNEL_COMPARATOR
     */
     public Comparator<Modifier> getComparator() {
         return CHANNEL_COMPARATOR;
     }
}
