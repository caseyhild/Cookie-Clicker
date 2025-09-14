import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class CookieClicker extends JFrame implements Runnable, MouseListener {
    private final int width = 800;
    private final int height = 600;
    private final Thread thread;
    private boolean running;
    private double cookies = 0;
    private double cps = 0;
    private final ArrayList<ClickableItem> items;
    private final int cookieSize = 150;
    private final int cx = width / 2;
    private final int cy = height * 3 / 4;
    private int cookiePressedFrames = 0;
    ArrayList<Point2D.Double> chipOffsets = new ArrayList<>();

    public CookieClicker() {
        setSize(width, height + 28);
        setResizable(false);
        setTitle("Cookie Clicker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        addMouseListener(this);

        thread = new Thread(this);

        items = new ArrayList<>();

        // add items (name, x, y, baseCost, cps)
        items.add(new ClickableItem("Cursor", 40, 40, 100, 1));
        items.add(new ClickableItem("Grandma", 120, 40, 500, 5));
        items.add(new ClickableItem("Farm", 200, 40, 1000, 10));
        items.add(new ClickableItem("Factory", 280, 40, 5000, 50));
        items.add(new ClickableItem("Mine", 360, 40, 10000, 100));
        items.add(new ClickableItem("Shipment", 440, 40, 50000, 500));
        items.add(new ClickableItem("Alchemy Lab", 40, 140, 100000, 1000));
        items.add(new ClickableItem("Portal", 120, 140, 500000, 5000));
        items.add(new ClickableItem("Time Machine", 200, 140, 1000000, 10000));
        items.add(new ClickableItem("Antimatter", 280, 140, 2000000, 50000));
        items.add(new ClickableItem("Prism", 360, 140, 10000000, 100000));

        // set chocolate chip locations
        chipOffsets = new ArrayList<>();
        int cookieRadius = 75;
        for (int i = 0; i < 15; i++) {
            Point2D.Double newChip;
            boolean valid;
            do {
                valid = true;
                double angle = Math.random() * 2 * Math.PI;
                double r = (cookieRadius - 15) * Math.sqrt(Math.random());
                double x = r * Math.cos(angle);
                double y = r * Math.sin(angle);
                newChip = new Point2D.Double(x, y);

                // check distance to existing chips
                for (Point2D.Double chip : chipOffsets) {
                    if (newChip.distance(chip) < 20) {
                        valid = false;
                        break;
                    }
                }
            } while (!valid);
            chipOffsets.add(newChip);
        }

        //start the program
        start();
    }

    private synchronized void start()
    {
        //starts game
        running = true;
        thread.start();
    }

    private void update() {
        // Increase cookies per second
        cps = 0;
        for (ClickableItem item : items) {
            cps += item.getCPS() * item.getCount();
        }
        cookies += cps / 60.0; // assuming 60 fps
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.translate(0, 28);

        // Background
        GradientPaint bg = new GradientPaint(0, 0, new Color(250, 240, 230),
                0, height, new Color(240, 230, 210));
        g.setPaint(bg);
        g.fillRect(0, 0, width, height);

        // Cookie button
        double scale = 1.0;
        if (cookiePressedFrames > 0) {
            scale = 0.9;
            cookiePressedFrames--;
        }
        int scaledSize = (int) (cookieSize * scale);

        // Draw cookie
        Color cookieColor = new Color(210, 160, 50);
        if (cookiePressedFrames > 0)
            cookieColor = new Color(162, 128, 40);
        g.setColor(cookieColor);
        g.fillOval(cx - scaledSize / 2, cy - scaledSize / 2, scaledSize, scaledSize);
        g.setStroke(new BasicStroke(6));
        g.setColor(new Color(120, 60, 0));
        g.drawOval(cx - scaledSize / 2, cy - scaledSize / 2, scaledSize, scaledSize);

        // Chocolate chips
        for (Point.Double offset : chipOffsets) {
            int chipX = cx + (int)(offset.x * scale);
            int chipY = cy + (int)(offset.y * scale);
            g.fillOval(chipX - 7, chipY - 7, 14, 14);
        }

        // Cookie count & CPS
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("Cookies: " + (int) cookies, 50, 40);

        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.drawString("Cookies per second: " + (int) cps, 50, 70);

        // Items
        int startX = 50;
        int startY = 90;
        int itemWidth = 160;
        int itemHeight = 70;
        int gapX = 15;
        int gapY = 15;
        int itemsPerRow = 4;

        for (int i = 0; i < items.size(); i++) {
            ClickableItem item = items.get(i);
            int col = i % itemsPerRow;
            int row = i / itemsPerRow;
            int ix = startX + col * (itemWidth + gapX);
            int iy = startY + row * (itemHeight + gapY);

            item.setRect(new Rectangle(ix, iy, itemWidth, itemHeight));
            item.render(g, cookies);
        }

        bs.show();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;
        double delta = 0;
        requestFocus();
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                update();
                delta--;
            }
            render();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mouseX = e.getX() - 1;
        int mouseY = e.getY() - 31;

        // Cookie click
        if ((mouseX - cx) * (mouseX - cx) + (mouseY - cy) * (mouseY - cy) <= cookieSize * cookieSize / 4) {
            cookies++;
            cookiePressedFrames = 100;
        }

        // Item purchases
        for (ClickableItem item : items) {
            if (item.getRect().contains(mouseX, mouseY) && cookies >= item.getCost()) {
                cookies -= item.getCost();
                item.purchase();
            }
        }
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        new CookieClicker();
    }
}