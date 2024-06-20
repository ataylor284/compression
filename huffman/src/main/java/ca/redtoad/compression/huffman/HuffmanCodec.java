package ca.redtoad.compression.huffman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import ca.redtoad.compression.Codec;
import ca.redtoad.bitstream.BitInputStream;
import ca.redtoad.bitstream.BitOutputStream;

public class HuffmanCodec implements Codec {
    private final static int BLOCK_SIZE = 10000;

    public String getName() {
        return "huffman";
    }

    public void setParameters(Map<String, String> parameters) {
    }

    /**
     * Encode the full input stream to the output stream.  The output
     * will consist of a set blocks.  Each block begins with an
     * encoded tree, followed by the number of bits used to encode the
     * block, and finally the encoded data.
     */
    public void encode(InputStream input, OutputStream out) throws IOException {
        byte[] block = new byte[BLOCK_SIZE];
        int blocksize = input.read(block);
        while (blocksize != -1) {
            HuffmanNode huff = genHuffmanTree(block, blocksize);
            BitOutputStream bitout = new BitOutputStream(out);
            huff.serialize(bitout);
            bitout.flush();
            encodeBlock(block, blocksize, huff, out);
            blocksize = input.read(block);
        }
    }

    /**
     * Decode the full input stream to the output stream.  The
     * expected input is a set of blocks containing an encoded tree
     * followed by the data encoded by that tree.
     */
    public void decode(InputStream input, OutputStream out) throws IOException {
        while (true) {
            HuffmanNode huff = HuffmanNode.deserialize(new BitInputStream(input));
            if (huff == null) {
                break;
            }
            decodeBlock(input, huff, out);
        }
    }

    /**
     * Generates a Huffman tree for the block.  Generates byte
     * frequencies and a tree that balances the frequencies as evenly
     * as possible.
     */
    private HuffmanNode genHuffmanTree(byte[] block, int blocksize) throws IOException {
        int[] freqs = genFrequencyCounts(block, blocksize);
        Queue<HuffmanNode> q = new PriorityQueue<HuffmanNode>();
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] > 0) {
                q.add(new HuffmanNode(freqs[i], Collections.singleton(Byte.valueOf((byte)i)), null, null));
            }
        }
        if (q.isEmpty()) {
            return new HuffmanNode(0, Collections.singleton(Byte.valueOf((byte)0)), null, null);
        } else {
            // merge the two nodes with lowest frequency until only one node is left
            while (q.size() > 1) {
                q.add(HuffmanNode.merge(q.poll(), q.poll()));
            }
            return q.poll();
        }
    }

    private int[] genFrequencyCounts(byte[] block, int blocksize) throws IOException {
        int[] freqs = new int[256];
        for (int i = 0; i < blocksize; i++) {
            freqs[Byte.toUnsignedInt(block[i])] += 1;
        }
        return freqs;
    }

    /**
     * Encode a single block with a given tree.
     */
    private void encodeBlock(byte[] block, int blocksize, HuffmanNode tree, OutputStream out) throws IOException {
        // need a tree of min height 2 to encode, so fake out an additional level if tree is a leaf
        if (tree.isLeaf()) {
            tree = HuffmanNode.merge(tree, tree);
        }

        int bits = 0;
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        BitOutputStream bitout = new BitOutputStream(encoded);
        for (int i = 0; i < blocksize; i++) {
            bits += encodeByte(block[i], tree, bitout);
        }
        bitout.flush();

        DataOutputStream dataout = new DataOutputStream(out);
        dataout.writeInt(bits);
        dataout.flush();
        out.write(encoded.toByteArray());
    }

    /**
     * Encode a byte with a given tree.
     * @return the number of bits written.
     */
    private int encodeByte(byte b, HuffmanNode tree, BitOutputStream out) throws IOException {
        HuffmanNode t = tree;
        int bits = 0;
        while (!t.isLeaf()) {
            if (t.getLeft().contains(b)) {
                out.write(0, 1);
                t = t.getLeft();
            } else {
                out.write(1, 1);
                t = t.getRight();
            }
            bits += 1;
        }
        return bits;
    }

    /**
     * Decode a single block with a given tree.
     */
    private void decodeBlock(InputStream input, HuffmanNode tree, OutputStream out) throws IOException {
        // need a tree of min height 2 to decode, so fake out an additional level if tree is a leaf
        if (tree.isLeaf()) {
            tree = HuffmanNode.merge(tree, tree);
        }

        DataInputStream datainput = new DataInputStream(input);
        int nbits = datainput.readInt();
        byte[] encoded = new byte[(nbits+7)/8];
        input.read(encoded);

        int offset = 0;
        BitInputStream bitin = new BitInputStream(new ByteArrayInputStream(encoded));
        while (offset < nbits) {
            HuffmanNode t = tree;
            while (!t.isLeaf()) {
                if (bitin.read(1) == 1) {
                    t = t.getRight();
                } else {
                    t = t.getLeft();
                }
                offset += 1;
            }
            out.write(t.value());
        }
    }
}
