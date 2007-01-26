package no.schibstedsok.searchportal.mode.command;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;

import java.util.Map;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import se.blocket.www2.search.SearchLocator;
import se.blocket.www2.search.SearchPortType;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.BlocketSearchConfiguration;

import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;

public class BlocketSearchCommand extends AbstractWebServiceSearchCommand {

	private static final Logger LOG = Logger
			.getLogger(BlocketSearchCommand.class);

	private static final String ERR_FAILED_BLOCKET_SEARCH = "Failed Blocket search command";

	private static final String ERR_FAILED_ENCODE_BLOCKET = "Failed to encode Blocket search query";

	public BlocketSearchCommand(final Context cxt,
			final Map<String, Object> parameters) {
		super(cxt, parameters);
	}

	@Override
	public SearchResult execute() {

		BlocketSearchConfiguration bsc = (BlocketSearchConfiguration) context
				.getSearchConfiguration();

		final SearchLocator service = new SearchLocator();
		final SearchResult result = new BasicSearchResult(this);

		try {
			final SearchPortType port = service.getsearchPort(new java.net.URL(
					service.getsearchPortAddress()));

			String query = getTransformedQuery();
			/*Trimmar frågan så den har samma format som i blocket.properties, 
			 dvs inga å ä ö eller mellanslag*/
			String trimQ = StringUtils.deleteWhitespace(query);
			String trimQ1 = StringUtils.replace(trimQ, "å", "a");
			String trimQ2 = StringUtils.replace(trimQ1, "ä", "a");
			String trimQ3 = StringUtils.replace(trimQ2, "ö", "o");
			/*Innhåller kategorimappningar för blocket*/
			Map top100BlocketMap = bsc.getBlocketMap();
			String categoryIndex = (String) top100BlocketMap.get(trimQ3);

			/*Svars parametrar ifrån blocket*/
			LongHolder lholder = new LongHolder();
			StringHolder sholder = new StringHolder();

			LOG.debug("Executing blocket search command with searchquery: "
					+ query);

			if (categoryIndex != null) {
				port.search(query, Integer.parseInt(categoryIndex),"", lholder,
						sholder);
			}

			String nads = Long.toString(lholder.value);
			if ((nads != null) && (!nads.equalsIgnoreCase("0"))) {

				result.addField("searchquery", query);
				result.addField("numberofads", nads);
				String blocketBackURL = URLEncoder.encode(sholder.value,
						"iso-8859-1");
				result.addField("blocketbackurl", blocketBackURL);

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
