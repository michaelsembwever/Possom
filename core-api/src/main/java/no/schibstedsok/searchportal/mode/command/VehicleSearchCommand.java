// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.VehicleSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.axis.client.Stub;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import se.blocket.www2.search.SearchLocator;
import se.blocket.www2.search.SearchPortType;

public final class VehicleSearchCommand extends AbstractWebServiceSearchCommand {

    private static final Logger LOG = Logger.getLogger(VehicleSearchCommand.class);

    private static final String ERR_FAILED_BLOCKET_SEARCH = "Failed Vehicle search command";

    private static final String ERR_FAILED_ENCODE_BLOCKET = "Failed to encode Vehicle search query";

    private final static String BYTBIL_BASE_URL = "http://www.bytbil.com/sellers/sesam/sesam.cgi?uid=4A35c0023F&brand=%s&model=%s";

    private final static int BLOCKET_CAR_CATEGORY = 4;

    private final static int BLOCKET_CAR_ACCESSORIES_CATEGORY = 5;

    private final static String BLOCKET_PRIVATE_ADS_TYPE = "p";

    private final static String BLOCKET_ALL_ADS_TYPE = "";

    public VehicleSearchCommand(final Context cxt) {

        super(cxt);
    }

    @Override
    public SearchResult execute() {

        VehicleSearchConfiguration vsc = (VehicleSearchConfiguration) context.getSearchConfiguration();

        final SearchLocator service = new SearchLocator();
        final SearchResult result = new BasicSearchResult(this);

        try {

            final SearchPortType port = service.getsearchPort(new java.net.URL(service.getsearchPortAddress()));

            String query = getTransformedQuery();
            /* Result from bytbil.com and blocket.se */
            LongHolder lholder = new LongHolder();
            StringHolder sholder = new StringHolder();
            Map carsMap = vsc.getCarsMap();

            LOG.debug("Executing vehicle search command with searchquery: " + query);

            // Blocket cars
            String nads = "0";
            if (carsMap.get(query.toLowerCase()) != null) {
                port.search(query, BLOCKET_CAR_CATEGORY, BLOCKET_PRIVATE_ADS_TYPE, lholder, sholder);
                ((Stub) port).setTimeout(1000);
                nads = Long.toString(lholder.value);
                if ((nads != null) && (!nads.equals("0"))) {
                    result.addField("searchquery", query);
                    String blocketBackURL = URLEncoder.encode(sholder.value, "iso-8859-1");
                    result.addField("blocketbackurl", blocketBackURL);
                }
            }
            result.addField("numberofads", nads);

            // bytbil cars
            String bbCount = "0";
            if (carsMap.get(query.toLowerCase()) != null) {
                SAXBuilder sb = new SAXBuilder();
                String[] queryParts = ((String) carsMap.get(query.toLowerCase())).split(";");
                Document doc = null;
                if (queryParts.length == 2 && queryParts[1].length() > 0) {
                    doc = sb.build(String.format(BYTBIL_BASE_URL, URLEncoder.encode(queryParts[0], "iso-8859-1"),
                            URLEncoder.encode(queryParts[1], "iso-8859-1")));
                    bbCount = doc.getRootElement().getAttribute("count").getValue();
                }
                if (!bbCount.equals("0")) {
                    result.addField("searchquery", query);
                    String bbUrl = doc.getRootElement().getChildTextTrim("searchquery");
                    result.addField("bytbilbackurl", URLEncoder.encode(bbUrl, "iso-8859-1"));
                }
            }
            result.addField("bbnumberofads", bbCount);

            boolean blocketContainsAccessories = vsc.getAccessriesSet().contains(query.toLowerCase());
            String accNads = "0";

            LOG.debug("Executing car accessories search command with searchquery: " + query);

            // Blocket car accessories
            if (blocketContainsAccessories) {
                LongHolder numberOfAccessories = new LongHolder();
                StringHolder accUrl = new StringHolder();
                port.search(query, BLOCKET_CAR_ACCESSORIES_CATEGORY, BLOCKET_ALL_ADS_TYPE, numberOfAccessories, accUrl);
                ((Stub) port).setTimeout(1000);
                accNads = Long.toString(numberOfAccessories.value);
                if ((accNads != null) && (!accNads.equalsIgnoreCase("0"))) {
                    result.addField("searchquery", query);
                    String accblocketBackURL = URLEncoder.encode(accUrl.value, "iso-8859-1");
                    result.addField("accblocketbackurl", accblocketBackURL);
                }
            }
            result.addField("accnumberofads", accNads);

            final int totalnumberofads = Integer.parseInt(nads) + Integer.parseInt(bbCount) + Integer.parseInt(accNads);
            result.addField("totalnumberofads", Integer.toString(totalnumberofads));
            result.setHitCount(totalnumberofads);
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
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public enum VehicleType {
        CAR(4), MC(6), MOPED(7);

        private final int blocketCategory;

        VehicleType(int blocketCategory) {
            this.blocketCategory = blocketCategory;
        }

        public int blocketCategory() {
            return blocketCategory;
        }

    };

    public class VehicleProperties {
        VehicleProperties() {
        }

        private VehicleType type = null;

        private String brand = "";

        private String model = "";
    }
    /**
     * getBrandFromModel(model) getBrand(str) getModel(str) getType(str)
     */

}
