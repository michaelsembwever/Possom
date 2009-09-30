/*
 * Copyright (2009) Schibsted ASA
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
package no.sesat.search.mode.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.List;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import net.sf.json.JSONSerializer;
import no.sesat.search.mode.config.GoogleCommandConfig;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.log4j.Logger;

/**
 * @xxx very little has been done on this
 * @todo extract out a AbstractJsonSearchCommand
 * @see GoogleCommandConfig
 * @version $Id$
 */
public class GoogleSearchCommand extends AbstractRestfulSearchCommand {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(GoogleSearchCommand.class);

    private static final String COMMAND_URL_QUERY = "?q={0}&rsz={1}&start={2}&v=1.0";

    // Static --------------------------------------------------------
    // Attributes ----------------------------------------------------

    private final Context cxt;
    private final GoogleCommandConfig conf;

    // Constructors -------------------------------------------------

    public GoogleSearchCommand(final Context cxt) {
        super(cxt);
        this.cxt = cxt;
        conf = (GoogleCommandConfig) cxt.getSearchConfiguration();
        final String commandPath = cxt.getDataModel().getSite().getSiteConfiguration().getProperty(conf.getPath());
        setRestful(
                new AbstractRestful(cxt) {
                    @Override
                    public String createRequestURL() {

                        return MessageFormat.format(
                                commandPath + COMMAND_URL_QUERY,
                                cxt.getDataModel().getQuery().getUtf8UrlEncoded(),
                                conf.getLargeResults() ? "large" : "small",
                                GoogleSearchCommand.this.getOffset());
                    }
        });
    }

    // Public --------------------------------------------------------

    @Override
    public ResultList<ResultItem> execute() {

        final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final BufferedReader reader;
        try {

            reader = getRestful().getHttpReader("UTF8");


            if(null != reader) {

                final StringBuilder builder = new StringBuilder();
                String line;
                while(null != (line = reader.readLine())){
                    builder.append(line);
                }

                final DynaBean bean = (DynaBean) JSONSerializer.toJava(JSONSerializer.toJSON(builder.toString()));
                final DynaBean data = (DynaBean)bean.get("responseData");

                final String totalResults = ((DynaBean)data.get("cursor")).get("estimatedResultCount").toString();
                result.setHitCount(Integer.parseInt(totalResults));

                final List<DynaBean> results = (List<DynaBean>)data.get("results");

                for(DynaBean r : results) {

                    result.addResult(createItem(r));
                }

            }
        } catch (SocketTimeoutException ste) {
            LOG.error(getSearchConfiguration().getName() +  " --> " + ste.getMessage());
            return new BasicResultList<ResultItem>();
        } catch (IOException ex) {
            throw new SearchCommandException(ex);
        }
        return result;
    }

    // Protected --------------------------------------------------------

    protected ResultItem createItem(final DynaBean entry) {

        ResultItem resItem = new BasicResultItem();

        final DynaProperty[] descriptors = entry.getDynaClass().getDynaProperties();
        for(DynaProperty d : descriptors){
            if(entry.get(d.getName()) instanceof Serializable){
                resItem = resItem.addObjectField(d.getName(), (Serializable) entry.get(d.getName()));
            }
        }

        return resItem;

    }

    @Override
    protected int getOffset() {
        return super.getOffset();
    }

    // Private --------------------------------------------------------

}