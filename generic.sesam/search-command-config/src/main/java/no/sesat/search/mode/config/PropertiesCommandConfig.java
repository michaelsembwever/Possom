/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * PropertiesCommandConfig.java
 *
 * Created on June 26, 2006, 12:00 PM
 *
 */

package no.sesat.search.mode.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import no.sesat.search.mode.SearchModeFactory.Context;
import no.sesat.search.mode.config.CommandConfig.Controller;
import no.sesat.search.site.config.AbstractDocumentFactory;
import no.sesat.search.site.config.AbstractDocumentFactory.ParseType;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/** Given the resource name for a properties file, loads it into a Map<String,String>.
 *
 * The following is a tutorial for a simple enrichment using this command
 * https://dev.schibstedsok.no/confluence/display/SESAT/Developing+a+quick+and+simple+Enrichment+tutorial
 *
 *
 * @version $Id$
 */
@Controller("PropertiesCommand")
public class PropertiesCommandConfig extends CommandConfig {

    private static final Logger LOG = Logger.getLogger(PropertiesCommandConfig.class);

    private String propertiesFilename;

    private Map<String,String> properties;

    public String getPropertiesFilename(){
        return propertiesFilename;
    }

    public void setPropertiesFilename(final String propertiesFilename){
        this.propertiesFilename = propertiesFilename;
    }

    public Map<String,String> getProperties(){
        return properties;
    }

    public String getProperty(final String key){
        return properties.get(key);
    }

    @Override
    public PropertiesCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit,
            final Context context) {


        super.readSearchConfiguration(element, inherit, context);

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "propertiesFilename", ParseType.String, element, "");

        final Properties props = new Properties();
        context.newPropertiesLoader(context, propertiesFilename + ".properties", props).abut();

        final Map<String,String> map = new HashMap<String,String>();
        for(Map.Entry<Object,Object> entry : props.entrySet()){
            if(entry.getValue() instanceof String){
                map.put((String)entry.getKey(), (String)entry.getValue());
            }
        }
        properties = Collections.unmodifiableMap(map);

        return this;
    }
}
