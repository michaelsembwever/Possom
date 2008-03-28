/* Copyright (2006-2007) Schibsted Søk AS
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

 */
package no.sesat.search.result.handler;


import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;

/**
 * DateFormatHandler is part of no.sesat.search.result
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

        for (final ResultItem item : cxt.getSearchResult().getResults()) {

            final String docDateTime = item.getField(config.getSource());

            if (docDateTime != null) {
                
                final String year = docDateTime.substring(0,4);
                final String month = docDateTime.substring(5,7);
                final String day = docDateTime.substring(8,10);
                final String hour = docDateTime.substring(11, 13);
                final String minute = docDateTime.substring(14, 16);
                final String second = docDateTime.substring(17, 19);
                
                cxt.getSearchResult().replaceResult(item, 
                    item.addField(config.getPrefix() + Fields.YEAR.name(), year)
                            .addField(config.getPrefix() + Fields.MONTH.name(), month)
                            .addField(config.getPrefix() + Fields.DAY.name(), day)
                            .addField(config.getPrefix() + Fields.HOUR.name(), hour)
                            .addField(config.getPrefix() + Fields.MINUTE.name(), minute)
                            .addField(config.getPrefix() + Fields.SECOND.name(), second)
                    );
            }

        }
    }
}
