import java.util.*;

class HuffmanNode {
    char c;
    int value;
    HuffmanNode left;
    HuffmanNode right;
}

class Compare implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.value - y.value;
    }
}

public class Huffman {

    private static Vector<Character> charVec = new Vector<>();
    private static Vector<Integer> charFreq = new Vector<>();
    private static HuffmanNode root = null;
    private static Map<Character, String> codes = new HashMap<>();


    public static void dfs(HuffmanNode root, String BinaryCode) {
        if (root.left == null && root.right == null && root.c != '$') {
            codes.put(root.c, BinaryCode);
            return;
        }
        dfs(root.left, BinaryCode + "0");
        dfs(root.right, BinaryCode + "1");
    }

    public static void HuffmanTree() {

        PriorityQueue<HuffmanNode> q = new PriorityQueue<>(new Compare());
        for (int i = 0; i < charVec.size(); i++) {
            HuffmanNode node = new HuffmanNode();
            node.c = charVec.get(i);
            node.value = charFreq.get(i);
            node.left = null;
            node.right = null;
            q.add(node);
        }

        root = null;

        while (q.size() > 1) {
            HuffmanNode x = q.peek();
            q.poll();
            HuffmanNode y = q.peek();
            q.poll();

            HuffmanNode newNode = new HuffmanNode();
            newNode.value = x.value + y.value;
            newNode.c = '$';
            newNode.left = x;
            newNode.right = y;

            root = newNode;
            q.add(newNode);
        }
        dfs(root, "");
    }

    public static void calcFreq(String data) {
        int freq[] = new int[128];
        for (int i = 0; i < data.length(); i++)
            freq[data.charAt(i)]++;
        for (int i = 0; i < 128; i++) {
            if (freq[i] != 0) {
                charVec.add((char) i);
                charFreq.add(freq[i]);
            }
        }
    }

    public static String decode(HuffmanNode root, String s) {
        String ret = "";
        HuffmanNode cur = root;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '0')
                cur = cur.left;
            else
                cur = cur.right;
            if (cur.left == null && cur.right == null) {
                ret += cur.c;
                cur = root;
            }
        }
        return ret;
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String data = in.nextLine(), encodedStr = "", decodedStr;
        calcFreq(data);
        HuffmanTree();
        System.out.println("Characters with their codes");
        for (Map.Entry<Character, String> entry : codes.entrySet())
            System.out.println(entry.getKey() + " " + entry.getValue());
        for (int i = 0; i < data.length(); i++)
            encodedStr += codes.get(data.charAt(i));
        System.out.println("Encoded Huffman data:\n" + encodedStr);

        // decompress
        codes.clear();
        HuffmanTree();
        decodedStr = decode(root, encodedStr);
        System.out.println("Decoded Huffman data:\n" + decodedStr);

    }
}