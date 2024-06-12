package org.example.Gui;

import org.example.Models.Device;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StreamGui {



    private DrawAnnotationGui drawAnnotationGui=null;
    private ViewAnnotationGui viewAnnotationGui=null;

   public StreamGui(String url, Frame previousGUI, ArrayList<Integer> treeIndexs, int deviceIndex, Device device){
       System.out.println(treeIndexs.size());
        JFrame frame = new JFrame("RTSP Streaming Example");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a panel to hold the video component
        JPanel videoPanel = new JPanel(new BorderLayout());
        EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        videoPanel.add(mediaPlayerComponent, BorderLayout.CENTER);

        // Create a button
        JButton button = new JButton("Draw A Slot");
        button.addActionListener(e -> {
            if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Perform the long-running task in the background
                        if(drawAnnotationGui!=null)
                            drawAnnotationGui.dispose();
                        drawAnnotationGui= new DrawAnnotationGui(mediaPlayerComponent, treeIndexs, deviceIndex, device);
                        return null;
                    }

                }.execute();

            }
        });

        JButton viewButton = new JButton("View Slots");
        viewButton.addActionListener(e -> {
            if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Perform the long-running task in the background
                        if(viewAnnotationGui!=null)
                            viewAnnotationGui.dispose();
                        viewAnnotationGui=new ViewAnnotationGui(mediaPlayerComponent,device);
                        return null;
                    }

                }.execute();

            }
        });
       JButton backButton = new JButton("Back");
       backButton.addActionListener(e -> {
           // Make this GUI invisible
           frame.setVisible(false);

           // Make the previous GUI visible again
           if (previousGUI != null) {
               previousGUI.setVisible(true);
           }
       });



        // Create a panel to hold the button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
       buttonPanel.add(backButton);

        buttonPanel.add(button);
        buttonPanel.add(viewButton);

        // Add the video panel and button panel to the frame
        frame.add(videoPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add mouse listener to the frame


        frame.setVisible(true);

        // Start playing the video stream
        mediaPlayerComponent.mediaPlayer().media().play(url);

    }
}
