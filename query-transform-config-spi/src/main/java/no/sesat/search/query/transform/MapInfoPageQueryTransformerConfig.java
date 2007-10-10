/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

/**
 * 
 * @author Stian Hegglund
 * @version $Revision:$
 */
@Controller("MapInfoPageQueryTransformer")
public class MapInfoPageQueryTransformerConfig extends AbstractQueryTransformerConfig{
    private String parameterName = "contentid";
    private String prefix = "recordid";
    private String filterPrefix = "ywpostnr";
    private String filterParameterName = "fp";
            
    public final String getParameterName() {
        return parameterName;
    }
    
    public final String getPrefix() {
        return prefix;
    }
    
    public final String getFilterPrefix() {
        return filterPrefix;
    }
    
    public final String getFilterParameterName() {
        return filterParameterName;
    }
    
    @Override
    public MapInfoPageQueryTransformerConfig readQueryTransformer(Element element) {
        if (element.hasAttribute("parameter-name")){
            this.parameterName = element.getAttribute("parameter-name");
        }
        
        if (element.hasAttribute("prefix")) {
            this.prefix = element.getAttribute("prefix");
        }
    
        if (element.hasAttribute("filter-prefix")) {
            this.filterPrefix = element.getAttribute("filter-prefix");
        }
        
        if (element.hasAttribute("filter-parameter-name")) {
            this.filterParameterName = element.getAttribute("filter-parameter-name");
        }
        
        return this;
    }
}
