package org.jax.bam2af.bed;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GenomicIntervalTest {

    @Test
    public void testConstructor() {
        String chr="chr42";
        int from =7;
        int to = 13;
        GenomicInterval gi = new GenomicInterval(chr,from,to);
        assertEquals(gi.getChromosome(),"chr42");
    }

    @Test public void testSort1() {
        String chr="chr1";
        int from =7;
        int to = 13;
        GenomicInterval gi1 = new GenomicInterval(chr,from,to);
        String chr2="chr2";
        GenomicInterval gi2 = new GenomicInterval(chr2,from,to);
        List<GenomicInterval> gilist = new ArrayList<>();
        gilist.add(gi1);
        gilist.add(gi2);
        Collections.sort(gilist);
        assertEquals(2,gilist.size());
        assertEquals(gi1,gilist.get(0));
        assertEquals(gi2,gilist.get(1));
    }

    /** Test that lower from position is sorted first. */
    @Test public void testSort2() {
        String chr="chr1";
        int from =7;
        int to = 13;
        GenomicInterval gi1 = new GenomicInterval(chr,from,to);
        int from2=6;
        GenomicInterval gi2 = new GenomicInterval(chr,from2,to);
        List<GenomicInterval> gilist = new ArrayList<>();
        gilist.add(gi1);
        gilist.add(gi2);
        Collections.sort(gilist);
        assertEquals(2,gilist.size());
        assertEquals(gi2,gilist.get(0));
        assertEquals(gi1,gilist.get(1));
    }
}
