// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Nest building task.

package moxworx;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class Nest
{
   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java moxworx.Nest\n" +
      "      -steps <steps> (stops when food consumed) | -dashboard\n" +
      "      -dimensions <width> <height>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (mox driver: default=autopilot)]\n" +
      "     [-numMoxen <quantity> (default=0)]\n" +
      "     [-numLandmarkTypes <quantity> (default=1)]\n" +
      "     [-numLandmarks <quantity> (default=0)]\n" +
      "     [-numFoods <quantity> (default=0)]\n" +
      "     [-numNeighborhoods <quantity> (default=" + Morphognostic.DEFAULT_NUM_NEIGHBORHOODS + ")]\n" +
      "     [-neighborhoodInitialDimension <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION + ")]\n" +
      "     [-neighborhoodDimensionStride <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE + ")]\n" +
      "     [-neighborhoodDimensionMultiplier <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER + ")]\n" +
      "     [-epochIntervalStride <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE + ")]\n" +
      "     [-epochIntervalMultiplier <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER + ")]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n" +
      "  Resume run:\n" +
      "    java moxworx.Nest\n" +
      "      -steps <steps> (stops when food consumed) | -dashboard\n" +
      "      -load <file name>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (default=autopilot)]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=fail\n" +
      "  2=error";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Number of landmark types.
   int numLandmarkTypes;

   // Moxen.
   ArrayList<NestingMox> moxen;

   // Cells.
   NestCells nestCells;

   // Dashboard display.
   NestDashboard dashboard;

   // Random numbers.
   Random random;

   // Constructor.
   public Nest()
   {
   }


   // Initialize cells.
   public void initCells(int width, int height, int numLandmarkTypes, int numLandmarks, int numFoods)
   {
      int i, j, n, x, y;

      this.numLandmarkTypes = numLandmarkTypes;

      // Create cells.
      nestCells = new NestCells(new Dimension(width, height));
      for (x = 0; x < width; x++)
      {
         for (y = 0; y < height; y++)
         {
            nestCells.cells[x][y] = nestCells.restoreCells[x][y] = MoxWorx.EMPTY_CELL_VALUE;
         }
      }

      // Create landmarks.
      n = 10;
      for (i = 0; i < numLandmarks; i++)
      {
         for (j = 0; j < n; j++)
         {
            x = random.nextInt(width);
            y = random.nextInt(height);
            if (nestCells.cells[x][y] == MoxWorx.EMPTY_CELL_VALUE)
            {
               int k = 0;
               if (numLandmarkTypes > 1)
               {
                  k = random.nextInt(numLandmarkTypes);
               }
               nestCells.cells[x][y] = nestCells.restoreCells[x][y] = NestCells.LANDMARK_CELLS_BEGIN_VALUE + k;
               break;
            }
         }
      }

      // Create foods.
      for (i = 0; i < numFoods; i++)
      {
         for (j = 0; j < n; j++)
         {
            x = random.nextInt(width);
            y = random.nextInt(height);
            if (nestCells.cells[x][y] == MoxWorx.EMPTY_CELL_VALUE)
            {
               nestCells.cells[x][y] = nestCells.restoreCells[x][y] = NestCells.FOOD_CELL_VALUE;
               break;
            }
         }
      }
      nestCells.checkpoint();
   }


   // Get width.
   int getWidth()
   {
      if (nestCells != null)
      {
         return(nestCells.size.width);
      }
      else
      {
         return(0);
      }
   }


   // Get height.
   int getHeight()
   {
      if (nestCells != null)
      {
         return(nestCells.size.height);
      }
      else
      {
         return(0);
      }
   }


   // Create moxen.
   public void createMoxen(int numMoxen, int numLandmarkTypes,
                           int NUM_NEIGHBORHOODS,
                           int NEIGHBORHOOD_INITIAL_DIMENSION,
                           int NEIGHBORHOOD_DIMENSION_STRIDE,
                           int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                           int EPOCH_INTERVAL_STRIDE,
                           int EPOCH_INTERVAL_MULTIPLIER)
   {
      int i, j, n, x, y, w, h;

      // Create moxen.
      w     = nestCells.size.width;
      h     = nestCells.size.height;
      moxen = new ArrayList<NestingMox>(numMoxen);
      n     = 10;
      for (i = 0; i < numMoxen; i++)
      {
         for (j = 0; j < n; j++)
         {
            x = random.nextInt(w);
            y = random.nextInt(h);
            if (nestCells.cells[x][y] == MoxWorx.EMPTY_CELL_VALUE)
            {
               int o = Orientation.NORTH;
               o = random.nextInt(Orientation.NUM_ORIENTATIONS);
               moxen.add(i, new NestingMox(i, x, y, o, numLandmarkTypes, nestCells,
                                           NUM_NEIGHBORHOODS,
                                           NEIGHBORHOOD_INITIAL_DIMENSION,
                                           NEIGHBORHOOD_DIMENSION_STRIDE,
                                           NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                           EPOCH_INTERVAL_STRIDE,
                                           EPOCH_INTERVAL_MULTIPLIER));
               nestCells.cells[x][y] = nestCells.restoreCells[x][y] = NestCells.MOX_CELL_VALUE;
               break;
            }
         }
      }
   }


   // Set moxen.
   public void setMoxen(ArrayList<NestingMox> moxen)
   {
      this.moxen = moxen;
      if (dashboard != null)
      {
         dashboard.setMoxen(moxen);
      }
   }


   // Reset.
   public void reset()
   {
      if (nestCells != null)
      {
         nestCells.restore();
      }
      if (moxen != null)
      {
         int numMoxen = moxen.size();
         for (int i = 0; i < numMoxen; i++)
         {
            moxen.get(i).reset();
         }
      }
      if (dashboard != null)
      {
         dashboard.close();
      }
   }


   // Clear.
   public void clear()
   {
      if (dashboard != null)
      {
         dashboard.close();
         dashboard = null;
      }
      nestCells = null;
      moxen     = null;
   }


   // Save to file.
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


   // Save.
   public void save(FileOutputStream output) throws IOException
   {
      PrintWriter writer = new PrintWriter(output);

      // Save  types.
      Utility.saveInt(writer, numLandmarkTypes);

      // Save cells.
      nestCells.save(output);

      // Save moxen.
      int numMoxen = moxen.size();
      Utility.saveInt(writer, numMoxen);
      writer.flush();
      NestingMox mox;
      for (int i = 0; i < numMoxen; i++)
      {
         mox = moxen.get(i);
         mox.save(output);
      }
   }


   // Load from file.
   public void load(String filename) throws IOException
   {
      FileInputStream input;

      try {
         input = new FileInputStream(new File(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }
      load(input);
      input.close();
   }


   // Load.
   public void load(FileInputStream input) throws IOException
   {
      DataInputStream reader = new DataInputStream(input);

      // Load  types.
      numLandmarkTypes = Utility.loadInt(reader);

      // Load cells.
      nestCells = new NestCells();
      nestCells.load(input);

      // Load moxen.
      int numMoxen = Utility.loadInt(reader);
      moxen = new ArrayList<NestingMox>(numMoxen);
      NestingMox mox;
      for (int i = 0; i < numMoxen; i++)
      {
         mox = new NestingMox(nestCells);
         mox.load(input);
         moxen.add(i, mox);
      }
   }


   // Run.
   // Return count of remaining food.
   public int run(int steps)
   {
      if (steps >= 0)
      {
         for (int i = 0; i < steps && nestCells.countFood() > 0; i++)
         {
            stepMoxen();
         }
      }
      else
      {
         for (int i = 0; updateDashboard(i); )
         {
            if (nestCells.countFood() > 0)
            {
               stepMoxen();
               i++;
            }
         }
      }
      return(nestCells.countFood());
   }


   // Step moxen.
   public void stepMoxen()
   {
      int i, j, numMoxen;

      // Step moxen.
      numMoxen = moxen.size();
      if (numMoxen > 0)
      {
         for (i = 0, j = random.nextInt(numMoxen); i < numMoxen; i++, j = (j + 1) % numMoxen)
         {
            stepMox(j);
         }
      }
   }


   // Step mox.
   void stepMox(int moxIndex)
   {
      int fx, fy, lx, ly, rx, ry, width, height;
      int landmarkIndex, foodIndex;

      int        response;
      NestingMox mox;

      float[] sensors        = new float[NestingMox.NUM_SENSORS];
      landmarkIndex          = NestingMoxDashboard.LANDMARK_SENSOR_INDEX;
      foodIndex              = NestingMoxDashboard.FOOD_SENSOR_INDEX;
      sensors[landmarkIndex] = 0.0f;
      sensors[foodIndex]     = 0.0f;
      width  = nestCells.size.width;
      height = nestCells.size.height;
      mox    = moxen.get(moxIndex);

      // Detect object ahead.
      switch (mox.direction)
      {
      case Orientation.NORTH:
         fx = mox.x;
         fy = ((mox.y + 1) % height);
         lx = mox.x - 1;
         if (lx < 0) { lx += width; }
         ly = mox.y;
         rx = (mox.x + 1) % width;
         ry = mox.y;
         break;

      case Orientation.EAST:
         fx = (mox.x + 1) % width;
         fy = mox.y;
         lx = mox.x;
         ly = ((mox.y + 1) % height);
         rx = mox.x;
         ry = mox.y - 1;
         if (ry < 0) { ry += height; }
         break;

      case Orientation.SOUTH:
         fx = mox.x;
         fy = mox.y - 1;
         if (fy < 0) { fy += height; }
         lx = (mox.x + 1) % width;
         ly = mox.y;
         rx = mox.x - 1;
         if (rx < 0) { rx += width; }
         ry = mox.y;
         break;

      case Orientation.WEST:
         fx = mox.x - 1;
         if (fx < 0) { fx += width; }
         fy = mox.y;
         lx = mox.x;
         ly = mox.y - 1;
         if (ly < 0) { ly += height; }
         rx = mox.x;
         ry = ((mox.y + 1) % height);
         break;

      default:
         fx = fy = lx = ly = rx = ry = -1;
         break;
      }
      if (nestCells.cells[fx][fy] != MoxWorx.EMPTY_CELL_VALUE)
      {
         sensors[landmarkIndex] = 1.0f;
      }
      if (nestCells.cells[fx][fy] >= NestCells.LANDMARK_CELLS_BEGIN_VALUE)
      {
         mox.landmarkMap[fx][fy] = true;
      }

      // Detect food.
      sensors[foodIndex] = 1.0f / ((float)nestCells.foodDist(fx, fy) + 1.0f);

      // Cycle mox.
      response = mox.cycle(sensors, fx, fy);

      // Process response.
      if (response == NestingMox.FORWARD)
      {
         if (nestCells.cells[fx][fy] == MoxWorx.EMPTY_CELL_VALUE)
         {
            nestCells.cells[mox.x][mox.y] = MoxWorx.EMPTY_CELL_VALUE;
            mox.x = fx;
            mox.y = fy;
            nestCells.cells[mox.x][mox.y] = NestCells.MOX_CELL_VALUE;
         }
      }
      else if (response == NestingMox.RIGHT)
      {
         mox.direction = (mox.direction + 1) % Orientation.NUM_ORIENTATIONS;
      }
      else if (response == NestingMox.LEFT)
      {
         mox.direction = mox.direction - 1;
         if (mox.direction < 0)
         {
            mox.direction = mox.direction + Orientation.NUM_ORIENTATIONS;
         }
      }
      else if (response == NestingMox.EAT)
      {
         if (nestCells.cells[fx][fy] == NestCells.FOOD_CELL_VALUE)
         {
            nestCells.cells[fx][fy] = MoxWorx.EMPTY_CELL_VALUE;
         }
      }
   }


   // Create dashboard.
   public void createDashboard()
   {
      if (dashboard == null)
      {
         if (moxen == null)
         {
            dashboard = new NestDashboard(nestCells, numLandmarkTypes);
         }
         else
         {
            dashboard = new NestDashboard(nestCells, numLandmarkTypes, moxen);
         }
      }
   }


   // Destroy dashboard.
   public void destroyDashboard()
   {
      if (dashboard != null)
      {
         dashboard.close();
         dashboard = null;
      }
   }


   // Update dashboard.
   // Return false for dashboard quit.
   public boolean updateDashboard(int steps)
   {
      if (dashboard != null)
      {
         dashboard.update(steps);
         if (dashboard.quit)
         {
            dashboard = null;
            return(false);
         }
         else
         {
            return(true);
         }
      }
      else
      {
         return(false);
      }
   }


   // Main.
   // Exit codes:
   // 0=success
   // 1=fail
   // 2=error
   public static void main(String[] args)
   {
      // Get options.
      int     steps             = -1;
      int     width             = -1;
      int     height            = -1;
      int     driver            = NestingMox.DRIVER_TYPE.AUTOPILOT.getValue();
      int     numMoxen          = -1;
      int     numLandmarkTypes  = -1;
      int     numLandmarks      = -1;
      int     numFoods          = -1;
      int     randomSeed        = DEFAULT_RANDOM_SEED;
      String  loadfile          = null;
      String  savefile          = null;
      boolean dashboard         = false;
      boolean gotParm           = false;
      int     NUM_NEIGHBORHOODS = Morphognostic.DEFAULT_NUM_NEIGHBORHOODS;
      int     NEIGHBORHOOD_INITIAL_DIMENSION    = Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION;
      int     NEIGHBORHOOD_DIMENSION_STRIDE     = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE;
      int     NEIGHBORHOOD_DIMENSION_MULTIPLIER = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER;
      int     EPOCH_INTERVAL_STRIDE             = Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE;
      int     EPOCH_INTERVAL_MULTIPLIER         = Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-steps"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid steps option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               steps = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid steps option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (steps < 0)
            {
               System.err.println("Invalid steps option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-dashboard"))
         {
            dashboard = true;
            continue;
         }
         if (args[i].equals("-dimensions"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               width = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid width option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (width < 2)
            {
               System.err.println("Invalid width option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               height = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid height option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (height < 2)
            {
               System.err.println("Invalid height option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-driver"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid driver option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (args[i].equals("metamorphDB"))
            {
               driver = NestingMox.DRIVER_TYPE.METAMORPH_DB.getValue();
            }
            else if (args[i].equals("metamorphNN"))
            {
               driver = NestingMox.DRIVER_TYPE.METAMORPH_NN.getValue();
            }
            else if (args[i].equals("autopilot"))
            {
               driver = NestingMox.DRIVER_TYPE.AUTOPILOT.getValue();
            }
            else
            {
               System.err.println("Invalid driver option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-numMoxen"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numMoxen option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               numMoxen = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numMoxen option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (numMoxen < 0)
            {
               System.err.println("Invalid numMoxen option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-numLandmarkTypes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numLandmarkTypes option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               numLandmarkTypes = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numLandmarkTypes option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (numLandmarkTypes < 1)
            {
               System.err.println("Invalid numLandmarkTypes option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-numLandmarks"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numLandmarks option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               numLandmarks = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numLandmarks option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (numLandmarks < 0)
            {
               System.err.println("Invalid numLandmarks option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-numFoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numFoods option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               numFoods = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numFoods option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (numFoods < 0)
            {
               System.err.println("Invalid numFoods option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-numNeighborhoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               NUM_NEIGHBORHOODS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (NUM_NEIGHBORHOODS < 0)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodInitialDimension"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               NEIGHBORHOOD_INITIAL_DIMENSION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if ((NEIGHBORHOOD_INITIAL_DIMENSION < 3) ||
                ((NEIGHBORHOOD_INITIAL_DIMENSION % 2) == 0))
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               NEIGHBORHOOD_DIMENSION_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (NEIGHBORHOOD_DIMENSION_STRIDE < 0)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               NEIGHBORHOOD_DIMENSION_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (NEIGHBORHOOD_DIMENSION_MULTIPLIER < 0)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochIntervalStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               EPOCH_INTERVAL_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (EPOCH_INTERVAL_STRIDE < 0)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-epochIntervalMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               EPOCH_INTERVAL_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (EPOCH_INTERVAL_MULTIPLIER < 0)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            gotParm = true;
            continue;
         }
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               randomSeed = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-load"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid load option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (loadfile == null)
            {
               loadfile = args[i];
            }
            else
            {
               System.err.println("Duplicate load option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-save"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid save option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (savefile == null)
            {
               savefile = args[i];
            }
            else
            {
               System.err.println("Duplicate save option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         System.err.println(Nest.Usage);
         System.exit(2);
      }

      // Check options.
      if (((steps < 0) && !dashboard) || ((steps >= 0) && dashboard))
      {
         System.err.println(Nest.Usage);
         System.exit(2);
      }
      if (!dashboard)
      {
         if (driver == NestingMox.DRIVER_TYPE.MANUAL.getValue())
         {
            System.err.println("Cannot run manually without dashboard");
            System.err.println(Nest.Usage);
            System.exit(2);
         }
      }
      if (loadfile == null)
      {
         if ((width == -1) || (height == -1))
         {
            System.err.println(Nest.Usage);
            System.exit(2);
         }
         if (numMoxen == -1) { numMoxen = 0; }
         if (numLandmarkTypes == -1) { numLandmarkTypes = 1; }
         if (numLandmarks == -1) { numLandmarks = 0; }
         if (numFoods == -1) { numFoods = 0; }
      }
      else
      {
         if ((numMoxen != -1) || (numLandmarkTypes != -1) || (numLandmarks != -1) ||
             (numFoods != -1) || (width != -1) || (height != -1) || gotParm)
         {
            System.err.println(Nest.Usage);
            System.exit(2);
         }
      }

      // Set look and feel.
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         System.err.println("Warning: cannot set look and feel");
      }

      // Create world.
      Nest nest = new Nest();
      nest.random = new Random(randomSeed);
      if (loadfile != null)
      {
         try
         {
            nest.load(loadfile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot load from file " + loadfile + ": " + e.getMessage());
            System.exit(2);
         }
      }
      else
      {
         try
         {
            nest.initCells(width, height, numLandmarkTypes, numLandmarks, numFoods);
            nest.createMoxen(numMoxen, numLandmarkTypes + 1,
                             NUM_NEIGHBORHOODS,
                             NEIGHBORHOOD_INITIAL_DIMENSION,
                             NEIGHBORHOOD_DIMENSION_STRIDE,
                             NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                             EPOCH_INTERVAL_STRIDE,
                             EPOCH_INTERVAL_MULTIPLIER);
         }
         catch (Exception e)
         {
            System.err.println("Cannot initialize: " + e.getMessage());
            System.exit(2);
         }
      }

      // Create dashboard?
      if (dashboard)
      {
         nest.createDashboard();
      }
      else
      {
         nest.reset();
      }

      // Set mox driver.
      for (NestingMox mox : nest.moxen)
      {
         mox.driver = driver;
         if (driver == NestingMox.DRIVER_TYPE.METAMORPH_NN.getValue())
         {
            try
            {
               System.out.println("Training metamorph NN...");
               mox.createMetamorphNN();
            }
            catch (Exception e)
            {
               System.err.println("Cannot train metamorph NN: " + e.getMessage());
            }
         }
      }

      // Run.
      int foodCount = nest.run(steps);

      // Save?
      if (savefile != null)
      {
         try
         {
            nest.save(savefile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot save to file " + savefile + ": " + e.getMessage());
         }
      }
      if (foodCount > 0)
      {
         System.exit(1);
      }
      else
      {
         System.exit(0);
      }
      System.exit(0);
   }
}
