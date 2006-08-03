package no.schibstedsok.searchportal.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Hex;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;

import no.schibstedsok.searchportal.security.MD5Generator;

/**
 * 
 * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Revision$</tt>
 */
public class TradeDoubler {
	private final static String secretCode = "997";
	private final static String organization = "1064392";
	private final static String event = "46757";
	private HttpServletRequest request = null;
	
	public TradeDoubler(HttpServletRequest request) {
		this.request = request;
	}
	
	public String getChecksum(String orderNumber, String orderValue) 
			throws RuntimeException {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
		String s = new String(TradeDoubler.getSecretCode() + orderNumber + orderValue);
		digest.update(s.getBytes());
		return new String(Hex.encodeHex(digest.digest()));
	}

	public String getUUID() {
		UUIDGenerator uuidgen = UUIDGenerator.getInstance();
		UUID uuid = uuidgen.generateRandomBasedUUID();
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
		Cookie[] cookies = this.request.getCookies();
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
