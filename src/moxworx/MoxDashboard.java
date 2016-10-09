// For conditions of distribution and use, see copyright notice in MoxWorx.java

//  Mox dashboard.

package moxworx;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Vector;
import javax.swing.*;

// Mox dashboard.
public class MoxDashboard extends JFrame
{
   // Components.
   SensorsResponsePanel sensorsResponse;
   DriverPanel          driver;
   MorphognosticPanel   morphognostic;

   // Targets.
   Mox              mox;
   MoxWorxDashboard worxDashboard;

   // Constructor.
   public MoxDashboard(Mox mox, MoxWorxDashboard worxDashboard)
   {
      this.mox           = mox;
      this.worxDashboard = worxDashboard;

      setTitle("Mox " + mox.id);
      addWindowListener(new WindowAdapter()
                        {
                           public void windowClosing(WindowEvent e) { close(); }
                        }
                        );
      JPanel basePanel = (JPanel)getContentPane();
      basePanel.setLayout(new BorderLayout());
      sensorsResponse = new SensorsResponsePanel();
      basePanel.add(sensorsResponse, BorderLayout.NORTH);
      driver = new DriverPanel();
      basePanel.add(driver, BorderLayout.CENTER);
      morphognostic = new MorphognosticPanel();
      basePanel.add(morphognostic, BorderLayout.SOUTH);
      pack();
      setCenterLocation();
      setVisible(false);
      update();
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


   // Update dashboard.
   void update()
   {
      float f = (float)((int)(mox.sensors[Mox.SENSOR_CONFIG.FOOD_SENSOR_INDEX.getValue()] * 100.0f)) / 100.0f;

      setSensors(mox.sensors[Mox.SENSOR_CONFIG.LANDMARK_SENSOR_INDEX.getValue()] + "", f + "");
      if (mox.response == Mox.WAIT)
      {
         setResponse("wait");
      }
      else if (mox.response == Mox.FORWARD)
      {
         setResponse("move forward");
      }
      else if (mox.response == Mox.RIGHT)
      {
         setResponse("turn right");
      }
      else if (mox.response == Mox.LEFT)
      {
         setResponse("turn left");
      }
      else if (mox.response == Mox.EAT)
      {
         setResponse("eat");
      }
      else
      {
         setResponse("");
      }
      setDriverChoice(mox.driverType);
   }


   // Open the dashboard.
   void open()
   {
      setVisible(true);
   }


   // Close the dashboard.
   void close()
   {
      morphognostic.close();
      setVisible(false);
      worxDashboard.closeMoxDashboard();
   }


   // Set sensors display.
   void setSensors(String landmarkSensorString,
                   String foodSensorString)
   {
      sensorsResponse.landmarkText.setText(landmarkSensorString);
      sensorsResponse.foodText.setText(foodSensorString);
   }


   // Set response display.
   void setResponse(String responseString)
   {
      sensorsResponse.responseText.setText(responseString);
   }


   // Sensors/Response panel.
   class SensorsResponsePanel extends JPanel
   {
      // Components.
      JTextField landmarkText;
      JTextField foodText;
      JTextField responseText;

      // Constructor.
      public SensorsResponsePanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black),
                      "Sensors/Response"));
         JPanel sensorsPanel = new JPanel();
         sensorsPanel.setLayout(new BorderLayout());
         add(sensorsPanel, BorderLayout.NORTH);
         JPanel landmarkPanel = new JPanel();
         landmarkPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(landmarkPanel, BorderLayout.NORTH);
         landmarkPanel.add(new JLabel("Landmark:"));
         landmarkText = new JTextField(10);
         landmarkText.setEditable(false);
         landmarkPanel.add(landmarkText);
         JPanel foodPanel = new JPanel();
         foodPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(foodPanel, BorderLayout.CENTER);
         foodPanel.add(new JLabel("Food:"));
         foodText = new JTextField(10);
         foodText.setEditable(false);
         foodPanel.add(foodText);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(responsePanel, BorderLayout.SOUTH);
         responsePanel.add(new JLabel("Response:"));
         responseText = new JTextField(10);
         responseText.setEditable(false);
         responsePanel.add(responseText);
      }
   }

   // Get driver choice.
   int getDriverChoice()
   {
      return(driver.driverChoice.getSelectedIndex());
   }


   // Set driver choice.
   void setDriverChoice(int driverChoice)
   {
      driver.driverChoice.select(driverChoice);
   }


   // Driver panel.
   class DriverPanel extends JPanel implements ItemListener, ActionListener
   {
      // Components.
      Choice  driverChoice;
      JButton turnLeftButton;
      JButton moveForwardButton;
      JButton turnRightButton;
      JButton eatButton;

      // Constructor.
      public DriverPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black), "Driver"));
         JPanel driverPanel = new JPanel();
         driverPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(driverPanel, BorderLayout.NORTH);
         driverPanel.add(new JLabel("Driver:"));
         driverChoice = new Choice();
         driverPanel.add(driverChoice);
         driverChoice.add("mox");
         driverChoice.add("auto");
         driverChoice.add("manual");
         driverChoice.addItemListener(this);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout());
         add(responsePanel, BorderLayout.SOUTH);
         turnLeftButton = new JButton("Left");
         turnLeftButton.addActionListener(this);
         responsePanel.add(turnLeftButton);
         moveForwardButton = new JButton("Forward");
         moveForwardButton.addActionListener(this);
         responsePanel.add(moveForwardButton);
         turnRightButton = new JButton("Right");
         turnRightButton.addActionListener(this);
         responsePanel.add(turnRightButton);
         eatButton = new JButton("Eat");
         eatButton.addActionListener(this);
         responsePanel.add(eatButton);
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent e)
      {
         mox.driverType = driverChoice.getSelectedIndex();
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == turnLeftButton)
         {
            mox.driverResponse = Mox.LEFT;
            return;
         }

         if ((JButton)evt.getSource() == moveForwardButton)
         {
            mox.driverResponse = Mox.FORWARD;
            return;
         }

         if ((JButton)evt.getSource() == turnRightButton)
         {
            mox.driverResponse = Mox.RIGHT;
            return;
         }

         if ((JButton)evt.getSource() == eatButton)
         {
            mox.driverResponse = Mox.EAT;
            return;
         }
      }
   }

   // Morphognostic panel.
   public class MorphognosticPanel extends JPanel implements ItemListener, ActionListener
   {
      // Components.
      Choice                neighborhoodChoice;
      Choice                sectorChoice;
      Vector<SectorDisplay> displays;
      JButton               showButton;
      JButton               hideButton;

      // Constructor.
      public MorphognosticPanel()
      {
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createTitledBorder(
                      BorderFactory.createLineBorder(Color.black), "Morphognostic"));
         JPanel morphognosticPanel = new JPanel();
         morphognosticPanel.setLayout(new BorderLayout());
         add(morphognosticPanel);
         JPanel neighborhoodPanel = new JPanel();
         neighborhoodPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         morphognosticPanel.add(neighborhoodPanel, BorderLayout.NORTH);
         neighborhoodPanel.add(new JLabel("Neighborhood:"));
         neighborhoodChoice = new Choice();
         for (int i = 0, j = mox.morphognostic.neighborhoods.size(); i < j; i++)
         {
            neighborhoodChoice.add(i + " ");
         }
         neighborhoodChoice.addItemListener(this);
         neighborhoodPanel.add(neighborhoodChoice);
         neighborhoodPanel.add(new JLabel("Sector:"));
         sectorChoice = new Choice();
         int n = neighborhoodChoice.getSelectedIndex();
         if (n != -1)
         {
            sectorChoice.add("all");
            for (int x = 0, d = mox.morphognostic.neighborhoods.get(n).sectors.length; x < d; x++)
            {
               for (int y = 0; y < d; y++)
               {
                  sectorChoice.add("[" + x + "," + y + "] ");
               }
            }
         }
         neighborhoodPanel.add(sectorChoice);
         JPanel showPanel = new JPanel();
         showPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         morphognosticPanel.add(showPanel, BorderLayout.SOUTH);
         showButton = new JButton("Show");
         showButton.addActionListener(this);
         showPanel.add(showButton);
         hideButton = new JButton("Hide");
         hideButton.addActionListener(this);
         showPanel.add(hideButton);
         displays = new Vector<SectorDisplay>();
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         if ((Choice)evt.getSource() == neighborhoodChoice)
         {
            int n = neighborhoodChoice.getSelectedIndex();
            sectorChoice.removeAll();
            if (n != -1)
            {
               sectorChoice.add("all");
               for (int x = 0, d = mox.morphognostic.neighborhoods.get(n).sectors.length; x < d; x++)
               {
                  for (int y = 0; y < d; y++)
                  {
                     sectorChoice.add("[" + x + "," + y + "] ");
                  }
               }
            }
         }
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == showButton)
         {
            showSectors();
         }
         else if ((JButton)evt.getSource() == hideButton)
         {
            hideSectors();
         }
      }


      // Show sectors.
      void showSectors()
      {
         int n = neighborhoodChoice.getSelectedIndex();
         int s = sectorChoice.getSelectedIndex();

         if ((n != -1) && (s != -1))
         {
            int x = -1;
            int y = -1;
            if (s != 0)
            {
               String i = sectorChoice.getItem(s).trim();
               i = i.substring(1, i.length() - 1);
               String d = ",";
               String[] j = i.split(d);
               x          = Integer.parseInt(j[0]);
               y          = Integer.parseInt(j[1]);
            }
            Dimension dim  = Toolkit.getDefaultToolkit().getScreenSize();
            int       offx = new Random().nextInt((int)((float)dim.width * 0.5f));
            int       offy = new Random().nextInt((int)((float)dim.height * 0.5f));
            for (int x2 = 0, d = mox.morphognostic.neighborhoods.get(n).sectors.length; x2 < d; x2++)
            {
               for (int y2 = 0; y2 < d; y2++)
               {
                  if ((s == 0) || ((x2 == x) && (y2 == y)))
                  {
                     boolean found = false;
                     for (SectorDisplay display : displays)
                     {
                        if ((display.neighborhoodIndex == n) &&
                            (display.sectorXindex == x2) &&
                            (display.sectorYindex == y2))
                        {
                           found = true;
                           break;
                        }
                     }
                     if (!found)
                     {
                        SectorDisplay display = new SectorDisplay(this, mox, n, x2, y2);
                        displays.add(display);
                        int w = display.getSize().width;
                        int h = display.getSize().height;
                        display.setLocation(offx + (x2 * w), offy + (y2 * h));
                        display.open();
                     }
                  }
               }
            }
         }
      }


      // Hide sectors.
      void hideSectors()
      {
         int n = neighborhoodChoice.getSelectedIndex();
         int s = sectorChoice.getSelectedIndex();

         if ((n != -1) && (s != -1))
         {
            int x = -1;
            int y = -1;
            if (s != 0)
            {
               String i = sectorChoice.getItem(s).trim();
               i = i.substring(1, i.length() - 1);
               String d = ",";
               String[] j = i.split(d);
               x          = Integer.parseInt(j[0]);
               y          = Integer.parseInt(j[1]);
            }
            for (int x2 = 0, d = mox.morphognostic.neighborhoods.get(n).sectors.length; x2 < d; x2++)
            {
               for (int y2 = 0; y2 < d; y2++)
               {
                  if ((s == 0) || ((x2 == x) && (y2 == y)))
                  {
                     boolean done = false;
                     while (!done)
                     {
                        done = true;
                        for (SectorDisplay display : displays)
                        {
                           if ((display.neighborhoodIndex == n) &&
                               (display.sectorXindex == x2) &&
                               (display.sectorYindex == y2))
                           {
                              display.close();
                              displays.remove(display);
                              done = false;
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }


      // Close.
      void close()
      {
         while (displays.size() > 0)
         {
            displays.get(0).close();
         }
      }


      // Close display callback.
      void closeDisplay(int neighborhoodIndex, int sectorXindex, int sectorYindex)
      {
         for (SectorDisplay display : displays)
         {
            if ((display.neighborhoodIndex == neighborhoodIndex) &&
                (display.sectorXindex == sectorXindex) && (display.sectorYindex == sectorYindex))
            {
               displays.remove(display);
               break;
            }
         }
      }
   }
}
