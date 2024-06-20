package ca.redtoad.compression.rle;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Map;

import ca.redtoad.compression.Codec;

public class RleCodec implements Codec {

    private static final int RUN_CODE = 255;

    public String getName() {
        return "rle";
    }

    public void setParameters(Map<String, String> parameters) {
    }

    public void encode(InputStream input, OutputStream out) throws IOException {
        Stats stats = new Stats();
        PushbackInputStream pinput = new PushbackInputStream(input);
        int i;
        while ((i = pinput.read()) != -1) {
            int count = 1;
            int j;
            while ((j = pinput.read()) == i) {
                count++;
            }
            if (count <= 3 && i != RUN_CODE) {
                if (count > 1) {
                    stats.shortRuns += 1;
                }
                for (int k = 0; k < count; k++) {
                    out.write(i);
                }
            } else {
                stats.runCount += 1;
                if (count > stats.longestRun) {
                    stats.longestRun = count;
                }
                out.write(RUN_CODE);
                out.write(count);
                out.write(i);
            }
            if (j == -1) {
                break;
            } else {
                pinput.unread(j);
            }
        }
        System.out.println("runCount = " + stats.runCount);
        System.out.println("longestRun = " + stats.longestRun);
        System.out.println("shortRuns = " + stats.shortRuns);
    }

    public void decode(InputStream input, OutputStream out) throws IOException {
        int i;
        while ((i = input.read()) != -1) {
            if (i == RUN_CODE) {
                int count = input.read();
                int value = input.read();
                for (int j = 0; j < count; j++) {
                    out.write(value);
                }
            } else {
                out.write(i);
            }
        }
    }

    private static class Stats {
        int runCount;
        int longestRun;
        int shortRuns;
    }
}
