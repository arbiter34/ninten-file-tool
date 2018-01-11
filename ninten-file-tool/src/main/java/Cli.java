import com.arbiter34.byml.BymlFile;
import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.prod.ProdFile;
import com.arbiter34.yaz0.Yaz0Decoder;
import com.arbiter34.yaz0.Yaz0Encoder;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Scanner;

public class Cli {
    private final Options options;

    public Cli() {
        options = new Options();

        options.addOption("h", "help", false, "Display help.");
        options.addOption("e", "encode", false, "Encode file");
        options.addOption("d", "decode", false, "Decode file");
        options.addOption("i", "input", true, "Input file");
        options.addOption("o", "output", true, "Output file");
        options.addOption("c", "compress", false, "Compress after encoding");

        final OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(false);
        optionGroup.addOption(Option.builder("b")
                                    .longOpt("byml")
                                    .desc("BYML File Format (smubin|baniminfo|sbyml|others?)")
                                    .required(false)
                                    .hasArg(false)
                                    .build());
        optionGroup.addOption(Option.builder("p")
                                    .longOpt("prod")
                                    .desc("PrOD File Format (sblwp|others?)")
                                    .required(false)
                                    .hasArg(false)
                                    .build());

        options.addOptionGroup(optionGroup);

    }

    public void parse(final String[] args) {

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
            }

            final boolean compress = cmd.hasOption("compress");
            final String input = Optional.ofNullable(cmd.getOptionValue("input")).orElseThrow(RuntimeException::new);
            final String output = Optional.ofNullable(cmd.getOptionValue("output")).orElseThrow(RuntimeException::new);
            final String format = cmd.hasOption("byml") ? "b" : "p";

            if (cmd.hasOption("encode")) {
                if (!(cmd.hasOption("byml") || cmd.hasOption("prod"))) {
                    System.out.println("Must specify file type for encoding.");
                    help();
                }
                encode(input, output, format, compress);
            } else if (cmd.hasOption("decode")) {
                decode(input, output, format);
            } else {
                help();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void encode(final String input, String output, final String format, final boolean compress) throws IOException {
        final String json = new Scanner(new File(input)).useDelimiter("\\Z").next();
        switch (format) {
            case "b":
                final BymlFile jsonBymlFile = BymlFile.fromJson(json);
                jsonBymlFile.write(output);
                break;
            case "p":
                final ProdFile jsonProdfile = ProdFile.fromJson(json);
                jsonProdfile.write(output);
                break;
            default:
                help();
                break;
        }
        if (compress) {
            try (final BinaryAccessFile decompressed = new BinaryAccessFile(output, "r")) {
                String compressedOutput =  output.replaceAll("^(.*)\\.(.*)$", "$1.s$2");
                if (output.equals(compressedOutput)) {
                    compressedOutput = "Compressed" + compressedOutput;
                }
                Yaz0Encoder.encode(decompressed, compressedOutput);
            }
            new File(output).delete();
        }
    }

    private void decode(final String input, String output, final String format) throws IOException {
        BinaryAccessFile decompressed = null;
        try (final BinaryAccessFile file = new BinaryAccessFile(input, "r")) {
            long magicBytes = file.readUnsignedInt();
            if (magicBytes == Yaz0Decoder.MAGIC_BYTES) {
                file.seek(0);
                decompressed = Yaz0Decoder.decode(file);
                magicBytes = decompressed.readUnsignedInt();
                output = output.replaceAll("^(.*)\\.s(.*)$", "$1.$2");
            }
            decompressed.seek(0);
            String parsedJson;
            if ((int)(magicBytes >>> 16) == BymlFile.MAGIC_BYTES) {
                final BymlFile bymlFile = BymlFile.parse(Optional.ofNullable(decompressed).orElse(file));
                parsedJson = bymlFile.toJson();
                try (final PrintWriter out = new PrintWriter(output)) {
                    out.write(parsedJson);
                }
            } else if (magicBytes == ProdFile.MAGIC_BYTES) {
                final ProdFile prodFile = ProdFile.parse(Optional.ofNullable(decompressed).orElse(file));
                parsedJson = prodFile.toJson();
                try (final PrintWriter out = new PrintWriter(output)) {
                    out.write(parsedJson);
                }
            } else {
                System.out.println("Unable to detect file format for decoding.");
                help();
            }
        } finally {
            if (decompressed != null) {
                decompressed.close();
                new File(decompressed.getPath()).delete();
            }
        }
    }

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Main", options);
        System.exit(0);
    }
}