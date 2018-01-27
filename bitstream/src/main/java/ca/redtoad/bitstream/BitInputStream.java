package ca.redtoad.bitstream;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable {

    private final InputStream input;
    private int partialBits;
    private int partial;

    public BitInputStream(InputStream input) {
        this.input = input;
    }

    public int read(int bits) throws IOException {
        while (partialBits < bits) {
            int i = input.read();
            if (i == -1) {
                return -1;
            }
            partial = partial & ((1 << partialBits) - 1) | (i << partialBits);
            partialBits += 8;
        }
        int result = partial & ((1 << bits) - 1);
        partial = partial >> bits;
        partialBits -= bits;
        return result;
    }

    public void close() throws IOException {
        input.close();
    }
}
