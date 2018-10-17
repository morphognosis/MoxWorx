// For conditions of distribution and use, see copyright notice in MoxWorx.java

//  Forager mox dashboard.

package morphognosis.moxworx;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import morphognosis.MorphognosticDisplay;

public class ForagerMoxDashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Sensor semantics.
   static final int LANDMARK_SENSOR_INDEX = 0;
   static final int FOOD_SENSOR_INDEX     = 1;

   // Components.
   SensorsResponsePanel sensorsResponse;
   DriverPanel          driver;
   MorphognosticDisplay morphognostic;

   // Targets.
   ForagerMox    mox;
   ForageDisplay forageDisplay;

   // Constructor.
   public ForagerMoxDashboard(ForagerMox mox, ForageDisplay forageDisplay)
   {
      this.mox           = mox;
      this.forageDisplay = forageDisplay;

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
      morphognostic = new MorphognosticDisplay(mox.id, mox.morphognostic);
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
      float f = (float)((int)(mox.sensors[FOOD_SENSOR_INDEX] * 100.0f)) / 100.0f;

      setSensors(mox.sensors[LANDMARK_SENSOR_INDEX] + "", f + "");
      if (mox.response == ForagerMox.WAIT)
      {
         setResponse("wait");
      }
      else if (mox.response == ForagerMox.FORWARD)
      {
         setResponse("move forward");
      }
      else if (mox.response == ForagerMox.RIGHT)
      {
         setResponse("turn right");
      }
      else if (mox.response == ForagerMox.LEFT)
      {
         setResponse("turn left");
      }
      else if (mox.response == ForagerMox.EAT)
      {
         setResponse("eat");
      }
      else
      {
         setResponse("");
      }
      setDriverChoice(mox.driver);
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
      forageDisplay.closeMoxDashboard();
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
      private static final long serialVersionUID = 0L;

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
      private static final long serialVersionUID = 0L;

      // Components.
      Choice   driverChoice;
      JButton  turnLeftButton;
      JButton  moveForwardButton;
      JButton  turnRightButton;
      JButton  eatButton;
      Checkbox trainNNcheck;

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
         driverChoice.add("metamorphDB");
         driverChoice.add("metamorphNN");
         driverChoice.add("autopilot");
         driverChoice.add("manual");
         driverChoice.addItemListener(this);
         JPanel responsePanel = new JPanel();
         responsePanel.setLayout(new FlowLayout());
         add(responsePanel, BorderLayout.CENTER);
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
         JPanel trainNNpanel = new JPanel();
         trainNNpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         add(trainNNpanel, BorderLayout.SOUTH);
         trainNNpanel.add(new JLabel("Train NN:"));
         trainNNcheck = new Checkbox();
         trainNNcheck.setState(false);
         trainNNcheck.addItemListener(this);
         trainNNpanel.add(trainNNcheck);
      }


      // Choice listener.
      public void itemStateChanged(ItemEvent evt)
      {
         Object source = evt.getSource();

         if (source instanceof Choice && ((Choice)source == driverChoice))
         {
            mox.driver = driverChoice.getSelectedIndex();
            return;
         }
         if (source instanceof Checkbox && ((Checkbox)source == trainNNcheck))
         {
            if (trainNNcheck.getState())
            {
               try
               {
                  mox.createMetamorphNN();
               }
               catch (Exception e)
               {
                  forageDisplay.controls.messageText.setText("Cannot train metamorph NN: " + e.getMessage());
               }
               trainNNcheck.setState(false);
            }
            return;
         }
      }


      // Button listener.
      public void actionPerformed(ActionEvent evt)
      {
         if ((JButton)evt.getSource() == turnLeftButton)
         {
            mox.driverResponse = ForagerMox.LEFT;
            return;
         }

         if ((JButton)evt.getSource() == moveForwardButton)
         {
            mox.driverResponse = ForagerMox.FORWARD;
            return;
         }

         if ((JButton)evt.getSource() == turnRightButton)
         {
            mox.driverResponse = ForagerMox.RIGHT;
            return;
         }

         if ((JButton)evt.getSource() == eatButton)
         {
            mox.driverResponse = ForagerMox.EAT;
            return;
         }
      }
   }
}
