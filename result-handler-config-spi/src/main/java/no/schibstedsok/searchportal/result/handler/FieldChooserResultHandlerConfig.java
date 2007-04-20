// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collections;
import java.util.List;
import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 4510 $</tt>
 */
@Controller("FieldChooser")
public final class FieldChooserResultHandlerConfig extends AbstractResultHandlerConfig {

    private final List<String> fields = new ArrayList<String>();
    private String targetField;

    /**
     * 
     * @param fieldName 
     */
    public void addField(final String fieldName) {
        fields.add(fieldName);
    }

    /**
     * 
     * @return 
     */
    public List<String> getFields(){
        return Collections.unmodifiableList(fields);
    }
    /**
     * 
     * @param fieldName 
     */
    public void setTargetField(final String fieldName) {
        targetField = fieldName;
    }
    
    /**
     * 
     * @return 
     */
    public String getTargetField(){
        return targetField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);
        
        setTargetField(element.getAttribute("target"));
        final String[] fields = element.getAttribute("fields").split(",");
        for (String field : fields) {
            addField(field);
        }

        return this;
    }
    
    
}
