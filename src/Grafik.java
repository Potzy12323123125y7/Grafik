import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Grafik extends Canvas implements Runnable{
    int x, y;
    int x1 = 400;
    int y1 = 300;
    double angle = 0;
    BufferStrategy bs;
    int width = 800;
    int height = 600;
    BufferedImage img = null;
    // Genom att lägga uppdaterinegn av skärmen i en egen tråd kan vi styra exakt med vilken hastighet den ska
    // renderas
    private Thread thread;
    private boolean running = false;

    public Grafik() {
        try {
            img = ImageIO.read(new File("ddds.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSize(width, height);
        JFrame frame = new JFrame("Grafik version 4");
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Start, stop och run är metoder som kommer från Runnable. Det gör att vi kan starta en ny processortråd
     * som kör runmetoden där vi kan rita upp skärmen
     */
    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* Eftersom vi vill ta kontroll över hur snabbt vår animering updateras bestämmer vi en tid mellan varje uppdatering
       Tiden mellan två uppdateringar blir 1 /30 sekund (30 ups eller fps). Delta anger i hur nära vi är en ny uppdatering.
       När delta blir 1 är det dags att rita igen. delta nollställs inte eftersom det kan hända att något tagit lång tid
       och att vi måste göra flera uppdateringar efter varandra.

       Här ligger update och render i samma tidssteg. Det går att separera dessa. Egentligen kan vi rita ut hur fort som
       helst (lägga render utanför while(delta>1)) Det viktiga är att update anropas med konstant hastighet eftersom det
       är den som simulerar tiden i animeringar.
     */
    public void run() {
        double ns = 1000000000.0 / 1024.0;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while(delta >= 1) {
                // Uppdatera koordinaterna
                update();
                // Rita ut bilden med updaterad data
                render();
                delta--;
            }
        }
        stop();
    }

    /**
     * Eftersom vi inte längre behöver paint och repaint döper jag om metoden till render
     */
    public void render() {
        bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        // Rita ut den nya bilden
        draw(g);
        g.dispose();
        bs.show();
    }

    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0,0,800,600);
        drawHouse(400, 400, g);
        drawHouse(460, 400, g);
        drawHouse(520, 400, g);
        drawHouse(x, y, g);
        g.drawImage(img,400,0,null);
        g.setColor(new Color(0x00FF00));
        g.fillOval(x1, y1, 40, 40);
    }

    // Uppdatera x och y-koordinaterna. Ger en cirkel
    private void update() {
        x = 200 + (int) (100 * Math.cos(angle));
        y = 200 + (int) (100 * Math.sin(angle));
        angle += 2 * Math.PI / 720;
    }

    // Rita ett litet hus på koordinaterna (x,y)
    private void drawHouse(int x, int y, Graphics g) {
        g.setColor(new Color(0xAA1111));
        g.fillRect(x, y, 50, 50);
        g.setColor(new Color(0x444444));
        int[] xcoords = {x, x + 25, x + 50};
        int[] ycoords = {y, y - 50, y};
        g.fillPolygon(xcoords, ycoords, 3);
    }

    public static void main(String[] args) {
        GrafikV4 minGrafik = new GrafikV4();
        minGrafik.start();
    }
}
