// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Nest display.

package morphognosis.moxworx;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import morphognosis.Orientation;
import morphognosis.SectorDisplay;

public class NestDisplay extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Moxen.
   ArrayList<NestingMox> moxen;

   // Moxen dashboards.
   ArrayList<NestingMoxDashboard> moxenDashboards;
   int currentMox;

   // Nest cells.
   NestCells nestCells;

   // Dimensions.
   static final Dimension DISPLAY_SIZE = new Dimension(600, 700);

   // Mox display.
   MoxDisplay display;

   // Controls.
   MoxControls controls;

   // Step frequency (ms).
   static final int MIN_STEP_DELAY = 0;
   static final int MAX_STEP_DELAY = 1000;
   int              stepDelay      = MAX_STEP_DELAY;

   // Quit.
   boolean quit;

   // Constructors.
   public NestDisplay(NestCells nestCells, ArrayList<NestingMox> moxen)
   {
      this.nestCells = nestCells;
      setMoxen(moxen);
      currentMox = -1;
      init();
   }


   public NestDisplay(NestCells nestCells)
   {
      this.nestCells = nestCells;
      moxen          = new ArrayList<NestingMox>();
      setMoxen(moxen);
      currentMox = -1;
      init();
   }


   // Initialize.
   void init()
   {
      // Set up display.
      setTitle("Nest");
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e)
                           {
                              close();
                              quit = true;
                           }
                        }
                        );
      setBounds(0, 0, DISPLAY_SIZE.width, DISPLAY_SIZE.height);
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());

      // Create display.
      Dimension displaySize = new Dimension(DISPLAY_SIZE.width,
                                            (int)((double)DISPLAY_SIZE.height * .8));
      display = new MoxDisplay(displaySize);
      basePanel.add(display, BorderLayout.NORTH);

      // Create controls.
      controls = new MoxControls();
      basePanel.add(controls, BorderLayout.SOUTH);

      // Make display visible.
      pack();
      setCenterLocation();
      setVisible(true);
   }


   void setCenterLocation()
   {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int       w   = getSize().width;
      int       h   = getSize().height;
      int       x   = (dim.width - w) / 2;
      int       y   = (dim.height - h) / 2;

      setLocation(x, y);
   }


   // Set moxen.
   void setMoxen(ArrayList<NestingMox> moxen)
   {
      // Close previous dashboards.
      close();

      // Create moxen dashboards.
      this.moxen      = moxen;
      moxenDashboards = new ArrayList<NestingMoxDashboard>();
      NestingMoxDashboard moxDashboard;
      for (int i = 0; i < moxen.size(); i++)
      {
         moxDashboard = new NestingMoxDashboard(moxen.get(i), this);
         moxenDashboards.add(i, moxDashboard);
      }
   }


   // Close.
   void close()
   {
      currentMox = -1;
      if (moxenDashboards != null)
      {
         for (int i = 0; i < moxenDashboards.size(); i++)
         {
            moxenDashboards.get(i).close();
         }
      }
      setVisible(false);
   }


   // Update display.
   public void update(int steps)
   {
      controls.updateStepCounter(steps);
      update();
   }


   private int timer = 0;
   public void update()
   {
      if (quit) { return; }

      // Update moxen dashboards.
      for (int i = 0; i < moxenDashboards.size(); i++)
      {
         moxenDashboards.get(i).update();
      }

      // Update display.
      display.update();

      // Timer loop: count down delay by 1ms.
      for (timer = stepDelay; timer > 0 && !quit; )
      {
         try
         {
            Thread.sleep(1);
         }
         catch (InterruptedException e) {
            break;
         }

         display.update();

         if (stepDelay < MAX_STEP_DELAY)
         {
            timer--;
         }
      }
   }


   // Set step delay.
   void setStepDelay(int delay)
   {
      stepDelay = timer = delay;
   }


   // Step.
   void step()
   {
      setStepDelay(MAX_STEP_DELAY);
      controls.speedSlider.setValue(MAX_STEP_DELAY);
      timer = 0;
   }


   // Close mox dashboard callback.
   public void closeMoxDashboard()
   {
      currentMox = -1;
   }


   // Set message
   void setMessage(String message)
   {
      if (message == null)
      {
         controls.messageText.setText("");
      }
      else
      {
         controls.messageText.setText(message);
      }
   }


   // Mox display.
   public class MoxDisplay extends Canvas
   {
      private static final long serialVersionUID = 0L;

      final Color MOX_COLOR = Color.BLUE;

      // Buffered display.
      private Dimension canvasSize;
      private Graphics  graphics;
      private Image     image;
      private Graphics  imageGraphics;

      // Constructor.
      public MoxDisplay(Dimension canvasSize)
      {
         // Configure canvas.
         this.canvasSize = canvasSize;
         setBounds(0, 0, canvasSize.width, canvasSize.height);
         addMouseListener(new CanvasMouseListener());
         addMouseMotionListener(new CanvasMouseMotionListener());
      }


      // Update display.
      synchronized void update()
      {
         int   x;
         int   y;
         int   x2;
         int   y2;
         float cellWidth;
         float cellHeight;

         if (quit)
         {
            return;
         }

         if (graphics == null)
         {
            graphics      = getGraphics();
            image         = createImage(canvasSize.width, canvasSize.height);
            imageGraphics = image.getGraphics();
         }

         if (graphics == null)
         {
            return;
         }

         // Clear display.
         imageGraphics.setColor(Color.white);
         imageGraphics.fillRect(0, 0, canvasSize.width, canvasSize.height);

         cellWidth  = (float)canvasSize.width / (float)nestCells.size.width;
         cellHeight = (float)canvasSize.height / (float)nestCells.size.height;

         // Draw cells.
         for (x = x2 = 0; x < nestCells.size.width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (y = 0, y2 = canvasSize.height - (int)cellHeight;
                 y < nestCells.size.height;
                 y++, y2 = (int)(cellHeight * (double)(nestCells.size.height - (y + 1))))
            {
               Color color = SectorDisplay.getEventColor(NestCells.ELEVATION_CELL_INDEX,
                                                         nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX]);
               imageGraphics.setColor(color);
               imageGraphics.fillRect(x2, y2, (int)cellWidth + 1, (int)cellHeight + 1);
               if (nestCells.cells[x][y][NestCells.STONE_CELL_INDEX] == NestCells.STONE_CELL_VALUE)
               {
                  color = SectorDisplay.getEventColor(NestCells.STONE_CELL_INDEX,
                                                      nestCells.cells[x][y][NestCells.STONE_CELL_INDEX]);
                  imageGraphics.setColor(color);
                  imageGraphics.fillOval(x2, y2, (int)cellWidth, (int)cellHeight);
               }
            }
         }

         // Draw grid.
         imageGraphics.setColor(Color.black);
         y2 = canvasSize.height;
         for (x = 1, x2 = (int)cellWidth; x < nestCells.size.width;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            imageGraphics.drawLine(x2, 0, x2, y2);
         }
         x2 = canvasSize.width;
         for (y = 1, y2 = (int)cellHeight; y < nestCells.size.height;
              y++, y2 = (int)(cellHeight * (double)y))
         {
            imageGraphics.drawLine(0, y2, x2, y2);
         }
         imageGraphics.setColor(Color.black);

         // Draw moxen.
         NestingMox mox;
         int[] vx = new int[3];
         int[] vy = new int[3];
         for (int i = 0; i < moxen.size(); i++)
         {
            mox = (NestingMox)moxen.get(i);
            x2  = (int)(cellWidth * (double)mox.x);
            y2  = (int)(cellHeight * (double)(nestCells.size.height - (mox.y + 1)));
            imageGraphics.setColor(MOX_COLOR);
            if (mox.direction == Orientation.NORTH)
            {
               vx[0] = x2 + (int)(cellWidth * 0.5f);
               vy[0] = y2;
               vx[1] = x2;
               vy[1] = y2 + (int)cellHeight;
               vx[2] = x2 + (int)cellWidth;
               vy[2] = y2 + (int)cellHeight;
            }
            else if (mox.direction == Orientation.EAST)
            {
               vx[0] = x2 + (int)(cellWidth);
               vy[0] = y2 + (int)(cellHeight * 0.5f);
               vx[1] = x2;
               vy[1] = y2;
               vx[2] = x2;
               vy[2] = y2 + (int)cellHeight;
            }
            else if (mox.direction == Orientation.SOUTH)
            {
               vx[0] = x2 + (int)(cellWidth * 0.5f);
               vy[0] = y2 + (int)cellHeight;
               vx[1] = x2;
               vy[1] = y2;
               vx[2] = x2 + (int)cellWidth;
               vy[2] = y2;
            }
            else
            {
               vx[0] = x2;
               vy[0] = y2 + (int)(cellHeight * 0.5f);
               vx[1] = x2 + (int)cellWidth;
               vy[1] = y2;
               vx[2] = x2 + (int)cellWidth;
               vy[2] = y2 + (int)cellHeight;
            }
            imageGraphics.fillPolygon(vx, vy, 3);
            if (i == currentMox)
            {
               imageGraphics.setColor(Color.RED);
               imageGraphics.drawPolygon(vx, vy, 3);
            }
            if (mox.hasStone)
            {
               Color color = SectorDisplay.getEventColor(NestCells.STONE_CELL_INDEX,
                                                         NestCells.STONE_CELL_VALUE);
               imageGraphics.setColor(color);
               x2  = (int)(cellWidth * (double)mox.x);
               x2 += (int)(cellWidth * 0.25f);
               y2  = (int)(cellHeight * (double)(nestCells.size.height - (mox.y + 1)));
               y2 += (int)(cellHeight * 0.25f);
               imageGraphics.fillOval(x2, y2, (int)(cellWidth * 0.5f), (int)(cellHeight * 0.5f));
            }
         }

         // Refresh display.
         graphics.drawImage(image, 0, 0, this);
      }


      // Canvas mouse listener.
      class CanvasMouseListener extends MouseAdapter
      {
         // Mouse pressed.
         public void mousePressed(MouseEvent evt)
         {
            int        i;
            int        x;
            int        y;
            double     cellWidth  = (double)canvasSize.width / (double)nestCells.size.width;
            double     cellHeight = (double)canvasSize.height / (double)nestCells.size.height;
            NestingMox mox;
            boolean    moxSelected;

            x = (int)((double)evt.getX() / cellWidth);
            y = nestCells.size.height - (int)((double)evt.getY() / cellHeight) - 1;

            if ((x >= 0) && (x < nestCells.size.width) &&
                (y >= 0) && (y < nestCells.size.height))
            {
               // Selecting mox?
               moxSelected = false;
               for (i = 0; i < moxen.size(); i++)
               {
                  mox = moxen.get(i);
                  if ((mox.x == x) && (mox.y == y))
                  {
                     moxSelected = true;
                     if (currentMox == -1)
                     {
                        currentMox = i;
                        moxenDashboards.get(currentMox).open();
                     }
                     else
                     {
                        if (i == currentMox)
                        {
                           moxenDashboards.get(currentMox).close();
                           currentMox = -1;
                        }
                        else
                        {
                           moxenDashboards.get(currentMox).close();
                           currentMox = i;
                           moxenDashboards.get(currentMox).open();
                        }
                     }
                     break;
                  }
               }
               if (!moxSelected && (currentMox != -1))
               {
                  moxenDashboards.get(currentMox).close();
                  currentMox = -1;
               }

               // Refresh display.
               update();
            }
         }
      }

      // Canvas mouse motion listener.
      class CanvasMouseMotionListener extends MouseMotionAdapter
      {
         // Mouse dragged.
         public void mouseDragged(MouseEvent evt)
         {
         }
      }
   }

   // Control panel.
   class MoxControls extends JPanel implements ActionListener, ChangeListener
   {
      private static final long serialVersionUID = 0L;

      // Components.
      JButton    resetButton;
      JLabel     stepCounter;
      JSlider    speedSlider;
      JButton    stepButton;
      JTextField messageText;

      // Constructor.
      MoxControls()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createRaisedBevelBorder());

         JPanel panel = new JPanel();
         resetButton = new JButton("Reset");
         resetButton.addActionListener(this);
         panel.add(resetButton);
         panel.add(new JLabel("Speed:   Fast", Label.RIGHT));
         speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_STEP_DELAY,
                                   MAX_STEP_DELAY, MAX_STEP_DELAY);
         speedSlider.addChangeListener(this);
         panel.add(speedSlider);
         panel.add(new JLabel("Stop", Label.LEFT));
         stepButton = new JButton("Step");
         stepButton.addActionListener(this);
         panel.add(stepButton);
         stepCounter = new JLabel("");
         panel.add(stepCounter);
         add(panel, BorderLayout.NORTH);
         panel       = new JPanel();
         messageText = new JTextField("", 40);
         messageText.setEditable(false);
         panel.add(messageText);
         add(panel, BorderLayout.SOUTH);
      }


      // Update step counter display
      void updateStepCounter(int steps)
      {
         stepCounter.setText("Steps: " + steps);
      }


      // Speed slider listener.
      public void stateChanged(ChangeEvent evt)
      {
         setStepDelay(speedSlider.getValue());
      }


      // Step button listener.
      public void actionPerformed(ActionEvent evt)
      {
         // Reset?
         if (evt.getSource() == (Object)resetButton)
         {
            currentMox = -1;
            nestCells.restore();
            int numMoxen = moxen.size();
            for (int i = 0; i < numMoxen; i++)
            {
               moxen.get(i).reset();
               moxenDashboards.get(i).update();
            }

            return;
         }

         // Step?
         if (evt.getSource() == (Object)stepButton)
         {
            step();

            return;
         }
      }
   }
}
