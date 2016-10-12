// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Mox: mophognosis organism.

package moxworx;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

// Mox.
public class Mox
{
   // Properties.
   public int      id;
   public int      x, y;
   public int      direction;
   public MoxCells moxCells;
   public int      numLandmarkTypes;
   public int      x2, y2;
   public int      direction2;
   public int      driverType;
   public int      driverResponse;

   // Learning.
   public Morphognostic        morphognostic;
   public ArrayList<Metamorph> metamorphs;

   // Input/output.
   float[] sensors;
   int response;

   // Sensor configuration.
   public enum SENSOR_CONFIG
   {
      NUM_LANDMARK_SENSORS(1),
      LANDMARK_SENSOR_INDEX(0),
      NUM_FOOD_SENSORS(1),
      FOOD_SENSOR_INDEX(1),
      NUM_SENSORS(2);

      private int value;

      SENSOR_CONFIG(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return(value);
      }
   }

   // Response types.
   public static           final int WAIT = 0;
   public static final int FORWARD = 1;
   public static final int RIGHT   = 2;
   public static final int LEFT    = 3;
   public static final int EAT     = 4;

   // Navigation.
   public boolean[][] obstacleMap;
   public int         maxLandmarkEventAge;
   public class LandmarkEvent
   {
      public int value;
      public int x;
      public int y;
      public int time;
      public LandmarkEvent(int value, int x, int y, int time)
      {
         this.value = value;
         this.x     = x;
         this.y     = y;
         this.time  = time;
      }
   }
   public Vector<LandmarkEvent> landmarkEvents;
   public int eventTime;

   // Driver type.
   public enum DRIVER_TYPE
   {
      MOX(0),
      AUTO(1),
      MANUAL(2);

      private int value;

      DRIVER_TYPE(int value)
      {
         this.value = value;
      }

      public int getValue()
      {
         return(value);
      }
   }

   // Constructors.
   public Mox(int id, int x, int y, int direction, int numLandmarkTypes, MoxCells moxCells)
   {
      this.id               = id;
      this.moxCells         = moxCells;
      this.numLandmarkTypes = numLandmarkTypes;
      init(x, y, direction);
   }


   public Mox(MoxCells moxCells)
   {
      id               = -1;
      this.moxCells    = moxCells;
      numLandmarkTypes = 1;
      init();
   }


   // Initialize.
   void init(int x, int y, int direction)
   {
      this.x         = x2 = x;
      this.y         = y2 = y;
      this.direction = direction2 = direction;
      sensors        = new float[SENSOR_CONFIG.NUM_SENSORS.getValue()];
      for (int i = 0; i < SENSOR_CONFIG.NUM_SENSORS.getValue(); i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driverType     = DRIVER_TYPE.MOX.getValue();
      driverResponse = WAIT;
      obstacleMap    = new boolean[moxCells.size.width][moxCells.size.height];
      for (int i = 0; i < moxCells.size.width; i++)
      {
         for (int j = 0; j < moxCells.size.height; j++)
         {
            obstacleMap[i][j] = false;
         }
      }
      landmarkEvents = new Vector<LandmarkEvent>();
      morphognostic  = new Morphognostic(direction, numLandmarkTypes);
      metamorphs     = new ArrayList<Metamorph>();
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(Morphognostic.NUM_NEIGHBORHOODS - 1);
      maxLandmarkEventAge = n.epoch + n.duration - 1;
      eventTime           = 0;
   }


   void init()
   {
      init(0, 0, Orientation.NORTH);
   }


   // Reset state.
   void reset()
   {
      x         = x2;
      y         = y2;
      direction = direction2;
      for (int i = 0; i < SENSOR_CONFIG.NUM_SENSORS.getValue(); i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driverType     = DRIVER_TYPE.MOX.getValue();
      driverResponse = WAIT;
      for (int i = 0; i < moxCells.size.width; i++)
      {
         for (int j = 0; j < moxCells.size.height; j++)
         {
            obstacleMap[i][j] = false;
         }
      }
      landmarkEvents.clear();
      morphognostic.clear();
   }


   // Save mox to file.
   public void save(String filename) throws IOException
   {
      FileOutputStream output;

      try
      {
         output = new FileOutputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open output file " + filename + ":" + e.getMessage());
      }
      save(output);
      output.close();
   }


   // Save mox.
   public void save(FileOutputStream output) throws IOException
   {
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));

      Utility.saveInt(writer, id);
      Utility.saveInt(writer, x);
      Utility.saveInt(writer, y);
      Utility.saveInt(writer, direction);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, direction2);
      Utility.saveInt(writer, metamorphs.size());
      for (Metamorph m : metamorphs)
      {
         m.save(output);
      }
      writer.flush();
   }


   // Load mox from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try
      {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load mox.
   public void load(FileInputStream input) throws IOException
   {
      // Load the properties.
      // DataInputStream is for unbuffered input.
      DataInputStream reader = new DataInputStream(input);

      id         = Utility.loadInt(reader);
      x          = Utility.loadInt(reader);
      y          = Utility.loadInt(reader);
      direction  = Utility.loadInt(reader);
      x2         = Utility.loadInt(reader);
      y2         = Utility.loadInt(reader);
      direction2 = Utility.loadInt(reader);
      metamorphs.clear();
      int n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(input));
      }
   }


   // Sensor/response cycle.
   public int cycle(float[] sensors)
   {
      this.sensors = sensors;
      if (driverType == DRIVER_TYPE.MOX.getValue())
      {
         myResponse();
      }
      else if (driverType == DRIVER_TYPE.AUTO.getValue())
      {
         autoResponse();
      }
      else
      {
         response = driverResponse;
      }
      return(response);
   }


   // Landmark event.
   public void landmark(int fx, int fy)
   {
      // Save event.
      landmarkEvents.add(new LandmarkEvent(moxCells.cells[fx][fy], fx, fy, eventTime));
      if ((eventTime - landmarkEvents.get(0).time) > maxLandmarkEventAge)
      {
         landmarkEvents.remove(0);
      }

      // Update metamorphs.
      Metamorph metamorph = new Metamorph(morphognostic.clone(), response);
      boolean   found     = false;
      for (Metamorph m : metamorphs)
      {
         if (m.equals(metamorph))
         {
            found = true;
            break;
         }
      }
      if (!found)
      {
         metamorphs.add(metamorph);
      }

      // Update morphognostic.
      int w = moxCells.size.width;
      int h = moxCells.size.height;
      int a = maxLandmarkEventAge + 1;
      int landmarks[][][] = new int[w][h][a];
      for (int x = 0; x < w; x++)
      {
         for (int y = 0; y < h; y++)
         {
            for (int t = 0; t < a; t++)
            {
               landmarks[x][y][t] = -1;
            }
         }
      }
      for (LandmarkEvent e : landmarkEvents)
      {
         if (e.value >= MoxCells.OBSTACLE_CELLS_BEGIN_VALUE)
         {
            landmarks[e.x][e.y][eventTime - e.time] = e.value;
         }
         else
         {
            landmarks[e.x][e.y][eventTime - e.time] = MoxCells.EMPTY_CELL_VALUE;
         }
      }
      morphognostic.update(landmarks, x, y);
      eventTime++;
   }


   // Get response.
   void myResponse()
   {
      if (sensors[Mox.SENSOR_CONFIG.FOOD_SENSOR_INDEX.getValue()] == 1.0f)
      {
         response = EAT;
      }
      else
      {
         response = WAIT;
         Metamorph metamorph = null;
         float     d         = 0.0f;
         float     d2;
         for (Metamorph m : metamorphs)
         {
            d2 = morphognostic.compare(m.morphognostic);
            if ((metamorph == null) || (d2 < d))
            {
               d         = d2;
               metamorph = m;
            }
         }
         if (metamorph != null)
         {
            response = metamorph.response;
         }
      }
   }


   // Auto response.
   void autoResponse()
   {
      // Search for best response leading to food.
      int fx, fy;
      int left, right;
      int r;
      int w = moxCells.size.width;
      int h = moxCells.size.height;

      ArrayList<FoodSearch> open   = new ArrayList<FoodSearch>();
      ArrayList<FoodSearch> closed = new ArrayList<FoodSearch>();
      response = WAIT;
      FoodSearch current = new FoodSearch(EAT, x, y, direction, 0);
      if (current.foodDist == -1)
      {
         return;
      }
      open.add(current);
      FoodSearch next;
      boolean    found;
      while (open.size() > 0)
      {
         current = open.get(0);
         open.remove(0);
         closed.add(current);
         switch (current.dir)
         {
         case Orientation.NORTH:
            fx    = current.x;
            fy    = ((current.y + 1) % h);
            left  = Orientation.WEST;
            right = Orientation.EAST;
            break;

         case Orientation.EAST:
            fx    = (current.x + 1) % w;
            fy    = current.y;
            left  = Orientation.NORTH;
            right = Orientation.SOUTH;
            break;

         case Orientation.SOUTH:
            fx = current.x;
            fy = current.y - 1;
            if (fy < 0) { fy += h; }
            left  = Orientation.EAST;
            right = Orientation.WEST;
            break;

         case Orientation.WEST:
            fx = current.x - 1;
            if (fx < 0) { fx += w; }
            fy    = current.y;
            left  = Orientation.SOUTH;
            right = Orientation.NORTH;
            break;

         default:
            fx    = -1;
            fy    = -1;
            left  = Orientation.WEST;
            right = Orientation.EAST;
            break;
         }
         if (moxCells.cells[fx][fy] == MoxCells.FOOD_CELL_VALUE)
         {
            response = current.response;
            return;
         }
         r = current.response;
         if (current.response == EAT)
         {
            r = FORWARD;
         }
         if (!obstacleMap[fx][fy])
         {
            next  = new FoodSearch(r, fx, fy, current.dir, current.depth + 1);
            found = false;
            for (FoodSearch s : closed)
            {
               if (next.equals(s))
               {
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               open.add(next);
            }
         }
         if (current.response == EAT)
         {
            r = LEFT;
         }
         next  = new FoodSearch(r, current.x, current.y, left, current.depth + 1);
         found = false;
         for (FoodSearch s : closed)
         {
            if (next.equals(s))
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            open.add(next);
         }
         if (current.response == EAT)
         {
            r = RIGHT;
         }
         next  = new FoodSearch(r, current.x, current.y, right, current.depth + 1);
         found = false;
         for (FoodSearch s : closed)
         {
            if (next.equals(s))
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            open.add(next);
         }
         Collections.sort(open);
      }
   }


   // Food search state.
   class FoodSearch implements Comparable<FoodSearch>
   {
      int response;
      int x, y;
      int dir;
      int depth;
      int foodDist;

      // Constructor.
      FoodSearch(int response, int x, int y, int dir, int depth)
      {
         this.response = response;
         this.x        = x;
         this.y        = y;
         this.dir      = dir;
         this.depth    = depth;
         foodDist      = depth + moxCells.foodDist(x, y);
      }


      // Equal comparison.
      boolean equals(FoodSearch s)
      {
         if ((x == s.x) && (y == s.y) && (dir == s.dir))
         {
            return(true);
         }
         else
         {
            return(false);
         }
      }


      @Override
      public int compareTo(FoodSearch s)
      {
         return(foodDist - s.foodDist);
      }
   }
}
