package org.example.Gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.Models.Device;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.example.Gui.DrawAnnotationGui.*;

public class ViewAnnotationGui {

    private void initializeDrawing(JPanel panel, EmbeddedMediaPlayerComponent mediaPlayerComponent, BufferedImage[] snapshot, Device device) {
        Graphics2D g = snapshot[0].createGraphics();
        stack.clear();
        polygons.clear();
        Gson gson = new Gson();
        Type listType = new TypeToken<java.util.List<java.util.List<Integer>>>() {}.getType();
        final Dimension[] videoDimension = {mediaPlayerComponent.mediaPlayer().video().videoDimension()};
        final double[] widthScale = { panel.getWidth()/videoDimension[0].getWidth() };
        final double[] heightScale = {panel.getHeight()/videoDimension[0].getHeight() };

        System.out.println(device.getSlots().size());
        for (int i = 0; i < device.getSlots().size(); i++) {
            System.out.println(device.getSlots().get(i));
            List<List<Integer>> listOfLists = gson.fromJson(device.getSlots().get(i), listType);
            if(listOfLists!=null) {
                ArrayList<Point> points = new ArrayList<>();
                for (int j = 0; j < listOfLists.size(); j++) {
                    System.out.println(listOfLists.get(j).toString());
                    System.out.println(listOfLists.get(j).get(0) * widthScale[0]);
                    System.out.println(((panel.getWidth() - snapshot[0].getWidth()) / 2));
                    int x = (int) (listOfLists.get(j).get(0) * widthScale[0] - ((panel.getWidth() - snapshot[0].getWidth()) / 2));
                    int y = (int) (listOfLists.get(j).get(1) * heightScale[0] - ((panel.getHeight() - snapshot[0].getHeight()) / 2));

                    System.out.printf("new x: %d and new y: %d",x,y);

                    points.add(new Point(x, y));
                }
                polygons.add(points);
            }
        }
        System.out.println(polygons.toString());

        if(!polygons.isEmpty()){
            for(ArrayList<Point> points :polygons) {
                for (int i=0;i<points.size()-1;i++){
                    Point point=points.get(i);
                    stack.add(cloneImage(snapshot[0]));
                    drawPoint(point.x,point.y,g);
                }
                stack.add(cloneImage(snapshot[0]));
                drawPolygon(points,g);
            }
        }
        g.dispose();
        panel.repaint();
    }
    JFrame newFrame;
  public   ViewAnnotationGui(EmbeddedMediaPlayerComponent mediaPlayerComponent,Device device){
         newFrame = new JFrame("Snapshot for Slots");
        newFrame.setSize(800, 600);
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setLocationRelativeTo(null);

        // Capture a snapshot of the current video component
        final BufferedImage[] snapshot = {mediaPlayerComponent.mediaPlayer().snapshots().get()};



        Graphics2D g = snapshot[0].createGraphics();
        stack.clear();
        if(!polygons.isEmpty()){
            for(ArrayList<Point> points :polygons) {
                for (int i=0;i<points.size()-1;i++){
                    Point point=points.get(i);
                    stack.add(cloneImage(snapshot[0]));
                    drawPoint(point.x,point.y,g);
                }
                stack.add(cloneImage(snapshot[0]));
                drawPolygon(points,g);
            }
        }
        g.dispose();

        // Create a panel to display the snapshot



        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                int x = (getWidth() - snapshot[0].getWidth()) / 2;
                int y = (getHeight() - snapshot[0].getHeight()) / 2;
                g.drawImage(snapshot[0], x, y, this);
                System.out.println("painting is called.........");

            }
        };
        panel.setLayout(new BorderLayout());
        newFrame.add(panel, BorderLayout.CENTER);

        panel.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
              initializeDrawing(panel,mediaPlayerComponent,snapshot,device);
          }

          @Override
          public void componentShown(ComponentEvent e) {
              initializeDrawing(panel,mediaPlayerComponent,snapshot,device);
          }
         });

        // List to store the points


        // Add a mouse listener to capture user clicks

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton submitButton = new JButton("Back");



        controlPanel.add(submitButton);


        newFrame.add(controlPanel, BorderLayout.SOUTH);



        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Handle point submission (e.g., process points)
                newFrame.setVisible(false);

            }
        });



        // Make the new frame visible
        newFrame.setVisible(true);

    }

    public void dispose() {
      newFrame.setVisible(false);
      newFrame.dispose();
    }
}
