package org.jax.bam2af.analysis;

import htsjdk.samtools.SAMRecordIterator;
import org.jax.bam2af.bam.SingleEndBamReader;
import org.jax.bam2af.bed.BedParser;
import org.jax.bam2af.bed.BedParserTest;
import org.jax.bam2af.bed.GenomicInterval;
import org.jax.bam2af.exception.Bam2AfException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class IntervalAlleleCounterTest {

    private static SingleEndBamReader seBamReader;
    private static List<GenomicInterval> gilist;


    @BeforeClass
    public static void setup() throws Bam2AfException {
        ClassLoader classLoader = BedParserTest.class.getClassLoader();
        String bamFile = classLoader.getResource("bam/sorted_toy_aln.bam").getFile();
        String bedFile = classLoader.getResource("bam/toy_intervals.bed").getFile();
        seBamReader=new SingleEndBamReader(bamFile);

        BedParser parser=new BedParser(bedFile);
        gilist= parser.parse();

    }

    @Test
    public void testit(){
        assertTrue(true);

        for (GenomicInterval gi : gilist) {
            SAMRecordIterator iter=seBamReader.getRecordsInInterval(gi.getChromosome(),
                    gi.getFromPosition(),
                    gi.getToPosition());
            IntervalAlleleCounter iacounter = new IntervalAlleleCounter(iter,
                    gi.getChromosome(),
                    gi.getFromPosition(),
                    gi.getToPosition());

            }


    }


}
