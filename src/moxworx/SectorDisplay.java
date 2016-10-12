// For conditions of distribution and use, see copyright notice in MoxWorx.java

package moxworx;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.*;

public class SectorDisplay extends JFrame implements Runnable
{
   private static final long serialVersionUID = 0L;

   // Targets.
   MoxDashboard.MorphognosticPanel dashboard;
   Mox           mox;
   Morphognostic morphognostic;

   // Neighborhood and sector.
   int neighborhoodIndex;
   int sectorXindex, sectorYindex;
   Morphognostic.Neighborhood.Sector sector;

   // Display.
   static final int       DISPLAY_UPDATE_DELAY_MS = 50;
   static final Dimension displaySize             = new Dimension(275, 200);
   static final Dimension canvasSize = new Dimension(275, 175);
   Canvas                 canvas;
   Graphics               canvasGraphics;
   Image     image;
   Graphics  imageGraphics;
   Dimension imageSize;
   Thread    displayThread;
   enum DISPLAY_MODE { DENSITIES, LANDMARKS };
   DISPLAY_MODE displayMode;

   // Constructor.
   public SectorDisplay(MoxDashboard.MorphognosticPanel dashboard,
                        Mox mox, int neighborhoodIndex, int sectorXindex, int sectorYindex)
   {
      this.dashboard         = dashboard;
      this.mox               = mox;
      morphognostic          = mox.morphognostic;
      this.neighborhoodIndex = neighborhoodIndex;
      this.sectorXindex      = sectorXindex;
      this.sectorYindex      = sectorYindex;
      sector = morphognostic.neighborhoods.get(neighborhoodIndex).sectors[sectorXindex][sectorYindex];

      setTitle("M=" + mox.id + " N=" + neighborhoodIndex +
               " S=[" + sectorXindex + "," + sectorYindex + "]");
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e) { close(); }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());
      canvas = new Canvas();
      canvas.setBounds(0, 0, canvasSize.width, canvasSize.height);
      basePanel.add(canvas, BorderLayout.NORTH);
      JPanel modePanel = new JPanel();
      modePanel.setLayout(new FlowLayout());
      basePanel.add(modePanel, BorderLayout.SOUTH);
      JRadioButton densities = new JRadioButton("Densities", true);
      displayMode = DISPLAY_MODE.DENSITIES;
      densities.addActionListener(new ActionListener()
                                  {
                                     @Override
                                     public void actionPerformed(ActionEvent e)
                                     {
                                        displayMode = DISPLAY_MODE.DENSITIES;
                                     }
                                  }
                                  );
      JRadioButton landmarks = new JRadioButton("Landmarks");
      landmarks.addActionListener(new ActionListener()
                                  {
                                     @Override
                                     public void actionPerformed(ActionEvent e)
                                     {
                                        displayMode = DISPLAY_MODE.LANDMARKS;
                                     }
                                  }
                                  );
      ButtonGroup modeGroup = new ButtonGroup();
      modeGroup.add(densities);
      modeGroup.add(landmarks);
      modePanel.add(densities);
      modePanel.add(landmarks);
      pack();
      setVisible(false);

      // Get canvas image.
      canvasGraphics = canvas.getGraphics();
      image          = createImage(canvasSize.width, canvasSize.height);
      imageGraphics  = image.getGraphics();
      imageSize      = canvasSize;

      // Create display thread.
      displayThread = new Thread(this);
      displayThread.setPriority(Thread.MIN_PRIORITY);
      displayThread.start();
   }


   // Open display.
   void open()
   {
      setVisible(true);
   }


   // Close display.
   void close()
   {
      setVisible(false);
      dashboard.closeDisplay(neighborhoodIndex, sectorXindex, sectorYindex);
   }


   // Run.
   public void run()
   {
      // Display update loop.
      while (Thread.currentThread() == displayThread &&
             !displayThread.isInterrupted())
      {
         updateDisplay();

         try
         {
            Thread.sleep(DISPLAY_UPDATE_DELAY_MS);
         }
         catch (InterruptedException e) {
            break;
         }
      }
   }


   // Update display.
   public void updateDisplay()
   {
      imageGraphics.setColor(Color.gray);
      imageGraphics.fillRect(0, 0, imageSize.width, imageSize.height);
      Random random = new Random();

      if (displayMode == DISPLAY_MODE.DENSITIES)
      {
         // Draw type density histogram.
         int n = morphognostic.numLandmarkTypes;
         int w = imageSize.width / n;
         for (int i = 0, x = 0; i < n; i++, x += w)
         {
            if (i == MoxCells.EMPTY_CELL_VALUE)
            {
               imageGraphics.setColor(MoxCells.EMPTY_CELL_COLOR);
            }
            else
            {
               random.setSeed(i + MoxCells.OBSTACLE_CELLS_BEGIN_VALUE - 1);
               float r     = random.nextFloat();
               float g     = random.nextFloat();
               float b     = random.nextFloat();
               Color color = new Color(r, g, b);
               imageGraphics.setColor(color);
            }
            float h = (float)imageSize.height * sector.getTypeDensity(i);
            imageGraphics.fillRect(x, (int)(imageSize.height - h), w + 1, (int)h);
         }
         imageGraphics.setColor(Color.black);
         for (int i = 0, j = n - 1, x = w; i < j; i++, x += w)
         {
            imageGraphics.drawLine(x, 0, x, imageSize.height);
         }
      }
      else
      {
         // Draw landmarks.
         float cellWidth  = (float)imageSize.width / (float)sector.landmarks.length;
         float cellHeight = (float)imageSize.height / (float)sector.landmarks.length;
         for (int x = 0, x2 = 0; x < sector.landmarks.length;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            for (int y = 0, y2 = imageSize.height - (int)cellHeight;
                 y < sector.landmarks.length;
                 y++, y2 = (int)(cellHeight * (double)(sector.landmarks.length - (y + 1))))
            {
               switch (sector.landmarks[x][y])
               {
               case -1:
                  imageGraphics.setColor(Color.gray);
                  imageGraphics.fillRect(x2, y2, (int)cellWidth + 1,
                                         (int)cellHeight + 1);
                  break;

               case MoxCells.EMPTY_CELL_VALUE:
                  imageGraphics.setColor(MoxCells.EMPTY_CELL_COLOR);
                  imageGraphics.fillRect(x2, y2, (int)cellWidth + 1,
                                         (int)cellHeight + 1);
                  break;

               default:
                  random.setSeed(sector.landmarks[x][y]);
                  float r     = random.nextFloat();
                  float g     = random.nextFloat();
                  float b     = random.nextFloat();
                  Color color = new Color(r, g, b);
                  imageGraphics.setColor(color);
                  imageGraphics.fillRect(x2, y2, (int)cellWidth + 1,
                                         (int)cellHeight + 1);
                  break;
               }
            }
         }
         imageGraphics.setColor(Color.black);
         int h = imageSize.height;
         for (int x = 1, x2 = (int)cellWidth; x < sector.landmarks.length;
              x++, x2 = (int)(cellWidth * (double)x))
         {
            imageGraphics.drawLine(x2, 0, x2, h);
         }
         int w = imageSize.width;
         for (int y = 1, y2 = (int)cellHeight; y < sector.landmarks.length;
              y++, y2 = (int)(cellHeight * (double)y))
         {
            imageGraphics.drawLine(0, y2, w, y2);
         }
      }

      canvasGraphics.drawImage(image, 0, 0, this);
   }
}
