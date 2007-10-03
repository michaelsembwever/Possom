/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

@Controller("NewsMediumQueryTransformer")
public class NewsMediumQueryTransformerConfig extends AbstractQueryTransformerConfig {
    public static final String ALL_MEDIUMS = "all";
    private String mediumPrefix = "medium";
    private String defaultMedium = "webnewsarticle";
    private String mediumParameter = "medium";


    public String getMediumPrefix() {
        return mediumPrefix;
    }

    public String getDefaultMedium() {
        return defaultMedium;
    }

    public String getMediumParameter() {
        return mediumParameter;
    }

    @Override
    public AbstractQueryTransformerConfig readQueryTransformer(final Element element) {
        String s = element.getAttribute("medium-prefix");
        if (s != null && s.length() > 0) {
            mediumPrefix = s;
        }
        s = element.getAttribute("default-medium");
        if (s != null && s.length() > 0) {
            defaultMedium = s;
        }
        s = element.getAttribute("medium-parameter");
        if (s != null && s.length() > 0) {
            mediumParameter = s;
        }
        return this;
    }
}
