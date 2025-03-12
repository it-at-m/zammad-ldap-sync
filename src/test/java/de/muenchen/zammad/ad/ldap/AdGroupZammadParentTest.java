package de.muenchen.zammad.ad.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.muenchen.zammad.ad.ldap.mediator.ActiveDirectoryShadeTreeInclusion;


public class AdGroupZammadParentTest {

    @Test
    void findLowestDistinguishedNameCommonDenominatorTest() {

        assertEquals("B,A", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("C,B,A", "B,B,A")));
        assertEquals("B,A", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("C,B,A", "B,B,A", "D,C,B,A")));
        assertEquals("B,B,A", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("B,B,A", "B,B,A", "D,B,B,A")));
        assertEquals("A,B,A", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("F,C,A,B,A", "A,B,A", "D,A,B,A")));
        assertEquals("F,A\\, B", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("F,C,A,F,A\\, B", "A,F,A\\, B", "D,B,F,A\\, B")));
        assertEquals("Z", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("F,C,A,F,A\\, B,Z", "A,F,A\\, B,Z", "D,B,F,A\\, B,Z", "Z")));
        assertEquals("D,A\\,C,F,A\\, B,Z", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("G,F,D,A\\,C,F,A\\, B,Z","G,D,A\\,C,F,A\\, B,Z","F,G,D,A\\,C,F,A\\, B,Z","D,A\\,C,F,A\\, B,Z","D,A\\,C,F,A\\, B,Z")));
        assertEquals("ABC", ActiveDirectoryShadeTreeInclusion.findParentDistinguishedNameDenominator(List.of("F,A\\, B,ABC","C,F,A\\, B,ABC","C,F,A\\, B,ABC","F,A\\, B,ABC","D,A\\,C,F,A\\, B,ABC", "ABC")));

    }

}
