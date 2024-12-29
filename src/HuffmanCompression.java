import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;

class HuffmanNode implements Comparable<HuffmanNode> {
    char data;
    int frequency;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}

public class HuffmanCompression {

    private static HashMap<Character, String> huffmanCodes = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HuffmanCompressionGUI();
            }
        });
    }

    private static HuffmanNode buildHuffmanTree(HashMap<Character, Integer> charFrequency) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        for (char c : charFrequency.keySet()) {
            priorityQueue.add(new HuffmanNode(c, charFrequency.get(c)));
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();

            HuffmanNode mergedNode = new HuffmanNode('\0', left.frequency + right.frequency);
            mergedNode.left = left;
            mergedNode.right = right;

            priorityQueue.add(mergedNode);
        }

        return priorityQueue.poll();
    }

    private static void generateHuffmanCodes(HuffmanNode root, String code) {
        if (root == null) {
            return;
        }

        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.data, code);
        }

        generateHuffmanCodes(root.left, code + "0");
        generateHuffmanCodes(root.right, code + "1");
    }

    private static String compressText(String inputText) {
        StringBuilder compressedText = new StringBuilder();
        for (char c : inputText.toCharArray()) {
            compressedText.append(huffmanCodes.get(c));
        }
        return compressedText.toString();
    }

    private static String convertToBinaryString(String compressedText) {
        StringBuilder binaryString = new StringBuilder();
        int len = compressedText.length();

        for (int i = 0; i < len; i += 8) {
            String byteStr = compressedText.substring(i, Math.min(i + 8, len));
            int byteValue = Integer.parseInt(byteStr, 2);
            binaryString.append((char) byteValue);
        }

        return binaryString.toString();
    }

    static void compressFile(File inputFile, File outputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        StringBuilder inputText = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            inputText.append(line).append("\n");
        }
        reader.close();

        HashMap<Character, Integer> charFrequency = new HashMap<>();
        for (char c : inputText.toString().toCharArray()) {
            charFrequency.put(c, charFrequency.getOrDefault(c, 0) + 1);
        }

        HuffmanNode root = buildHuffmanTree(charFrequency);
        generateHuffmanCodes(root, "");

        String compressedText = compressText(inputText.toString());
        String binaryString = convertToBinaryString(compressedText);

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(binaryString);
        writer.close();
    }
}

class HuffmanCompressionGUI extends JFrame {
    private JLabel originalSizeLabel, reducedSizeLabel;
    private JButton compressButton;
    private JFileChooser fileChooser;

    public HuffmanCompressionGUI() {
        super("Huffman Compression");

        originalSizeLabel = new JLabel("Original Size: N/A");
        reducedSizeLabel = new JLabel("Reduced Size: N/A");
        compressButton = new JButton("Compress");
        fileChooser = new JFileChooser();

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(HuffmanCompressionGUI.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File inputFile = fileChooser.getSelectedFile();
                    File outputFile = new File(inputFile.getParent(), "compressed.txt");

                    try {
                        HuffmanCompression.compressFile(inputFile, outputFile);

                        long originalSize = inputFile.length();
                        long reducedSize = outputFile.length();

                        originalSizeLabel.setText("Original Size: " + originalSize + " bytes");
                        reducedSizeLabel.setText("Reduced Size: " + reducedSize + " bytes");

                        JOptionPane.showMessageDialog(HuffmanCompressionGUI.this,
                                "Compression completed successfully.\nCompressed file saved as 'compressed.txt'.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(HuffmanCompressionGUI.this,
                                "Error occurred while compressing the file.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });

        setLayout(new FlowLayout());
        add(compressButton);
        add(originalSizeLabel);
        add(reducedSizeLabel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}