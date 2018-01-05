import com.arbiter34.byml.BymlFile;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BymlFile bymlFile = BymlFile.parse("ActorInfo.product.byml.bak");
        bymlFile.getClass();
        //bymlFile.write("Actorinfo.product.byml.bak");
    }
}
