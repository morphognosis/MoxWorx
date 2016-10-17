/*
 * Copyright (c) 2016 Tom Portegys (portegys@gmail.com). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY TOM PORTEGYS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// MoxWorx.

package moxworx;

import java.util.*;
import java.io.*;
import java.awt.*;

import javax.swing.*;

// Mox world.
public class MoxWorx
{
   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java MoxWorx\n" +
      "      -steps <steps> | -dashboard\n" +
      "      -dimensions <width> <height>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (mox driver: default=autopilot)]\n" +
      "     [-numMoxen <quantity> (default=0)]\n" +
      "     [-numObstacleTypes <quantity> (default=1)]\n" +
      "     [-numObstacles <quantity> (default=0)]\n" +
      "     [-numFoods <quantity> (default=0)]\n" +
      "     [-numNeighborhoods <quantity> (default=" + Morphognostic.NUM_NEIGHBORHOODS + ")]\n" +
      "     [-neighborhoodInitialDimension <quantity> (default=" + Morphognostic.NEIGHBORHOOD_INITIAL_DIMENSION + ")]\n" +
      "     [-neighborhoodDimensionStride <quantity> (default=" + Morphognostic.NEIGHBORHOOD_DIMENSION_STRIDE + ")]\n" +
      "     [-neighborhoodDimensionMultiplier <quantity> (default=" + Morphognostic.NEIGHBORHOOD_DIMENSION_MULTIPLIER + ")]\n" +
      "     [-epochIntervalStride <quantity> (default=" + Morphognostic.EPOCH_INTERVAL_STRIDE + ")]\n" +
      "     [-epochIntervalMultiplier <quantity> (default=" + Morphognostic.EPOCH_INTERVAL_MULTIPLIER + ")]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]\n" +
      "  Resume run:\n" +
      "    java MoxWorx\n" +
      "      -steps <steps> | -dashboard\n" +
      "      -load <file name>\n" +
      "     [-driver <metamorphDB | metamorphNN | autopilot> (default=autopilot)]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "     [-save <file name>]";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Number of obstacle types.
   int numObstacleTypes;

   // Moxen.
   ArrayList<Mox> moxen;

   // Cells.
   MoxCells moxCells;

   // Dashboard display.
   MoxWorxDashboard dashboard;

   // Random numbers.
   Random random;

   // Constructor.
   public MoxWorx()
   {
   }


   // Initialize cells.
   public void initCells(int width, int height, int numObstacleTypes, int numObstacles, int numFoods)
   {
      int i, j, n, x, y;

      this.numObstacleTypes = numObstacleTypes;

      // Create cells.
      moxCells = new MoxCells(new Dimension(width, height));
      for (x = 0; x < width; x++)
      {
         for (y = 0; y < height; y++)
         {
            moxCells.cells[x][y] = moxCells.restoreCells[x][y] = MoxCells.EMPTY_CELL_VALUE;
         }
      }

      // Create landmarks.
      n = 10;
      for (i = 0; i < numObstacles; i++)
      {
         for (j = 0; j < n; j++)
         {
            x = random.nextInt(width);
            y = random.nextInt(height);
            if (moxCells.cells[x][y] == MoxCells.EMPTY_CELL_VALUE)
            {
               int k = 0;
               if (numObstacleTypes > 1)
               {
                  k = random.nextInt(numObstacleTypes);
               }
               moxCells.cells[x][y] = moxCells.restoreCells[x][y] = MoxCells.OBSTACLE_CELLS_BEGIN_VALUE + k;
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
            if (moxCells.cells[x][y] == MoxCells.EMPTY_CELL_VALUE)
            {
               moxCells.cells[x][y] = moxCells.restoreCells[x][y] = MoxCells.FOOD_CELL_VALUE;
               break;
            }
         }
      }
      moxCells.checkpoint();
   }


   // Get width.
   int getWidth()
   {
      if (moxCells != null)
      {
         return(moxCells.size.width);
      }
      else
      {
         return(0);
      }
   }


   // Get height.
   int getHeight()
   {
      if (moxCells != null)
      {
         return(moxCells.size.height);
      }
      else
      {
         return(0);
      }
   }


   // Create moxen.
   public void createMoxen(int numMoxen, int numLandmarkTypes)
   {
      int i, j, n, x, y, w, h;

      // Create moxen.
      w     = moxCells.size.width;
      h     = moxCells.size.height;
      moxen = new ArrayList<Mox>(numMoxen);
      n     = 10;
      for (i = 0; i < numMoxen; i++)
      {
         for (j = 0; j < n; j++)
         {
            x = random.nextInt(w);
            y = random.nextInt(h);
            if (moxCells.cells[x][y] == MoxCells.EMPTY_CELL_VALUE)
            {
               int o = Orientation.NORTH;
               o = random.nextInt(Orientation.NUM_ORIENTATIONS);
               moxen.add(i, new Mox(i, x, y, o, numLandmarkTypes, moxCells));
               moxCells.cells[x][y] = moxCells.restoreCells[x][y] = MoxCells.MOX_CELL_VALUE;
               break;
            }
         }
      }
   }


   // Set moxen.
   public void setMoxen(ArrayList<Mox> moxen)
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
      if (moxCells != null)
      {
         moxCells.restore();
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
      moxCells = null;
      if (moxen != null)
      {
         int numMoxen = moxen.size();
         for (int i = 0; i < numMoxen; i++)
         {
            moxen.get(i).init();
         }
         moxen = null;
      }
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

      // Save parameters.
      Utility.saveInt(writer, Morphognostic.NUM_NEIGHBORHOODS);
      Utility.saveInt(writer, Morphognostic.NEIGHBORHOOD_INITIAL_DIMENSION);
      Utility.saveInt(writer, Morphognostic.NEIGHBORHOOD_DIMENSION_STRIDE);
      Utility.saveInt(writer, Morphognostic.NEIGHBORHOOD_DIMENSION_MULTIPLIER);
      Utility.saveInt(writer, Morphognostic.EPOCH_INTERVAL_STRIDE);
      Utility.saveInt(writer, Morphognostic.EPOCH_INTERVAL_MULTIPLIER);
      Utility.saveInt(writer, numObstacleTypes);

      // Save cells.
      moxCells.save(output);

      // Save moxen.
      int numMoxen = moxen.size();
      Utility.saveInt(writer, numMoxen);
      writer.flush();
      Mox mox;
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

      // Load parameters.
      Morphognostic.NUM_NEIGHBORHOODS = Utility.loadInt(reader);
      Morphognostic.NEIGHBORHOOD_INITIAL_DIMENSION    = Utility.loadInt(reader);
      Morphognostic.NEIGHBORHOOD_DIMENSION_STRIDE     = Utility.loadInt(reader);
      Morphognostic.NEIGHBORHOOD_DIMENSION_MULTIPLIER = Utility.loadInt(reader);
      Morphognostic.EPOCH_INTERVAL_STRIDE             = Utility.loadInt(reader);
      Morphognostic.EPOCH_INTERVAL_MULTIPLIER         = Utility.loadInt(reader);
      numObstacleTypes = Utility.loadInt(reader);

      // Load cells.
      moxCells = new MoxCells();
      moxCells.load(input);

      // Load moxen.
      int numMoxen = Utility.loadInt(reader);
      moxen = new ArrayList<Mox>(numMoxen);
      Mox mox;
      for (int i = 0; i < numMoxen; i++)
      {
         mox = new Mox(moxCells);
         mox.load(input);
         moxen.add(i, mox);
      }
   }


   // Run.
   public void run(int steps)
   {
      if (steps >= 0)
      {
         for (int i = 0; i < steps; i++)
         {
            stepMoxen();
         }
      }
      else
      {
         for (int i = 0; updateDashboard(i); i++)
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
      int landmarkIndex, foodIndex;

      int response;
      Mox mox;

      float[] sensors        = new float[Mox.SENSOR_CONFIG.NUM_SENSORS.getValue()];
      landmarkIndex          = Mox.SENSOR_CONFIG.LANDMARK_SENSOR_INDEX.getValue();
      foodIndex              = Mox.SENSOR_CONFIG.FOOD_SENSOR_INDEX.getValue();
      sensors[landmarkIndex] = 0.0f;
      sensors[foodIndex]     = 0.0f;
      width  = moxCells.size.width;
      height = moxCells.size.height;
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
      if (moxCells.cells[fx][fy] != MoxCells.EMPTY_CELL_VALUE)
      {
         sensors[landmarkIndex] = 1.0f;
      }
      if (moxCells.cells[fx][fy] >= MoxCells.OBSTACLE_CELLS_BEGIN_VALUE)
      {
         mox.obstacleMap[fx][fy] = true;
      }

      // Detect food.
      sensors[foodIndex] = 1.0f / ((float)moxCells.foodDist(fx, fy) + 1.0f);

      // Cycle mox.
      response = mox.cycle(sensors);

      // Landmark event.
      mox.landmark(fx, fy);

      // Process response.
      if (response == Mox.FORWARD)
      {
         if (moxCells.cells[fx][fy] == MoxCells.EMPTY_CELL_VALUE)
         {
            moxCells.cells[mox.x][mox.y] = MoxCells.EMPTY_CELL_VALUE;
            mox.x = fx;
            mox.y = fy;
            moxCells.cells[mox.x][mox.y] = MoxCells.MOX_CELL_VALUE;
         }
      }
      else if (response == Mox.RIGHT)
      {
         mox.direction = (mox.direction + 1) % Orientation.NUM_ORIENTATIONS;
      }
      else if (response == Mox.LEFT)
      {
         mox.direction = mox.direction - 1;
         if (mox.direction < 0)
         {
            mox.direction = mox.direction + Orientation.NUM_ORIENTATIONS;
         }
      }
      else if (response == Mox.EAT)
      {
         if (moxCells.cells[fx][fy] == MoxCells.FOOD_CELL_VALUE)
         {
            moxCells.cells[fx][fy] = MoxCells.EMPTY_CELL_VALUE;
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
            dashboard = new MoxWorxDashboard(moxCells, numObstacleTypes);
         }
         else
         {
            dashboard = new MoxWorxDashboard(moxCells, numObstacleTypes, moxen);
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
   public static void main(String[] args)
   {
      // Get options.
      int     steps            = -1;
      int     width            = -1;
      int     height           = -1;
      int     driver           = Mox.DRIVER_TYPE.AUTOPILOT.getValue();
      int     numMoxen         = -1;
      int     numObstacleTypes = -1;
      int     numObstacles     = -1;
      int     numFoods         = -1;
      int     randomSeed       = DEFAULT_RANDOM_SEED;
      String  loadfile         = null;
      String  savefile         = null;
      boolean dashboard        = false;
      boolean gotParm          = false;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-steps"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid steps option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               steps = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid steps option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (steps < 0)
            {
               System.err.println("Invalid steps option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               width = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid width option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (width < 2)
            {
               System.err.println("Invalid width option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               height = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid height option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (height < 2)
            {
               System.err.println("Invalid height option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-driver"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid driver option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (args[i].equals("metamorphDB"))
            {
               driver = Mox.DRIVER_TYPE.METAMORPH_DB.getValue();
            }
            else if (args[i].equals("metamorphNN"))
            {
               driver = Mox.DRIVER_TYPE.METAMORPH_NN.getValue();
            }
            else if (args[i].equals("autopilot"))
            {
               driver = Mox.DRIVER_TYPE.AUTOPILOT.getValue();
            }
            else
            {
               System.err.println("Invalid driver option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numMoxen"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numMoxen option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               numMoxen = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numMoxen option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (numMoxen < 0)
            {
               System.err.println("Invalid numMoxen option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numObstacleTypes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numObstacleTypes option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               numObstacleTypes = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numObstacleTypes option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (numObstacleTypes < 1)
            {
               System.err.println("Invalid numObstacleTypes option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numObstacles"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numObstacles option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               numObstacles = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numObstacles option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (numObstacles < 0)
            {
               System.err.println("Invalid numObstacles option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numFoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numFoods option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               numFoods = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numFoods option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (numFoods < 0)
            {
               System.err.println("Invalid numFoods option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-numNeighborhoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               Morphognostic.NUM_NEIGHBORHOODS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (Morphognostic.NUM_NEIGHBORHOODS < 0)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               Morphognostic.NEIGHBORHOOD_INITIAL_DIMENSION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if ((Morphognostic.NEIGHBORHOOD_INITIAL_DIMENSION < 3) ||
                ((Morphognostic.NEIGHBORHOOD_INITIAL_DIMENSION % 2) == 0))
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               Morphognostic.NEIGHBORHOOD_DIMENSION_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (Morphognostic.NEIGHBORHOOD_DIMENSION_STRIDE < 0)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               Morphognostic.NEIGHBORHOOD_DIMENSION_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (Morphognostic.NEIGHBORHOOD_DIMENSION_MULTIPLIER < 0)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               Morphognostic.EPOCH_INTERVAL_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (Morphognostic.EPOCH_INTERVAL_STRIDE < 0)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               Morphognostic.EPOCH_INTERVAL_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (Morphognostic.EPOCH_INTERVAL_MULTIPLIER < 0)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
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
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            try
            {
               randomSeed = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-load"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid load option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (loadfile == null)
            {
               loadfile = args[i];
            }
            else
            {
               System.err.println("Duplicate load option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-save"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid save option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            if (savefile == null)
            {
               savefile = args[i];
            }
            else
            {
               System.err.println("Duplicate save option");
               System.err.println(MoxWorx.Usage);
               System.exit(1);
            }
            continue;
         }
         System.err.println(MoxWorx.Usage);
         System.exit(1);
      }

      // Check options.
      if (((steps < 0) && !dashboard) || ((steps >= 0) && dashboard))
      {
         System.err.println(MoxWorx.Usage);
         System.exit(1);
      }
      if (loadfile == null)
      {
         if ((width == -1) || (height == -1))
         {
            System.err.println(MoxWorx.Usage);
            System.exit(1);
         }
         if (numMoxen == -1) { numMoxen = 0; }
         if (numObstacleTypes == -1) { numObstacleTypes = 1; }
         if (numObstacles == -1) { numObstacles = 0; }
         if (numFoods == -1) { numFoods = 0; }
      }
      else
      {
         if ((numMoxen != -1) || (numObstacleTypes != -1) || (numObstacles != -1) ||
             (numFoods != -1) || (width != -1) || (height != -1) || gotParm)
         {
            System.err.println(MoxWorx.Usage);
            System.exit(1);
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
      MoxWorx MoxWorx = new MoxWorx();
      MoxWorx.random = new Random(randomSeed);
      if (loadfile != null)
      {
         try
         {
            MoxWorx.load(loadfile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot load from file " + loadfile + ": " + e.getMessage());
            System.exit(1);
         }
      }
      else
      {
         try
         {
            MoxWorx.initCells(width, height, numObstacleTypes, numObstacles, numFoods);
            MoxWorx.createMoxen(numMoxen, numObstacleTypes + 1);
         }
         catch (Exception e)
         {
            System.err.println("Cannot initialize: " + e.getMessage());
            System.exit(1);
         }
      }

      // Create dashboard?
      if (dashboard)
      {
         MoxWorx.createDashboard();
      }

      // Set mox driver.
      for (Mox mox : MoxWorx.moxen)
      {
         mox.driver = driver;
         if (driver == Mox.DRIVER_TYPE.METAMORPH_NN.getValue())
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
      MoxWorx.run(steps);

      // Save?
      if (savefile != null)
      {
         try
         {
            MoxWorx.save(savefile);
         }
         catch (Exception e)
         {
            System.err.println("Cannot save to file " + savefile + ": " + e.getMessage());
         }
      }
      System.exit(0);
   }
}
