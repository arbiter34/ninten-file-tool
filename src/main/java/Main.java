import com.arbiter34.byml.BymlFile;
import com.arbiter34.byml.nodes.ArrayNode;
import com.arbiter34.byml.nodes.DictionaryNode;
import com.arbiter34.byml.nodes.Node;
import com.arbiter34.byml.nodes.StringNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            String jar = new java.io.File(Main.class
                                                          .getProtectionDomain()
                                                          .getCodeSource()
                                                          .getLocation()
                                                          .getPath())
                            .getName();
            System.out.println(String.format("%s file.byml", jar));
            System.exit(0);
        }
        final BymlFile bymlFile = BymlFile.parse(args[0]);
        bymlFile.write(args[0] + ".bak");
    }
}
