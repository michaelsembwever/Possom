// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

/**
 * @author ssthkjer
 * @version $Id$
 */
@Controller("ImageHelper")
public final class ImageHelperResultHandlerConfig extends AbstractResultHandlerConfig {

    private final Map<String,String> fieldMap = new HashMap<String,String>();

    /**
     * 
     * @param field 
     * @param as 
     */
    public void addField(final String field, final String as){
        fieldMap.put(field, as);
    }

    /**
     * 
     * @return 
     */
    public Map<String,String> getFieldMap(){
        return Collections.unmodifiableMap(fieldMap);
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);

        if (element.getAttribute("fields").length() > 0) {
            final String[] fields = element.getAttribute("fields").split(",");
            for (String field : fields) {
                if (field.contains(" AS ")) {
                    final String[] ff = field.split(" AS ");
                    addField(ff[0], ff[1]);
                } else {
                    addField(field, field);
                }
            }
        }

        return this;
    }
    
    
}
