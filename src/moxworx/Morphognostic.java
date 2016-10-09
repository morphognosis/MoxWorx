// For conditions of distribution and use, see copyright notice in MoxWorx.java

package moxworx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

/*
 * Morphognostic space-time neighborhoods:
 * Neighborhoods are nested by increasing spatial
 * size and receding temporal distance from the present.
 * A neighborhood is a tiled configuration of sectors.
 * A sector is a cube of 2D space-time which contains a
 * vector of landmark type densities contained within it.
 */
public class Morphognostic
{
   // Parameters.
   public static int NUM_NEIGHBORHOODS = 2;
   public static int NEIGHBORHOOD_INITIAL_DIMENSION    = 3;
   public static int NEIGHBORHOOD_DIMENSION_STRIDE     = 0;
   public static int NEIGHBORHOOD_DIMENSION_MULTIPLIER = 3;
   public static int EPOCH_INTERVAL_STRIDE             = 1;
   public static int EPOCH_INTERVAL_MULTIPLIER         = 3;
   public static int NUM_LANDMARK_TYPES = 2;

   // Neighborhood.
   public class Neighborhood
   {
      public int dx, dy, dimension;
      public int epoch, duration;

      // Sector.
      public class Sector
      {
         public int     dx, dy, dimension;
         public float[] typeDensities;
         public int[][] landmarks;

         public Sector(int dx, int dy, int dimension)
         {
            this.dx        = dx;
            this.dy        = dy;
            this.dimension = dimension;
            typeDensities  = new float[NUM_LANDMARK_TYPES];
            landmarks      = new int[dimension][dimension];
            for (int x = 0; x < dimension; x++)
            {
               for (int y = 0; y < dimension; y++)
               {
                  landmarks[x][y] = -1;
               }
            }
         }


         public void setTypeDensity(int index, float density)
         {
            typeDensities[index] = density;
         }


         public float getTypeDensity(int index)
         {
            return(typeDensities[index]);
         }
      }

      // Sectors.
      public Sector[][] sectors;

      // Constructor.
      public Neighborhood(int dx, int dy, int dimension,
                          int epoch, int duration, int sectorDimension)
      {
         this.dx        = dx;
         this.dy        = dy;
         this.dimension = dimension;
         this.epoch     = epoch;
         this.duration  = duration;
         int d = dimension / sectorDimension;
         if ((d * sectorDimension) < dimension) { d++; }
         sectors = new Sector[d][d];
         float f = 0.0f;
         if (d > 1)
         {
            f = (float)((d * sectorDimension) - dimension) / (float)(d - 1);
         }
         for (int x = 0; x < d; x++)
         {
            for (int y = 0; y < d; y++)
            {
               int sdx = (int)((float)(x * sectorDimension) - ((float)x * f));
               int sdy = (int)((float)(y * sectorDimension) - ((float)y * f));
               sectors[x][y] = new Sector(sdx, sdy, sectorDimension);
            }
         }
      }


      // Update.
      public void update(int[][][] landmarks, int cx, int cy)
      {
         // Clear type densities.
         for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
         {
            for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               Sector s = sectors[sx1][sy1];
               for (int i = 0; i < NUM_LANDMARK_TYPES; i++)
               {
                  s.typeDensities[i] = 0.0f;
               }
               for (int x = 0; x < s.dimension; x++)
               {
                  for (int y = 0; y < s.dimension; y++)
                  {
                     s.landmarks[x][y] = -1;
                  }
               }
            }
         }

         // Accumulate types per sector.
         for (int x0 = cx + dx, x1 = x0, x2 = x0 + dimension; x1 < x2; x1++)
         {
            for (int y0 = cy + dy, y1 = y0, y2 = y0 + dimension; y1 < y2; y1++)
            {
               if ((x1 >= 0) && (x1 < landmarks.length) &&
                   (y1 >= 0) && (y1 < landmarks[x1].length))
               {
                  int t = -1;
                  for (int i = epoch + duration - 1; i >= epoch; i--)
                  {
                     if (landmarks[x1][y1][i] != -1)
                     {
                        t = landmarks[x1][y1][i];
                     }
                  }
                  if (t != -1)
                  {
                     for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
                     {
                        for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
                        {
                           int    x3 = x1 - x0;
                           int    y3 = y1 - y0;
                           Sector s  = sectors[sx1][sy1];
                           if ((x3 >= s.dx) &&
                               (x3 < (s.dx + s.dimension)) &&
                               (y3 >= s.dy) &&
                               (y3 < (s.dy + s.dimension)))
                           {
                              s.typeDensities[t] += 1.0f;
                              s.landmarks[x3 - s.dx][y3 - s.dy] = t;
                           }
                        }
                     }
                  }
               }
            }
         }

         // Type density is relative to sector dimension.
         for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
         {
            for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               Sector s = sectors[sx1][sy1];
               for (int i = 0; i < NUM_LANDMARK_TYPES; i++)
               {
                  s.typeDensities[i] /= (float)sx2;
               }
            }
         }
      }


      // Compare neighborhood.
      public float compare(Neighborhood n)
      {
         float d = 0.0f;

         float[][] densities1 = rectifySectorTypeDensities();
         float[][] densities2 = n.rectifySectorTypeDensities();
         for (int i = 0, j = sectors.length * sectors.length; i < j; i++)
         {
            for (int k = 0; k < NUM_LANDMARK_TYPES; k++)
            {
               d += Math.abs(densities1[i][k] - densities2[i][k]);
            }
         }
         return(d);
      }


      // Rectify sector densities.
      public float[][] rectifySectorTypeDensities()
      {
         float[][] densities = new float[sectors.length * sectors.length][NUM_LANDMARK_TYPES];
         switch (orientation)
         {
         case Orientation.NORTH:
            for (int i = 0, sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
            {
               for (int sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
               {
                  for (int j = 0; j < NUM_LANDMARK_TYPES; j++)
                  {
                     densities[i][j] = sectors[sx1][sy1].typeDensities[j];
                  }
                  i++;
               }
            }
            break;

         case Orientation.SOUTH:
            for (int i = 0, sy1 = sectors.length - 1; sy1 >= 0; sy1--)
            {
               for (int sx1 = sectors.length - 1; sx1 >= 0; sx1--)
               {
                  for (int j = 0; j < NUM_LANDMARK_TYPES; j++)
                  {
                     densities[i][j] = sectors[sx1][sy1].typeDensities[j];
                  }
                  i++;
               }
            }
            break;

         case Orientation.EAST:
            for (int i = 0, sx1 = sectors.length - 1; sx1 >= 0; sx1--)
            {
               for (int sy1 = 0, sy2 = sectors.length; sy1 < sy2; sy1++)
               {
                  for (int j = 0; j < NUM_LANDMARK_TYPES; j++)
                  {
                     densities[i][j] = sectors[sx1][sy1].typeDensities[j];
                  }
                  i++;
               }
            }
            break;

         case Orientation.WEST:
            for (int i = 0, sx1 = 0, sx2 = sectors.length; sx1 < sx2; sx1++)
            {
               for (int sy1 = sectors.length - 1; sy1 >= 0; sy1--)
               {
                  for (int j = 0; j < NUM_LANDMARK_TYPES; j++)
                  {
                     densities[i][j] = sectors[sx1][sy1].typeDensities[j];
                  }
                  i++;
               }
            }
            break;

         default:
            break;
         }
         return(densities);
      }
   }

   // Neighborhoods.
   public Vector<Neighborhood> neighborhoods;

   // Orientation.
   public int orientation;

   // Constructors.
   public Morphognostic(int orientation)
   {
      this.orientation = orientation;

      // Create neighborhoods.
      neighborhoods = new Vector<Neighborhood>();
      int d  = NEIGHBORHOOD_INITIAL_DIMENSION;
      int s  = 1;
      int t1 = 0;
      int t2 = EPOCH_INTERVAL_STRIDE;
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         neighborhoods.add(new Neighborhood(-d / 2, -d / 2, d, t1, t2 - t1, s));
         s   = d;
         d  *= NEIGHBORHOOD_DIMENSION_MULTIPLIER;
         d  += NEIGHBORHOOD_DIMENSION_STRIDE;
         t1  = t2;
         t2 *= EPOCH_INTERVAL_MULTIPLIER;
         t2 += EPOCH_INTERVAL_STRIDE;
      }
   }


   // Update.
   public void update(int[][][] landmarks, int cx, int cy)
   {
      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         neighborhoods.get(i).update(landmarks, cx, cy);
      }
   }


   // Compare.
   public float compare(Morphognostic m)
   {
      float d = 0.0f;

      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         d += neighborhoods.get(i).compare(m.neighborhoods.get(i));
      }
      return(d);
   }


   // Save.
   public void save(FileOutputStream output) throws IOException
   {
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));

      for (Neighborhood n : neighborhoods)
      {
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int i = 0; i < NUM_LANDMARK_TYPES; i++)
               {
                  Utility.saveFloat(writer, s.typeDensities[i]);
               }
               for (int x2 = 0; x2 < s.landmarks.length; x2++)
               {
                  for (int y2 = 0; y2 < s.landmarks.length; y2++)
                  {
                     Utility.saveInt(writer, s.landmarks[x2][y2]);
                  }
               }
            }
         }
      }
      Utility.saveInt(writer, orientation);
      writer.flush();
   }


   // Load.
   public static Morphognostic load(FileInputStream input) throws EOFException, IOException
   {
      DataInputStream reader = new DataInputStream(input);
      Morphognostic   m      = new Morphognostic(Orientation.NORTH);

      for (Neighborhood n : m.neighborhoods)
      {
         for (int x = 0; x < n.sectors.length; x++)
         {
            for (int y = 0; y < n.sectors.length; y++)
            {
               Neighborhood.Sector s = n.sectors[x][y];
               for (int i = 0; i < NUM_LANDMARK_TYPES; i++)
               {
                  s.typeDensities[i] = Utility.loadFloat(reader);
               }
               for (int x2 = 0; x2 < s.landmarks.length; x2++)
               {
                  for (int y2 = 0; y2 < s.landmarks.length; y2++)
                  {
                     s.landmarks[x2][y2] = Utility.loadInt(reader);
                  }
               }
            }
         }
      }
      m.orientation = Utility.loadInt(reader);
      return(m);
   }


   // Clone.
   public Morphognostic clone()
   {
      Morphognostic m = new Morphognostic(orientation);

      for (int i = 0; i < NUM_NEIGHBORHOODS; i++)
      {
         Neighborhood n1 = m.neighborhoods.get(i);
         Neighborhood n2 = neighborhoods.get(i);
         for (int x = 0; x < n1.sectors.length; x++)
         {
            for (int y = 0; y < n1.sectors.length; y++)
            {
               Neighborhood.Sector s1 = n1.sectors[x][y];
               Neighborhood.Sector s2 = n2.sectors[x][y];
               for (int j = 0; j < NUM_LANDMARK_TYPES; j++)
               {
                  s1.typeDensities[j] = s2.typeDensities[j];
               }
               for (int x2 = 0; x2 < s1.landmarks.length; x2++)
               {
                  for (int y2 = 0; y2 < s1.landmarks.length; y2++)
                  {
                     s1.landmarks[x2][y2] = s2.landmarks[x2][y2];
                  }
               }
            }
         }
      }
      return(m);
   }


   // Print.
   public void print()
   {
   }
}
