// Copyright (2007) Schibsted Sök AB
package no.schibstedsok.searchportal.mode.command;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.xml.rpc.ServiceException;

import no.schibstedsok.searchportal.InfrastructureException;
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
import org.apache.log4j.Logger;

/**
 * This command implements the integration with prisjakt.
 */
public final class PrisjaktSearchCommand extends AbstractWebServiceSearchCommand {

    private static final Logger LOG = Logger.getLogger(PrisjaktSearchCommand.class);

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
     * @param cxt
     *            The context to execute in.
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
            port = service.getPrisjaktPort(new java.net.URL(service.getPrisjaktPortAddress()));
            ((Stub) port).setTimeout(1000);
            result.addField("urlEncodedsearchquery", URLEncoder.encode(query, "iso-8859-1").replaceAll("\\+", "%20"));
            result.addField("searchquery", query);
            if (engine.evaluateQuery(TokenPredicate.PRISJAKT_PRODUCTS, getQuery())) {
                final Resultat prisjaktResult = port.getProduktViaNamn(query);
                final Produkt[] products = prisjaktResult.getProdukter();
                result.addField("searchtype", "productsearch");
                result.setHitCount(products != null ? products.length : 0);
                productConverter(result, products);
                // } else if
                // (engine.evaluateQuery(TokenPredicate.PRISJAKT_CATEGORIES_AND_MANUFACTURERS,
                // getQuery())) {
                // final IndataTillverkare_Kategorinamn itk = new
                // IndataTillverkare_Kategorinamn();
                // final Resultat prisjaktResult =
                // port.getTillverkare_KategoriViaNamn(itk);
                // final Tillverkare_Kategori[] tillverkareKategorier =
                // prisjaktResult.getTillverkare_Kategorier();
                // result.addField("searchtype", "manufacturer_categorysearch");
                // result.setHitCount(tillverkareKategorier != null ?
                // tillverkareKategorier.length : 0);
            } else if (engine.evaluateQuery(TokenPredicate.PRISJAKT_CATEGORIES, getQuery())) {
                final Resultat prisjaktResult = port.getKategoriViaNamn(query);
                final Kategori[] kategorier = prisjaktResult.getKategorier();
                result.addField("searchtype", "categorysearch");
                result.setHitCount(kategorier != null ? kategorier.length : 0);
                categoryConverter(result, kategorier);
            } else if (engine.evaluateQuery(TokenPredicate.PRISJAKT_MANUFACTURERS, getQuery())) {
                final Resultat prisjaktResult = port.getTillverkareViaNamn(query);
                final Tillverkare[] manufacturers = prisjaktResult.getTillverkare_plur();
                result.addField("searchtype", "manufacturersearch");
                result.setHitCount(manufacturers != null ? manufacturers.length : 0);
                manufacturerConverter(result, manufacturers);
            } else if (engine.evaluateQuery(TokenPredicate.PRISJAKT_SHOPS, getQuery())) {
                final Resultat prisjaktResult = port.getButikViaNamn(query);
                final Butik[] butiker = prisjaktResult.getButiker();
                result.addField("searchtype", "storesearch");
                result.setHitCount(butiker != null ? butiker.length : 0);
                storeConverter(result, butiker);
            }
        } catch (MalformedURLException e) {
            LOG.error("MalformedURLException:",e);
            throw new InfrastructureException(e);
        } catch (ServiceException e) {
            LOG.error("InfrastructureException:",e);
            throw new InfrastructureException(e);
        } catch (RemoteException e) {
            LOG.error("RemoteException:",e);
            throw new InfrastructureException(e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("UnsupportedEncodingException:",e);
            throw new InfrastructureException(e);
        }
        return result;
    }

    private void productConverter(final ResultList<ResultItem> result, final Produkt[] products) {
        if (products != null) {
            for (final Produkt product : products) {
                ResultItem item = new BasicResultItem();
                item = item.addField("productName", product.getProduktnamn());
                final int lPris = product.getLagstaPris();
                final String cf = nummerAvskFormat(lPris);
                item = item.addField("lowestPrice", cf);
                item = item.addField("numberofStores", Integer.toString(product.getAntalButiker()));
                /* &st=4 shows small images in prisjakt enrichment */
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
        if (stores != null) {
            for (final Butik store : stores) {
                ResultItem item = new BasicResultItem();
                item = item.addField("numberofProducts", Integer.toString(store.getAntalProdukter()));
                item = item.addField("storeURL", store.getUrl());
                item = item.addField("storeName", store.getButiksnamn());
                result.addResult(item);
            }
        }
    }

    private void manufacturerConverter(ResultList<ResultItem> result, Tillverkare[] manufacturers) {
        if (manufacturers != null) {
            for (final Tillverkare manufacturer : manufacturers) {
                ResultItem item = new BasicResultList();
                item = item.addField("numberofProducts", Integer.toString(manufacturer.getAntalProdukter()));
                item = item.addField("manufacturerURL", manufacturer.getUrl());
                item = item.addField("manufacturerName", manufacturer.getTillverkarnamn());
                result.addResult(item);
            }
        }
    }

    /**
     * Formatera nummer med tusenavskiljare utan decimaler.
     */
    private static String nummerAvskFormat(final Number nummer) {

        return nummer == null ? "" : NUMMER_AVSK_FORMAT.format(nummer.doubleValue());
    }

}
