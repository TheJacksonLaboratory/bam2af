package org.jax.bam2af.bam;

import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import org.jax.bam2af.exception.Bam2AfException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SingleEndBamReaderTest {

    static private SingleEndBamReader seBamReader;

    static private int my_chromosome_index;

    @BeforeClass
    public static void setup() throws Bam2AfException {
        ClassLoader classLoader = SingleEndBamReader.class.getClassLoader();
        String bamFile = classLoader.getResource("bam/sorted_toy_aln.bam").getFile();
        String bedFile = classLoader.getResource("bam/toy_intervals.bed").getFile();
        seBamReader=new SingleEndBamReader(bamFile);
        my_chromosome_index = 0;
    }

    /**
     * <pre>samtools view sorted_toy_aln.bam | wc -l
     * 175</pre>
     */
    @Test
    public void testNumberOfReads() {
        int n=0;
        SAMRecordIterator it = seBamReader.getGlobalIterator();
        while (it.hasNext()) {
            SAMRecord rec = it.next();
            n++;
        }
        assertEquals(175,n);
    }

    /** 5 reads each start on position 1 and 2 on my_chromosome. */
    @Test
    public void testNumberOfReadsThatStartOnPos1or2() {
        QueryInterval gi = new QueryInterval(my_chromosome_index,1,2);
        int n=0;
        SAMRecordIterator it = seBamReader.getRecordsInInterval(gi);
        while (it.hasNext()) {
            SAMRecord rec = it.next();
            n++;
        }
        assertEquals(10,n);
    }

    /** 5 reads each start on position 1 and 2 and 3 on my_chromosome.
     * Therefore, position 2/3 have 15 overlapping readings
     * */
    @Test
    public void testNumberOfReadsThatStartOnPos2or3() {
        QueryInterval gi = new QueryInterval(my_chromosome_index,2,3);
        int n=0;
        SAMRecordIterator it = seBamReader.getRecordsInInterval(gi);
        while (it.hasNext()) {
            SAMRecord rec = it.next();
            n++;
        }
        assertEquals(15,n);
    }

    /** By inspection of sorted_toy_aln.bam in IGV, position 44 has 130 reads.
     * In addition to the 15 reads from the interval (2,3), we expect 145
     */
    @Test
    public void testMultiIntervalSearch() {
        QueryInterval q1 = new QueryInterval(my_chromosome_index,2,3);
        QueryInterval q2 = new QueryInterval(my_chromosome_index,44,44);
        QueryInterval[] qints = new QueryInterval[2];
        qints[0]=q1;
        qints[1]=q2;
        SAMRecordIterator it = seBamReader.getRecordsInIntervals(qints);
        int n=0;
        while (it.hasNext()) {
            SAMRecord rec = it.next();
            n++;
        }
        assertEquals(145,n);

    }



}
