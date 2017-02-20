// For conditions of distribution and use, see copyright notice in MoxWorx.java

//  Nesting mox dashboard.

package moxworx;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class NestingMoxDashboard extends JFrame
{
   private static final long serialVersionUID = 0L;

   // Components.
   SensorsResponsePanel sensorsResponse;
   DriverPanel          driver;
   MorphognosticDisplay morphognostic;

   // Targets.
   NestingMox  mox;
   NestDisplay nestDisplay;

   // Constructor.
   public NestingMoxDashboard(NestingMox mox, NestDisplay nestDisplay)
   {
      this.mox         = mox;
      this.nestDisplay = nestDisplay;

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
      String forwardGradientString;

      if (mox.sensors[NestingMox.FORWARD_GRADIENT_SENSOR_INDEX] == NestingMox.FLAT_GRADIENT)
      {
         forwardGradientString = "flat";
      }
      else if (mox.sensors[NestingMox.FORWARD_GRADIENT_SENSOR_INDEX] == NestingMox.PEAK_GRADIENT)
      {
         forwardGradientString = "peak";
      }
      else if (mox.sensors[NestingMox.FORWARD_GRADIENT_SENSOR_INDEX] == NestingMox.FORWARD_UP_GRADIENT)
      {
         forwardGradientString = "up";
      }
      else
      {
         forwardGradientString = "down";
      }
      String lateralGradientString;
      if (mox.sensors[NestingMox.LATERAL_GRADIENT_SENSOR_INDEX] == NestingMox.FLAT_GRADIENT)
      {
         lateralGradientString = "flat";
      }
      else if (mox.sensors[NestingMox.LATERAL_GRADIENT_SENSOR_INDEX] == NestingMox.PEAK_GRADIENT)
      {
         lateralGradientString = "peak";
      }
      else if (mox.sensors[NestingMox.LATERAL_GRADIENT_SENSOR_INDEX] == NestingMox.RIGHT_UP_GRADIENT)
      {
         lateralGradientString = "right up";
      }
      else
      {
         lateralGradientString = "left up";
      }
      setSensors(mox.sensors[NestingMox.STONE_AHEAD_SENSOR_INDEX] + "",
                 forwardGradientString, lateralGradientString);
      if (mox.response == NestingMox.WAIT)
      {
         setResponse("wait");
      }
      else if (mox.response == NestingMox.FORWARD)
      {
         setResponse("move forward");
      }
      else if (mox.response == NestingMox.RIGHT)
      {
         setResponse("turn right");
      }
      else if (mox.response == NestingMox.LEFT)
      {
         setResponse("turn left");
      }
      else if (mox.response == NestingMox.TAKE_STONE)
      {
         setResponse("take stone");
      }
      else if (mox.response == NestingMox.DROP_STONE)
      {
         setResponse("drop stone");
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
      nestDisplay.closeMoxDashboard();
   }


   // Set sensors display.
   void setSensors(String stoneAheadSensorString,
                   String forwardGradientSensorString,
                   String lateralGradientSensorString)
   {
      sensorsResponse.stoneAheadText.setText(stoneAheadSensorString);
      sensorsResponse.forwardGradientText.setText(forwardGradientSensorString);
      sensorsResponse.lateralGradientText.setText(lateralGradientSensorString);
      if (mox.hasStone)
      {
         sensorsResponse.hasStoneText.setText("true");
      }
      else
      {
         sensorsResponse.hasStoneText.setText("false");
      }
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
      JTextField stoneAheadText;
      JTextField hasStoneText;
      JTextField forwardGradientText;
      JTextField lateralGradientText;
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
         JPanel stonePanel = new JPanel();
         stonePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(stonePanel, BorderLayout.NORTH);
         stonePanel.add(new JLabel("Stone ahead:"));
         stoneAheadText = new JTextField(10);
         stoneAheadText.setEditable(false);
         stonePanel.add(stoneAheadText);
         stonePanel.add(new JLabel("Stone carried:"));
         hasStoneText = new JTextField(10);
         hasStoneText.setEditable(false);
         stonePanel.add(hasStoneText);
         JPanel gradientPanel = new JPanel();
         gradientPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         sensorsPanel.add(gradientPanel, BorderLayout.CENTER);
         gradientPanel.add(new JLabel("Forward:"));
         forwardGradientText = new JTextField(10);
         forwardGradientText.setEditable(false);
         gradientPanel.add(forwardGradientText);
         gradientPanel.add(new JLabel("Lateral:"));
         lateralGradientText = new JTextField(10);
         lateralGradientText.setEditable(false);
         gradientPanel.add(lateralGradientText);
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
      JButton  takeStoneButton;
      JButton  dropStoneButton;
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
         takeStoneButton = new JButton("Take");
         takeStoneButton.addActionListener(this);
         responsePanel.add(takeStoneButton);
         dropStoneButton = new JButton("Drop");
         dropStoneButton.addActionListener(this);
         responsePanel.add(dropStoneButton);
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
                  nestDisplay.controls.messageText.setText("Cannot train metamorph NN: " + e.getMessage());
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
            mox.driverResponse = NestingMox.LEFT;
            return;
         }

         if ((JButton)evt.getSource() == moveForwardButton)
         {
            mox.driverResponse = NestingMox.FORWARD;
            return;
         }

         if ((JButton)evt.getSource() == turnRightButton)
         {
            mox.driverResponse = NestingMox.RIGHT;
            return;
         }

         if ((JButton)evt.getSource() == takeStoneButton)
         {
            mox.driverResponse = NestingMox.TAKE_STONE;
            return;
         }

         if ((JButton)evt.getSource() == dropStoneButton)
         {
            mox.driverResponse = NestingMox.DROP_STONE;
            return;
         }
      }
   }
}
