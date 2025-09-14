import java.awt.*;

public class ClickableItem {
    private final String name;
    private final double baseCost;
    private double cost;
    private int count = 0;
    private final double cps;
    private Rectangle rect;

    public ClickableItem(String name, int x, int y, double baseCost, double cps) {
        this.name = name;
        this.baseCost = baseCost;
        this.cost = baseCost;
        this.cps = cps;
        this.rect = new Rectangle(x, y, 0, 0);
    }

    public void render(Graphics2D g, double totalCookies) {
        // Background gradient for nicer look
        Color start = totalCookies >= cost ? new Color(255, 220, 130) : new Color(200, 200, 200);
        Color end = totalCookies >= cost ? new Color(255, 200, 80) : new Color(180, 180, 180);
        GradientPaint gp = new GradientPaint(rect.x, rect.y, start, rect.x, rect.y + rect.height, end);
        g.setPaint(gp);
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

        // Border
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

        // Small circular icon
        g.setColor(new Color(255, 200, 50, 180));
        g.fillOval(rect.x + rect.width - 25, rect.y + 10, 15, 15);

        // Text: name
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(name, rect.x + 10, rect.y + 25);

        // Text: count
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("x" + count, rect.x + 10, rect.y + 45);

        // Text: cost
        g.drawString("Cost: " + (int)cost, rect.x + 10, rect.y + 65);
    }

    public void purchase() {
        count++;
        cost = baseCost + count * baseCost * 0.1; // update cost after charging previous cost
    }

    public void setRect(Rectangle r) {
        this.rect = r;
    }

    public Rectangle getRect() { return rect; }

    public int getCount() { return count; }

    public double getCost() { return cost; }

    public double getCPS() { return cps; }
}
