/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.servlet;

import no.geodata.arcweb.AuthenticationLocator;
import no.geodata.arcweb.AuthenticationSoap;
import no.geodata.maputil.CoordHelper;
import no.geodata.maputil.MapEnvelope;
import no.geodata.maputil.MapPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;
import no.geodata.prod.webservices.arcweb.Envelope;
import no.geodata.prod.webservices.arcweb.MapImageInfo;
import no.geodata.prod.webservices.arcweb.MapImageLocator;
import no.geodata.prod.webservices.arcweb.MapImageOptions;
import no.geodata.prod.webservices.arcweb.MapImageSize;
import no.geodata.prod.webservices.arcweb.MapImageSoap;
import org.apache.log4j.Logger;


/**
 *
 * @author hanst
 * @version
 *
 */
public final class MapServlet extends HttpServlet {
    /** The serialVersionUID */
    private static final long serialVersionUID = -5879777378093939926L;

    //globale konstanter. Hvor bør disse settes?? xml fil.
    /*final static long zoomLevel1 = 10000; //kartskala ved zoom til ett punkt
    final static long zoomLevel2 = 20000;
    final static long zoomLevel3 = 50000;
    final static long zoomLevel4 = 150000;
    final static long zoomLevel5 = 500000;
     */
    //final static double envFactor = 1.2; //faktor for å lage rom rundt envelope
    //final static int imgWidth = 350;//bildestørrelse i pixler, bredde
    //final static int imgHeigth = 400;//bildestørrelse i pixler, høyde
    /** TODO comment me. **/
    final static String datasource = "GEODATA.N50";
    /** TODO comment me. **/
    final static String imgFormat = "png8";

    /** TODO comment me. **/
    int zoomnivaa = 2;//default zoomnivaa, brukes når ikke annet er angitt

    /** TODO comment me. **/
    String token;
    /** TODO comment me. **/
    long tokenTimeStamp = 0;
    private static final long TOKEN_REFRESH_INTERVAL = 10 * 60 * 1000;

    private static Logger LOG = Logger.getLogger(SearchServlet.class);

    private String authenticate() throws RemoteException, ServiceException{

        long current = System.currentTimeMillis();

        if (current - tokenTimeStamp > TOKEN_REFRESH_INTERVAL) {
            synchronized(this) {
                if (current - tokenTimeStamp > TOKEN_REFRESH_INTERVAL) {
                    AuthenticationLocator authLocator = new AuthenticationLocator();
                    AuthenticationSoap auth = authLocator.getAuthenticationSoap();
                    token = auth.getToken("schi", "zofren");
                    tokenTimeStamp = System.currentTimeMillis();
                }
            }
        }

        return token;
    }

    private String getUrl(String token, MapEnvelope me, CoordHelper coordHelper) throws RemoteException, ServiceException{
         String URL = new String();
         Envelope envelope = new Envelope();
         envelope.setMinX(me.getMinX());
         envelope.setMinY(me.getMinY());
         envelope.setMaxX(me.getMaxX());
         envelope.setMaxY(me.getMaxY());

         MapImageSize size = new MapImageSize();
         size.setWidth(coordHelper.getImgWidth());
         size.setHeight(coordHelper.getImgHeight());

         MapImageOptions mapOptions = new MapImageOptions();
         mapOptions.setDataSource(datasource);
         mapOptions.setMapImageFormat(imgFormat);
         mapOptions.setMapImageSize(size);

         MapImageLocator mapimageLocator = new MapImageLocator();
         MapImageSoap mapImage = mapimageLocator.getMapImageSoap();
         MapImageInfo result = mapImage.getMap(envelope, mapOptions, token);
         URL = result.getMapURL();
         LOG.info(URL);
         return URL;
    }

    /**
     * Metoden parser requesten fra kartklienten. Den kan motta to ulike typer sett med parametrer for � generere kart.
     * Enten i form av fire hj�rnekoordinater, eller iform av et punkt samt m�lestokk.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");

        CoordHelper coordHelper = new CoordHelper();

        String sUrl = new String();
        String token = new String();
        boolean retriveMapError = false;
        MapEnvelope me = new MapEnvelope();

        String imgWidth = request.getParameter("width");
        String imgHeight = request.getParameter("height");

        if (imgWidth != null && imgHeight != null){
            coordHelper.setImgWidth(Integer.parseInt(imgWidth));
            coordHelper.setImgHeight(Integer.parseInt(imgHeight));
        }

        String maxX = request.getParameter("maxX");
        if (maxX != null){
            //koordinater for envelope er sendt i requesten
            me.setMaxX(Double.parseDouble(maxX));
            String minX = request.getParameter("minX");
            me.setMinX(Double.parseDouble(minX));
            String maxY = request.getParameter("maxY");
            me.setMaxY(Double.parseDouble(maxY));
            String minY = request.getParameter("minY");
            me.setMinY(Double.parseDouble(minY));
        }
        else{
            //kun sendt koordinater inn i requesten. Envelope m� beregnes.
            String sCoords = request.getParameter("coords");
            Vector vMapPoints = coordHelper.parseCoordString(sCoords);
            //Sjekk om det finnes noen koordinater, hvis ikke kan resten glemmes.
            if(!vMapPoints.isEmpty()){

                String action = request.getParameter("action");

                String temp = request.getParameter("zoom");
                if (temp != null)
                    zoomnivaa = Integer.parseInt(temp);
                if(action.compareToIgnoreCase("viewone") == 0){//enkelt bedriftstreff. M� beregne envelope utifra ett pkt, zoomlevel og bildest�rrelse
                    MapPoint mp = (MapPoint) vMapPoints.get(0);
                    me = coordHelper.makeEnvelope(mp.getX(), mp.getY(), zoomnivaa);
                }
                else if(action.compareToIgnoreCase("viewmany") == 0){//enkelt bedriftstreff. M� beregne envelope utifra ett pkt, zoomlevel og bildest�rrelse
                    me = coordHelper.makeEnvelope(vMapPoints);
                }
            }
        }
        try {
            token = authenticate();
            sUrl = getUrl(token, me, coordHelper);
        }
        catch (ServiceException serviceExcep) {
        }
        catch (RemoteException remoteExcep){

        }

        //redirecter resultat URL tilbake.
        if (!retriveMapError){
            response.sendRedirect(sUrl);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);

    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }

    /*
     * Parser koordinatstreng til ett arra
     */
    protected void makeCoordArray(String coords){
        //Array
    }
    // </editor-fold>
}


