// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Nest cells.

package morphognosis.moxworx;

import java.io.*;
import java.security.SecureRandom;

import morphognosis.Utility;

import java.awt.*;

public class NestCells
{
   // Cell values.
   // See MoxWorx.EMPTY_CELL_VALUE.
   public static final int STONE_CELL_VALUE    = 1;
   public static final int NUM_STONE_VALUES    = 2;
   public static int       MAX_ELEVATION_VALUE = 4;

   // Cells.
   public static final int CELL_DIMENSIONS      = 2;
   public static final int STONE_CELL_INDEX     = 0;
   public static final int ELEVATION_CELL_INDEX = 1;
   public Dimension        size;
   public int[][][]        cells;
   public int[][][]        restoreCells;
   int nestX, nestY;

   // Constructors.
   public NestCells(Dimension size, int numStones, int randomSeed)
   {
      int i, j, n, x, y, width, height;

      // Create cells.
      this.size    = size;
      width        = size.width;
      height       = size.height;
      cells        = new int[width][height][CELL_DIMENSIONS];
      restoreCells = new int[width][height][CELL_DIMENSIONS];
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d]        = MoxWorx.EMPTY_CELL_VALUE;
               restoreCells[x][y][d] = MoxWorx.EMPTY_CELL_VALUE;
            }
         }
      }

      // Set elevations.
      SecureRandom random = new SecureRandom();
      random.setSeed(randomSeed);
      nestX = random.nextInt(width - 2) + 1;
      nestY = random.nextInt(height - 2) + 1;
      cells[nestX][nestY][NestCells.ELEVATION_CELL_INDEX] = NestCells.MAX_ELEVATION_VALUE;
      for (i = NestCells.MAX_ELEVATION_VALUE; i > MoxWorx.EMPTY_CELL_VALUE; i--)
      {
         for (x = 0; x < width; x++)
         {
            for (y = 0; y < height; y++)
            {
               int m = cells[x][y][NestCells.ELEVATION_CELL_INDEX];
               if (m < i)
               {
                  int x2 = x;
                  int y2 = ((y + 1) % height);
                  int m2 = cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
                  x2 = (x + 1) % width;
                  y2 = y;
                  m2 = cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
                  x2 = x;
                  y2 = y - 1;
                  if (y2 < 0) { y2 += height; }
                  m2 = cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
                  x2 = x - 1;
                  if (x2 < 0) { x2 += width; }
                  y2 = y;
                  m2 = cells[x2][y2][NestCells.ELEVATION_CELL_INDEX];
                  if (m2 == i)
                  {
                     cells[x][y][NestCells.ELEVATION_CELL_INDEX] = i - 1;
                     continue;
                  }
               }
            }
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
            if ((cells[x][y][NestCells.STONE_CELL_INDEX] == MoxWorx.EMPTY_CELL_VALUE) &&
                ((x != nestX) || (y != nestY)))
            {
               cells[x][y][NestCells.STONE_CELL_INDEX]           =
                  restoreCells[x][y][NestCells.STONE_CELL_INDEX] = NestCells.STONE_CELL_VALUE;
               break;
            }
         }
      }
      checkpoint();
   }


   public NestCells()
   {
      size = new Dimension();
   }


   // Get grid width.
   public int getWidth()
   {
      return(size.width);
   }


   // Get grid height.
   public int getHeight()
   {
      return(size.height);
   }


   // Distance to nearest stone.
   int stoneDist(int x, int y)
   {
      int x2, y2, d, d2;
      int w = size.width;
      int h = size.height;

      d = -1;
      for (x2 = 0; x2 < w; x2++)
      {
         for (y2 = 0; y2 < h; y2++)
         {
            if (cells[x2][y2][STONE_CELL_INDEX] == STONE_CELL_VALUE)
            {
               d2 = cellDist(x, y, x2, y2);
               if ((d == -1) || (d2 < d))
               {
                  d = d2;
               }
            }
         }
      }
      return(d);
   }


   // Cell distance.
   public int cellDist(int fromX, int fromY, int toX, int toY)
   {
      int w  = size.width;
      int w2 = w / 2;
      int h  = size.height;
      int h2 = h / 2;
      int dx = Math.abs(toX - fromX);

      if (dx > w2) { dx = w - dx; }
      int dy = Math.abs(toY - fromY);
      if (dy > h2) { dy = h - dy; }
      return(dx + dy);
   }


   // Save cells.
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


   // Save cells.
   public void save(FileOutputStream output) throws IOException
   {
      int x, y;

      DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(output));

      Utility.saveInt(writer, size.width);
      Utility.saveInt(writer, size.height);
      Utility.saveInt(writer, NestCells.MAX_ELEVATION_VALUE);

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               Utility.saveInt(writer, cells[x][y][d]);
            }
         }
      }

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               Utility.saveInt(writer, restoreCells[x][y][d]);
            }
         }
      }
   }


   // Load cells from file.
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


   // Load cells.
   public void load(FileInputStream input) throws IOException
   {
      int w, h, x, y;

      DataInputStream reader = new DataInputStream(input);

      w = Utility.loadInt(reader);
      h = Utility.loadInt(reader);
      NestCells.MAX_ELEVATION_VALUE = Utility.loadInt(reader);

      size.width   = w;
      size.height  = h;
      cells        = new int[size.width][size.height][2];
      restoreCells = new int[size.width][size.height][2];
      clear();

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = Utility.loadInt(reader);
            }
         }
      }

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               restoreCells[x][y][d] = Utility.loadInt(reader);
            }
         }
      }
   }


   // Clear cells.
   public void clear()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = MoxWorx.EMPTY_CELL_VALUE;
            }
         }
      }
   }


   // Checkpoint cells.
   public void checkpoint()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               restoreCells[x][y][d] = cells[x][y][d];
            }
         }
      }
   }


   // Restore cells.
   public void restore()
   {
      int x, y;

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            for (int d = 0; d < CELL_DIMENSIONS; d++)
            {
               cells[x][y][d] = restoreCells[x][y][d];
            }
         }
      }
   }
}
