// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 4510 $</tt>
 */
@Controller("FieldChooser")
public final class FieldChooserResultHandlerConfig extends AbstractResultHandlerConfig {

    private final List<String> fields = new ArrayList<String>();
    private String targetField;
    private String defaultValue;
    private String recursiveField;

    /**
     * @param fieldName
     */
    public void addField(final String fieldName) {
        fields.add(fieldName);
    }

    /**
     * @return
     */
    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * @param fieldName
     */
    public void setTargetField(final String fieldName) {
        targetField = fieldName;
    }

    /**
     * @return
     */
    public String getTargetField() {
        return targetField;
    }


    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRecursiveField() {
        return recursiveField;
    }

    public void setRecursiveField(String recursiveField) {
        this.recursiveField = recursiveField;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {

        super.readResultHandler(element);

        setTargetField(element.getAttribute("target"));
        String optAttr = element.getAttribute("recursive-field");
        if (optAttr != null && optAttr.length() > 0) {
            recursiveField = optAttr;
        }
        optAttr = element.getAttribute("default-value");
        if (optAttr != null && optAttr.length() > 0) {
            defaultValue = optAttr;
        }
        final String[] fields = element.getAttribute("fields").split(",");
        for (String field : fields) {
            addField(field);
        }

        return this;
    }


}
