// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.w3c.dom.Element;

/**
 * Used to create a document summary. Basically concatinates two fields if both are peresent and not empty(For all
 * results) If not bothe fields are there it will populate the target field with a fallback field.
 *
 * @author Geir H. Pettersen (T-Rank)
 */
@Controller("DocumentSummaryResultHandler")
public class DocumentSummaryResultHandlerConfig extends AbstractResultHandlerConfig {
    private String firstSummaryField;
    private String secondSummaryField;
    private String fallbackField;
    private String targetField;
    private String recursiveField;
    private String fieldSeparator;

    /**
     * @return the firrst summary field to cocncatenate
     */
    public String getFirstSummaryField() {
        return firstSummaryField;
    }

    /**
     * @return the second summary field to concatenate
     */
    public String getSecondSummaryField() {
        return secondSummaryField;
    }

    /**
     * @return the field to fall back to if not both summary fields are populated
     */
    public String getFallbackField() {
        return fallbackField;
    }

    /**
     * @return the target field to populate
     */
    public String getTargetField() {
        return targetField;
    }

    /**
     * @return if defined, the field that contains subresults to process
     */
    public String getRecursiveField() {
        return recursiveField;
    }


    /**
     * @return the text to separate the conactenated fields
     */
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        firstSummaryField = element.getAttribute("first-summary-field");
        secondSummaryField = element.getAttribute("second-summary-field");
        fallbackField = element.getAttribute("fallback-field");
        targetField = element.getAttribute("target-field");
        recursiveField = element.getAttribute("recursive-field");
        if (recursiveField != null && recursiveField.length() == 0) {
            recursiveField = null;
        }
        fieldSeparator = element.getAttribute("field-separator");
        return this;
    }

}
