package no.schibstedsok.searchportal.view.velocity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;



public class ShareHoldersDirectiveTest extends TestCase {
    
    /** The new format with shareholders */
    String input1 = "#roller0#Daglig leder/adm.dir#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styrets leder#sep#Torstein Thorsen (f 1962)#id#3757147#sepnl#Styremedlem#sep#Kjetil Krogvig Bergstrand (f 1970)#id#7492519#sepnl#Styremedlem#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styremedlem#sep#Per Anders Waaler (f 1966)#id#2908670#sepnl#Revisor#sep#Bdo Noraudit Oslo Da#id##sepnl##aksjonaer0##bold#Navn#sep#Eierandel i %#sep#Antall aksjer#sepnl#TRULS BERG#id##sep#18,8#sep#37601#sepnl#PER ANDERS WAALER#id##sep#18,8#sep#37601#sepnl#KJETIL KROGVIG BERGSTRAND#id##sep#18,8#sep#37601#sepnl#BJØRN OLAV KÅSIN#id##sep#18,8#sep#37601#sepnl#TORSTEIN THORSEN#id##sep#18,8#sep#37601#sepnl#KNUT ERIK TERJESEN#id##sep#3#sep#6000#sepnl#RUNE HILLEREN#id##sep#3#sep#6000#sepnl##newdsctiongoeshere##bold#header1#sep#header2#sep#header3#sepnl#Ola#sep#Markveien#sep#olso#sepnl";        
    /** The new format without shareholders */
    String input2 = "#roller0#Daglig leder/adm.dir#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styrets leder#sep#Torstein Thorsen (f 1962)#id#3757147#sepnl#Styremedlem#sep#Kjetil Krogvig Bergstrand (f 1970)#id#7492519#sepnl#Styremedlem#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styremedlem#sep#Per Anders Waaler (f 1966)#id#2908670#sepnl#Revisor#sep#Bdo Noraudit Oslo Da#id##sepnl#";
    /** The old format without shareholders */
    String input3 = "Daglig leder/adm.dir#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styrets leder#sep#Torstein Thorsen (f 1962)#id#3757147#sepnl#Styremedlem#sep#Kjetil Krogvig Bergstrand (f 1970)#id#7492519#sepnl#Styremedlem#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styremedlem#sep#Per Anders Waaler (f 1966)#id#2908670#sepnl#Revisor#sep#Bdo Noraudit Oslo Da#id##sepnl#";
    
    public ShareHoldersDirectiveTest(String testName) {
		super(testName);
	}
	
	/**
	 * Test the parse 3 share holders
	 */
	public void testParsing() {
        ShareHoldersDirective shd = new ShareHoldersDirective();
        List shareHolders = shd.parseShareHolders(input1);
        assertEquals(shareHolders.size(), shareHolders.size(), 6);
	}

	/**
	 * Test parse null string
	 */
	public void testParsing2() {
		ShareHoldersDirective shd = new ShareHoldersDirective();
		List shareHolders = shd.parseShareHolders(null);
		assertEquals(shareHolders.size (), 0);

	}

	/** 
	 * Test parse empty string
	 */
	public void testParstingEmpty () {
		ShareHoldersDirective shd = new ShareHoldersDirective();
		List shareHolders = shd.parseShareHolders("");
		assertEquals(shareHolders.size (), 0);
	}

    /** Parse the old format */
    public void testParseOldFormat() {
        ShareHoldersDirective shd = new ShareHoldersDirective();
        List shareHolders = shd.parseShareHolders(input3);
        assertEquals(shareHolders.size (), 0);
    }
	
	/**
	 * Try to parse crappy data
	 */
	public void testParseRandomString() {
        String ypRoles = "#aksjonaer0##sepnl#" +  
        "#bold#Navn#sep#Eierandel i %sep#Antall aksjer#" +
        "STENSENTERET AS#27035950sep100epnl#" +  
        "ARILD C. GUSTAVSEN#idsep#25#sep0" +
        "INGER A. O. GUSTAVSENsep#2550e";		
		ShareHoldersDirective shd = new ShareHoldersDirective();
		List shareHolders = shd.parseShareHolders(ypRoles);
		assertEquals(0, shareHolders.size ());
		
	}

}
