import com.arbiter34.byml.BymlFile;
import com.arbiter34.prod.ProdFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {

    public Main() {
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            exitGracefully();
        }
        final String operation = args[0];
        final String fileType = args[1];
        switch (operation) {
            case "d":
                String parsedJson;
                switch (fileType) {
                    case "b":
                        final BymlFile bymlFile = BymlFile.parse(args[2]);
                        parsedJson = bymlFile.toJson();
                        try (final PrintWriter out = new PrintWriter(args[3])) {
                            out.write(parsedJson);
                        }
                        break;
                    case "p":
                        final ProdFile prodFile = ProdFile.parse(args[2]);
                        parsedJson = prodFile.toJson();
                        try (final PrintWriter out = new PrintWriter(args[3])) {
                            out.write(parsedJson);
                        }
                        break;
                    default:
                        exitGracefully();
                        break;
                }
                break;

            case "c":
                final String json = new Scanner(new File(args[2])).useDelimiter("\\Z").next();
                switch (fileType) {
                    case "b":
                        final BymlFile jsonBymlFile = BymlFile.fromJson(json);
                        jsonBymlFile.write(args[3]);
                        break;
                    case "p":
                        final ProdFile jsonProdfile = ProdFile.fromJson(json);
                        jsonProdfile.write(args[3]);
                        break;
                    default:
                        exitGracefully();
                        break;
                }
                break;
            default:
                exitGracefully();
                break;
        }
    }

    public static void exitGracefully() {
        String jar = new java.io.File(Main.class
                                                      .getProtectionDomain()
                                                      .getCodeSource()
                                                      .getLocation()
                                                      .getPath())
                        .getName();
        System.out.println(String.format("%s [d|c] [b|p](only if encoding) [<input file path> <output file path>", jar));
        System.out.println("d - decode to json");
        System.out.println("c - encode to given type");
        System.out.println("b - byml (byml|mubin|baniminfo|etc)");
        System.out.println("p - prod (blwp)");
        System.exit(0);
    }
}
