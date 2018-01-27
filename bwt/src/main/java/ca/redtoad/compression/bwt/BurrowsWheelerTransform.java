package ca.redtoad.compression.bwt;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import ca.redtoad.compression.Codec;

public class BurrowsWheelerTransform implements Codec {

    private final static int BLOCKSIZE = 20000;

    public String getName() {
        return "bwt";
    }

    public void setParameters(Map<String, String> parameters) {
    }

    public void encode(InputStream input, OutputStream out) throws IOException {
        byte[] block = new byte[BLOCKSIZE + 1];
        int len;
        while ((len = input.read(block, 0, BLOCKSIZE)) != -1) {
            transformBlock(block, len, out);
        }
    }

    public void decode(InputStream input, OutputStream out) throws IOException {
        byte[] buffer = new byte[BLOCKSIZE + 1];
        int len;
        while ((len = input.read()) != -1) {
            int eofChar = input.read();
            int nread = input.read(buffer, 0, len + 1);

            int bwtSize = len + 1;
            byte[][] rotations = new byte[bwtSize][];
            for (int i = 0; i < bwtSize; i++) {
                rotations[i] = new byte[bwtSize];
            }
            for (int i = 0; i < bwtSize; i++) {
                for (int j = 0; j < bwtSize; j++) {
                    byte v = buffer[j];
                    rotations[j][bwtSize - i - 1] = v;
                }
                Arrays.sort(rotations, this::compareByteArrays);
            }
            for (int i = 0; i < bwtSize; i++) {
                if (rotations[i][bwtSize - 1] == eofChar) {
                    out.write(rotations[i], 0, len);
                }
            }
        }
    }

    private void transformBlock(byte[] block, int len, OutputStream out) throws IOException {
        int eofChar = findFreeChar(block, len);
        if (eofChar >= 256) {
            System.out.println("splitting");
            int split = len / 2;
            byte[] lower = new byte[split + 1];
            System.arraycopy(block, 0, lower, 0, split);
            transformBlock(lower, split, out);
            byte[] upper = new byte[split + 1];
            System.arraycopy(block, split, upper, 0, len - split);
            transformBlock(upper, len - split, out);
            return;
        }

        out.write(len);
        out.write(eofChar);

        block[len] = (byte) eofChar;
        int bwtSize = len + 1;
        byte[][] rotations = new byte[bwtSize][];
        for (int i = 0; i < bwtSize; i++) {
            rotations[i] = new byte[bwtSize];
            System.arraycopy(block, bwtSize - i, rotations[i], 0, i);
            System.arraycopy(block, 0, rotations[i], i, bwtSize - i);
        }
        Arrays.sort(rotations, this::compareByteArrays);
        for (int i = 0; i < bwtSize; i++) {
            out.write(rotations[i][len]);
        }
    }

    private int findFreeChar(byte[] block, int len) {
        BitSet usedChars = new BitSet();
        for (int i = 0; i < len; i++) {
            usedChars.set(block[i] & 0xFF);
        }
        return usedChars.nextClearBit(0);
    }

    private int compareByteArrays(byte[] left, byte[] right) {
        for (int i = 0; i < left.length && i < right.length; i++) {
            int a = (left[i] & 0xff);
            int b = (right[i] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }
}
