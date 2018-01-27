package ca.redtoad.compression.mtf;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import ca.redtoad.compression.Codec;

public class MoveToFrontTransform implements Codec {

    public String getName() {
        return "mtf";
    }

    public void setParameters(Map<String, String> parameters) {
    }

    public void encode(InputStream input, OutputStream out) throws IOException {
        byte[] list = genByteValues();
        int ch;
        while ((ch = input.read()) != -1) {
            for (int i = 0; i < 256; i++) {
                if (list[i] == (byte) ch) {
                    out.write(i);
                    if (i > 0) {
                        byte tmp = list[i];
                        System.arraycopy(list, 0, list, 1, i);
                        list[0] = tmp;
                    }
                    break;
                }
            }
        }
    }

    public void decode(InputStream input, OutputStream out) throws IOException {
        byte[] list = genByteValues();
        int ch;
        while ((ch = input.read()) != -1) {
            out.write(list[ch]);
            if (ch > 0) {
                byte tmp = list[ch];
                System.arraycopy(list, 0, list, 1, ch);
                list[0] = tmp;
            }
        }
    }

    private byte[] genByteValues() {
        byte[] vals = new byte[256];
        for (int i = 0; i < 256; i++) {
            vals[i] = (byte) i;
        }
        return vals;
    }
}
