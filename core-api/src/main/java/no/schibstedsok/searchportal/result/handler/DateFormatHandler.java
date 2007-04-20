// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * DateFormatHandler is part of no.schibstedsok.searchportal.result
 * Transform fast inputdate to how it will be displayed in tv enrichment . Tv Enrichment
 * will most likely only display Hour of day.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public final class DateFormatHandler implements ResultHandler {

    /**
     * 
     */
    public enum Fields {
        /**
         * 
         */
        YEAR,
        /**
         * 
         */
        MONTH,
        /**
         * 
         */
        DAY,
        /**
         * 
         */
        HOUR,
        /**
         * 
         */
        MINUTE,
        /**
         * 
         */
        SECOND;
    }
    
    private final DateFormatResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public DateFormatHandler(final ResultHandlerConfig config){
        this.config = (DateFormatResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {

            final String docDateTime = item.getField(config.getSourceField());

            if (docDateTime != null) {
                
                final String year = docDateTime.substring(0,4);
                item.addField(config.getFieldPrefix() + Fields.YEAR.name(), year);

                final String month = docDateTime.substring(5,7);
                item.addField(config.getFieldPrefix() + Fields.MONTH.name(), month);

                final String day = docDateTime.substring(8,10);
                item.addField(config.getFieldPrefix() + Fields.DAY.name(), day);

                final String hour = docDateTime.substring(11, 13);
                item.addField(config.getFieldPrefix() + Fields.HOUR.name(), hour);

                final String minute = docDateTime.substring(14, 16);
                item.addField(config.getFieldPrefix() + Fields.MINUTE.name(), minute);

                final String second = docDateTime.substring(17, 19);
                item.addField(config.getFieldPrefix() + Fields.SECOND.name(), second);
            }

        }
    }
}
