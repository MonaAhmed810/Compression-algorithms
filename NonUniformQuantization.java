import javafx.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class NonUniformQuantization {

    private static int target, mxValue, width, height, q;
    private static Vector<Integer> originalVec = new Vector<>(), encodedVec = new Vector<>(), rangeMean = new Vector<>();
    private static Vector<Pair<Integer, Integer>> ranges = new Vector<>();
    private static File imagePath, outputPath;
    private static BufferedImage image = null, newImage = null;

    public static void readImage() {
        try {
            image = ImageIO.read(imagePath);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public static void convertToPixels() {
        width = image.getWidth();
        height = image.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;
                originalVec.add(r);
                mxValue = Math.max(mxValue, r);
            }
        }
        encodedVec.setSize(width * height);
    }

    public static BufferedImage getImageFromArray(Vector<Integer> pixels, int width, int height) {
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image2.setRGB(x, y, (pixels.elementAt(y * width + x) << 16) | (pixels.elementAt(y * width + x) << 8) | (pixels.elementAt(y * width + x)));
            }
        }
        return image2;
    }

    public static int getMean(Vector<Integer> v) {
        int ret = 0;
        for (int i = 0; i < v.size(); i++)
            ret += v.get(i);
        ret /= v.size();
        return ret;
    }

    public static void split(Vector<Integer> v, int st, int ed, int level) {
        if (st > ed) {
            return;
        }
        int mean = getMean(v);
        if (level == target) {
            for (int i = 0; i < originalVec.size(); i++) {
                if (originalVec.get(i) >= st && originalVec.get(i) <= ed)
                    encodedVec.set(i, q);
            }
            ranges.add(new Pair<>(st, ed));
            rangeMean.add(mean);
            q++;
            return;
        }
        Vector<Integer> v1 = new Vector<>(), v2 = new Vector<>();
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i) <= mean)
                v1.add(v.get(i));
            else
                v2.add(v.get(i));
        }
        split(v1, st, mean, level + 1);
        split(v2, mean + 1, ed, level + 1);
    }

    public static Vector<Integer> decompress(Vector<Integer> encodedVec) {
        Vector<Integer> ret = new Vector<>();
        for (int i = 0; i < encodedVec.size(); i++) {
            ret.add(rangeMean.get(encodedVec.get(i)));
        }
        return ret;
    }

    public static void writeRanges() {
        try {
            File f = new File(outputPath.getAbsolutePath() + "\\output.txt");
            PrintWriter out = new PrintWriter(f);
            for (int i = 0; i < ranges.size(); i++)
                out.println(ranges.get(i).getKey() + " " + ranges.get(i).getValue() + " " + i + " " + rangeMean.get(i));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeImage() {
        try {
            File f = new File(outputPath.getAbsolutePath() + "\\output3.png");
            ImageIO.write(newImage, "jpg", f);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    public static void go() {
        readImage();
        convertToPixels();
        int x = 1;
        while (x - 1 < mxValue) {
            x *= 2;
        }
        --x;
        split(originalVec, 0, x, 0);
        writeRanges();
        Vector<Integer> decodedVec = decompress(encodedVec);
        newImage = getImageFromArray(decodedVec, width, height);
        writeImage();
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Non uniform quantization");
        frame.setBounds(700, 300, 500, 500);
        frame.setLayout(null);
        frame.setVisible(true);

        JTextField tf;
        tf = new JTextField();
        tf.setBounds(150, 100, 200, 50);
        tf.setVisible(true);
        frame.add(tf);

        JButton chooseImagePath = new JButton("Choose image path");
        chooseImagePath.setBounds(150, 150, 200, 50);
        frame.add(chooseImagePath);
        chooseImagePath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser f1 = new JFileChooser();
                int retVal = f1.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    imagePath = f1.getSelectedFile();
                }
            }
        });

        JButton chooseOutputFilePath = new JButton("Choose output Directory");
        chooseOutputFilePath.setBounds(150, 200, 200, 50);
        frame.add(chooseOutputFilePath);
        chooseOutputFilePath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser f1 = new JFileChooser();
                f1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int retVal = f1.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    outputPath = f1.getSelectedFile();
                }
            }
        });

        JButton goButton = new JButton("GO");
        goButton.setBounds(150, 250, 200, 50);
        frame.add(goButton);
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                target = Integer.valueOf(tf.getText());
                go();
            }
        });
    }
}
