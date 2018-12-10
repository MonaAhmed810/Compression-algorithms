import java.util.Scanner;
import java.util.Vector;

public class LZW {

    public static Vector<Integer> compress(String text){
        Vector<String> dic = new Vector<>();
        for (int i = 0; i < 128; i++)
            dic.add((char) i + "");
        Vector<Integer> ret = new Vector<>();
        for (int i = 0; i < text.length(); ) {
            String tmp = text.charAt(i) + "", prev = "";
            while (dic.contains(tmp)) {
                prev = tmp;
                i++;
                if (i >= text.length()) break;
                tmp += text.charAt(i);
            }
            ret.add(dic.indexOf(prev));
            if (i >= text.length())
                break;
            dic.add(tmp);
        }
        return ret;
    }

    public static String decompress(Vector<Integer> compressedValues){
        Vector<String> dic = new Vector<>();
        for (int i = 0; i < 128; i++)
            dic.add((char) i + "");
        String ret = "";
        for (int i = 0; i < compressedValues.size(); i++) {
            String tmp = dic.get(compressedValues.get(i));
            if (i + 1 < compressedValues.size()) {
                if (compressedValues.get(i+1) < dic.size())
                    dic.add(tmp + dic.get(compressedValues.get(i + 1)).charAt(0));
                else
                    dic.add(tmp + tmp.charAt(0));
            }
            ret += tmp;
        }
        return ret;
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String text = in.nextLine();
        Vector<Integer> compressedValues = compress(text);

        for (int x : compressedValues)
            System.out.print(x + " ");
        System.out.println();

        String decodedStr = decompress(compressedValues);
        System.out.println(decodedStr);

    }
}
