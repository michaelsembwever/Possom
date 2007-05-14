// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.BlocketCommandConfig;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.axis.client.Stub;
import org.apache.log4j.Logger;
import se.blocket.www2.search.SearchLocator;
import se.blocket.www2.search.SearchPortType;


public final class BlocketSearchCommand extends AbstractWebServiceSearchCommand {

    private static final Logger LOG = Logger.getLogger(BlocketSearchCommand.class);

    private static final String ERR_FAILED_BLOCKET_SEARCH = "Failed Blocket search command";

    private static final String ERR_FAILED_ENCODE_BLOCKET = "Failed to encode Blocket search query";

    public BlocketSearchCommand(final Context cxt) {

        super(cxt);
    }

    @Override
    public ResultList<? extends ResultItem> execute() {

        BlocketCommandConfig bsc = (BlocketCommandConfig) context.getSearchConfiguration();

        final SearchLocator service = new SearchLocator();
        final ResultList<ResultItem> result = new BasicSearchResult<ResultItem>();

        try {
            final SearchPortType port = service.getsearchPort(new java.net.URL(service.getsearchPortAddress()));
            ((Stub)port).setTimeout(1000);

            final String query = getTransformedQuery();

            /* Innhåller kategorimappningar för blocket */
            final String category = (String) bsc.getBlocketMap().get(query.toLowerCase());
            String categoryFields[] = null;
            String categoryName = "";           // Dont use category name for "Alla kategorier"
            String categoryIndex = "0";         // 0 = Alla kategorier, constant for this?

            if (category != null) {
                categoryFields = category.split(":");
                if (categoryFields.length == 2) {
                    categoryIndex = categoryFields[0];
                    StringBuilder sb = new StringBuilder("i ");            //  1042 annonsträffar i TV-spel & PC-spel på WOW  ELLER 834 annonsträffar på LG
                    sb.append(categoryFields[1].replace("&","&amp;"));
                    categoryName = sb.toString();
                }
            }

            /* Svarsparametrar ifrån blocket */
            LongHolder lholder = new LongHolder();
            StringHolder sholder = new StringHolder();

            LOG.debug("Executing blocket search command with searchquery: " + query);

            if (category != null) {
                port.search(query, Integer.parseInt(categoryIndex), "", lholder, sholder);
            }

            final String nads = Long.toString(lholder.value);
            if ((nads != null) && (!nads.equalsIgnoreCase("0"))) {
                result.addField("searchquery", query);
                result.addField("categoryName", categoryName);
                result.addField("numberofads", nads);
                result.addField("blocketbackurl", URLEncoder.encode(sholder.value, "iso-8859-1"));
                result.setHitCount(Integer.parseInt(nads));
            }
        }

        catch (ServiceException se) {
            LOG.error(ERR_FAILED_BLOCKET_SEARCH, se);
            throw new InfrastructureException(se);
        } catch (MalformedURLException murle) {
            LOG.error(ERR_FAILED_BLOCKET_SEARCH, murle);
            throw new InfrastructureException(murle);
        } catch (RemoteException re) {
            LOG.error(ERR_FAILED_BLOCKET_SEARCH, re);
            throw new InfrastructureException(re);
        } catch (UnsupportedEncodingException usee) {
            LOG.error(ERR_FAILED_ENCODE_BLOCKET, usee);
        }
        return result;
    }
}
