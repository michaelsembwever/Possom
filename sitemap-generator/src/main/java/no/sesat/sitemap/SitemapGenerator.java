package no.sesat.sitemap;

import no.sesat.search.sitemap.PageProvider;
import no.sesat.search.sitemap.Page;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A class for generating sitemaps. The entries are provided by implementors of
 * <tt>no.sesat.commons.sitemaps.PageProvider</tt>. This class will generate a sitemap index file and any number of
 * sitemap files required to meet the size resitriction of a single sitemap file.
 *
 * A list of <tt>no.sesat.search.sitemap.PageProvider</tt> can be supplied at instantiation. Another options is to
 * have this class load providers available in a skin using the the java6 ServiceLoader mechanism. The main method
 * of this class expect three parameters:
 *
 * <ul>
 * <li>The skin from which to load page providers (e.g. <tt>http://sesam.no</tt>)</li>
 * <li>The directory to which you want the sitemap files to be written (e.g. <tt>/www/data/sitemaps/</tt>)</li>
 * <li>The URL at which this directory can be accessed using HTTP (e.g. <tt>http://sesam.no/sitemaps/</tt>).
 * This information is needed for the sitemap index file</li>
 * </ul>
 *
 *
 * @version $Id$
 */
public final class SitemapGenerator {

    private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

    private static final SimpleDateFormat ISO_8601_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final Logger LOG = Logger.getLogger(SitemapGenerator.class);

    private final Collection<PageProvider> providers;
    private final File location;
    private final URI uri;
    private final SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
    private final TransformerHandler transformerHandler;
    private final FileWriter writer;

    /**
     * Creates a new generator.
     *
     * @param providers the list of page providers to use.
     * @param dest the destination directory.
     * @param url the public url corresponding to the the destination directory.
     * @throws IOException if the files could not be written.
     */
    public SitemapGenerator(final Collection<PageProvider> providers, final File dest, final URI url)
            throws IOException {

        LOG.info("Initializing...");

        this.uri = url;
        this.providers = providers;
        this.location = dest;

        this.writer = new FileWriter(new File(location, "sitemap_index.xml"));

        final StreamResult streamResult = new StreamResult(writer);

        try {
            tf.setAttribute("indent-number", 2);

            this.transformerHandler = tf.newTransformerHandler();

            final Transformer serializer = transformerHandler.getTransformer();

            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");//
            serializer.setOutputProperty(OutputKeys.METHOD,"xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformerHandler.setResult(streamResult);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new instance loading any providers found in the provided skin.
     *
     * @param site The skin to generate a sitemap for
     * @param dest The directory to write files to.
     * @param url The URI at which this directory is accessible using HTTP.
     *
     * @throws IOException If the files can't be written.
     */
    public SitemapGenerator(final URI site, final File dest, final URI url) throws IOException {

        this(new ArrayList<PageProvider>(), dest, url);

        for (PageProvider provider : ServiceLoader.load(PageProvider.class, getClassLoader(site))) {
            LOG.info("Found " + provider.getName());
            providers.add(provider);
        }
    }

    public static void main(String[] args)
            throws URISyntaxException, IOException, TransformerConfigurationException, SAXException {

        new SitemapGenerator(new URI(args[0]), new File(args[1]), new URI(args[2])).generate();
    }
    /**
     * Generate the sitemap files.
     *
     * @throws IOException if the files could not be created.
     * @throws SAXException if a xml error occurs.
     */
    public void generate() throws IOException, SAXException {

        int totalCount = 0;

        AttributesImpl schemaLocation = new AttributesImpl();

        transformerHandler.startDocument();
        transformerHandler.startPrefixMapping("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        transformerHandler.startPrefixMapping("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

        schemaLocation.addAttribute(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schemaLocation", "xsi:schemaLocation", "CDATA", "http://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd");

        transformerHandler.startElement(NS, "", "sitemapindex", schemaLocation);

        for (final PageProvider provider : providers) {

            LOG.info("Processing " + provider.getName());

            final SiteMap group = new SiteMap(provider.getName());

            try {
                for (final Page page : provider) {
                    if (null != page) {
                        group.addPage(page);
                    }
                }
            } finally {
                group.finish();
                LOG.info(group.getCount() + " entries processed for " + provider.getName());
                totalCount += group.getCount();
            }

            for (final SiteMap.SiteMapFile map : group.getSiteMaps()) {
                transformerHandler.startElement("", "", "sitemap", new AttributesImpl());
                addElement("loc", uri.resolve(map.getFileName()).toString());
                addElement("lastmod", formatDateW3c((new Date())));
                transformerHandler.endElement("", "", "sitemap");
            }
        }

        transformerHandler.endElement(NS, "", "sitemapindex");
        transformerHandler.endDocument();
        writer.close();

        LOG.info("All done (" + totalCount + " entries)");
    }

    private void addElement(final String element, final String string) throws SAXException {
        final AttributesImpl noAttributes = new AttributesImpl();

        transformerHandler.startElement("", "", element, noAttributes);

        if (string != null) {
            transformerHandler.characters(string.toCharArray(), 0, string.length());
        }

        transformerHandler.endElement("", "", element);
    }

    /**
     * A sitemap group represents a sitemap. It will create multiple underlying sitemaps if the number of entries exceed
     * 25000.
     */
    private class SiteMap {

        private final String name;
        private SiteMapFile currentSiteMapFile;
        private List<SiteMapFile> siteMaps;
        private int count = 0;

        public SiteMap(final String name) throws IOException, SAXException {
            this.name = name;

            this.currentSiteMapFile = new SiteMapFile(1);
            this.siteMaps = new ArrayList<SiteMapFile>();
        }

        public void addPage(final Page page) throws IOException, SAXException {
            if (currentSiteMapFile.getEntryCount() == 25000) {
                siteMaps.add(currentSiteMapFile);
                count += currentSiteMapFile.getEntryCount();
                currentSiteMapFile.finish();

                currentSiteMapFile = new SiteMapFile(siteMaps.size() + 1);
            }

            currentSiteMapFile.addPage(page);
        }

        public void finish() throws IOException {
            siteMaps.add(currentSiteMapFile);
            count += currentSiteMapFile.getEntryCount();
            currentSiteMapFile.finish();
        }

        public List<SiteMapFile> getSiteMaps() {
            return siteMaps;
        }

        public int getCount() {
            return count;
        }

        /**
         * Representation of a single sitemap file.
         */
        private class SiteMapFile {

            private int entryCount = 0;

            private final String fileName;
            private final TransformerHandler transformerHandler;
            private final FileWriter writer;

            private boolean finished = false;

            public SiteMapFile(int count) throws IOException, SAXException {
                this.fileName = name + '_' + count + ".xml";
                writer = new FileWriter(new File(location, fileName));
                final StreamResult streamResult = new StreamResult(writer);

                try {
                    transformerHandler = tf.newTransformerHandler();

                    Transformer serializer = transformerHandler.getTransformer();

                    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");//
                    serializer.setOutputProperty(OutputKeys.METHOD,"xml");
                    serializer.setOutputProperty(OutputKeys.INDENT, "yes");

                    transformerHandler.setResult(streamResult);
                    transformerHandler.startDocument();

                    AttributesImpl schemaLocation = new AttributesImpl();

                    transformerHandler.startPrefixMapping("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    transformerHandler.startPrefixMapping("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

                    schemaLocation.addAttribute(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schemaLocation", "xsi:schemaLocation", "CDATA", "http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd");


                    transformerHandler.startElement(NS, "", "urlset", schemaLocation);
                } catch (TransformerConfigurationException e) {
                    throw new RuntimeException(e);
                }
            }

            public void addPage(final Page page) throws SAXException {
                transformerHandler.startElement("", "", "url", new AttributesImpl());

                addElement("loc", page.getLocation().toString());

                if (null != page.getLastModified()) {
                    addElement("lastmod", formatDateW3c(page.getLastModified()));
                }

                if (0.5 != page.getPriority()) { // 0.5 is the default and we don't want to waste tags.
                    addElement("priority", Double.toString(page.getPriority()));
                }

                if (null != page.getFrequency()) {
                    addElement("changefreq" , page.getFrequency().name().toLowerCase());
                }

                transformerHandler.endElement("", "", "url");

                entryCount++;
            }

            public String getFileName() {
                return fileName;
            }

            private void addElement(final String element, final String string) throws SAXException {
                final AttributesImpl noAttributes = new AttributesImpl();

                transformerHandler.startElement("", "", element, noAttributes);

                if (string != null) {
                    transformerHandler.characters(string.toCharArray(), 0, string.length());
                }

                transformerHandler.endElement("", "", element);
            }

            public int getEntryCount() {
                return entryCount;
            }

            public void finish() throws IOException {
                if (!finished) {
                    try {
                        transformerHandler.endElement(NS, "", "urlset");
                        transformerHandler.endDocument();
                        this.writer.close();
                        finished = true;
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /*
     * @todo This should really be using the site-spi, but SEARCH-3732 needs to be resolved first.
     */
    private ClassLoader getClassLoader(final URI site) throws MalformedURLException {

        final URL url = site.resolve("/" + site.getHost() + "/lib/sitemap.jar").toURL();
        LOG.info("skin's sitemap.jar at " + url);
        return new URLClassLoader((new URL[] {url}));
    }

    private String formatDateW3c(final Date date) {
        final String iso8601Date = ISO_8601_DATE.format(date);
        final StringBuilder w3cDate = new StringBuilder(iso8601Date);
        // Hmm..is it really not possible to create a w3c compliant date using java.util.Date...
        return w3cDate.insert(w3cDate.length() - 2, ':').toString();
    }
}
