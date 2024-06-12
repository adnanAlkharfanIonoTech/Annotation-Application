package org.example.Gui;

import org.example.Models.Data;
import org.example.Models.Device;
import org.example.Models.Tree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FilePicker extends JFrame {

    private final JTextField filePathTextField;

    public static String filePath;

    public static Data data;


    private static Data readJsonFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        // Parse the JSON content
        JSONObject jsonObject = new JSONObject(content);
        JSONArray jsonDataArray = jsonObject.getJSONArray("data");

        // Process the JSON array into Java objects
        List<Tree> trees = new ArrayList<>();
        for (int i = 0; i < jsonDataArray.length(); i++) {
            JSONObject jsonTree = jsonDataArray.getJSONObject(i);
            Tree tree = parseTree(jsonTree);
            if(tree==null)
                continue;
            trees.add(tree);
        }

        return new Data(trees);
    }

    private static Tree parseTree(JSONObject jsonTree) {
        if(!jsonTree.has("id") && jsonTree.isNull("id"))
        {
            return null;
        }
        int id = jsonTree.getInt("id");
        String name = jsonTree.getString("name");

        List<Device> devices = new ArrayList<>();
        if(jsonTree.has("devices") && !jsonTree.isNull("devices"))
        {
            JSONArray jsonDevices = jsonTree.getJSONArray("devices");
            for (int j = 0; j < jsonDevices.length(); j++) {
                JSONObject jsonDevice = jsonDevices.getJSONObject(j);
                Device device = parseDevice(jsonDevice);
                devices.add(device);
            }
        }

        List<Tree> subTree = new ArrayList<>();
        if (jsonTree.has("tree") && !jsonTree.isNull("tree")) {
            JSONArray treesJson=jsonTree.getJSONArray("tree");
            for (int i = 0; i < treesJson.length(); i++) {
                subTree.add(parseTree(treesJson.getJSONObject(i)));
            }

        }

        return new Tree(id, name, devices, subTree);
    }

    private static Device parseDevice(JSONObject jsonDevice) {
        int id = jsonDevice.getInt("id");
        String name = jsonDevice.getString("name");
        String url = jsonDevice.getString("url");
        List<String> slots = new ArrayList<>();

        if(jsonDevice.has("slots")) {
            JSONArray jsonSlots = jsonDevice.getJSONArray("slots");
            for (int k = 0; k < jsonSlots.length(); k++) {
                // Assuming Slot has no fields based on provided JSON
                slots.add(jsonSlots.getString(k));
            }
        }
        return new Device(id, name, url, slots);
    }

    public FilePicker() {
        super("File Picker");

        // Create a panel for layout with better spacing and margins
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 15, 10, 15);

        // Create a label for the file path
        JLabel filePathLabel = new JLabel("Selected File:");
        constraints.gridy = 0;
        constraints.gridx = 0;
        panel.add(filePathLabel, constraints);

        // Create a text field to display the selected file path
        filePathTextField = new JTextField(20);
        filePathTextField.setEditable(false); // Prevent direct editing of path
        constraints.gridy = 0;
        constraints.gridx = 1;
        constraints.gridwidth = 2; // Span two columns
        panel.add(filePathTextField, constraints);

        // Create a browse button with an icon
        JButton browseButton = new JButton("Browse");
        browseButton.setPreferredSize(new Dimension(100, 30)); // Set button size
        constraints.gridy = 1;
        constraints.gridx = 2;
        constraints.gridwidth = 1; // Back to single column
        constraints.weightx = 0.0; // No horizontal space weight
        panel.add(browseButton, constraints);

        // Action listener for browse button with file filter and validation
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Select only files

            // Filter for JSON files only
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(FilePicker.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();

                if (filePath.endsWith(".json")) { // Validate file extension
                    filePathTextField.setText(filePath);
                    FilePicker.this.filePath=filePath;
                } else {
                    JOptionPane.showMessageDialog(FilePicker.this, "Please select a JSON file (.json)", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Create a Submit button
        JButton submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(100, 30)); // Set button size

        submitButton.addActionListener(
                e -> {
                   if(filePath==null||filePath.isEmpty()){
                       JOptionPane.showMessageDialog(FilePicker.this, "Please select a JSON file (.json)", "Invalid File", JOptionPane.ERROR_MESSAGE);
                       return;
                   }
                   try{
                       data=readJsonFile(filePath);
                       new TreeDeviceSelectionGUI(data.getData(),null,null);
                       setVisible(false);
                   }catch (Exception ex){
                       JOptionPane.showMessageDialog(FilePicker.this, "can't parse the file as intended due: "+ex.getMessage(), "Invalid formating", JOptionPane.ERROR_MESSAGE);

                   }
                }
        );

        constraints.gridy = 2;
        constraints.gridx = 2;
        constraints.weightx = 1.0; // Fill remaining horizontal space
        constraints.fill = GridBagConstraints.BOTH; // Fill both width and height
        panel.add(submitButton, constraints);

        // (Optional) Implement action listener for the submit button based on your application logic

        // Set preferred window size and improve layout
        setPreferredSize(new Dimension(500, 200));
        pack();

        // Center the window on the screen
        setLocationRelativeTo(null);

        // Set default close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the content pane and make it visible
        getContentPane().add(panel);
        setVisible(true);
    }
}
