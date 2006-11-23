package no.schibstedsok.searchportal.view.velocity;

import junit.framework.TestCase;

public class RolesDirectiveTest extends TestCase {
    /** The new format with shareholders */
    String input1 = "#roller0#Daglig leder/adm.dir#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styrets leder#sep#Torstein Thorsen (f 1962)#id#3757147#sepnl#Styremedlem#sep#Kjetil Krogvig Bergstrand (f 1970)#id#7492519#sepnl#Styremedlem#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styremedlem#sep#Per Anders Waaler (f 1966)#id#2908670#sepnl#Revisor#sep#Bdo Noraudit Oslo Da#id##sepnl##aksjonaer0##bold#Navn#sep#Eierandel i %#sep#Antall aksjer#sepnl#TRULS BERG#id##sep#18,8#sep#37601#sepnl#PER ANDERS WAALER#id##sep#18,8#sep#37601#sepnl#KJETIL KROGVIG BERGSTRAND#id##sep#18,8#sep#37601#sepnl#BJØRN OLAV KÅSIN#id##sep#18,8#sep#37601#sepnl#TORSTEIN THORSEN#id##sep#18,8#sep#37601#sepnl#KNUT ERIK TERJESEN#id##sep#3#sep#6000#sepnl#RUNE HILLEREN#id##sep#3#sep#6000#sepnl#";        
    /** The new format without shareholders */
    String input2 = "#roller0#Daglig leder/adm.dir#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styrets leder#sep#Torstein Thorsen (f 1962)#id#3757147#sepnl#Styremedlem#sep#Kjetil Krogvig Bergstrand (f 1970)#id#7492519#sepnl#Styremedlem#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styremedlem#sep#Per Anders Waaler (f 1966)#id#2908670#sepnl#Revisor#sep#Bdo Noraudit Oslo Da#id##sepnl#";
    /** The old format without shareholders */
    String input3 = "Daglig leder/adm.dir#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styrets leder#sep#Torstein Thorsen (f 1962)#id#3757147#sepnl#Styremedlem#sep#Kjetil Krogvig Bergstrand (f 1970)#id#7492519#sepnl#Styremedlem#sep#Bjørn Olav Kåsin (f 1964)#id#566302#sepnl#Styremedlem#sep#Per Anders Waaler (f 1966)#id#2908670#sepnl#Revisor#sep#Bdo Noraudit Oslo Da#id##sepnl#";
    
    public void testPreprocessRoles () {
        
        RolesDirective rd = new RolesDirective();
        
        String split1 = rd.preprocessInput(input1);
        String split2 = rd.preprocessInput(input2);
        String split3 = rd.preprocessInput(input3);
        
        // Make a very raw split. Seperate roles and shareholders
        assertEquals(split1, split2);
        assertEquals(split1, split3);
    }
}
