/*
 * Copyright (2005) Schibsted Søk AS
 * 
 */
package no.schibstedsok.front.searchportal.servlet;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import no.geodata.maputil.CoordHelper;
import no.geodata.maputil.MapPoint;
import no.geodata.maputil.MapEnvelope;


//webservices
import no.geodata.arcweb.AuthenticationLocator;
import no.geodata.arcweb.AuthenticationSoap;
import no.geodata.prod.webservices.arcweb.Envelope;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import no.geodata.prod.webservices.arcweb.MapImageInfo;
import no.geodata.prod.webservices.arcweb.MapImageLocator;
import no.geodata.prod.webservices.arcweb.MapImageOptions;
import no.geodata.prod.webservices.arcweb.MapImageSize;
import no.geodata.prod.webservices.arcweb.MapImageSoap;


/**
 *
 * @author hanst
 * @version
 *
 */
public class MapServlet extends HttpServlet {
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
    final static String datasource = "GEODATA.N50";
    final static String imgFormat = "png8";

    int zoomnivaa = 2;//default zoomnivaa, brukes når ikke annet er angitt

    CoordHelper coordHelper = new CoordHelper();
    String token;
    long tokenTimeStamp = 0;
    private static final long TOKEN_REFRESH_INTERVAL = 10 * 60 * 1000;


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

    private String getUrl(String token, MapEnvelope me) throws RemoteException, ServiceException{
         String URL = new String();
         Envelope envelope = new Envelope();
         envelope.setMinX(me.getMinX());
         envelope.setMinY(me.getMinY());
         envelope.setMaxX(me.getMaxX());
         envelope.setMaxY(me.getMaxY());

         MapImageSize size = new MapImageSize();
         size.setWidth(coordHelper.getImgWidth());
         size.setHeight(coordHelper.getImgHeigth());

         MapImageOptions mapOptions = new MapImageOptions();
         mapOptions.setDataSource(datasource);
         mapOptions.setMapImageFormat(imgFormat);
         mapOptions.setMapImageSize(size);

         MapImageLocator mapimageLocator = new MapImageLocator();
         MapImageSoap mapImage = mapimageLocator.getMapImageSoap();
         MapImageInfo result = mapImage.getMap(envelope, mapOptions, token);
         URL = result.getMapURL();
         return URL;
    }

    /**
     * Metoden parser requesten fra kartklienten. Den kan motta to ulike typer sett med parametrer for å generere kart.
     * Enten i form av fire hjørnekoordinater, eller iform av et punkt samt målestokk.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String sUrl = new String();
        String token = new String();
        boolean retriveMapError = false;
        MapEnvelope me = new MapEnvelope();
        boolean envelope = false;
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
            //kun sendt koordinater inn i requesten. Envelope må beregnes.
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
            sUrl = getUrl(token, me);
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


