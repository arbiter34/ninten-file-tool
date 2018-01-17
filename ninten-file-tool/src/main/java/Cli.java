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
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public void parse(final String[] args) throws IOException {

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
            }

            final boolean compress = cmd.hasOption("compress");
            final String input = Optional.ofNullable(cmd.getOptionValue("input")).orElseThrow(RuntimeException::new);

            BinaryAccessFile decompressed = null;
            try (final BinaryAccessFile file = new BinaryAccessFile(input, "r")) {
                long magicBytes = file.readUnsignedInt();
                file.seek(0);

                if (magicBytes == Yaz0Decoder.MAGIC_BYTES) {
                    file.seek(0);
                    decompressed = Yaz0Decoder.decode(file);
                    magicBytes = decompressed.readUnsignedInt();
                    decompressed.seek(0);
                }
                String parsedJson;
                if ((int)(magicBytes >>> 16) == BymlFile.MAGIC_BYTES) {
                    final BymlFile bymlFile = BymlFile.parse(Optional.ofNullable(decompressed).orElse(file));
                    parsedJson = bymlFile.toJson();
                    try (final PrintWriter out = new PrintWriter(input + ".json")) {
                        out.write(parsedJson);
                    }
                } else if (magicBytes == ProdFile.MAGIC_BYTES) {
                    final ProdFile prodFile = ProdFile.parse(Optional.ofNullable(decompressed).orElse(file));
                    parsedJson = prodFile.toJson();
                    try (final PrintWriter out = new PrintWriter(input + ".json")) {
                        out.write(parsedJson);
                    }
                } else if ((int)(magicBytes >>> 24) == 0x7B){
                    // Jackson always start's objects with 7B (open curly) and 0D (\r)
                    final byte[] jsonBytes;
                    try (BinaryAccessFile jsonFile = new BinaryAccessFile(input, "r")) {
                        jsonBytes = new byte[(int)jsonFile.length()];
                        file.read(jsonBytes);
                    }
                    String output = input.replaceAll("^(.*)\\.json$", "$1");
                    if (compress) {
                        output = output + "Decompressed";
                    }

                    // Try and parse each file type
                    boolean parsed = false;
                    try {
                        BymlFile bymlFile = BymlFile.fromJson(jsonBytes);
                        bymlFile.write(output);
                        parsed = true;
                    } catch (Throwable t) {
                        // Guess it wasn't byml
                    }

                    if (!parsed) {
                        try {
                            ProdFile prodFile = ProdFile.fromJson(jsonBytes);
                            prodFile.write(output);
                            parsed = true;
                        } catch (Throwable t) {
                            t.printStackTrace();
                            // Not PrOD
                        }
                    }

                    if (parsed) {
                        if (compress) {
                            compress(output);
                        }
                        return;
                    }

                    System.out.println("Unable to detect file format for decoding.");
                    help();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                if (decompressed != null) {
                    decompressed.close();
                    new File(decompressed.getPath()).delete();
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void compress(final String output) throws IOException {
            try (final BinaryAccessFile decompressed = new BinaryAccessFile(output, "r")) {
                String compressedOutput =  output.replaceAll("^(.*)Decompressed$", "$1");
                if (output.equals(compressedOutput)) {
                    compressedOutput = "Compressed" + compressedOutput;
                }
                Yaz0Encoder.encode(decompressed, compressedOutput);
            } finally {
                new File(output).delete();
            }
    }

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Main", options);
        System.exit(0);
    }
}
