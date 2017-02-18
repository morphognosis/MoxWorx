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
      "     [-numMoxen <quantity> (default=0)]\n" +
      "     [-numStones <quantity> (default=0)]\n" +
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
   Random random;

   // Constructor.
   public Nest()
   {
   }


   // Initialize cells.
   public void initCells(int width, int height, int numStones)
   {
      int i, j, n, x, y;

      // Create cells.
      nestCells = new NestCells(new Dimension(width, height));
      for (x = 0; x < width; x++)
      {
         for (y = 0; y < height; y++)
         {
            nestCells.cells[x][y][NestCells.STONE_CELL_INDEX]               =
               nestCells.restoreCells[x][y][NestCells.STONE_CELL_INDEX]     = MoxWorx.EMPTY_CELL_VALUE;
            nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX]           =
               nestCells.restoreCells[x][y][NestCells.ELEVATION_CELL_INDEX] = MoxWorx.EMPTY_CELL_VALUE;
         }
      }

      // Create stones.
      n = 10;
      for (i = 0; i < numStones; i++)
      {
         for (j = 0; j < n; j++)
         {
            x = random.nextInt(width);
            y = random.nextInt(height);
            if (nestCells.cells[x][y][NestCells.STONE_CELL_INDEX] == MoxWorx.EMPTY_CELL_VALUE)
            {
               nestCells.cells[x][y][NestCells.STONE_CELL_INDEX]           =
                  nestCells.restoreCells[x][y][NestCells.STONE_CELL_INDEX] = NestCells.STONE_CELL_VALUE;
               break;
            }
         }
      }

      // Set elevations.
      x = random.nextInt(width - 2) + 1;
      y = random.nextInt(height - 2) + 1;
      nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX]     = NestCells.NUM_ELEVATION_VALUES - 1;
      nestCells.cells[x + 1][y][NestCells.ELEVATION_CELL_INDEX] = NestCells.NUM_ELEVATION_VALUES - 1;
      nestCells.cells[x - 1][y][NestCells.ELEVATION_CELL_INDEX] = NestCells.NUM_ELEVATION_VALUES - 1;
      nestCells.cells[x][y + 1][NestCells.ELEVATION_CELL_INDEX] = NestCells.NUM_ELEVATION_VALUES - 1;
      nestCells.cells[x][y - 1][NestCells.ELEVATION_CELL_INDEX] = NestCells.NUM_ELEVATION_VALUES - 1;
      for (i = NestCells.NUM_ELEVATION_VALUES - 1; i > MoxWorx.EMPTY_CELL_VALUE; i--)
      {
         for (x = 0; x < width; x++)
         {
            for (y = 0; y < height; y++)
            {
               int m = nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX];
               if (m < i)
               {
                  int x2 = x;
                  int y2 = ((y + 1) % height);
                  int m2 = nestCells.cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
                  x2 = (x + 1) % width;
                  y2 = y;
                  m2 = nestCells.cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
                  x2 = x;
                  y2 = y - 1;
                  if (y2 < 0) { y2 += height; }
                  m2 = nestCells.cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
                  x2 = x - 1;
                  if (x2 < 0) { x2 += width; }
                  y2 = y;
                  m2 = nestCells.cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     nestCells.cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
               }
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
   public void createMoxen(int numMoxen,
                           int NUM_NEIGHBORHOODS,
                           int NEIGHBORHOOD_INITIAL_DIMENSION,
                           int NEIGHBORHOOD_DIMENSION_STRIDE,
                           int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                           int EPOCH_INTERVAL_STRIDE,
                           int EPOCH_INTERVAL_MULTIPLIER)
   {
      int i, x, y, w, h;

      // Create moxen.
      w     = nestCells.size.width;
      h     = nestCells.size.height;
      moxen = new ArrayList<NestingMox>(numMoxen);
      for (i = 0; i < numMoxen; i++)
      {
         x = random.nextInt(w);
         y = random.nextInt(h);
         int o = Orientation.NORTH;
         o = random.nextInt(Orientation.NUM_ORIENTATIONS);
         moxen.add(i, new NestingMox(i, x, y, o, nestCells,
                                     NUM_NEIGHBORHOODS,
                                     NEIGHBORHOOD_INITIAL_DIMENSION,
                                     NEIGHBORHOOD_DIMENSION_STRIDE,
                                     NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                     EPOCH_INTERVAL_STRIDE,
                                     EPOCH_INTERVAL_MULTIPLIER));
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
         mox = new NestingMox(nestCells);
         mox.load(input);
         moxen.add(i, mox);
      }
   }


   // Run.
   public void run(int steps)
   {
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
      int fx, fy, lx, ly, rx, ry, width, height;
      int stoneIndex, elevationIndex;

      int        response;
      NestingMox mox;

      float[] sensors         = new float[NestingMox.NUM_SENSORS];
      stoneIndex              = NestingMox.STONE_SENSOR_INDEX;
      elevationIndex          = NestingMox.ELEVATION_SENSOR_INDEX;
      sensors[stoneIndex]     = 0.0f;
      sensors[elevationIndex] = 0.0f;
      width  = nestCells.size.width;
      height = nestCells.size.height;
      mox    = moxen.get(moxIndex);

      // Update landmarks.
      mox.landmarkMap[mox.x][mox.y] = true;

      // Cycle mox.
      sensors[stoneIndex] = (float)nestCells.cells[mox.x][mox.y][NestCells.STONE_CELL_INDEX];
      sensors[stoneIndex] = (float)nestCells.cells[mox.x][mox.y][NestCells.ELEVATION_CELL_INDEX];
      response            = mox.cycle(sensors);

      // Process response.
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
            if (nestCells.cells[mox.x][mox.y][NestCells.STONE_CELL_INDEX] == NestCells.STONE_CELL_VALUE)
            {
               nestCells.cells[mox.x][mox.y][NestCells.STONE_CELL_INDEX] = MoxWorx.EMPTY_CELL_VALUE;
               mox.hasStone = true;
            }
         }
      }
      else if (response == NestingMox.DROP_STONE)
      {
         if (mox.hasStone)
         {
            if (nestCells.cells[mox.x][mox.y][NestCells.STONE_CELL_INDEX] != NestCells.STONE_CELL_VALUE)
            {
               nestCells.cells[mox.x][mox.y][NestCells.STONE_CELL_INDEX] = NestCells.STONE_CELL_VALUE;
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
      int     numMoxen          = -1;
      int     numStones         = -1;
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
         if (numMoxen == -1) { numMoxen = 0; }
         if (numStones == -1) { numStones = 0; }
      }
      else
      {
         if ((numMoxen != -1) || (numStones != -1) ||
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
            nest.initCells(width, height, numStones);
            nest.createMoxen(numMoxen,
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
      SectorDisplay.graduatedColors = new boolean[NestCells.CELL_DIMENSIONS];
      SectorDisplay.graduatedColors[NestCells.STONE_CELL_INDEX]     = false;
      SectorDisplay.graduatedColors[NestCells.ELEVATION_CELL_INDEX] = true;
      SectorDisplay.graduatedColorMaximums = new int[NestCells.CELL_DIMENSIONS];
      SectorDisplay.graduatedColorMaximums[NestCells.ELEVATION_CELL_INDEX] =
         NestCells.NUM_ELEVATION_VALUES - 1;
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
