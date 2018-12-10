import java.util.Scanner;
import java.util.Vector;
import javafx.util.Pair;

public class LZ78 {

    private static Vector<String> dic = new Vector<>();

    public static Vector<Pair<Integer, Character>> compress(String s) {
        Vector<Pair<Integer, Character>> tags = new Vector<>();
        dic.add("");
        for (int i = 0; i < s.length(); i++) {
            String tmp = "";
            tmp += s.charAt(i);
            int test = dic.indexOf(tmp), prev = 0;
            while (test != -1) {
                prev = test;
                i++;
                if (i >= s.length()) break;
                tmp += s.charAt(i);
                test = dic.indexOf(tmp);
            }
            if (i >= s.length()) {
                tags.add(new Pair(prev, ""));
                break;
            }
            tags.add(new Pair(prev, s.charAt(i)));
            dic.add(tmp);
        }
        dic.clear();
        return tags;
    }

    public static String decompress(Vector<Pair<Integer, Character>> tags){
        String ret = "";
        dic.add("");
        for (int i = 0; i < tags.size(); i++) {
            ret += dic.get(tags.get(i).getKey());
            ret += tags.get(i).getValue();
            dic.add(dic.get(tags.get(i).getKey()) + tags.get(i).getValue());
        }
        return ret;
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        Vector<Pair<Integer, Character>> tags = compress(s);
        String decodedStr = decompress(tags);
        System.out.println(decodedStr);
    }
}
