// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Mox cells.

package moxworx;

import java.io.*;
import java.awt.*;

// Mox cells.
public class MoxCells
{
   // Cell values.
   public static final int EMPTY_CELL_VALUE           = 0;
   public static final int FOOD_CELL_VALUE            = 1;
   public static final int MOX_CELL_VALUE             = 2;
   public static final int OBSTACLE_CELLS_BEGIN_VALUE = 3;

   // Color.
   public static final Color EMPTY_CELL_COLOR = Color.WHITE;
   public static final Color FOOD_CELL_COLOR  = Color.GREEN;
   public static final Color MOX_CELL_COLOR   = Color.BLUE;

   // Cells.
   public Dimension size;
   public int[][]   cells;
   public int[][]   restoreCells;

   // Constructors.
   public MoxCells(Dimension size)
   {
      // Create cells.
      this.size    = size;
      cells        = new int[size.width][size.height];
      restoreCells = new int[size.width][size.height];
      for (int x = 0; x < size.width; x++)
      {
         for (int y = 0; y < size.height; y++)
         {
            cells[x][y] = restoreCells[x][y] = 0;
         }
      }
   }


   public MoxCells()
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


   // Distance to nearest food.
   int foodDist(int x, int y)
   {
      int x2, y2, d, d2;
      int w = size.width;
      int h = size.height;

      d = -1;
      for (x2 = 0; x2 < w; x2++)
      {
         for (y2 = 0; y2 < h; y2++)
         {
            if (cells[x2][y2] == MoxCells.FOOD_CELL_VALUE)
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
      int n, x, y;

      PrintWriter writer = new PrintWriter(output);

      Utility.saveInt(writer, size.width);
      Utility.saveInt(writer, size.height);
      n = 0;
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            if (cells[x][y] > 0)
            {
               n++;
            }
         }
      }
      Utility.saveInt(writer, n);

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            if (cells[x][y] > 0)
            {
               Utility.saveInt(writer, x);
               Utility.saveInt(writer, y);
               Utility.saveInt(writer, cells[x][y]);
            }
         }
      }

      n = 0;
      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            if (restoreCells[x][y] > 0)
            {
               n++;
            }
         }
      }
      Utility.saveInt(writer, n);

      for (x = 0; x < size.width; x++)
      {
         for (y = 0; y < size.height; y++)
         {
            if (restoreCells[x][y] > 0)
            {
               Utility.saveInt(writer, x);
               Utility.saveInt(writer, y);
               Utility.saveInt(writer, restoreCells[x][y]);
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
      int w, h, n, x, y;

      DataInputStream reader = new DataInputStream(input);

      w = Utility.loadInt(reader);
      h = Utility.loadInt(reader);

      size.width   = w;
      size.height  = h;
      cells        = new int[size.width][size.height];
      restoreCells = new int[size.width][size.height];
      clear();

      n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         x = Utility.loadInt(reader);
         y = Utility.loadInt(reader);
         if ((x < 0) || (x >= w))
         {
            throw (new IOException("Invalid x value " + x));
         }
         if ((y < 0) || (y >= h))
         {
            throw (new IOException("Invalid y value " + y));
         }
         cells[x][y] = Utility.loadInt(reader);
      }

      n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         x = Utility.loadInt(reader);
         y = Utility.loadInt(reader);
         if ((x < 0) || (x >= w))
         {
            throw (new IOException("Invalid x value " + x));
         }
         if ((y < 0) || (y >= h))
         {
            throw (new IOException("Invalid y value " + y));
         }
         restoreCells[x][y] = Utility.loadInt(reader);
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
            cells[x][y] = 0;
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
            restoreCells[x][y] = cells[x][y];
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
            cells[x][y] = restoreCells[x][y];
         }
      }
   }
}
