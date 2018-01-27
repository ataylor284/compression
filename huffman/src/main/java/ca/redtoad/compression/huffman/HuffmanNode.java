package ca.redtoad.compression.huffman;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ca.redtoad.bitstream.BitInputStream;
import ca.redtoad.bitstream.BitOutputStream;

public class HuffmanNode implements Comparable<HuffmanNode> {
    private final int freq;
    private final Set<Byte> values;
    private final HuffmanNode left;
    private final HuffmanNode right;

    public HuffmanNode(int freq, Set<Byte> values, HuffmanNode left, HuffmanNode right) {
        this.freq = freq;
        this.values = values;
        this.left = left;
        this.right = right;
    }

    public boolean contains(byte value) {
        return values.contains(value);
    }

    public byte value() {
        return values.iterator().next();
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public static HuffmanNode merge(HuffmanNode l, HuffmanNode r) {
        Set<Byte> newValues = new HashSet<Byte>();
        newValues.addAll(l.values);
        newValues.addAll(r.values);
        return new HuffmanNode(l.freq + r.freq, newValues, l, r);
    }

    public void serialize(BitOutputStream out) throws IOException {
        if (isLeaf()) {
            out.write(0, 1);
            out.write(values.iterator().next(), 8);
        } else {
            out.write(1, 1);
            left.serialize(out);
            right.serialize(out);
        }
    }

    public static HuffmanNode deserialize(BitInputStream input) throws IOException {
        int type = input.read(1);
        if (type == 1) {
            HuffmanNode left = deserialize(input);
            HuffmanNode right = deserialize(input);
            if (left == null || right == null) {
                throw new IllegalStateException("bad huffman tree");
            }
            return merge(left, right);
        } else if (type == 0) {
            return new HuffmanNode(1, Set.of(Byte.valueOf((byte)input.read(8))), null, null);
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return freq - other.freq;
    }

    public void print(int indent) {
        if (isLeaf()) {
            for (int i = 0; i < indent; i++) {
                System.out.print(" ");
            }
            System.out.println("L: values = " + values + ", freq = " + freq);
        } else {
            for (int i = 0; i < indent; i++) {
                System.out.print(" ");
            }
            System.out.println("N: values = " + values + ", freq = " + freq);
            left.print(indent + 4);
            right.print(indent + 4);
        }
    }
}
