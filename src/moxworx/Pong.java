// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Pong game task.

package moxworx;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import morphognosis.Morphognostic;
import morphognosis.Orientation;

public class Pong
{
   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "    java moxworx.Pong\n" +
      "      -pongTrainingFile <file name>\n" +
      "      -pongTestingFile <file name>\n" +
      "     [-numNeighborhoods <quantity> (default=" + Morphognostic.DEFAULT_NUM_NEIGHBORHOODS + ")]\n" +
      "     [-neighborhoodInitialDimension <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION + ")]\n" +
      "     [-neighborhoodDimensionStride <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE + ")]\n" +
      "     [-neighborhoodDimensionMultiplier <quantity> (default=" + Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER + ")]\n" +
      "     [-epochIntervalStride <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE + ")]\n" +
      "     [-epochIntervalMultiplier <quantity> (default=" + Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER + ")]\n" +
      "     [-randomSeed <random number seed>]\n" +
      "Exit codes:\n" +
      "  0=success\n" +
      "  1=fail\n" +
      "  2=error";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Mox.
   PongMox mox;

   // Cells.
   PongCells pongCells;

   // Random numbers.
   Random random;

   // Constructor.
   public Pong()
   {
   }


   // Pong sensory-response event.
   class PongEvent
   {
      int ballState;
      int paddleState;
      int response;
      PongEvent(int ballState, int paddleState, int response)
      {
         this.ballState   = ballState;
         this.paddleState = paddleState;
         this.response    = response;
      }
   };

   // Pong game sequence.
   class PongGame
   {
      int number;
      ArrayList<PongEvent> sequence;
      PongGame(int number)
      {
         this.number = number;
         sequence    = new ArrayList<PongEvent>();
      }
   };

   // Pong dimensions.
   int pongDimensions;

   // Pong training and testing games.
   ArrayList<PongGame> pongTrainingGames;
   ArrayList<PongGame> pongTestingGames;

   // Load pong games.
   // Return dimensions.
   int loadPongGames(String filename, ArrayList<PongGame> games) throws IOException
   {
      int dimensions;

      BufferedReader input;

      try
      {
         input = new BufferedReader(new FileReader(filename));
      }
      catch (Exception e)
      {
         throw new IOException("Cannot open input file " + filename + ":" + e.getMessage());
      }

      // Load dimensions.
      String s = input.readLine();
      dimensions = Integer.parseInt(s);

      // Load game sequences.
      try
      {
         PongGame currentGame = null;
         while ((s = input.readLine()) != null)
         {
            String[] p = s.split(",");
            int       gameNum     = Integer.parseInt(p[0]);
            int       ballState   = Integer.parseInt(p[1]);
            int       paddleState = Integer.parseInt(p[2]);
            int       response    = Integer.parseInt(p[3]);
            PongEvent e           = new PongEvent(ballState, paddleState, response);
            if ((currentGame == null) || (gameNum != currentGame.number))
            {
               currentGame = new PongGame(gameNum);
               games.add(currentGame);
            }
            currentGame.sequence.add(e);
         }
      }
      catch (Exception e) {}
      input.close();
      return(dimensions);
   }


   // Load pong.
   public void loadPong(String pongTrainingFile, String pongTestingFile) throws IOException
   {
      // Load training games.
      pongTrainingGames = new ArrayList<PongGame>();
      pongDimensions    = loadPongGames(pongTrainingFile, pongTrainingGames);

      // Load testing games.
      pongTestingGames = new ArrayList<PongGame>();
      int d = loadPongGames(pongTestingFile, pongTestingGames);
      if (d != pongDimensions)
      {
         System.err.println("Training and testing dimensions mismatch");
         System.exit(2);
      }
   }


   // Run pong.
   public void runPong() throws Exception
   {
      Dimension dimensions = new Dimension();

      dimensions.width  = pongDimensions;
      dimensions.height = dimensions.width;
      pongCells         = new PongCells(dimensions);
      int x = dimensions.width / 2;
      int y = x;
      mox = new PongMox(0, x, y, Orientation.NORTH, 8, pongCells);

      // Train.
      mox.driver = PongMox.DRIVER_TYPE.MANUAL.getValue();
      for (PongGame game : pongTrainingGames)
      {
         System.out.println("Training game = " + game.number);
         playPongGame(game, mox);
      }

      // Test.
      mox.driver = PongMox.DRIVER_TYPE.METAMORPH_DB.getValue();
      //mox.driver = Mox.DRIVER_TYPE.METAMORPH_NN.getValue();
      //mox.createMetamorphNN();
      for (PongGame game : pongTestingGames)
      {
         System.out.print("Testing game = " + game.number);
         if (playPongGame(game, mox))
         {
            System.out.println(": success");
         }
         else
         {
            System.out.println(": fail");
         }
      }
   }


   // Play pong game.
   public boolean playPongGame(PongGame game, PongMox mox)
   {
      boolean result = true;

      float[] sensors = new float[2];
      int driver = mox.driver;
      reset();
      mox.driver = driver;
      for (PongEvent e : game.sequence)
      {
         // Set up landmark and sensors.
         if (e.ballState > 0)
         {
            if (e.paddleState == 0)
            {
               pongCells.cells[mox.x][mox.y] = e.ballState + PongCells.LANDMARK_CELLS_BEGIN_VALUE - 1;
               sensors[0] = (float)(e.ballState + PongCells.LANDMARK_CELLS_BEGIN_VALUE - 1);
            }
            else
            {
               pongCells.cells[mox.x][mox.y] = PongCells.LANDMARK_CELLS_BEGIN_VALUE + 5;
               sensors[0] = (float)(PongCells.LANDMARK_CELLS_BEGIN_VALUE + 5);
            }
         }
         else if (e.paddleState > 0)
         {
            pongCells.cells[mox.x][mox.y] = PongCells.LANDMARK_CELLS_BEGIN_VALUE + 6;
            sensors[0] = (float)(PongCells.LANDMARK_CELLS_BEGIN_VALUE + 6);
         }
         else
         {
            pongCells.cells[mox.x][mox.y] = MoxWorx.EMPTY_CELL_VALUE;
            sensors[0] = (float)MoxWorx.EMPTY_CELL_VALUE;
         }
         sensors[1] = 0.0f;

         // Cycle mox.
         if (mox.driver == PongMox.DRIVER_TYPE.MANUAL.getValue())
         {
            mox.driverResponse = e.response;
         }
         int response = mox.cycle(sensors, mox.x, mox.y);
         if (response != e.response)
         {
            result = false;
         }

         // Process response.
         if (response == PongMox.FORWARD)
         {
            int width  = pongCells.size.width;
            int height = pongCells.size.height;
            switch (mox.direction)
            {
            case Orientation.NORTH:
               if (mox.y < height - 1)
               {
                  mox.y++;
               }
               break;

            case Orientation.EAST:
               if (mox.x < width - 1)
               {
                  mox.x++;
               }
               break;

            case Orientation.SOUTH:
               if (mox.y > 0)
               {
                  mox.y--;
               }
               break;

            case Orientation.WEST:
               if (mox.x > 0)
               {
                  mox.x--;
               }
               break;
            }
         }
         else if (response == PongMox.RIGHT)
         {
            mox.direction = (mox.direction + 1) % Orientation.NUM_ORIENTATIONS;
         }
         else if (response == PongMox.LEFT)
         {
            mox.direction = mox.direction - 1;
            if (mox.direction < 0)
            {
               mox.direction = mox.direction + Orientation.NUM_ORIENTATIONS;
            }
         }
      }
      return(result);
   }


   // Reset.
   public void reset()
   {
      if (pongCells != null)
      {
         pongCells.restore();
      }
      if (mox != null)
      {
         mox.reset();
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
      int    randomSeed        = DEFAULT_RANDOM_SEED;
      String pongTrainingFile  = null;
      String pongTestingFile   = null;
      int    NUM_NEIGHBORHOODS = Morphognostic.DEFAULT_NUM_NEIGHBORHOODS;
      int    NEIGHBORHOOD_INITIAL_DIMENSION    = Morphognostic.DEFAULT_NEIGHBORHOOD_INITIAL_DIMENSION;
      int    NEIGHBORHOOD_DIMENSION_STRIDE     = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_STRIDE;
      int    NEIGHBORHOOD_DIMENSION_MULTIPLIER = Morphognostic.DEFAULT_NEIGHBORHOOD_DIMENSION_MULTIPLIER;
      int    EPOCH_INTERVAL_STRIDE             = Morphognostic.DEFAULT_EPOCH_INTERVAL_STRIDE;
      int    EPOCH_INTERVAL_MULTIPLIER         = Morphognostic.DEFAULT_EPOCH_INTERVAL_MULTIPLIER;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-numNeighborhoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               NUM_NEIGHBORHOODS = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (NUM_NEIGHBORHOODS < 0)
            {
               System.err.println("Invalid numNeighborhoods option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-neighborhoodInitialDimension"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               NEIGHBORHOOD_INITIAL_DIMENSION = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if ((NEIGHBORHOOD_INITIAL_DIMENSION < 3) ||
                ((NEIGHBORHOOD_INITIAL_DIMENSION % 2) == 0))
            {
               System.err.println("Invalid neighborhoodInitialDimension option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               NEIGHBORHOOD_DIMENSION_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (NEIGHBORHOOD_DIMENSION_STRIDE < 0)
            {
               System.err.println("Invalid neighborhoodDimensionStride option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-neighborhoodDimensionMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               NEIGHBORHOOD_DIMENSION_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (NEIGHBORHOOD_DIMENSION_MULTIPLIER < 0)
            {
               System.err.println("Invalid neighborhoodDimensionMultiplier option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-epochIntervalStride"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               EPOCH_INTERVAL_STRIDE = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (EPOCH_INTERVAL_STRIDE < 0)
            {
               System.err.println("Invalid epochIntervalStride option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-epochIntervalMultiplier"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               EPOCH_INTERVAL_MULTIPLIER = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (EPOCH_INTERVAL_MULTIPLIER < 0)
            {
               System.err.println("Invalid epochIntervalMultiplier option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid randomSeed option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            try
            {
               randomSeed = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid randomSeed option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-pongTrainingFile"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid pongTrainingFile option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (pongTrainingFile == null)
            {
               pongTrainingFile = args[i];
            }
            else
            {
               System.err.println("Duplicate pongTrainingFile option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         if (args[i].equals("-pongTestingFile"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid pongTestingFile option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            if (pongTestingFile == null)
            {
               pongTestingFile = args[i];
            }
            else
            {
               System.err.println("Duplicate pongTestingFile option");
               System.err.println(Pong.Usage);
               System.exit(2);
            }
            continue;
         }
         System.err.println(Pong.Usage);
         System.exit(2);
      }

      // Check options.
      if ((pongTrainingFile == null) || (pongTestingFile == null))
      {
         System.err.println(Pong.Usage);
         System.exit(2);
      }

      // Set look and feel.
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (Exception e)
      {
         System.err.println("Warning: cannot set look and feel");
      }

      // Create game.
      Pong pong = new Pong();
      pong.random = new Random(randomSeed);

      // Run pong.
      try
      {
         pong.loadPong(pongTrainingFile, pongTestingFile);
         pong.runPong();
      }
      catch (Exception e)
      {
         System.err.println("Cannot run pong: " + e.getMessage());
         System.exit(2);
      }
      System.exit(0);
   }
}
