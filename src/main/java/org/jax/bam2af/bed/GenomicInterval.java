package org.jax.bam2af.bed;


/**
 * A convenience class designed to capture the information in a BED3 file
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class GenomicInterval implements Comparable<GenomicInterval> {

    private final String chromosome;
    private final int fromPosition;
    private final int toPosition;

    /**
     *
     * @param chr chromosome
     * @param f from (start) position, one-based inclusive
     * @param t to (end) position, one-based inclusive
     */
    public GenomicInterval(String chr, int f, int t){
        this.chromosome=chr;
        this.fromPosition=f;
        this.toPosition=t;
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getFromPosition() {
        return fromPosition;
    }

    public int getToPosition() {
        return toPosition;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((chromosome == null) ? 0 : chromosome.hashCode());
        result = prime * result + fromPosition;
        result = prime * result + toPosition;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GenomicInterval)) {
            return false;
        }
        GenomicInterval gi = (GenomicInterval) o;
        return this.chromosome.equals(gi.chromosome) &&
                this.fromPosition==gi.fromPosition &&
                this.toPosition==gi.toPosition;

    }

    /**
     * Sorting order is based on the chromosome (lexicographical) and then on fromPosition and then on toPosition
     * @param gi the genomic interval to be compared to
     * @return true if this GenomicInterval should come first
     */
    @Override
    public int compareTo(GenomicInterval gi) {
        return this.chromosome.equals(gi.chromosome) ?
                this.toPosition==gi.toPosition ?
                        this.fromPosition-gi.fromPosition :
                        this.toPosition - gi.toPosition :
                this.chromosome.compareTo(gi.chromosome);
    }

}
