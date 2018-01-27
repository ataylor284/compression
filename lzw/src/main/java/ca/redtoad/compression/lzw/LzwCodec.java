package ca.redtoad.compression.lzw;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.redtoad.compression.Codec;
import ca.redtoad.bitstream.BitInputStream;
import ca.redtoad.bitstream.BitOutputStream;

public class LzwCodec implements Codec {

    private static final List<Byte> EOF = Collections.emptyList();
    private static final int EOF_CODE = 256;
    private static final List<Byte> BUMP_WORDSIZE = Collections.singletonList(null);
    private static final int BUMP_WORDSIZE_CODE = 257;

    public String getName() {
        return "lzw";
    }

    public void setParameters(Map<String, String> parameters) {
    }

    public void encode(InputStream input, OutputStream out) throws IOException {
        Map<List<Byte>, Integer> dict = initEncodingDict();

        BitOutputStream bitout = new BitOutputStream(out);
        List<Byte> current = new ArrayList<>();

        int wordsize = 9;
        int first = input.read();
        if (first == -1) {
            bitout.write(EOF_CODE, wordsize);
            bitout.flush();
            return;
        }
        current.add(Byte.valueOf((byte)first));

        int i;
        while ((i = input.read()) != -1) {
            current.add(Byte.valueOf((byte)i));
            if (!dict.containsKey(current)) {
                dict.put(Collections.unmodifiableList(new ArrayList<>(current)), dict.size());
                if (dict.size() >= (1 << wordsize)) {
                    bitout.write(BUMP_WORDSIZE_CODE, wordsize);
                    wordsize += 1;
                }
                current.remove(current.size() - 1);
                bitout.write(dict.get(current), wordsize);
                current.clear();
                current.add(Byte.valueOf((byte)i));
            }
        }
        bitout.write(dict.get(current), wordsize);
        bitout.write(EOF_CODE, wordsize);
        bitout.flush();
    }

    public void decode(InputStream input, OutputStream out) throws IOException {
        Map<Integer, List<Byte>> dict = initDecodingDict();

        BitInputStream bitin = new BitInputStream(input);
        List<Byte> current = new ArrayList<>();

        int wordsize = 9;
        int i;
        while ((i = bitin.read(wordsize)) != -1) {
            if (i == EOF_CODE) {
                break;
            }
            if (i == BUMP_WORDSIZE_CODE) {
                wordsize += 1;
                continue;
            }
            List<Byte> decoded = dict.get(i);
            if (decoded != null) {
                for (Byte b : decoded) {
                    out.write(b);
                }
                if (!current.isEmpty()) {
                    current.add(decoded.get(0));
                    dict.put(dict.size(), Collections.unmodifiableList(new ArrayList<>(current)));
                    current.clear();
                }
                current.addAll(decoded);
            } else {
                current.add(current.get(0));
                dict.put(i, Collections.unmodifiableList(new ArrayList<>(current)));
                for (Byte b : current) {
                    out.write(b);
                }
            }
        }
    }

    private Map<List<Byte>, Integer> initEncodingDict() {
        Map<List<Byte>, Integer> dict = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dict.put(Arrays.asList(new Byte[]{Byte.valueOf((byte)i)}), i);
        }
        dict.put(EOF, EOF_CODE);
        dict.put(BUMP_WORDSIZE, BUMP_WORDSIZE_CODE);
        return dict;
    }

    private Map<Integer, List<Byte>> initDecodingDict() {
        Map<Integer, List<Byte>> dict = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dict.put(i, Arrays.asList(new Byte[]{Byte.valueOf((byte)i)}));
        }
        dict.put(EOF_CODE, EOF);
        dict.put(BUMP_WORDSIZE_CODE, BUMP_WORDSIZE);
        return dict;
    }
}
