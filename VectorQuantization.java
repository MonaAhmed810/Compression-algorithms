import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;

class Block {
    double[][] array;

    Block(int n, int m) {
        array = new double[n][m];
    }
}

public class VectorQuantization {

    private static BufferedImage image = null, newImage = null;
    private static int n, m, numOfVectors, pixels[][], newPixels[][], width, height;
    private static Vector<Block> blocks = new Vector<>(), codeBook = new Vector<>();
    private static Vector<Integer> encodedVec = new Vector<>();
    private static File imageFile = null, outputFilePath = null;

    public static void readImage() {
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public static void convertToPixels() {
        width = image.getWidth();
        height = image.getHeight();
        pixels = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;
                pixels[i][j] = r;
            }
        }
    }

    public static void buildBlocks() {
        for (int i = 0; i < height; i += n) {
            for (int j = 0; j < width; j += m) {
                Block tmp = new Block(n, m);
                for (int x = 0; x < n; x++) {
                    for (int y = 0; y < m; y++) {
                        if (i + x >= height || j + y >= width)
                            continue;
                        tmp.array[x][y] = pixels[i + x][j + y];
                    }
                }
                blocks.add(tmp);
            }
        }
    }

    public static Block getAverage(Vector<Block> v) {
        Block ret = new Block(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < v.size(); k++) {
                    ret.array[i][j] += v.get(k).array[i][j];
                }
                ret.array[i][j] /= v.size();
            }
        }
        return ret;
    }

    public static void split() {
        Vector<Block> tmp = new Vector<>();
        for (int i = 0; i < codeBook.size(); i++) {
            Block b1 = new Block(n, m), b2 = new Block(n, m);
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < m; y++) {
                    b1.array[x][y] = ceil(codeBook.get(i).array[x][y] - 1);
                    b2.array[x][y] = floor(codeBook.get(i).array[x][y] + 1);
                }
            }
            tmp.add(b1);
            tmp.add(b2);
        }
        codeBook = tmp;
    }

    public static double getDiff(Block x, Block y) {
        double ret = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                ret += abs(x.array[i][j] - y.array[i][j]);
            }
        }
        return ret;
    }

    public static Vector<Vector<Block>> assignVectors() {
        Vector<Vector<Block>> ret = new Vector<>();
        ret.setSize(codeBook.size());
        for (int i = 0; i < ret.size(); i++)
            ret.set(i, new Vector<>());
        for (int i = 0; i < blocks.size(); i++) {
            double mn = 1e9;
            int idx = 0;
            for (int j = 0; j < codeBook.size(); j++) {
                double diff = getDiff(blocks.get(i), codeBook.get(j));
                if (mn > diff) {
                    mn = diff;
                    idx = j;
                }
            }
            ret.get(idx).add(blocks.get(i));
        }
        return ret;
    }

    public static void buildVectors() {
        Block firstAverage = getAverage(blocks);
        codeBook.add(firstAverage);
        while (codeBook.size() < numOfVectors) {
            split();
            Vector<Vector<Block>> nearestVectors = assignVectors();
            for (int i = 0; i < codeBook.size(); i++) {
                codeBook.set(i, getAverage(nearestVectors.get(i)));
            }
        }
        Vector<Block> prev = codeBook;
        Boolean flag = true;
        while (flag) {
            Vector<Vector<Block>> nearestVectors = assignVectors();
            for (int i = 0; i < codeBook.size(); i++) {
                codeBook.set(i, getAverage(nearestVectors.get(i)));
            }
            if (codeBook.equals(prev)) {
                flag = false;
            }
            prev = codeBook;
        }
    }

    public static void encode() {
        for (int i = 0; i < blocks.size(); i++) {
            double mn = 1e9;
            int idx = 0;
            for (int j = 0; j < codeBook.size(); j++) {
                double diff = getDiff(blocks.get(i), codeBook.get(j));
                if (mn > diff) {
                    mn = diff;
                    idx = j;
                }
            }
            encodedVec.add(idx);
        }
    }

    public static void decode() {
        newPixels = new int[height][width];
        int i = 0, j = 0, cnt = 0;
        for (int k = 0; k < encodedVec.size(); k++) {
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < m; y++) {
                    if (i + x >= height || j + y >= width)
                        continue;
                    newPixels[i + x][j + y] = (int) codeBook.get(encodedVec.get(k)).array[x][y];
                }
            }
            cnt++;
            if (cnt % ((width + m - 1) / m) != 0)
                j += m;
            else {
                i += n;
                j = 0;
            }
        }
    }

    public static BufferedImage getImageFromArray() {
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image2.setRGB(x, y, (newPixels[y][x] << 16) | (newPixels[y][x] << 8) | (newPixels[y][x]));
            }
        }
        return image2;
    }

    public static void writeImage() {
        try {
            File f = new File(outputFilePath.getAbsolutePath() + "\\output.png");
            ImageIO.write(newImage, "jpg", f);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public static void go() {
        readImage();
        convertToPixels();
        buildBlocks();
        buildVectors();
        encode();
        decode();
        newImage = getImageFromArray();
        writeImage();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Vector quantization");
        frame.setBounds(700, 300, 500, 500);
        frame.setLayout(null);
        frame.setVisible(true);

        JTextField tf1 = new JTextField(), tf2 = new JTextField(), tf3 = new JTextField();
        tf1.setBounds(150, 100, 100, 50);
        tf2.setBounds(250, 100, 100, 50);
        tf3.setBounds(150, 150, 200, 50);
        tf1.setVisible(true);
        tf2.setVisible(true);
        tf3.setVisible(true);
        frame.add(tf1);
        frame.add(tf2);
        frame.add(tf3);

        JButton chooseImagePath = new JButton("Choose image path");
        chooseImagePath.setBounds(150, 200, 200, 50);
        frame.add(chooseImagePath);
        chooseImagePath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser f1 = new JFileChooser();
                int retVal = f1.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    imageFile = f1.getSelectedFile();
                }
            }
        });

        JButton chooseOutputFilePath = new JButton("Choose output path");
        chooseOutputFilePath.setBounds(150, 250, 200, 50);
        frame.add(chooseOutputFilePath);
        chooseOutputFilePath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser f1 = new JFileChooser();
                f1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int retVal = f1.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    outputFilePath = f1.getSelectedFile();
                }
            }
        });

        JButton goButton = new JButton("GO");
        goButton.setBounds(150, 300, 200, 50);
        frame.add(goButton);
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                n = Integer.valueOf(tf1.getText());
                m = Integer.valueOf(tf2.getText());
                numOfVectors = Integer.valueOf(tf3.getText());
                go();
            }
        });
    }
}
