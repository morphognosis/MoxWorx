// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Pong game cells.

package moxworx;

import java.io.*;
import java.awt.*;

public class PongCells
{
   // Cell values.
   // See MoxWorx.EMPTY_CELL_VALUE.
   public static final int LANDMARK_CELLS_BEGIN_VALUE = 1;

   // Cells.
   public Dimension size;
   public int[][]   cells;
   public int[][]   restoreCells;

   // Constructors.
   public PongCells(Dimension size)
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


   public PongCells()
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
