// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Configuration for the blocket command.
 * 
 * @version $Id$
 */
@Controller("BlocketSearchCommand")
public final class BlocketCommandConfig extends CommandConfig {

    private final Map<String, String> blocketMap = new HashMap<String, String>();

    /**
     * 
     * @return 
     */
    public Map<String, String> getBlocketMap() {
        
        return Collections.unmodifiableMap(blocketMap);
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        // Read blocket.se's around 400 most commonly used search phrases excluding vehicle oriented stuff.
        if(element.getElementsByTagName("blocket-search-words").getLength() > 0){
            
            final Element words = (Element) element.getElementsByTagName("blocket-search-words").item(0);

            final NodeList wordList = words.getElementsByTagName("word");

            // loop through words.
            for (int i = 0; i < wordList.getLength(); ++i) {
                final Element wordElement = (Element) wordList.item(i);
                final String cid = wordElement.getAttribute("category-id");
                final String catName = wordElement.getAttribute("category");
                final String word = wordElement.getTextContent();
                // Put words into a map
                blocketMap.put(word, cid + ":" + catName);
            }
        }else if(null != inherit && inherit instanceof BlocketCommandConfig){
            
            blocketMap.putAll(((BlocketCommandConfig)inherit).getBlocketMap());
        }

        return this;
    }

    
}
