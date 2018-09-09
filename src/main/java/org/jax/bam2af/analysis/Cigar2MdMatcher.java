package org.jax.bam2af.analysis;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A convenience class that takes an MD string and a CIGAR object and
 * matches them. The class is intended to test the approach and may not be
 * efficient enough for final use -- TODO check me
 */
public class Cigar2MdMatcher {

    /** A regular expression to break down blocks of the MD field */
    private final static String MDfieldRegEx = "([A-Z]|\\d+)";
    /** Pattern used for the MD field. */
    private final static Pattern MDfieldPattern =  Pattern.compile(MDfieldRegEx);


    public static BaseCall[] getPositions(Cigar cigar, String MD) {
        List<CigarElement> elementList = cigar.getCigarElements();
        int readlen = cigar.getReadLength();
        BaseCall [] positions=new BaseCall[readlen];
        int currentPos=0;
        Matcher m = MDfieldPattern.matcher(MD);
        if (m.matches()) {
            int c= m.groupCount();
            for (int i=0;i<c;i++) {
                String group = m.group(i);
                if (isMatch(group)) {
                    int matchLen = Integer.parseInt(group);
                    for (int j=currentPos,k=0;k<matchLen;j++,k++) {
                        positions[j]=BaseCall.REFBASE;
                    }
                }
            }
        }
        return positions;
    }



    private static boolean isMatch(String MdPart) {
        int len=MdPart.length();
        for (int i=0;i<len;i++) {
            if (! Character.isDigit(MdPart.charAt(i)) ){
                return false;
            }
        }
        return true;
    }


    public Cigar2MdMatcher(Cigar cigar, String MD) {

    }

}
