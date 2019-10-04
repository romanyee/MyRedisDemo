package roman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private static Map<String, List<String>> lists = new HashMap<>();
    private static Map<String, Map<String, String>> hashes = new HashMap<>();//单例

    public static List<String> getList(String key) {

        List<String> list =  lists.get(key);
        if (list == null) {
            list = new ArrayList<>();
            lists.put(key, list);
        }
        return list;
    }

    public static Map<String, String> getHashes(String key) {

        Map<String, String> hash =  hashes.get(key);
        if (hash == null) {
            //必须创建一个列表,否则返回的是NPE,程序无法进行
            hash = new HashMap<>();
            hashes.put(key, hash);
        }
        return hash;
    }
}
