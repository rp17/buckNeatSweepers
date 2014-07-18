/**
 * @author Petr (http://www.sallyx.org/)
 */
package sweepers.sim;

import static sweepers.utils.Script1.MyMenuBar;
import static sweepers.utils.Constants.FrameRate;
import static sweepers.utils.Constants.WindowHeight;
import static sweepers.utils.Constants.WindowWidth;
import static sweepers.sim.Raven_ObjectEnumerations.*;
import static sweepers.utils.resource.*;
import static sweepers.sim.Raven_UserOptions.UserOptions;
import static common.misc.WindowUtils.Window;
import static common.windows.MAKEPOINTS;
import common.Time.PrecisionTimer;
import static common.misc.Cgdi.gdi;
import common.misc.CppToJava;
import static common.misc.WindowUtils.CheckMenuItemAppropriately;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class Main {

    public static void TODO(String todo) {
    }

    //--------------------------------- Globals ------------------------------
    //------------------------------------------------------------------------
    static String g_szApplicationName = "NEAT sweepers";
    //static String g_szWindowClassName = "MyWindowClass";
    static Lock SimLock = new ReentrantLock();
    static CController g_pContr;

    /**
     * used when a user clicks on a menu item to ensure the option is 'checked'
     * correctly
     */
    static void CheckAllMenuItemsAppropriately(MyMenuBar hwnd) {
        //make sure the menu items are ticked/unticked accordingly
        CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SHOW_NAVGRAPH, UserOptions.m_bShowGraph);
        CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SHOW_PATH, UserOptions.m_bShowPathOfSelectedBot);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_IDS, UserOptions.m_bShowBotIDs);
        CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SMOOTH_PATHS_QUICK, UserOptions.m_bSmoothPathsQuick);
        CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SMOOTH_PATHS_PRECISE, UserOptions.m_bSmoothPathsPrecise);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_HEALTH, UserOptions.m_bShowBotHealth);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_TARGET, UserOptions.m_bShowTargetOfSelectedBot);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_FOV, UserOptions.m_bOnlyShowBotsInTargetsFOV);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_SCORES, UserOptions.m_bShowScore);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_GOAL_Q, UserOptions.m_bShowGoalsOfSelectedBot);
        CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SHOW_INDICES, UserOptions.m_bShowNodeIndices);
        CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_SENSED, UserOptions.m_bShowOpponentsSensedBySelectedBot);
    }

    public static void HandleMenuItems(int ID, MyMenuBar hwnd) {
        switch (ID) {
            case IDM_GAME_LOAD:
                final JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        return f.getName().endsWith(".map");
                    }

                    @Override
                    public String getDescription() {
                        return "Map (*.map)";
                    }
                });
                java.io.File currentDir = new java.io.File(".");
                fc.setCurrentDirectory(currentDir);
                int returnVal = fc.showOpenDialog(hwnd.getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    SimLock.lock();
                    g_pContr.loadMap(fc.getSelectedFile().getAbsolutePath());
                    SimLock.unlock();
                }

                /*
                 FileOpenDlg(hwnd, szFileName, szTitleName, "Raven map file (*.map)", "map");

                 debug_con << "Filename: " << szTitleName << "";

                 if (strlen(szTitleName) > 0)
                 {
                 g_pContr->LoadMap(szTitleName);
                 }
                 **/
                break;
            /*
            case IDM_GAME_ADDBOT:
                SimLock.lock();
                g_pContr.AddBots(1);
                SimLock.unlock();
                break;

            case IDM_GAME_REMOVEBOT:
                SimLock.lock();
                g_pContr.RemoveBot();
                SimLock.unlock();
                break;
            */
                
            case IDM_GAME_PAUSE:
                g_pContr.togglePause();
                break;
                
            /*
            case IDM_NAVIGATION_SHOW_NAVGRAPH:
                UserOptions.m_bShowGraph = !UserOptions.m_bShowGraph;
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SHOW_NAVGRAPH, UserOptions.m_bShowGraph);
                break;

            case IDM_NAVIGATION_SHOW_PATH:
                UserOptions.m_bShowPathOfSelectedBot = !UserOptions.m_bShowPathOfSelectedBot;
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SHOW_PATH, UserOptions.m_bShowPathOfSelectedBot);
                break;

            case IDM_NAVIGATION_SHOW_INDICES:
                UserOptions.m_bShowNodeIndices = !UserOptions.m_bShowNodeIndices;
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SHOW_INDICES, UserOptions.m_bShowNodeIndices);
                break;

            case IDM_NAVIGATION_SMOOTH_PATHS_QUICK:
                UserOptions.m_bSmoothPathsQuick = !UserOptions.m_bSmoothPathsQuick;
                UserOptions.m_bSmoothPathsPrecise = false;
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SMOOTH_PATHS_PRECISE, UserOptions.m_bSmoothPathsPrecise);
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SMOOTH_PATHS_QUICK, UserOptions.m_bSmoothPathsQuick);
                break;

            case IDM_NAVIGATION_SMOOTH_PATHS_PRECISE:
                UserOptions.m_bSmoothPathsPrecise = !UserOptions.m_bSmoothPathsPrecise;
                UserOptions.m_bSmoothPathsQuick = false;
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SMOOTH_PATHS_QUICK, UserOptions.m_bSmoothPathsQuick);
                CheckMenuItemAppropriately(hwnd, IDM_NAVIGATION_SMOOTH_PATHS_PRECISE, UserOptions.m_bSmoothPathsPrecise);
                break;

            case IDM_BOTS_SHOW_IDS:
                UserOptions.m_bShowBotIDs = !UserOptions.m_bShowBotIDs;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_IDS, UserOptions.m_bShowBotIDs);
                break;

            case IDM_BOTS_SHOW_HEALTH:
                UserOptions.m_bShowBotHealth = !UserOptions.m_bShowBotHealth;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_HEALTH, UserOptions.m_bShowBotHealth);
                break;

            case IDM_BOTS_SHOW_TARGET:
                UserOptions.m_bShowTargetOfSelectedBot = !UserOptions.m_bShowTargetOfSelectedBot;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_TARGET, UserOptions.m_bShowTargetOfSelectedBot);
                break;

            case IDM_BOTS_SHOW_SENSED:
                UserOptions.m_bShowOpponentsSensedBySelectedBot = !UserOptions.m_bShowOpponentsSensedBySelectedBot;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_SENSED, UserOptions.m_bShowOpponentsSensedBySelectedBot);
                break;

            case IDM_BOTS_SHOW_FOV:
                UserOptions.m_bOnlyShowBotsInTargetsFOV = !UserOptions.m_bOnlyShowBotsInTargetsFOV;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_FOV, UserOptions.m_bOnlyShowBotsInTargetsFOV);
                break;

            case IDM_BOTS_SHOW_SCORES:
                UserOptions.m_bShowScore = !UserOptions.m_bShowScore;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_SCORES, UserOptions.m_bShowScore);
                break;

            case IDM_BOTS_SHOW_GOAL_Q:
                UserOptions.m_bShowGoalsOfSelectedBot = !UserOptions.m_bShowGoalsOfSelectedBot;
                CheckMenuItemAppropriately(hwnd, IDM_BOTS_SHOW_GOAL_Q, UserOptions.m_bShowGoalsOfSelectedBot);
                break;
            */
        }
    }
    static BufferedImage buffer;
    static Graphics2D hdcBackBuffer;
    //these hold the dimensions of the client window area
    static int cxClient;
    static int cyClient;

    public static void main(String[] args) {
        final Window hWnd = new Window(g_szApplicationName);
        CppToJava.windowCache = hWnd;
        hWnd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        buffer = new BufferedImage(WindowWidth, WindowHeight, BufferedImage.TYPE_INT_RGB);
        hdcBackBuffer = buffer.createGraphics();
        //these hold the dimensions of the client window area
        cxClient = buffer.getWidth();
        cyClient = buffer.getHeight();
        //seed random number generator
        common.misc.utils.setSeed(0);

        hWnd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        //Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        hWnd.setResizable(false);

        int y = center.y - hWnd.getHeight() / 2;
        hWnd.setLocation(center.x - hWnd.getWidth() / 2, y >= 0 ? y : 0);
        sweepers.utils.Script1.MyMenuBar menu = sweepers.utils.Script1.createMenu(IDR_MENU1);
        hWnd.setJMenuBar(menu);

        CheckAllMenuItemsAppropriately(menu);

        final JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                gdi.StartDrawing(hdcBackBuffer);
                //fill our backbuffer with white
                gdi.fillRect(Color.WHITE, 0, 0, WindowWidth, WindowHeight);
                SimLock.lock();
                g_pContr.render();
                SimLock.unlock();
                gdi.StopDrawing(hdcBackBuffer);
                g.drawImage(buffer, 0, 0, null);
            }
        };
        panel.setSize(WindowWidth, WindowHeight);
        panel.setPreferredSize(new Dimension(WindowWidth, WindowHeight));
        hWnd.addPanel(panel);
        hWnd.pack();
        
        g_pContr = new CController();
        
        hWnd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                CppToJava.keyCache.released(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE: {
                        System.exit(0);
                    }
                    break;
                    case KeyEvent.VK_R: {
                        SimLock.lock();
                        g_pContr = null;
                        g_pContr = new CController();
                        //JMenuBar bar = sweepers.utils.Script1.createMenu(IDR_MENU1);
                        //hWnd.setJMenuBar(bar);
                        //bar.revalidate();
                        SimLock.unlock();
                    }
                    break;

                    case KeyEvent.VK_P: {
                        g_pContr.togglePause();
                    }
                    break;
                    /*
                    case KeyEvent.VK_1:
                        g_pContr.ChangeWeaponOfPossessedBot(type_blaster);
                        break;

                    case KeyEvent.VK_2:
                        g_pContr.ChangeWeaponOfPossessedBot(type_shotgun);
                        break;

                    case KeyEvent.VK_3:
                        g_pContr.ChangeWeaponOfPossessedBot(type_rocket_launcher);
                        break;

                    case KeyEvent.VK_4:
                        g_pContr.ChangeWeaponOfPossessedBot(type_rail_gun);
                        break;

                    case KeyEvent.VK_X:
                        g_pContr.ExorciseAnyPossessedBot();
                        break;

                    case KeyEvent.VK_UP:
                        SimLock.lock();
                        g_pContr.AddBots(1);
                        SimLock.unlock();
                        break;

                    case KeyEvent.VK_DOWN:
                        SimLock.lock();
                        g_pContr.RemoveBot();
                        SimLock.unlock();
                        break;
                        */
                }//end switch
            }

            @Override
            public void keyPressed(KeyEvent e) {
                CppToJava.keyCache.pressed(e);
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1: //left
                        g_pContr.ClickLeftMouseButton(MAKEPOINTS(e.getPoint()));
                        break;
                    case MouseEvent.BUTTON2: //middle
                    case MouseEvent.BUTTON3: //right
                        g_pContr.ClickRightMouseButton(MAKEPOINTS(e.getPoint()));
                        break;
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                CppToJava.mouseCache.moved(e);
            }
        });
        
        hWnd.addComponentListener(new ComponentAdapter() {
            @Override //has the user resized the client area?
            public void componentResized(ComponentEvent e) {
                //if so we need to update our variables so that any drawing
                //we do using cxClient and cyClient is scaled accordingly
                cxClient = e.getComponent().getBounds().width;
                cyClient = e.getComponent().getBounds().height;
                //now to resize the backbuffer accordingly. 
                buffer = new BufferedImage(cxClient, cyClient, BufferedImage.TYPE_INT_RGB);
                hdcBackBuffer = buffer.createGraphics();
            }
        });

        //make the window visible
        hWnd.setVisible(true);

        //timer.SmoothUpdatesOn();
        //create a timer
        PrecisionTimer timer = new PrecisionTimer(FrameRate);

        //start the timer
        timer.Start();

        //enter the message loop
        boolean bDone = false;

        while (!bDone) {
            if (timer.ReadyForNextFrame()) {
                SimLock.lock();
                g_pContr.update();
                SimLock.unlock();

                //render 
                panel.repaint();
                
                try {
                    //System.out.println(timer.TimeElapsed());
                    //give the OS a little time
                    Thread.sleep(2);
                } catch (InterruptedException ex) {
                }
            }
        }//end while

    }
}
