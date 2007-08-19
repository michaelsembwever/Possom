/*
 * DataModelSerializeTest.java
 *
 * Created on 11-Jun-2007, 13:42:18
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.sesat.searchportal.datamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import no.sesat.searchportal.datamodel.generic.DataObject;
import no.sesat.searchportal.datamodel.generic.StringDataObject;
import no.sesat.searchportal.datamodel.junkyard.JunkYardDataObject;
import no.sesat.searchportal.datamodel.navigation.NavigationDataObject;
import no.sesat.searchportal.datamodel.query.QueryDataObject;
import no.sesat.searchportal.datamodel.request.BrowserDataObject;
import no.sesat.searchportal.datamodel.request.ParametersDataObject;
import no.sesat.searchportal.datamodel.search.SearchDataObject;
import no.sesat.searchportal.datamodel.site.SiteDataObject;
import no.sesat.searchportal.datamodel.user.UserDataObject;
import no.sesat.searchportal.view.navigation.NavigationConfig;
import no.sesat.searchportal.query.Clause;
import no.sesat.searchportal.query.Query;
import no.sesat.searchportal.query.Visitor;
import no.sesat.searchportal.query.finder.ParentFinder;
import no.sesat.searchportal.query.parser.AbstractQuery;
import no.sesat.searchportal.query.token.TokenPredicate;
import no.sesat.searchportal.result.BasicNavigationItem;
import no.sesat.searchportal.result.BasicResultItem;
import no.sesat.searchportal.result.BasicResultList;
import no.sesat.searchportal.result.NavigationItem;
import no.sesat.searchportal.result.ResultList;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.Site.Context;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.config.FileResourceLoader;
import no.sesat.searchportal.site.config.PropertiesLoader;
import no.sesat.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.user.User;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 *
 * @author andersjj
 *  @version $Id$
 */

public class DataModelSerializeTest {
    
    private static final Logger LOG = Logger.getLogger(DataModelSerializeTest.class);
    
    private static final String JUNK_YARD_KEY = "foo";
    private static final String JUNK_YARD_VALUE = "bar";
    private static final int    SEARCH_HIT_COUNT = 10;
    private static final String SEARCH_KEY = "default";
    private static final String NAVIGATION_TITLE = "navigationTitle";
    private static final String NAVIGATION_URL = "http://navigationURL";
    private static final int    NAVIGATION_HIT_COUNT = 11;
    private static final String NAVIGATION_KEY = "navigationKey";
    private static final String QUERY_STRING = "sesam";
    private static final String USER_AGENT = "Opera (Linux)";
    private static final String REMOTE_ADDRESS = "127.0.0.1";
    
    public DataModelSerializeTest() {
        
    }
    
    @Test
    public void testSerialize() throws Exception {
        final DataModelFactory factory = new DataModelFactoryImpl(new DataModelFactory.Context(){
            public Site getSite() {
                return Site.DEFAULT;
            }
            
            public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {
                return TestResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
        });
        
        final DataModel datamodel = factory.instantiate();
        
        final Site site = getTestingSite();
        
        final SiteConfiguration.Context cxt = new SiteConfiguration.Context() {
            public Site getSite() {
                return site;
            }
            
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteContext,
                    final String resource,
                    final Properties properties) {
                return TestResourceLoader.newPropertiesLoader(siteContext, resource, properties);
            }
        };
        
        final SiteConfiguration siteConfig = SiteConfiguration.valueOf(cxt);
        
        final SiteDataObject siteDO = factory.instantiate(
                SiteDataObject.class,
                new DataObject.Property("site", site),
                new DataObject.Property("siteConfiguration", siteConfig));
        datamodel.setSite(siteDO);
        
        final JunkYardDataObject junkYardDO = factory.instantiate(
                JunkYardDataObject.class,
                new DataObject.Property("values", new Hashtable<String,Object>()));
        datamodel.setJunkYard(junkYardDO);
        
        datamodel.getJunkYard().setValue(JUNK_YARD_KEY, JUNK_YARD_VALUE);
        
//        final SearchConfiguration searchConfiguration = new StaticCommandConfig();
       final ResultList<BasicResultItem> searchResults = new BasicResultList<BasicResultItem>();
        searchResults.setHitCount(SEARCH_HIT_COUNT);
     
        final Set<TokenPredicate> tokenPredicateSet = new HashSet<TokenPredicate>();
        tokenPredicateSet.add(TokenPredicate.FOOD);
        
        final Clause root = ClauseFactory.createClause(QUERY_STRING, tokenPredicateSet);
        /* new Clause() {

            public String getTerm() {
                return QUERY_STRING;
            }

            public Set<TokenPredicate> getKnownPredicates() {
                return tokenPredicateSet;
            }

            public Set<TokenPredicate> getPossiblePredicates() {
                return tokenPredicateSet;
            }

            public void accept(Visitor visitor) {
            }
        };*/
       
        final ParentFinder parentFinder = new ParentFinder();
        
        final Query query = AbstractQuery.createQuery(QUERY_STRING, false, root, parentFinder); 
        /*new AbstractQuery(QUERY_STRING){
            public Clause getRootClause(){
                return root;
            }
            public ParentFinder getParentFinder(){
                return parentFinder;
            }
            public boolean isBlank(){
                return false;
            }
        };*/
        
        final QueryDataObject queryDO = factory.instantiate(
                QueryDataObject.class,
                new DataObject.Property("query", query));
        
        datamodel.setQuery(queryDO);
        
        final SearchDataObject searchDO = factory.instantiate(
                SearchDataObject.class,
                new DataObject.Property("configuration", null),
                new DataObject.Property("results", searchResults),
                new DataObject.Property("query", queryDO));
        datamodel.setSearch(SEARCH_KEY, searchDO);
       
        final StringDataObject userAgent = factory.instantiate(
                StringDataObject.class,
                new DataObject.Property("string", USER_AGENT));
        
        final StringDataObject remoteAddress = factory.instantiate(
                StringDataObject.class,
                new DataObject.Property("string", REMOTE_ADDRESS));
        
        final BrowserDataObject browserDO = factory.instantiate(
                BrowserDataObject.class,
                new DataObject.Property("userAgent", userAgent),
                new DataObject.Property("remoteAddress", remoteAddress));
        
        datamodel.setBrowser(browserDO);
        
        final NavigationConfig navigationConfiguration = new NavigationConfig(null);
        final Map<String,NavigationItem> navigation = new HashMap<String,NavigationItem>();
        final NavigationItem navigationItem = new BasicNavigationItem(NAVIGATION_TITLE, NAVIGATION_URL, NAVIGATION_HIT_COUNT);
        navigation.put(NAVIGATION_KEY, navigationItem);
        
        final NavigationDataObject navigationDO = factory.instantiate(
                NavigationDataObject.class,
                new DataObject.Property("configuration", navigationConfiguration),
                new DataObject.Property("navigations", navigation));
        datamodel.setNavigation(navigationDO);
      
        final ParametersDataObject parametersDO = factory.instantiate(
                ParametersDataObject.class);
        datamodel.setParameters(parametersDO);
     
        final User user = new TestUser();
        
        final UserDataObject userDO = factory.instantiate(
                UserDataObject.class,
                new DataObject.Property("user", user));
        datamodel.setUser(userDO);
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream os = new ObjectOutputStream(baos);
        
        LOG.info("Serializing datamodel");
        os.writeObject(datamodel);
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream is = new ObjectInputStream(bais);
        
        LOG.info("De-serializing datamodel");
        final DataModel datamodel2 = (DataModel) is.readObject();
        
        assert datamodel2 != null : "is.readObject() returned NULL";
        LOG.info("Datamodel: OK");
        
        final SiteDataObject siteDO2 = datamodel2.getSite();
        assert siteDO2 != null : "siteDO2 == null";
        
        final Site site2 = siteDO2.getSite();
        assert site2 != null : "site2 == null";
        
        assert site.getName().equals(site2.getName()) : "Expected: '" + site.getName() + "', got: '" + site2.getName() + "'";
        assert site.getConfigContext().equals(site2.getConfigContext()) : "Expected: '" + site.getConfigContext() + "', got: '" + site2.getName() + "'";
        assert site.getTemplateDir().equals(site2.getTemplateDir()) : "Expected: '" + site.getTemplateDir() + "', got: '" + site2.getTemplateDir() + "'";
        
        LOG.info("Site: OK");
        
        assert datamodel2.getJunkYard() != null : "getJunkYard() returned NULL";
        
        assert datamodel2.getJunkYard() instanceof JunkYardDataObject
                : "expected getJunkYard() to return instance of JunkYardObject, instead it returned: "
                + datamodel2.getJunkYard().toString();
        
        final JunkYardDataObject junkYardDO2 = datamodel2.getJunkYard();
        
        assert JUNK_YARD_VALUE.equals(junkYardDO2.getValue(JUNK_YARD_KEY)) : "Expected: '" + JUNK_YARD_VALUE + "', got: '" + junkYardDO2.getValue(JUNK_YARD_KEY) + "'";
        LOG.info("Junk yard: OK");
        
        assert datamodel2.getSearch(SEARCH_KEY) != null : "datamodel2.getSearch(\"" + SEARCH_KEY + "\") == null";
        LOG.info("Search: OK");
        
        assert datamodel2.getNavigation() != null : "datamodel2.getNavigation() == null";
        
        final Map<String,NavigationItem> navigation2 = datamodel2.getNavigation().getNavigations();
        assert navigation2.get(NAVIGATION_KEY) != null : "navigation2.get(" + NAVIGATION_KEY + ") == null";
        
        final NavigationItem navigationItem2 = navigation2.get(NAVIGATION_KEY);
        assert navigationItem2.getTitle().equals(NAVIGATION_TITLE) : "Expected: '" + NAVIGATION_TITLE + "', got: '" + navigationItem2.getTitle() + "'";
        assert navigationItem2.getUrl().equals(NAVIGATION_URL) : "Expected: '" + NAVIGATION_URL + "', got: '" + navigationItem2.getUrl() + "'";
        assert navigationItem2.getHitCount() == NAVIGATION_HIT_COUNT : "Expected: '" + NAVIGATION_HIT_COUNT + "', got: '" + navigationItem2.getHitCount() + "'";
        
        LOG.info("Navigation: OK");
        
        assert datamodel2.getQuery() != null : "datamodel2.getQuery()";
    
        final Query query2 = datamodel2.getQuery().getQuery();
        assert query2.getQueryString().equals(QUERY_STRING) : "Expected: '" + QUERY_STRING + "', got: '" + query2.getQueryString() + "'";
       
        LOG.info("Query: OK");
        
        final BrowserDataObject browserDO2 = datamodel2.getBrowser();
        assert browserDO2 != null : "browserDO2 == null";
        assert browserDO2.getUserAgent().getString().equals(USER_AGENT) : "Expected: '" + USER_AGENT + "', got: '" + browserDO2.getUserAgent().getString();
        
        LOG.info("Browser: OK");
        
        final ParametersDataObject parametersDO2 = datamodel2.getParameters();
        assert parametersDO2 != null : "parametersDO2 == null";
      
        LOG.info("Parameters: OK");
        
        final UserDataObject userDO2 = datamodel2.getUser();
        assert userDO2 != null : "userDO2 == null";
        LOG.info("User: OK");
    }
    
    protected Site.Context getSiteConstructingContext(){
        
        return new Context(){
            public String getParentSiteName(final SiteContext siteContext){
                // we have to do this manually instead of using SiteConfiguration,
                //  because SiteConfiguration relies on the parent site that we haven't get initialised.
                // That is, the PARENT_SITE_KEY property MUST be explicit in the site's configuration.properties.
                final Properties props = new Properties();
                final PropertiesLoader loader
                        = TestResourceLoader.newPropertiesLoader(siteContext, Site.CONFIGURATION_FILE, props);
                loader.abut();
                return props.getProperty(Site.PARENT_SITE_KEY);
            }
        };
    }
    
    protected Site getTestingSite(){
        
        final String basedir = "localhost";
        return Site.valueOf(
                getSiteConstructingContext(),
                basedir,
                Locale.getDefault());
    }    

}

class TestResourceLoader extends FileResourceLoader {
    
    private static final Logger LOG = Logger.getLogger(TestResourceLoader.class);
    
    protected TestResourceLoader(final SiteContext cxt) {
        super(cxt);
    }
    
    public static PropertiesLoader newPropertiesLoader(
            final SiteContext siteCxt,
            final String resource,
            final Properties properties) {
        
        final PropertiesLoader pl = new TestResourceLoader(siteCxt);
        return pl;
    }
    
    @Override
    public void init(final String resource, final Properties properties) {}
    
    private static final Properties props = new Properties();
    static {
        props.setProperty(Site.DEFAULT_SITE_LOCALE_KEY, "no");
        props.setProperty("sesam.datamodel.impl", "no.sesat.searchportal.datamodel.DataModelFactoryImpl");
        props.setProperty(Site.DEFAULT_SERVER_PORT_KEY, "0");
        props.setProperty(Site.DEFAULT_SITE_KEY, "localhost");
        props.setProperty(Site.PARENT_SITE_KEY, "");
    }
    
    @Override
    public Properties getProperties() {
        return props;
    }
    
    @Override
    public void abut() {}
}

class ClauseFactory implements Serializable {
    static Clause createClause(final String term, final Set<TokenPredicate> predicateSet) {
        return new Clause() {
            public String getTerm() {
                return term;
            }

            public Set<TokenPredicate> getKnownPredicates() {
                return predicateSet;
            }

            public Set<TokenPredicate> getPossiblePredicates() {
                return predicateSet;
            }

            public void accept(Visitor visitor) {
            }
        };
    }
}

class TestUser implements User {}
