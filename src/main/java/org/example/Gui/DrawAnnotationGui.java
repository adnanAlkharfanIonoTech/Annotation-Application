package org.example.Gui;

import com.google.gson.reflect.TypeToken;
import org.example.Models.Device;
import org.example.Models.Tree;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.example.Gui.FilePicker.data;
import static org.example.Gui.FilePicker.filePath;

public class DrawAnnotationGui {
     static ArrayList<ArrayList<Point>> polygons=new ArrayList<>();
     static Stack<BufferedImage> stack = new Stack<>();

     public void dispose(){
         newFrame.setVisible(false);
         newFrame.dispose();
     }



     static BufferedImage cloneImage(BufferedImage source) {
        BufferedImage clone = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = clone.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return clone;
     }
     static void drawPoint(int x,int y,Graphics2D g){
        g.setColor(Color.RED);
        g.fillOval(x - 2, y - 2, 5, 5);
     }
    static void drawPolygon(ArrayList<Point> points,Graphics2D g){
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];
        for (int i = 0; i < 4; i++) {
            xPoints[i] = points.get(i).x;
            yPoints[i] = points.get(i).y;
        }

        g.setColor(Color.RED);

        g.drawPolygon(xPoints, yPoints, 4);
    }


    public void writeToFile(ArrayList<Integer> treeIndexs,int deviceIndex,ArrayList<ArrayList<Point>> normalizedPolygons) {
        List<Tree> trees=data.getData();

        Tree temp=null;

        for (Integer treeIndex : treeIndexs) {
            if(temp==null)
             temp = trees.get(treeIndex);
            else
                temp = temp.getTree().get(treeIndex);
        }

        System.out.println(temp.getName());


        Device device=temp.getDevices().get(deviceIndex);


        List<String> slots =device.getSlots();

        slots.clear();

        for (int i = 0; i < normalizedPolygons.size(); i++) {
            ArrayList<Point> pointsArray=normalizedPolygons.get(i);
            ArrayList<ArrayList<Integer>> coordinations=new ArrayList<>();
            for (Point point:pointsArray){
                ArrayList<Integer> coordination=new ArrayList<>();
                coordination.add(point.x);
                coordination.add(point.y);
                coordinations.add(coordination);
            }
            slots.add(coordinations.toString());
        }
        // Convert the entire Data object to JSON and write to file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(data);

        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonString);
            file.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println(slots.toString());

    }



    JFrame newFrame;
    public DrawAnnotationGui(EmbeddedMediaPlayerComponent mediaPlayerComponent,ArrayList<Integer> treeIndexs,int deviceIndex,Device device){
        newFrame = new JFrame("Snapshot for Slots");
        newFrame.setSize(800, 600);
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setLocationRelativeTo(null);

        // Capture a snapshot of the current video component
        final BufferedImage[] snapshot = {mediaPlayerComponent.mediaPlayer().snapshots().get()};





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
        final ArrayList<Point>[] points = new ArrayList[]{new ArrayList<>()};

        // Add a mouse listener to capture user clicks
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                stack.add(cloneImage(snapshot[0]));
                System.out.println(points[0].size());
                if ( points[0].size()< 4) {
                    int x = e.getX() - (panel.getWidth() - snapshot[0].getWidth()) / 2;
                    int y = e.getY() - (panel.getHeight() - snapshot[0].getHeight()) / 2;
                    if (x >= 0 && y >= 0 && x < snapshot[0].getWidth() && y < snapshot[0].getHeight()) {
                        points[0].add(new Point(x, y));
                        System.out.println("Point added: " + new Point(x, y));

                        // Draw the point on the image
                        Graphics2D g = snapshot[0].createGraphics();
                        drawPoint(x,y,g);
                        g.dispose();

//stack.add(g);
                        // Repaint the panel to show the point
                        panel.repaint();
                    }
                }
                if (points[0].size() == 4) {
                    // Draw the rectangle on the image

                    Graphics2D g = snapshot[0].createGraphics();
                    drawPolygon(points[0],g);
                    g.dispose();

                    // Repaint the panel to show the rectangle
                    panel.repaint();

                    polygons.add((ArrayList<Point>) points[0].clone());

                    // Clear the points list for future use
                    points[0].clear();
                }
            }
        });

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton clearButton = new JButton("Clear");
        JButton submitButton = new JButton("Submit");
        JButton undoButton = new JButton("Undo");

        controlPanel.add(clearButton);
        controlPanel.add(submitButton);
        controlPanel.add(undoButton);

        newFrame.add(controlPanel, BorderLayout.SOUTH);

        clearButton.addActionListener(e -> {
            points[0].clear();
            polygons.clear();
            // Redraw the snapshot without points

            if (!stack.isEmpty()) {
                stack.pop();
                snapshot[0] = stack.firstElement();
                stack.clear();
                panel.repaint(); // Repaint the panel to reflect the cleared canvas
            }
        });

        submitButton.addActionListener(e -> {
            System.out.println("Polygons submitted: " + polygons);
             Dimension videoDimension = mediaPlayerComponent.mediaPlayer().video().videoDimension();
            if (videoDimension != null) {
                System.out.println("parsed:" + videoDimension);
                double widthScale = videoDimension.getWidth() / panel.getWidth();
                 double heightScale = videoDimension.getHeight() / panel.getHeight();


                // Normalize the polygons
                ArrayList<ArrayList<Point>> normalizedPolygons = new ArrayList<>();
                for (ArrayList<Point> polygon : polygons) {
                    ArrayList<Point> normalizedPolygon = new ArrayList<>();
                    for (Point point : polygon) {
                        System.out.println(point.x);
                        System.out.println((panel.getWidth() - snapshot[0].getWidth()) / 2);
                        System.out.println(widthScale);
                        int normalizedX = (int) ((point.x+( (panel.getWidth() - snapshot[0].getWidth()) / 2)) * widthScale);
                        int normalizedY = (int) ((point.y+( (panel.getHeight() - snapshot[0].getHeight()) / 2)) * heightScale);
//                            int normalizedX = (int) (point.x * widthScale);
//                            int normalizedY = (int) (point.y * heightScale);
                        normalizedPolygon.add(new Point(normalizedX, normalizedY));
                    }
                    normalizedPolygons.add(normalizedPolygon);
                }

                System.out.println("Normalized Polygons: " + normalizedPolygons);
                writeToFile(treeIndexs, deviceIndex,normalizedPolygons);
            }
            // Handle point submission (e.g., process points)

            newFrame.setVisible(false);

        });

        undoButton.addActionListener(e -> {

            // Redraw the snapshot with remaining points


            if (!stack.isEmpty()) {

                snapshot[0] = stack.pop();
                boolean isEmpty=points[0].isEmpty();
                if(isEmpty){
                    if(!polygons.isEmpty()){
                        points[0] = (ArrayList<Point>) polygons.get(polygons.size()-1).clone();
                        polygons.remove(polygons.size()-1);


                    }
                }
                points[0].remove(points[0].size() - 1);
                panel.repaint(); // Repaint the panel to reflect the cleared canvas
            }
        });

        // Make the new frame visible
        newFrame.setVisible(true);

    }

    private void initializeDrawing(JPanel panel, EmbeddedMediaPlayerComponent mediaPlayerComponent,BufferedImage[] snapshot,Device device) {
        Graphics2D g = snapshot[0].createGraphics();
        stack.clear();
        polygons.clear();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<List<Integer>>>() {}.getType();
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
}
