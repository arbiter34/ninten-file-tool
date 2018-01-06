import com.arbiter34.byml.BymlFile;
import com.arbiter34.byml.nodes.ArrayNode;
import com.arbiter34.byml.nodes.DictionaryNode;
import com.arbiter34.byml.nodes.Node;
import com.arbiter34.byml.nodes.StringNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public Main() {
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            exitGracefully();
        }
        final String operation = args[0];
        switch (operation) {
            case "d":
                final BymlFile bymlFile = BymlFile.parse(args[1]);
                final String parsedJson = bymlFile.toJson();
                try (final PrintWriter out = new PrintWriter(args[2])) {
                    out.write(parsedJson);
                }
                break;
            case "c":
                final String json = new Scanner(new File(args[1])).useDelimiter("\\Z").next();
                final BymlFile jsonBymlFile = BymlFile.fromJson(json);
                jsonBymlFile.write(args[2]);
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
        System.out.println(String.format("%s [d|c] <input file path> <output file path>" +
                                         "d - decode to json " +
                                         "c - encode to byml ", jar));
        System.exit(0);
    }
}
