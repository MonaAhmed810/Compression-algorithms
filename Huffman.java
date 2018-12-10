import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
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
    private static File outputDir, freqFile;

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

    private static String compress(String data) {
        String encodedStr = "";
        calcFreq(data);
        HuffmanTree();
        File file = new File(outputDir.getAbsolutePath() + "//codesTable.txt");
        File freqFile = new File(outputDir.getAbsolutePath() + "//freqTable.txt");
        try {
            PrintWriter out = new PrintWriter(file);
            for (Map.Entry<Character, String> entry : codes.entrySet())
                out.println(entry.getKey() + " " + entry.getValue());
            out.close();
            out = new PrintWriter(freqFile);
            for (int i = 0; i < charVec.size(); i++)
                out.println(charVec.get(i) + " " + charFreq.get(i));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < data.length(); i++)
            encodedStr += codes.get(data.charAt(i));
        charFreq.clear();
        charVec.clear();
        codes.clear();
        return encodedStr;
    }

    public static String decompress(String encodedStr) {
        try {
            Scanner in = new Scanner(freqFile);
            while (in.hasNext()) {
                char c = in.next().charAt(0);
                int freq = Integer.valueOf(in.next());
                System.out.println(c + " " + freq);
                charVec.add(c);
                charFreq.add(freq);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String decodedStr;
        HuffmanTree();
        decodedStr = decode(root, encodedStr);
        return decodedStr;
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Huffman");
        frame.setBounds(700, 300, 500, 500);
        frame.setLayout(null);
        frame.setVisible(true);

        JButton comButton = new JButton("Compress");
        comButton.setBounds(150, 120, 200, 50);
        frame.add(comButton);
        comButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame compressFrame = new JFrame("Compress");
                compressFrame.setBounds(700, 300, 500, 500);
                compressFrame.setLayout(null);
                compressFrame.setVisible(true);

                JTextField tf1 = new JTextField(), tf2 = new JTextField();
                tf1.setBounds(150, 100, 200, 50);
                tf2.setBounds(150, 160, 200, 50);
                compressFrame.add(tf1);
                compressFrame.add(tf2);


                JButton chooseFileButton = new JButton("Choose output file");
                chooseFileButton.setBounds(150, 220, 200, 50);
                compressFrame.add(chooseFileButton);
                chooseFileButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser f1 = new JFileChooser();
                        f1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        int retVal = f1.showOpenDialog(null);
                        if (retVal == JFileChooser.APPROVE_OPTION) {
                            outputDir = f1.getSelectedFile();
                        }
                    }
                });

                JButton okButton = new JButton("GO");
                okButton.setBounds(150, 280, 200, 50);
                compressFrame.add(okButton);
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String data = tf1.getText();
                        if (data != null && outputDir.isDirectory()) {
                            String encodedStr = compress(data);
                            tf2.setText(encodedStr);
                        }
                    }
                });


            }
        });

        JButton decompressButton = new JButton("Decompress");
        decompressButton.setBounds(150, 220, 200, 50);
        frame.add(decompressButton);
        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame decompressFrame = new JFrame("Decompress");
                decompressFrame.setBounds(700, 300, 500, 500);
                decompressFrame.setLayout(null);
                decompressFrame.setVisible(true);

                JTextField tf1 = new JTextField(), tf2 = new JTextField();
                tf1.setBounds(150, 100, 200, 50);
                tf2.setBounds(150, 220, 200, 50);
                decompressFrame.add(tf1);
                decompressFrame.add(tf2);

                JButton chooseFileButton = new JButton("Choose Frequencies File");
                chooseFileButton.setBounds(150, 160, 200, 50);
                decompressFrame.add(chooseFileButton);
                chooseFileButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser f1 = new JFileChooser();
                        int retVal = f1.showOpenDialog(null);
                        if (retVal == JFileChooser.APPROVE_OPTION) {
                            freqFile = f1.getSelectedFile();
                        }
                    }
                });
                JButton okButton = new JButton("GO");
                okButton.setBounds(150, 280, 200, 50);
                decompressFrame.add(okButton);
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String encodedStr = tf1.getText();
                        if (encodedStr != null && freqFile.isFile()) {
                            String decodedStr = decompress(encodedStr);
                            tf2.setText(decodedStr);
                        }
                    }
                });

            }
        });
    }
}