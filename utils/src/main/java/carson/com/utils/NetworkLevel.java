package carson.com.utils;

import java.util.logging.Level;

public class NetworkLevel extends Level {
    private static final String bundle = "com.carson.chatapp.NetworkLevel";
    protected NetworkLevel(String name, int value) {
        super(name, value);
    }

    protected NetworkLevel(String name, int value, String resourceBundleName) {
        super(name, value, resourceBundleName);
    }
    public static final NetworkLevel NETWORK = new NetworkLevel("NETWORK",850,bundle);
}
