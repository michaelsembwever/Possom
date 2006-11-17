package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.InfrastructureException;

import java.util.Map;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import nu.prisjakt.www.wsdl.PrisjaktLocator;
import nu.prisjakt.www.wsdl.PrisjaktPortType;
import nu.prisjakt.www.wsdl.Resultat;
import nu.prisjakt.www.wsdl.Produkt;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * This command implements the integration with prisjakt.
 */
public class PrisjaktSearchCommand extends AbstractSearchCommand {

    private static final String SOAP_ENDPOINT = "http://www.prisjakt.nu/sesam/soap.php";

    private static final Logger LOG = Logger.getLogger(OverturePPCSearchCommand.class);


    /**
     * Creates a new instance of this class.
     *
     * @param cxt The context to execute in.
     * @param parameters The search parameters.
     */
    public PrisjaktSearchCommand(final Context cxt, final Map<String, Object> parameters) {
        super(cxt, parameters);
    }

    /** {@inheritDoc} */
    public SearchResult execute() {
        final SearchResult result = new BasicSearchResult(this);

        final PrisjaktLocator service = new PrisjaktLocator();

        LOG.debug("Executing");

        try {

            final PrisjaktPortType port = service.getPrisjaktPort(new java.net.URL(SOAP_ENDPOINT));

            final Resultat prisjaktResult= port.getData(getTransformedQuery());

            Produkt[] products = prisjaktResult.getProdukter();

            result.setHitCount(products.length);

            LOG.debug("Number of results " + result.getHitCount());

            for (final Produkt product : products) {
                final SearchResultItem item = new BasicSearchResultItem();
                item.addField("productName", product.getProduktnamn());

                result.addResult(item);
            }

            return result;
        } catch (ServiceException e) {
            throw new InfrastructureException(e);
        } catch (MalformedURLException e) {
            throw new InfrastructureException(e);
        } catch (RemoteException e) {
            throw new InfrastructureException(e);
        }
    }
}
