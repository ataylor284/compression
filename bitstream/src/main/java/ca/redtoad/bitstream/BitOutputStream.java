package ca.redtoad.bitstream;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements AutoCloseable {

    private final OutputStream out;
    private int partialBits;
    private int partial;

    public BitOutputStream(OutputStream out) {
        this.out = out;
    }

    public void write(int bits, int nbits) throws IOException {
        partial = (partial & ((1 << partialBits) - 1)) | bits << partialBits;

        int totalBits = partialBits + nbits;
        while (totalBits >= 8) {
            out.write((byte) partial);
            totalBits -= 8;
            partial = partial >> 8;
        }
        partialBits = totalBits;
    }

    public void flush() throws IOException {
        if (partialBits > 0) {
            out.write((byte) partial & ((1 << partialBits) - 1));
            partialBits = 0;
        }
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}
