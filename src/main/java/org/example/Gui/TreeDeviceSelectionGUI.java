package org.example.Gui;


import org.example.Models.Device;
import org.example.Models.Tree;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

// Assuming you have your Tree and Device classes defined
public class TreeDeviceSelectionGUI extends JFrame {



    private JComboBox<String> treeComboBox;
    private JComboBox<String> deviceComboBox;
    private JButton nextButton;
    private ArrayList<Integer> treeIndexs;

    public TreeDeviceSelectionGUI(List<Tree> trees,JFrame previousFrame,ArrayList<Integer> treeIndexs) {
        this.treeIndexs=treeIndexs;
        JPanel contentPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for centering

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL; // Stretch components horizontally
        constraints.weightx = 1.0; // Even distribution of horizontal space
        constraints.insets = new Insets(10, 15, 10, 15); // Add margins around components

        JLabel treeLabel = new JLabel("Select Tree:");
        treeLabel.setFont(new Font("SansSerif", Font.BOLD, 14)); // Bold heading
        treeComboBox = createComboBox(trees);
        constraints.gridy = 0; // Place tree elements at row 0
        contentPanel.add(treeLabel, constraints);
        constraints.gridy = 1;
        contentPanel.add(treeComboBox, constraints);

        JLabel deviceLabel = new JLabel("Select Device:");
        deviceLabel.setFont(new Font("SansSerif", Font.BOLD, 14)); // Bold heading
        deviceLabel.setVisible(false);
        deviceComboBox = createComboBox(new ArrayList<>()); // Initially empty device list
        deviceComboBox.setEnabled(false); // Initially disabled
        deviceComboBox.setVisible(false);
        constraints.gridy = 2; // Place device elements at row 2
        contentPanel.add(deviceLabel, constraints);
        constraints.gridy = 3;
        contentPanel.add(deviceComboBox, constraints);

        treeComboBox.addActionListener(e -> {
            deviceComboBox.setSelectedIndex(-1);
            boolean isTreeChosen=treeComboBox.getSelectedItem() != null
                    && !treeComboBox.getSelectedItem().toString().isEmpty();
            nextButton.setEnabled(isTreeChosen);
            if (isTreeChosen
                     && trees.get(treeComboBox.getSelectedIndex()-1).getDevices()!=null
                        && !trees.get(treeComboBox.getSelectedIndex()-1).getDevices().isEmpty()) {
                Tree selectedTree = trees.get(treeComboBox.getSelectedIndex()-1);
                List<Device> availableDevices = selectedTree.getDevices(); // Get devices based on the tree

                // Update device combo box with available devices (assuming getDevicesForTree returns a list)
                List<String> deviceNamesWithEmpty = new ArrayList<>();
                deviceNamesWithEmpty.add("");
                deviceNamesWithEmpty.addAll(availableDevices.stream().map(Device::getName).collect(Collectors.toList()));


                // Update device combo box with available devices (including the empty string)
                deviceComboBox.setModel(new DefaultComboBoxModel<>(deviceNamesWithEmpty.toArray(new String[0])));
                deviceComboBox.setEnabled(true); // Enable device selection
                deviceComboBox.setVisible(true);
                deviceLabel.setVisible(true);
            } else {
                deviceComboBox.setModel(new DefaultComboBoxModel<>(new String[0])); // Empty model
                deviceLabel.setVisible(false);
                deviceComboBox.setEnabled(false); // Disable device selection
                deviceComboBox.setVisible(false);
            }
        });


        nextButton = new JButton("Next");
        nextButton.setEnabled(false); // Initially disabled until a selection is made
        nextButton.setPreferredSize(new Dimension(100, 30)); // Set button size


        nextButton.addActionListener(e -> {
            boolean isTreeChosen=treeComboBox.getSelectedItem() != null
                    && !treeComboBox.getSelectedItem().toString().isEmpty();
            if(isTreeChosen){
                setVisible(false);
                int treeIndex=treeComboBox.getSelectedIndex()-1;
                Tree selectedTree = trees.get(treeIndex);
                if(TreeDeviceSelectionGUI.this.treeIndexs==null)
                    TreeDeviceSelectionGUI.this.treeIndexs=new ArrayList<>();
                if (deviceComboBox.isVisible() && deviceComboBox.getSelectedIndex()>0){
                    TreeDeviceSelectionGUI.this.treeIndexs.add(treeIndex);
                    int deviceIndex=deviceComboBox.getSelectedIndex()-1;
                    Device selectedDevice = selectedTree.getDevices().get(deviceIndex);
                    new StreamGui(selectedDevice.getUrl(),TreeDeviceSelectionGUI.this, (ArrayList<Integer>) TreeDeviceSelectionGUI.this.treeIndexs.clone(),deviceIndex,selectedDevice);
                    TreeDeviceSelectionGUI.this.treeIndexs.remove(TreeDeviceSelectionGUI.this.treeIndexs.size()-1);
                }
                else if(!selectedTree.getTree().isEmpty()) {
                    TreeDeviceSelectionGUI.this.treeIndexs.add(treeIndex);
                    new TreeDeviceSelectionGUI(selectedTree.getTree(),TreeDeviceSelectionGUI.this,(ArrayList<Integer>) TreeDeviceSelectionGUI.this.treeIndexs.clone());
                    TreeDeviceSelectionGUI.this.treeIndexs.remove(TreeDeviceSelectionGUI.this.treeIndexs.size()-1);

                }
                else {
                    JOptionPane.showMessageDialog(TreeDeviceSelectionGUI.this, "No further subtree available.", "Information", JOptionPane.INFORMATION_MESSAGE);
                    setVisible(true);
                }

            }
        }
        );

        // Action listeners for selection changes and Next button (same as before)
        // ... (code remains the same)
        JPanel buttonPanel = new JPanel();
        if(previousFrame!=null) {
            JButton backButton = new JButton("Back");
            backButton.setPreferredSize(new Dimension(100, 30));
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false); // Make this GUI invisible
                    previousFrame.setVisible(true); // Make the previous GUI visible again

                }
            });
            buttonPanel.add(backButton);
        }


        buttonPanel.add(nextButton);


        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Improved frame configuration for larger size and better appearance
        setPreferredSize(new Dimension(800, 600)); // Set desired window size
        pack();
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("RTSP Streaming Selection");
        setVisible(true);
    }

    private JComboBox<String> createComboBox(List<?> items) {
        List<String> itemNames = new ArrayList<>();
        itemNames.add("");
        for (Object item : items) {
            itemNames.add(item.toString()); // Assuming `toString()` provides a valid display name
        }
        return new JComboBox<>(itemNames.toArray(new String[0]));
    }


}
