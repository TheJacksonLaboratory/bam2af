package org.jax.bam2af.bam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import htsjdk.samtools.*;
import org.apache.log4j.Logger;
import org.jax.bam2af.bed.BedParser;
import org.jax.bam2af.exception.Bam2AfException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class is a wrapper around the HTSJDK SamReader files and provides an iterator
 * to obtain SAMRecord objects either from the entire BAM file or from a certain interval.
 * This class is intended to be used with single-end (SE) BAM files, which are commonly used
 * for ChIP-seq experiments.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class SingleEndBamReader {
    private static final Logger logger = Logger.getLogger(SingleEndBamReader.class.getName());
    /** Complete path to the BAM file of interest. */
    private final String bamFilePath;
    /** Reader we will use to access {@link #bamFilePath}.*/
    private SamReader reader;
    private SAMFileHeader samFileHeader;
    private List<SAMSequenceRecord> refSeqs;

    private QueryInterval[] queryIntervals;

    private Map<String,Integer> chromName2IndexMap;

    SAMRecordIterator iterator=null;
    boolean iteratorInUse=false;


    public SingleEndBamReader(String path, File bedFile) {
        this.bamFilePath = path;
        File bamFile = new File(bamFilePath);
        if (!bamFile.exists()) {
            System.err.println(bamFilePath + " was not found. Please check path");
            logger.fatal(1);
            refSeqs=ImmutableList.of();
            reader=null;
            samFileHeader=null;
            return;
        }
        initSequenceToIndexMap();
        initQueryIntervalsFromBed(bedFile);
    }



    private void initSequenceToIndexMap() {
        initReader();
        ImmutableMap.Builder<String,Integer> builder = new ImmutableMap.Builder<>();
        for (SAMSequenceRecord rec : refSeqs) {
            String name = rec.getSequenceName();
            int index = rec.getSequenceIndex();
            builder.put(name,index);
        }
        this.chromName2IndexMap=builder.build();
    }

    private void initQueryIntervalsFromBed(File bedFile) {
        BedParser parser = new BedParser(bedFile);
        Objects.requireNonNull(chromName2IndexMap,
                "Should never happen: Bam file sequence indices not initialized");
        try {
            this.queryIntervals = parser.parse2QueryInterval(chromName2IndexMap);
        } catch (Bam2AfException e) {
            e.printStackTrace();
            // todo die gracefully?
        }
    }


    public int getIndexOf(String chrom) throws Bam2AfException {
        if (! chromName2IndexMap.containsKey(chrom)) {
            throw new Bam2AfException("Could not get index for " + chrom);
        }
        return chromName2IndexMap.get(chrom);
    }


    private void initReader() {
        this.reader = SamReaderFactory.makeDefault().open(new File(this.bamFilePath));
        refSeqs=reader.getFileHeader().getSequenceDictionary().getSequences();
        this.samFileHeader=reader.getFileHeader();
    }


    /**
     * This function resets the SAMreader and gets a global iterator to all sequences in the BAM file
     * @return
     */
    public final SAMRecordIterator getGlobalIterator() {
        initReader();
        return reader.iterator();
    }

    /**
     * Just for testing. This should be removed in the final version! TODO
     * @param qint
     * @return
     */
    public final SAMRecordIterator getRecordsInInterval(QueryInterval qint) {
        initReader();
        QueryInterval[] qi = new QueryInterval[1];
        qi[0]=qint;
        return reader.queryOverlapping(qi);
    }

    public final SAMRecordIterator getRecordsInIntervals(QueryInterval[] inteverals) {
        initReader();
        return reader.queryOverlapping(inteverals);
    }



    public final SAMRecordIterator getRecordsInIntervals() {
        initReader();
        return reader.queryOverlapping(this.queryIntervals);
    }




}
