package no.schibstedsok.searchportal.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;



/**
 * <b> Must be threadsafe </b>
 * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Id$</tt>
 */
public final class TradeDoubler {
    
    private final static String secretCode = "997";
    private final static String organization = "1064392";
    private final static String event = "46757";
    private final HttpServletRequest request;
    
    public TradeDoubler(HttpServletRequest request) {
        this.request = request;
    }
    
    public String getChecksum(
            final String orderNumber,
            final String orderValue) throws RuntimeException {
        
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        final String s = new String(TradeDoubler.getSecretCode() + orderNumber + orderValue);
        digest.update(s.getBytes());
        return new String(Hex.encodeHex(digest.digest()));
    }
    
    public String getUUID() {
        final UUIDGenerator uuidgen = UUIDGenerator.getInstance();
        final UUID uuid = uuidgen.generateRandomBasedUUID();
        return uuid.toString();
    }
    
    public static String getEvent() {
        return event;
    }
    
    public static String getOrganization() {
        return organization;
    }
    
    public static String getSecretCode() {
        return secretCode;
    }
    
    public String getCookieTDUID() {
        return getCookie("TRADEDOUBLER");
    }
    
    public String getCookieOrderNumber(){
        return getCookie("TRADEDOUBLER-onr");
    }
    
    public String getCookieChecksum(){
        return getCookie("TRADEDOUBLER-cs");
    }
    
    public String getCookieReportInfo(){
        return getCookie("TRADEDOUBLER-ri");
    }
    
    private String getCookie(String name){
        if (this.request == null){
            return "";
        }
        String value = "";
        final Cookie[] cookies = this.request.getCookies();
        if (cookies != null){
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(name)) {
                    if (cookies[i].getValue() != null) {
                        value = cookies[i].getValue();
                    }
                }
            }
        }
        return value;
    }
}
