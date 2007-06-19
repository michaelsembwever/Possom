// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.xml.rpc.ServiceException;

import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.result.BasicResultItem;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import nu.prisjakt.www.wsdl.Butik;
import nu.prisjakt.www.wsdl.Kategori;
import nu.prisjakt.www.wsdl.PrisjaktLocator;
import nu.prisjakt.www.wsdl.PrisjaktPortType;
import nu.prisjakt.www.wsdl.Produkt;
import nu.prisjakt.www.wsdl.Resultat;
import nu.prisjakt.www.wsdl.Tillverkare;

import org.apache.axis.client.Stub;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This command implements the integration with prisjakt.
 */
public final class PrisjaktSearchCommand extends AbstractWebServiceSearchCommand {

    private static final Logger LOG = Logger.getLogger(PrisjaktSearchCommand.class);

    private static final String ERR_FAILED_PRISJAKT_SEARCH = "Failed Prisjakt search command";
    private static final String ERR_FAILED_ENCODE_PRISJAKT = "Failed to encode Prisjakt search query";

    /**
     * Format för siffror utan decimaler med tusenavskiljare.
     */
    private static DecimalFormat NUMMER_AVSK_FORMAT;

    static {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        NUMMER_AVSK_FORMAT = new DecimalFormat("#,##0", symbols);
        NUMMER_AVSK_FORMAT.setGroupingSize(3);
    }

    /**
     * Creates a new instance of this class.
     *
     * @param cxt The context to execute in.
     */
    public PrisjaktSearchCommand(final Context cxt) {

        super(cxt);
    }

    /** {@inheritDoc} */
    @Override
    public ResultList<? extends ResultItem> execute() {

        final TokenEvaluationEngine engine = getEngine();

        final ResultList<ResultItem> result = new BasicResultList<ResultItem>();
        final PrisjaktLocator service = new PrisjaktLocator();
        PrisjaktPortType port;
        final String query = getTransformedQuery();
        result.setHitCount(0);
        try {
            LOG.debug("PJ: pre1");
            port = service.getPrisjaktPort(new java.net.URL(service.getPrisjaktPortAddress()));
            LOG.debug("PJ: pre2");
            ((Stub)port).setTimeout(1000);
            LOG.debug("PJ: pre3");
            result.addField("urlEncodedsearchquery", URLEncoder.encode(query, "iso-8859-1").replaceAll("\\+", "%20"));
            result.addField("searchquery", query);
            if (engine.evaluateQuery(TokenPredicate.PRISJAKT_PRODUCTS, getQuery())) {
                LOG.debug("PJ: prod");
                final Resultat prisjaktResult = port.getProduktViaNamn(query);
                final Produkt[] products = prisjaktResult.getProdukter();
                result.addField("searchtype", "productsearch");
                result.setHitCount(products.length);
                productConverter(result, products);
            } else if (engine.evaluateQuery(TokenPredicate.PRISJAKT_CATEGORIES, getQuery())) {
                LOG.debug("PJ: cat");
                final Resultat prisjaktResult = port.getKategoriViaNamn(query);
                final Kategori[] kategorier = prisjaktResult.getKategorier();
                result.addField("searchtype", "categorysearch");
                result.setHitCount(kategorier != null ? kategorier.length : 0);
                categoryConverter(result, kategorier);
            } else if (engine.evaluateQuery(TokenPredicate.PRISJAKT_MANUFACTURERS, getQuery())) {
                LOG.debug("PJ: manufacturers");
                final Resultat prisjaktResult = port.getTillverkareViaNamn(query);
                final Tillverkare[] manufacturers = prisjaktResult.getTillverkare_plur();
                result.addField("searchtype", "manufacturersearch");
                result.setHitCount(manufacturers.length);
                manufacturerConverter(result, manufacturers);
            } else if (engine.evaluateQuery(TokenPredicate.PRISJAKT_SHOPS, getQuery())) {
                LOG.debug("PJ: shop");
                final Resultat prisjaktResult = port.getButikViaNamn(query);
                final Butik[] butiker = prisjaktResult.getButiker();
                result.addField("searchtype", "storesearch");
                result.setHitCount(butiker.length);
                storeConverter(result, butiker);
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    private void productConverter(final ResultList<ResultItem> result, final Produkt[] products) {

        for (final Produkt product : products) {

            ResultItem item = new BasicResultItem();
            item = item.addField("productName", product.getProduktnamn());
            final int lPris = product.getLagstaPris();
            final String cf = nummerAvskFormat(lPris);
            item = item.addField("lowestPrice", cf);
            item = item.addField("numberofStores", Integer.toString(product
                    .getAntalButiker()));
            /* &st=4 tillägget gör att vi får en liten bild ifrån prisjakt*/
            if (product.getBild().equalsIgnoreCase("")) {
                item = item.addField("productPicture", "");
            } else {
                item = item.addField("productPicture", product.getBild() + "&st=4");
            }
            item = item.addField("productURL", product.getUrl());
            item = item.addField("categoryName", product.getKategorinamn());
            result.addResult(item);
        }
    }

    private void categoryConverter(final ResultList<ResultItem> result, final Kategori[] kategorier) {
        if (kategorier != null) {
            for (final Kategori kategori : kategorier) {
                ResultItem item = new BasicResultItem();
                item = item.addField("numberofProducts", Integer.toString(kategori.getAntalProdukter()));
                item = item.addField("categoryURL", kategori.getUrl());
                item = item.addField("categoryName", kategori.getKategorinamn());
                result.addResult(item);
            }
        }
    }

    private void storeConverter(ResultList<ResultItem> result, Butik[] stores) {

        for (final Butik store : stores) {

            ResultItem item = new BasicResultItem();
            item = item.addField("numberofProducts", Integer.toString(store.getAntalProdukter()));
            item = item.addField("storeURL", store.getUrl());
            item = item.addField("storeName", store.getButiksnamn());
            result.addResult(item);
        }
    }

    private void manufacturerConverter(ResultList<ResultItem> result, Tillverkare[] manufacturers) {

        for (final Tillverkare manufacturer : manufacturers) {

            ResultItem item = new BasicResultList();
            item = item.addField("numberofProducts", Integer.toString(manufacturer.getAntalProdukter()));
            item = item.addField("manufacturerURL", manufacturer.getUrl());
            item = item.addField("manufacturerName", manufacturer.getTillverkarnamn());
            result.addResult(item);
        }
    }

    /**
     * Formatera nummer med tusenavskiljare utan decimaler.
     */
    private static String nummerAvskFormat(final Number nummer) {

        return nummer == null ? "" :NUMMER_AVSK_FORMAT.format(nummer.doubleValue());
    }

    /** Isn't there a java class for number formatting? */

    /**
     * Formatera med tusenavskiljare och lägger till decimaldel med komma-tecken
     * om decimaldel förekommer. Tar bort alla inledande och avslutande blanksteg
     * samt ev inledande '+'-tecken Ex: +5555510.35->5 555 510,35 +5555510,35->5
     * 555 510,35 +10002000 ->10 002 000
     *
     * @param numStr <code>String</code> Strängen som ska formateras
     * @return En sträng som har formaterats enligt ovan.
     */
    private static String nummerAvskFormat(String numStr) {

        // XXX please use a StringBuilder here instead

        if (numStr == null || numStr.equals("")) {
            return "";
        }
        // Bort med alla inledande och avslutande blanksteg samt
        // ev inledande '+'-tecken
        numStr = numStr.trim();
        numStr = removeBlanks(numStr);
        numStr = numStr.indexOf("+") > 0 ? numStr.substring(1) : numStr;
        // Spara undan ev. decimaldelen och se till att den alltid bestÃ¥r av ett
        // komma-tecken om den fÃ¶rekommer.
        final String decPart = numStr.indexOf(',') > 0
                ? numStr.substring(numStr.indexOf(','))
                : numStr.indexOf('.') > 0 ? numStr.substring(numStr.indexOf('.')).replaceFirst(".", ",") : "";
        // Substringa ut heltalsdelen
        final double d;
        try {
            d = Double.valueOf(numStr.substring(0, numStr.length() - decPart.length()));
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            return "";
        }

        return NUMMER_AVSK_FORMAT.format(d).concat(decPart);
    }

    /**
     * Tar bort blanktecken ur strängen
     */
    private static String removeBlanks(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return StringUtils.deleteSpaces(str);
    }

    /**
     * Kontrollerar om strängargumentet är null eller tom sträng.
     */
    private static boolean isEmpty(String s) {
        return StringUtils.isEmpty(s);
    }
}
