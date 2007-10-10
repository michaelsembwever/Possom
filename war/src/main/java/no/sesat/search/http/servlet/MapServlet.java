/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package no.sesat.search.http.servlet;

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
import org.apache.axis.client.Stub;
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

    private static final String DEFAULT_IMAGE = "http://www.sesam.no/images/pix.gif";
    
    /** 
     * Timeout for SOAP call to authentication and mapimage service.
     * The authentication token has a lifetime of 10 minutes. So if Geodata
     * goes down getUrl() will get called with a 20 sec timeout. 
     * After the initial 10 minutest the authenticate() method will be executed with a much shorter timeout. If
     * authenticate() throws an exception getUrl() is not executed.
     * This should prevent the thread count from going through the roof and at the same time give the mapimage service
     * enough time to generate the map images.
     */
    private static final int AUTHENTICATION_TIMEOUT = 2000;
    private static final int MAPIMAGE_TIMEOUT = 20000;
    
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
                    Stub s = (Stub) auth;
                    s.setTimeout(AUTHENTICATION_TIMEOUT);
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
         Stub s = (Stub) mapImage;
         s.setTimeout(MAPIMAGE_TIMEOUT);
         MapImageInfo result = mapImage.getMap(envelope, mapOptions, token);
         URL = result.getMapURL();
         LOG.info(URL);
         return URL;
    }

    /**
     * Metoden parser requesten fra kartklienten. Den kan motta to ulike typer sett med parametrer for å generere kart.
     * Enten i form av fire hjørnekoordinater, eller iform av et punkt samt målestokk.
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
            //kun sendt koordinater inn i requesten. Envelope m\uFFFD beregnes.
            String sCoords = request.getParameter("coords");
            Vector vMapPoints = coordHelper.parseCoordString(sCoords);
            //Sjekk om det finnes noen koordinater, hvis ikke kan resten glemmes.
            if(!vMapPoints.isEmpty()){

                String action = request.getParameter("action");

                String temp = request.getParameter("zoom");
                if (temp != null)
                    zoomnivaa = Integer.parseInt(temp);
                if(action.compareToIgnoreCase("viewone") == 0){//enkelt bedriftstreff. Må beregne envelope utifra ett pkt, zoomlevel og bildestørrelse
                    MapPoint mp = (MapPoint) vMapPoints.get(0);
                    me = coordHelper.makeEnvelope(mp.getX(), mp.getY(), zoomnivaa);
                }
                else if(action.compareToIgnoreCase("viewmany") == 0){//enkelt bedriftstreff. Må beregne envelope utifra ett pkt, zoomlevel og bildestørrelse
                    me = coordHelper.makeEnvelope(vMapPoints);
                }
            }
        }
        try {
            token = authenticate();
            sUrl = getUrl(token, me, coordHelper);
        }
        catch (ServiceException serviceExcep) {
            LOG.error(serviceExcep);
            retriveMapError = true;
        }
        catch (RemoteException remoteExcep){
            LOG.error(remoteExcep);
            retriveMapError = true;
        }

        //redirecter resultat URL tilbake.
        if (!retriveMapError){
            response.sendRedirect(sUrl);
        } else {
            response.sendRedirect(DEFAULT_IMAGE);
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


