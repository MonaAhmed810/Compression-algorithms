import javafx.util.Pair;

import java.util.*;

import static java.lang.Math.abs;

class HuffmanNode {
    Pair<Integer, Integer> p;
    int value;
    HuffmanNode left;
    HuffmanNode right;
}

class Compare implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.value - y.value;
    }
}

public class HuffmanWithRLE {

    private static Map<Pair<Integer, Integer>, String> codes = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Integer> freq = new HashMap<>();
    private static HuffmanNode root = null;

    public static Vector<Pair<Integer, Integer>> RLE(Vector<Integer> data) {
        Vector<Pair<Integer, Integer>> tags = new Vector<>();
        int cnt = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != 0) {
                tags.add(new Pair<>(cnt, data.get(i)));
                cnt = 0;
            } else cnt++;
        }
        tags.add(new Pair<>(cnt, 0)); // 0 means EOB
        return tags;
    }

    public static Vector<Pair<Integer, Integer>> categorizing(Vector<Pair<Integer, Integer>> tags) {
        Vector<Pair<Integer, Integer>> ret = new Vector<>();
        for (int i = 0; i < tags.size() - 1; i++) {
            int nonZeroValue = tags.get(i).getValue();
            String binary = Integer.toBinaryString(abs(nonZeroValue));
            ret.add(new Pair<>(tags.get(i).getKey(), binary.length()));
        }
        ret.add(tags.get(tags.size() - 1));
        return ret;
    }

    public static void dfs(HuffmanNode root, String BinaryCode) {
        if (root.left == null && root.right == null) {
            codes.put(root.p, BinaryCode);
            return;
        }
        dfs(root.left, BinaryCode + "0");
        dfs(root.right, BinaryCode + "1");
    }

    public static void HuffmanTree() {

        PriorityQueue<HuffmanNode> q = new PriorityQueue<>(new Compare());
        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : freq.entrySet()) {
            HuffmanNode node = new HuffmanNode();
            node.p = entry.getKey();
            node.value = entry.getValue();
            node.left = null;
            node.right = null;
            q.add(node);
        }

        while (q.size() > 1) {
            HuffmanNode x = q.poll();
            HuffmanNode y = q.poll();

            HuffmanNode newNode = new HuffmanNode();
            newNode.value = x.value + y.value;
            newNode.left = x;
            newNode.right = y;

            root = newNode;
            q.add(newNode);
        }
        dfs(root, "");
    }

    public static void calcFreq(Vector<Pair<Integer, Integer>> tags) {
        for (int i = 0; i < tags.size(); i++) {
            int count = freq.containsKey(tags.get(i)) ? freq.get(tags.get(i)) : 0;
            freq.put(tags.get(i), count + 1);
        }
    }

    private static String compress(Vector<Pair<Integer, Integer>> tags) {
        String encodedStr = "";
        Vector<Pair<Integer, Integer>> categorizedTags = categorizing(tags);
        calcFreq(categorizedTags);
        HuffmanTree();
        for (int i = 0; i < categorizedTags.size() - 1; i++) {
            encodedStr += codes.get(categorizedTags.get(i));
            int nonZeroValue = tags.get(i).getValue();
            String binary = Integer.toBinaryString(abs(nonZeroValue));
            if (nonZeroValue < 0) {
                for (int j = 0; j < binary.length(); j++) {
                    if (binary.charAt(j) == '0') {
                        String tmp = binary.substring(0, j) + '1' + binary.substring(j + 1);
                        binary = tmp;
                    } else {
                        String tmp = binary.substring(0, j) + '0' + binary.substring(j + 1);
                        binary = tmp;
                    }
                }
            }
            encodedStr += binary;
        }
        encodedStr += codes.get(categorizedTags.get(categorizedTags.size() - 1));
        codes.clear();
        freq.clear();
        return encodedStr;
    }

    public static Vector<Pair<Integer, Integer>> decompress(String s) {
        Vector<Pair<Integer, Integer>> ret = new Vector<>();
        HuffmanNode cur = root;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '0')
                cur = cur.left;
            else
                cur = cur.right;
            if (cur.left == null && cur.right == null) {
                Pair<Integer, Integer> categorizedTag;
                categorizedTag = cur.p;
                int categorySize = categorizedTag.getValue(), nonZeroValue;
                if (categorySize == 0) {
                    ret.add(categorizedTag);
                    continue;
                }
                String binary = "";
                while (categorySize != 0) {
                    i++;
                    binary += s.charAt(i);
                    categorySize--;
                }
                if (binary.charAt(0) == '0') {
                    for (int j = 0; j < binary.length(); j++) {
                        if (binary.charAt(j) == '0') {
                            String tmp = binary.substring(0, j) + '1' + binary.substring(j + 1);
                            binary = tmp;
                        } else {
                            String tmp = binary.substring(0, j) + '0' + binary.substring(j + 1);
                            binary = tmp;
                        }
                    }
                    nonZeroValue = Integer.parseInt(binary, 2);
                    nonZeroValue *= -1;
                } else
                    nonZeroValue = Integer.parseInt(binary, 2);
                ret.add(new Pair<>(categorizedTag.getKey(), nonZeroValue));
                cur = root;
            }
        }
        return ret;
    }

    public static Vector<Integer> de_RLE(Vector<Pair<Integer, Integer>> tags) {
        Vector<Integer> ret = new Vector<>();
        for (int i = 0; i < tags.size(); i++) {
            int x = tags.get(i).getKey();
            while (x != 0) {
                ret.add(0);
                x--;
            }
            if (tags.get(i).getValue() != 0)
                ret.add(tags.get(i).getValue());
        }
        return ret;
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        int size = in.nextInt();
        Vector<Integer> data = new Vector<>();
        for (int i = 0; i < size; i++)
            data.add(in.nextInt());

        Vector<Pair<Integer, Integer>> tags = RLE(data);
        String encodedStr = compress(tags);
        System.out.println(encodedStr);

        Vector<Pair<Integer, Integer>> decodedTags = decompress(encodedStr);
        Vector<Integer> decodedData = de_RLE(decodedTags);
        for (int i = 0; i < decodedData.size(); i++)
            System.out.print(decodedData.get(i) + " ");

    }

}
