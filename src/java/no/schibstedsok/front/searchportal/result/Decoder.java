package no.schibstedsok.front.searchportal.result;

import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: SSTHKJER
 * Date: 13.jan.2006
 * Time: 11:32:54
 * To change this template use File | Settings | File Templates.
 *
 * @todo make this a generic helper class for functions needed in templates
 *
 */


public class Decoder {

    public Decoder() {}

    public String yip_decoder(String s) {
        try {
            s = java.net.URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return s;
    }

    //function for infotext, must decode before stripping string
    public String yip_decoder(String s, int length) {
        try {
            s = java.net.URLDecoder.decode(s, "UTF-8");
            if (s.length() < length) {
                length = s.length();
                s = s.substring(0, length);
            } else
                s = s.substring(0, length) + "..";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return s;
    }

    public String ypurl(String s) {

        if (!s.startsWith("http://")) {
            s = "http://" + s;
        }
        return s;
    }

    public boolean checkInfoTextLength(String s, int i) {
        try {
            s = java.net.URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        int length = s.length();
        if (length > i)
            return true;

        return false;
    }
}
