/*
 * SearchResultModuleGenerator.java
 */

package no.schibstedsok.searchportal.view.output.syndication.modules;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Syndication feed generator for the extended sesam feed syntax. 
 */
public class SearchResultModuleGenerator implements ModuleGenerator {
    
    private static Set<Namespace> NAMESPACES;
    
    static {
        final Set nss = new HashSet();
        nss.add(SearchResultModuleImpl.NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }
    
    /**
     * Creates a new instance of this class.
     */
    public SearchResultModuleGenerator() {
    }

    /**
     * @inherit
     */
    public String getNamespaceUri() {
        return SearchResultModule.URI;
    }

    /**
     * @inherit
     */
    public Set getNamespaces() {
        return NAMESPACES;
    }

    /**
     * @inherit
     */
    public void generate(final Module module, final Element element) {

        // this is not necessary, it is done to avoid the namespace definition in every item.
        Element root = element;
        while (root.getParent()!=null && root.getParent() instanceof Element) {
            root = (Element) element.getParent();
        }
        root.addNamespaceDeclaration(SearchResultModuleImpl.NS);

        final SearchResultModule m = (SearchResultModule) module;
        
        if (m.getNumberOfHits() != null) {
            element.addContent(generateSimpleElement("numberOfHits", m.getNumberOfHits()));
        }
    }

    private  Element generateSimpleElement(final String name, final String value)  {
        final Element element = new Element(name, SearchResultModuleImpl.NS);
        element.addContent(value);
        return element;
    }
}
