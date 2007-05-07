package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Geir H. Pettersn (T-Rank)
 */
@Controller("FieldFilter")
public class FieldFilterResultHandlerConfig extends AbstractResultHandlerConfig {
    private String recursiveField;
    private String filterSrc;
    private String matchList;
    private String removeFields;
    private String srcPrefixes;
    private Set<String> matchListSet;
    private String[] removeFieldsArray;


    public String getRecursiveField() {
        return recursiveField;
    }

    public String getFilterSrc() {
        return filterSrc;
    }

    public Set<String> getMatchListSet() {
        if (matchListSet == null) {
            matchListSet = new HashSet<String>();
            final String[] mlArray = StringUtils.split(matchList, ',');
            if (mlArray != null) {
                for (String s : mlArray) {
                    matchListSet.add(s.toLowerCase());
                    // Handle srcPrefixes
                    final String[] srcPref = StringUtils.split(srcPrefixes, ',');
                    if (srcPref != null) {
                        for (String prefix : srcPref) {
                            matchListSet.add(prefix.toLowerCase() + s.toLowerCase());
                        }
                    }
                }
            }
        }
        return matchListSet;
    }

    public String[] getRemoveFieldsArray() {
        if (removeFieldsArray == null) {
            removeFieldsArray = StringUtils.split(removeFields, ',');
        }
        return removeFieldsArray;
    }

    public String getMatchList() {
        return matchList;
    }

    public String getRemoveFields() {
        return removeFields;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        recursiveField = element.getAttribute("recursive-field");
        filterSrc = element.getAttribute("filter-src");
        matchList = element.getAttribute("match-list");
        removeFields = element.getAttribute("remove-fields");
        srcPrefixes = element.getAttribute("src-prefixes");
        return this;
    }
}
