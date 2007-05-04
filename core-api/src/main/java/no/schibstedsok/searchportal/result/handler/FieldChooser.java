// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;

import java.util.Collection;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class FieldChooser implements ResultHandler {
    private static final Logger LOG = Logger.getLogger(FieldChooser.class);

    private final FieldChooserResultHandlerConfig config;

    public FieldChooser(final ResultHandlerConfig config) {
        this.config = (FieldChooserResultHandlerConfig) config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {

        final Collection<String> fields = config.getFields();
        final SearchResult searchResult = cxt.getSearchResult();
        chooseField(searchResult, fields);
    }

    public void chooseField(SearchResult searchResult, Collection<String> fields) {
        if (searchResult != null && searchResult.getResults().size() > 0) {
            for (SearchResultItem item : searchResult.getResults()) {
                for (String field : fields) {
                    if (item.getField(field) != null) {
                        item.addField(config.getTargetField(), item.getField(field));
                        break;
                    }
                }
                if (config.getDefaultValue() != null && item.getField(config.getTargetField()) == null) {
                    item.addField(config.getTargetField(), config.getDefaultValue());
                }
                if (config.getRecursiveField() != null) {
                    chooseField(item.getNestedSearchResult(config.getRecursiveField()), fields);
                }
            }
        }
    }


}
