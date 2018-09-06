package org.jax.bam2af.bed;

import htsjdk.samtools.util.Interval;
import org.jax.bam2af.exception.Bam2AfException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BedParserTest {

    private static BedParser parser;
    private static List<Interval> gilist;

    @BeforeClass
    public static void setup() throws Bam2AfException {
        ClassLoader classLoader = BedParserTest.class.getClassLoader();
        String refgene = classLoader.getResource("bed/smallBed.bed").getFile();
        parser=new BedParser(refgene);
        gilist= parser.parse();
    }

    /** our test file smallBed.bed has ten items. */
    @Test
    public void testParseItems() {
        int expectedItemCount=10;
        assertEquals(expectedItemCount,gilist.size());
    }

}
