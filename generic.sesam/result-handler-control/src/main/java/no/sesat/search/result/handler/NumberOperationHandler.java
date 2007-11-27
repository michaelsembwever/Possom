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


import java.text.NumberFormat;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;


/** Perform a JEP operation.
 *
 * @author mick
 * @version <tt>$Id$</tt>
 */
public final class NumberOperationHandler implements ResultHandler {

    private static final Logger LOG = Logger.getLogger(NumberOperationHandler.class);

    private final NumberOperationResultHandlerConfig config;

    /** {@inherit} **/
    public NumberOperationHandler(final ResultHandlerConfig config){
        this.config = (NumberOperationResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        final ResultList<ResultItem> result = cxt.getSearchResult();

        final NumberFormat formatter = NumberFormat.getInstance(datamodel.getSite().getSite().getLocale());
        formatter.setMinimumIntegerDigits(config.getMinDigits());
        formatter.setMaximumIntegerDigits(config.getMaxDigits());
        formatter.setMinimumFractionDigits(config.getMinFractionDigits());
        formatter.setMaximumFractionDigits(config.getMaxFractionDigits());

        final JEP parser = new JEP();

        parser.addStandardConstants();
        parser.addStandardFunctions();
        parser.addComplex();

        for(ResultItem i : result.getResults()){

            final BasicResultItem item = (BasicResultItem) i;
            for(String field : config.getFields()){
                final String value = (String) item.getField(field);
                parser.addVariable(field, value != null && value.length()>0 ? Double.parseDouble(value) : 0D);
            }

            parser.parseExpression(config.getOperation());

            final String r = formatter.format(parser.getValue());
            LOG.debug(config.getOperation() + '=' + r);
            item.addField(config.getTarget(), r);
        }
    }

}
