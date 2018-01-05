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
        final BymlFile bymlFile = BymlFile.parse("ActorInfo.product.byml");
        final Node node = bymlFile.getRoot();
        if (node instanceof DictionaryNode) {
            final DictionaryNode dictionaryNode = DictionaryNode.class.cast(node);
            final ArrayNode arrayNode = ArrayNode.class.cast(dictionaryNode.get("Actors"));
            final List<DictionaryNode> weapons =  arrayNode.stream()
                    .map(n -> (DictionaryNode)n)
                    .filter(d -> d.containsKey("name") &&
                            d.get("name").eq("Item_Enemy_27"))
                    .collect(Collectors.toList());
            weapons.forEach(d -> d.get("itemSellingPrice").setValue(27000));
        }
        bymlFile.write("ActorInfo.product.byml.bak");
    }
}
