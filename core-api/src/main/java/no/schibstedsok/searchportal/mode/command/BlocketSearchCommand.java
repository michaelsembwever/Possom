package no.schibstedsok.searchportal.mode.command;

import java.net.MalformedURLException;
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
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;

public class BlocketSearchCommand extends AbstractWebServiceSearchCommand {

	private static final Logger LOG = Logger
			.getLogger(BlocketSearchCommand.class);

	private static final String ERR_FAILED_BLOCKET_SEARCH = "Failed Prisjakt search command";

	public BlocketSearchCommand(final Context cxt,
			final Map<String, Object> parameters) {
		super(cxt, parameters);
	}

	@Override
	public SearchResult execute() {

		final TokenEvaluationEngine engine = context.getTokenEvaluationEngine();

		final boolean isBlocket = engine.evaluateQuery(TokenPredicate.BLOCKET,
				context.getQuery());

		BlocketSearchConfiguration bsc = (BlocketSearchConfiguration) context
				.getSearchConfiguration();

		Map m = bsc.getBlocketMap();

		final SearchLocator service = new SearchLocator();

		final SearchResult result = new BasicSearchResult(this);
		if (isBlocket) {
			try {
				final SearchPortType port = service
						.getsearchPort(new java.net.URL(service
								.getsearchPortAddress()));
				String query = getTransformedQuery();

				LongHolder lholder = new LongHolder();
				StringHolder sholder = new StringHolder();
				String trimQ = StringUtils.deleteWhitespace(query);
				String categoryIndex = (String) m.get(trimQ);
				if (categoryIndex != null) {
					port.search(query, Integer.parseInt(categoryIndex),
							lholder, sholder);
				}

				String nads = Long.toString(lholder.value);
				result.addField("searchquery", query);
				result.addField("numberofads", nads);
				result.addField("blocketbackurl", sholder.value);

				result.setHitCount(1);

			} catch (ServiceException se) {
				LOG.error(ERR_FAILED_BLOCKET_SEARCH, se);
				throw new InfrastructureException(se);
			} catch (MalformedURLException murle) {
				LOG.error(ERR_FAILED_BLOCKET_SEARCH, murle);
				throw new InfrastructureException(murle);
			} catch (RemoteException re) {
				LOG.error(ERR_FAILED_BLOCKET_SEARCH, re);
				throw new InfrastructureException(re);
			}

		}

		return result;

	}

}
