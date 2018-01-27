package ca.redtoad.compression;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import com.google.common.io.CountingInputStream;
import com.google.common.io.CountingOutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import static org.apache.commons.cli.Option.builder;

public class Main {

    private final static Options options = new Options()
        .addOption(builder("a").longOpt("algorithm").hasArg().argName("algorithm").desc("algorithm").build())
        .addOption(builder("h").longOpt("help").desc("print this help").build());

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        if (line.getArgs().length != 1 || line.hasOption('h')) {
            usage();
            System.exit(1);
        }

        String algorithm = "huffman";
        if (line.hasOption('a')) {
            algorithm = line.getOptionValue('a');
        }

        Path inputFilename = Paths.get(line.getArgs()[0]);
        boolean decompressing = inputFilename.toString().endsWith("." + algorithm);
        Path outputFilename;
        if (decompressing) {
            outputFilename = Paths.get(inputFilename.toString().replaceFirst(Pattern.quote("." + algorithm) + "$", ""));
        } else {
            outputFilename = Paths.get(inputFilename.toString() + "." + algorithm);
        }

        if (Files.exists(outputFilename)) {
            Path backupFilename = Paths.get(outputFilename.toString() + ".bak");
            Files.deleteIfExists(backupFilename);
            Files.move(outputFilename, backupFilename);
        }

        Codec codec = CodecService.getInstance().getCodec(algorithm);

        InputStream input = new BufferedInputStream(new FileInputStream(inputFilename.toFile()));
        OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFilename.toFile()));

        CountingInputStream inCounter = new CountingInputStream(input);
        CountingOutputStream outCounter = new CountingOutputStream(output);

        long start = System.currentTimeMillis();

        if (decompressing) {
            codec.decode(inCounter, outCounter);
        } else {
            codec.encode(inCounter, outCounter);
        }
        outCounter.flush();

        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("%s %d to %d (%g%%) in %g seconds\n",
                          decompressing ? "decompressing" : "compressing",
                          inCounter.getCount(),
                          outCounter.getCount(),
                          ((100.0 * outCounter.getCount()) / inCounter.getCount()),
                          0.001 * elapsed);

    }

    private static void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("compress", "compress and decompress files", options, "", true);
    }

}

