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
      "      -steps <steps> (stops when food consumed) | -display\n" +
      "      -dimensions <width> <height>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (mox driver: default=autopilot)]\n" +
      "     [-numStones <quantity> (default=0)]\n" +
      "     [-maxElevation <quantity> (default=" + NestCells.MAX_ELEVATION_VALUE + ")]\n" +
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
      "      -steps <steps> (stops when food consumed) | -display\n" +
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

   // Moxen.
   ArrayList<NestingMox> moxen;

   // Cells.
   NestCells nestCells;

   // Display.
   NestDisplay display;

   // Random numbers.
   int    randomSeed;
   Random random;

   // Constructor.
   public Nest(int randomSeed)
   {
      this.randomSeed = randomSeed;
      random          = new Random(randomSeed);
   }


   // Initialize.
   public void init(int width, int height, int numStones,
                    int NUM_NEIGHBORHOODS,
                    int NEIGHBORHOOD_INITIAL_DIMENSION,
                    int NEIGHBORHOOD_DIMENSION_STRIDE,
                    int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                    int EPOCH_INTERVAL_STRIDE,
                    int EPOCH_INTERVAL_MULTIPLIER)
   {
      // Create cells.
      nestCells = new NestCells(new Dimension(width, height), numStones, randomSeed);

      // Create mox.
      moxen = new ArrayList<NestingMox>();
      moxen.add(0, new NestingMox(0, nestCells.nestX, nestCells.nestY,
                                  Orientation.NORTH, nestCells, randomSeed,
                                  NUM_NEIGHBORHOODS,
                                  NEIGHBORHOOD_INITIAL_DIMENSION,
                                  NEIGHBORHOOD_DIMENSION_STRIDE,
                                  NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                  EPOCH_INTERVAL_STRIDE,
                                  EPOCH_INTERVAL_MULTIPLIER));
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


   // Set moxen.
   public void setMoxen(ArrayList<NestingMox> moxen)
   {
      this.moxen = moxen;
      if (display != null)
      {
         display.setMoxen(moxen);
      }
   }


   // Reset.
   public void reset()
   {
      random.setSeed(randomSeed);
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
      if (display != null)
      {
         display.close();
      }
   }


   // Clear.
   public void clear()
   {
      if (display != null)
      {
         display.close();
         display = null;
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

      // Load cells.
      nestCells = new NestCells();
      nestCells.load(input);

      // Load moxen.
      int numMoxen = Utility.loadInt(reader);
      moxen = new ArrayList<NestingMox>(numMoxen);
      NestingMox mox;
      for (int i = 0; i < numMoxen; i++)
      {
         mox = new NestingMox(nestCells, randomSeed);
         mox.load(input);
         moxen.add(i, mox);
      }
   }


   // Run.
   public void run(int steps)
   {
      random.setSeed(randomSeed);
      if (steps >= 0)
      {
         stepMoxen();
      }
      else
      {
         for (int i = 0; updateDisplay(i); i++)
         {
            stepMoxen();
         }
      }
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
      int fx, fy, bx, by, lx, ly, rx, ry, width, height;
      int stoneIndex, forwardGradientIndex, lateralGradientIndex;

      int        response;
      NestingMox mox;

      float[] sensors      = new float[NestingMox.NUM_SENSORS];
      stoneIndex           = NestingMox.STONE_AHEAD_SENSOR_INDEX;
      forwardGradientIndex = NestingMox.FORWARD_GRADIENT_SENSOR_INDEX;
      lateralGradientIndex = NestingMox.LATERAL_GRADIENT_SENSOR_INDEX;
      for (int i = 0; i < NestingMox.NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      width  = nestCells.size.width;
      height = nestCells.size.height;
      mox    = moxen.get(moxIndex);

      // Update landmarks.
      mox.landmarkMap[mox.x][mox.y] = true;

      // Initialize sensors.
      switch (mox.direction)
      {
      case Orientation.NORTH:
         fx = mox.x;
         fy = ((mox.y + 1) % height);
         bx = mox.x;
         by = mox.y - 1;
         if (by < 0) { by += height; }
         lx = mox.x - 1;
         if (lx < 0) { lx += width; }
         ly = mox.y;
         rx = (mox.x + 1) % width;
         ry = mox.y;
         break;

      case Orientation.EAST:
         fx = (mox.x + 1) % width;
         fy = mox.y;
         bx = mox.x - 1;
         if (bx < 0) { bx += width; }
         by = mox.y;
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
         bx = mox.x;
         by = ((mox.y + 1) % height);
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
         bx = (mox.x + 1) % width;
         by = mox.y;
         lx = mox.x;
         ly = mox.y - 1;
         if (ly < 0) { ly += height; }
         rx = mox.x;
         ry = ((mox.y + 1) % height);
         break;

      default:
         fx = fy = bx = by = lx = ly = rx = ry = -1;
         break;
      }
      sensors[stoneIndex] = (float)nestCells.cells[fx][fy][NestCells.STONE_CELL_INDEX];
      int f = nestCells.cells[fx][fy][NestCells.ELEVATION_CELL_INDEX];
      int c = nestCells.cells[mox.x][mox.y][NestCells.ELEVATION_CELL_INDEX];
      int b = nestCells.cells[bx][by][NestCells.ELEVATION_CELL_INDEX];
      if (c > f)
      {
         if (c > b)
         {
            sensors[forwardGradientIndex] = (float)NestingMox.PEAK_GRADIENT;
         }
         else
         {
            sensors[forwardGradientIndex] = (float)NestingMox.FORWARD_DOWN_GRADIENT;
         }
      }
      else if (c < f)
      {
         sensors[forwardGradientIndex] = (float)NestingMox.FORWARD_UP_GRADIENT;
      }
      else
      {
         if (c > b)
         {
            sensors[forwardGradientIndex] = (float)NestingMox.FORWARD_UP_GRADIENT;
         }
         else if (c < b)
         {
            sensors[forwardGradientIndex] = (float)NestingMox.FORWARD_DOWN_GRADIENT;
         }
         else
         {
            sensors[forwardGradientIndex] = (float)NestingMox.FLAT_GRADIENT;
         }
      }
      int l = nestCells.cells[lx][ly][NestCells.ELEVATION_CELL_INDEX];
      int r = nestCells.cells[rx][ry][NestCells.ELEVATION_CELL_INDEX];
      if (c > r)
      {
         if (c > l)
         {
            sensors[lateralGradientIndex] = (float)NestingMox.PEAK_GRADIENT;
         }
         else
         {
            sensors[lateralGradientIndex] = (float)NestingMox.LEFT_UP_GRADIENT;
         }
      }
      else if (c < r)
      {
         sensors[lateralGradientIndex] = (float)NestingMox.RIGHT_UP_GRADIENT;
      }
      else
      {
         if (c > l)
         {
            sensors[lateralGradientIndex] = (float)NestingMox.RIGHT_UP_GRADIENT;
         }
         else if (c < l)
         {
            sensors[lateralGradientIndex] = (float)NestingMox.LEFT_UP_GRADIENT;
         }
         else
         {
            sensors[lateralGradientIndex] = (float)NestingMox.FLAT_GRADIENT;
         }
      }

      // Cycle mox.
      response = mox.cycle(sensors);

      // Process response.
      if (response == NestingMox.FORWARD)
      {
         mox.x = fx;
         mox.y = fy;
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
      else if (response == NestingMox.TAKE_STONE)
      {
         if (!mox.hasStone)
         {
            if (nestCells.cells[fx][fy][NestCells.STONE_CELL_INDEX] == NestCells.STONE_CELL_VALUE)
            {
               nestCells.cells[fx][fy][NestCells.STONE_CELL_INDEX] = MoxWorx.EMPTY_CELL_VALUE;
               mox.hasStone = true;
            }
         }
      }
      else if (response == NestingMox.DROP_STONE)
      {
         if (mox.hasStone)
         {
            if (nestCells.cells[fx][fy][NestCells.STONE_CELL_INDEX] != NestCells.STONE_CELL_VALUE)
            {
               nestCells.cells[fx][fy][NestCells.STONE_CELL_INDEX] = NestCells.STONE_CELL_VALUE;
               mox.hasStone = false;
            }
         }
      }
   }


   // Create display.
   public void createDisplay()
   {
      if (display == null)
      {
         if (moxen == null)
         {
            display = new NestDisplay(nestCells);
         }
         else
         {
            display = new NestDisplay(nestCells, moxen);
         }
      }
   }


   // Destroy display.
   public void destroyDisplay()
   {
      if (display != null)
      {
         display.close();
         display = null;
      }
   }


   // Update display.
   // Return false for display quit.
   public boolean updateDisplay(int steps)
   {
      if (display != null)
      {
         display.update(steps);
         if (display.quit)
         {
            display = null;
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
      int     numStones         = -1;
      int     maxElevation      = -1;
      int     randomSeed        = DEFAULT_RANDOM_SEED;
      String  loadfile          = null;
      String  savefile          = null;
      boolean display           = false;
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
         if (args[i].equals("-display"))
         {
            display = true;
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
         if (args[i].equals("-numStones"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numStones option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               numStones = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numStones option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (numStones < 0)
            {
               System.err.println("Invalid numStones option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-maxElevation"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid maxElevation option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            try
            {
               NestCells.MAX_ELEVATION_VALUE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid maxElevation option");
               System.err.println(Nest.Usage);
               System.exit(2);
            }
            if (NestCells.MAX_ELEVATION_VALUE < 0)
            {
               System.err.println("Invalid maxElevation option");
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
      if (((steps < 0) && !display) || ((steps >= 0) && display))
      {
         System.err.println(Nest.Usage);
         System.exit(2);
      }
      if (!display)
      {
         if (driver == NestingMox.DRIVER_TYPE.MANUAL.getValue())
         {
            System.err.println("Cannot run manually without display");
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
         if (numStones == -1) { numStones = 0; }
      }
      else
      {
         if ((maxElevation != -1) || (numStones != -1) ||
             (width != -1) || (height != -1) || gotParm)
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
      Nest nest = new Nest(randomSeed);
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
            nest.init(width, height, numStones,
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

      // Create display?
      SectorDisplay.graduatedColors = new boolean[NestingMox.NUM_SENSORS];
      SectorDisplay.graduatedColors[NestingMox.STONE_AHEAD_SENSOR_INDEX]      = false;
      SectorDisplay.graduatedColors[NestingMox.FORWARD_GRADIENT_SENSOR_INDEX] = true;
      SectorDisplay.graduatedColors[NestingMox.LATERAL_GRADIENT_SENSOR_INDEX] = true;
      SectorDisplay.graduatedColors[NestingMox.CARRIED_STONE_SENSOR_INDEX]    = false;
      SectorDisplay.graduatedColorMaximums = new int[NestingMox.NUM_SENSORS];
      SectorDisplay.graduatedColorMaximums[NestingMox.FORWARD_GRADIENT_SENSOR_INDEX] = NestCells.MAX_ELEVATION_VALUE;
      SectorDisplay.graduatedColorMaximums[NestingMox.LATERAL_GRADIENT_SENSOR_INDEX] = NestCells.MAX_ELEVATION_VALUE;
      if (display)
      {
         nest.createDisplay();
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
      nest.run(steps);

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
      System.exit(0);
   }
}
