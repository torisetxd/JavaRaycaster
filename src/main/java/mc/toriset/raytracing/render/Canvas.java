package mc.toriset.raytracing.render;

import mc.toriset.raytracing.data.Config;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Canvas extends java.awt.Canvas implements Runnable {
    private final int virtualWidth;
    private final int virtualHeight;
    private final int windowWidth;
    private final int windowHeight;
    private final int scaleX;
    private final int scaleY;

    private JFrame frame;
    private BufferedImage virtualSurface;
    private boolean running;
    private Thread gameThread;
    private Color clearColor = Color.BLACK;

    private final Map<Integer, Runnable> keyHandlers = new HashMap<>();
    private Consumer<Canvas> updateHandler = canvas -> {};

    private boolean mouseLocked = false;
    private Robot robot;
    private Consumer<Point> mouseMoveHandler = delta -> {};
    private Point centerPoint = new Point(0, 0);
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    private BufferedImage cursorImg;
    private Cursor blankCursor;

    public Canvas(int virtualWidth, int virtualHeight, int windowWidth, int windowHeight) {
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.scaleX = windowWidth / virtualWidth;
        this.scaleY = windowHeight / virtualHeight;

        this.virtualSurface = new BufferedImage(virtualWidth, virtualHeight, BufferedImage.TYPE_INT_RGB);

        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setFocusable(true);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    toggleMouseLock();
                }

                Runnable handler = keyHandlers.get(e.getKeyCode());
                if (handler != null) {
                    handler.run();
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseMovement(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMovement(e);
            }
        });

        clear();
    }

    private void handleMouseMovement(MouseEvent e) {
        if (mouseLocked) {
            int currentX = e.getXOnScreen();
            int currentY = e.getYOnScreen();

            int deltaX = currentX - lastMouseX;
            int deltaY = currentY - lastMouseY;

            if (centerPoint.x != currentX || centerPoint.y != currentY) {
                robot.mouseMove(centerPoint.x, centerPoint.y);
            }

            lastMouseX = centerPoint.x;
            lastMouseY = centerPoint.y;

            if (deltaX != 0 || deltaY != 0) {
                mouseMoveHandler.accept(new Point(deltaX, deltaY));
            }
        } else {
            lastMouseX = e.getXOnScreen();
            lastMouseY = e.getYOnScreen();
        }
    }

    public void setMouseMoveHandler(Consumer<Point> handler) {
        this.mouseMoveHandler = handler;
    }

    public int getVirtualWidth() {
        return virtualWidth;
    }

    public int getVirtualHeight() {
        return virtualHeight;
    }

    public void setUpdateHandler(Consumer<Canvas> updateHandler) {
        this.updateHandler = updateHandler;
    }

    public void registerKeyHandler(int keyCode, Runnable handler) {
        keyHandlers.put(keyCode, handler);
    }

    public void setPixel(int x, int y, Color color) {
        if (x >= 0 && x < virtualWidth && y >= 0 && y < virtualHeight) {
            virtualSurface.setRGB(x, y, color.getRGB());
        }
    }

    public void clear() {
        clear(clearColor);
    }

    public void clear(Color color) {
        Graphics g = virtualSurface.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, virtualWidth, virtualHeight);
        g.dispose();
    }

    public void start() {
        frame = new JFrame(Config.WINDOW_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        centerPoint = new Point(
                frame.getX() + frame.getWidth() / 2,
                frame.getY() + frame.getHeight() / 2
        );

        lastMouseX = centerPoint.x;
        lastMouseY = centerPoint.y;

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                SwingUtilities.invokeLater(() -> {
                    centerPoint = new Point(
                            frame.getX() + frame.getWidth() / 2,
                            frame.getY() + frame.getHeight() / 2
                    );
                });
            }
        });

        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void toggleMouseLock() {
        mouseLocked = !mouseLocked;

        if (mouseLocked) {
            setCursor(blankCursor);
            robot.mouseMove(centerPoint.x, centerPoint.y);
            lastMouseX = centerPoint.x;
            lastMouseY = centerPoint.y;
        } else {
            setCursor(Cursor.getDefaultCursor());
        }

        System.out.println("Mouse lock: " + (mouseLocked ? "ON" : "OFF"));
    }

    public boolean isMouseLocked() {
        return mouseLocked;
    }

    public void setMouseLocked(boolean locked) {
        if (mouseLocked != locked) {
            toggleMouseLock();
        }
    }

    public void stop() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (frame != null) {
            frame.dispose();
        }
    }

    private void update() {
        Renderer.render(this);
        updateHandler.accept(this);
    }

    private void draw() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.drawImage(virtualSurface, 0, 0, windowWidth, windowHeight, null);
        g.dispose();
        bs.show();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / Config.FPS;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                update();
                draw();
                delta--;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}