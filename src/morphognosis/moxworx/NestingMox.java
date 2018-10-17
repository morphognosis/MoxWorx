// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Nesting mox: morphognosis organism.

package morphognosis.moxworx;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Vector;

import morphognosis.Metamorph;
import morphognosis.Morphognostic;
import morphognosis.Orientation;
import morphognosis.Utility;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;

public class NestingMox
{
   // Properties.
   public int          id;
   public int          x, y;
   public int          direction;
   public boolean      hasStone;
   public NestCells    nestCells;
   public int          x2, y2;
   public int          direction2;
   public int          driver;
   public int          driverResponse;
   public int          randomSeed;
   public SecureRandom random;

   // Current morphognostic.
   public Morphognostic morphognostic;

   // Metamorphs.
   public ArrayList<Metamorph> metamorphs;
   public FastVector           metamorphNNattributeNames;
   public Instances            metamorphInstances;
   MultilayerPerceptron        metamorphNN;
   public static final boolean saveMetamorphInstances = false;
   public static final boolean saveMetamorphNN        = false;
   public static final boolean evaluateMetamorphNN    = true;

   // Input/output.
   float[] sensors;
   int response;

   // Sensor dimensions.
   public static final int NUM_SENSORS = 4;
   public static final int STONE_AHEAD_SENSOR_INDEX      = 0;
   public static final int FORWARD_GRADIENT_SENSOR_INDEX = 1;
   public static final int LATERAL_GRADIENT_SENSOR_INDEX = 2;
   public static final int CARRIED_STONE_SENSOR_INDEX    = 3;

   // Gradient sensor values.
   public static final int FLAT_GRADIENT         = 0;
   public static final int PEAK_GRADIENT         = 1;
   public static final int FORWARD_UP_GRADIENT   = 2;
   public static final int FORWARD_DOWN_GRADIENT = 3;
   public static final int LEFT_UP_GRADIENT      = 2;
   public static final int RIGHT_UP_GRADIENT     = 3;
   public static final int NUM_GRADIENT_VALUES   = 4;

   // Response types.
   public static final int WAIT          = 0;
   public static final int FORWARD       = 1;
   public static final int RIGHT         = 2;
   public static final int LEFT          = 3;
   public static final int TAKE_STONE    = 4;
   public static final int DROP_STONE    = 5;
   public static final int NUM_RESPONSES = 6;

   // Navigation.
   public boolean[][] landmarkMap;
   public int         maxEventAge;
   public class Event
   {
      public int[] values;
      public int   x;
      public int   y;
      public int   time;
      public Event(int[] values, int x, int y, int time)
      {
         int n = values.length;

         this.values = new int[n];
         for (int i = 0; i < n; i++)
         {
            this.values[i] = values[i];
         }
         this.x    = x;
         this.y    = y;
         this.time = time;
      }
   }
   public Vector<Event> events;
   public int           eventTime;

   // Driver type.
   public enum DRIVER_TYPE
   {
      METAMORPH_DB(0),
      METAMORPH_NN(1),
      AUTOPILOT(2),
      MANUAL(3);

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
   public NestingMox(int id, int x, int y, int direction, NestCells nestCells, int randomSeed)
   {
      this.id         = id;
      this.nestCells  = nestCells;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init(x, y, direction);
      int [] numEventTypes = new int[NUM_SENSORS];
      numEventTypes[STONE_AHEAD_SENSOR_INDEX]      = NestCells.NUM_STONE_VALUES;
      numEventTypes[FORWARD_GRADIENT_SENSOR_INDEX] = NUM_GRADIENT_VALUES;
      numEventTypes[LATERAL_GRADIENT_SENSOR_INDEX] = NUM_GRADIENT_VALUES;
      numEventTypes[CARRIED_STONE_SENSOR_INDEX]    = 2;
      morphognostic = new Morphognostic(direction, numEventTypes);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   public NestingMox(int id, int x, int y, int direction, NestCells nestCells, int randomSeed,
                     int NUM_NEIGHBORHOODS,
                     int NEIGHBORHOOD_INITIAL_DIMENSION,
                     int NEIGHBORHOOD_DIMENSION_STRIDE,
                     int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                     int EPOCH_INTERVAL_STRIDE,
                     int EPOCH_INTERVAL_MULTIPLIER)
   {
      this.id         = id;
      this.nestCells  = nestCells;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init(x, y, direction);
      int [] numEventTypes = new int[NUM_SENSORS];
      numEventTypes[STONE_AHEAD_SENSOR_INDEX]      = NestCells.NUM_STONE_VALUES;
      numEventTypes[FORWARD_GRADIENT_SENSOR_INDEX] = NUM_GRADIENT_VALUES;
      numEventTypes[LATERAL_GRADIENT_SENSOR_INDEX] = NUM_GRADIENT_VALUES;
      numEventTypes[CARRIED_STONE_SENSOR_INDEX]    = 2;
      morphognostic = new Morphognostic(direction, numEventTypes,
                                        NUM_NEIGHBORHOODS,
                                        NEIGHBORHOOD_INITIAL_DIMENSION,
                                        NEIGHBORHOOD_DIMENSION_STRIDE,
                                        NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                        EPOCH_INTERVAL_STRIDE,
                                        EPOCH_INTERVAL_MULTIPLIER);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   public NestingMox(NestCells nestCells, int randomSeed)
   {
      id              = -1;
      this.nestCells  = nestCells;
      this.randomSeed = randomSeed;
      random          = new SecureRandom();
      random.setSeed(randomSeed);
      init();
      int [] numEventTypes = new int[NUM_SENSORS];
      numEventTypes[STONE_AHEAD_SENSOR_INDEX]      = NestCells.NUM_STONE_VALUES;
      numEventTypes[FORWARD_GRADIENT_SENSOR_INDEX] = NUM_GRADIENT_VALUES;
      numEventTypes[LATERAL_GRADIENT_SENSOR_INDEX] = NUM_GRADIENT_VALUES;
      numEventTypes[CARRIED_STONE_SENSOR_INDEX]    = 2;
      morphognostic = new Morphognostic(direction, numEventTypes);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxEventAge = n.epoch + n.duration - 1;
      metamorphs  = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   // Initialize.
   void init(int x, int y, int direction)
   {
      this.x         = x2 = x;
      this.y         = y2 = y;
      this.direction = direction2 = direction;
      hasStone       = false;
      sensors        = new float[NUM_SENSORS];
      for (int n = 0; n < NUM_SENSORS; n++)
      {
         sensors[n] = 0.0f;
      }
      response       = WAIT;
      driver         = DRIVER_TYPE.METAMORPH_DB.getValue();
      driverResponse = WAIT;
      landmarkMap    = new boolean[nestCells.size.width][nestCells.size.height];
      for (int i = 0; i < nestCells.size.width; i++)
      {
         for (int j = 0; j < nestCells.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      events    = new Vector<Event>();
      eventTime = 0;
   }


   void init()
   {
      init(0, 0, Orientation.NORTH);
   }


   // Reset state.
   void reset()
   {
      random.setSeed(randomSeed);
      x         = x2;
      y         = y2;
      direction = direction2;
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driverResponse = WAIT;
      for (int i = 0; i < nestCells.size.width; i++)
      {
         for (int j = 0; j < nestCells.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      events.clear();
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
      DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(output));

      Utility.saveInt(writer, id);
      Utility.saveInt(writer, x);
      Utility.saveInt(writer, y);
      Utility.saveInt(writer, direction);
      int n = 0;
      if (hasStone) { n = 1; }
      Utility.saveInt(writer, n);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, direction2);
      morphognostic.save(writer);
      Utility.saveInt(writer, maxEventAge);
      Utility.saveInt(writer, metamorphs.size());
      for (Metamorph m : metamorphs)
      {
         m.save(writer);
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

      id        = Utility.loadInt(reader);
      x         = Utility.loadInt(reader);
      y         = Utility.loadInt(reader);
      direction = Utility.loadInt(reader);
      int n = Utility.loadInt(reader);
      if (n == 0) { hasStone = false; }else{ hasStone = true; }
      x2            = Utility.loadInt(reader);
      y2            = Utility.loadInt(reader);
      direction2    = Utility.loadInt(reader);
      morphognostic = Morphognostic.load(reader);
      maxEventAge   = Utility.loadInt(reader);
      metamorphs.clear();
      n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(reader));
      }
      initMetamorphNN();
   }


   // Sensor/response cycle.
   public int cycle(float[] sensors)
   {
      this.sensors[STONE_AHEAD_SENSOR_INDEX]      = sensors[STONE_AHEAD_SENSOR_INDEX];
      this.sensors[FORWARD_GRADIENT_SENSOR_INDEX] = sensors[FORWARD_GRADIENT_SENSOR_INDEX];
      this.sensors[LATERAL_GRADIENT_SENSOR_INDEX] = sensors[LATERAL_GRADIENT_SENSOR_INDEX];
      if (hasStone)
      {
         this.sensors[CARRIED_STONE_SENSOR_INDEX] = 1.0f;
      }
      else
      {
         this.sensors[CARRIED_STONE_SENSOR_INDEX] = 0.0f;
      }

      // Update morphognostic.
      int[] values = new int[NUM_SENSORS];
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         values[i] = (int)this.sensors[i];
      }
      events.add(new Event(values, x, y, eventTime));
      if ((eventTime - events.get(0).time) > maxEventAge)
      {
         events.remove(0);
      }
      int w = nestCells.size.width;
      int h = nestCells.size.height;
      int a = maxEventAge + 1;
      int morphEvents[][][][] = new int[w][h][NUM_SENSORS][a];
      for (int x2 = 0; x2 < w; x2++)
      {
         for (int y2 = 0; y2 < h; y2++)
         {
            for (int n = 0; n < NUM_SENSORS; n++)
            {
               for (int t = 0; t < a; t++)
               {
                  morphEvents[x2][y2][n][t] = -1;
               }
            }
         }
      }
      for (Event e : events)
      {
         for (int n = 0; n < NUM_SENSORS; n++)
         {
            morphEvents[e.x][e.y][n][eventTime - e.time] = e.values[n];
         }
      }
      morphognostic.update(morphEvents, x, y);

      // Respond.
      if (driver == DRIVER_TYPE.METAMORPH_DB.getValue())
      {
         metamorphDBresponse();
      }
      else if (driver == DRIVER_TYPE.METAMORPH_NN.getValue())
      {
         metamorphNNresponse();
      }
      else if (driver == DRIVER_TYPE.AUTOPILOT.getValue())
      {
         autoResponse();
      }
      else
      {
         response = driverResponse;
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

      eventTime++;
      return(response);
   }


   // Get metamorph DB response.
   void metamorphDBresponse()
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
         else
         {
            if (d2 == d)
            {
               if (random.nextBoolean())
               {
                  d         = d2;
                  metamorph = m;
               }
            }
         }
      }
      if (metamorph != null)
      {
         response = metamorph.response;
      }
   }


   // Get metamorph neural network response.
   void metamorphNNresponse()
   {
      response = classifyMorphognostic(morphognostic);
   }


   // Autopilot response.
   void autoResponse()
   {
      // Default response.
      response = WAIT;

      // Mox has stone?
      if (hasStone)
      {
         // Returned to nest?
         if ((sensors[FORWARD_GRADIENT_SENSOR_INDEX] == (int)PEAK_GRADIENT) &&
             (sensors[LATERAL_GRADIENT_SENSOR_INDEX] == (int)PEAK_GRADIENT))
         {
            if (sensors[STONE_AHEAD_SENSOR_INDEX] == (int)NestCells.STONE_CELL_VALUE)
            {
               response = RIGHT;
            }
            else
            {
               response = DROP_STONE;
            }
            return;
         }

         // Return to nest.
         switch ((int)sensors[FORWARD_GRADIENT_SENSOR_INDEX])
         {
         case FLAT_GRADIENT:
            switch ((int)sensors[LATERAL_GRADIENT_SENSOR_INDEX])
            {
            case FLAT_GRADIENT:
               randomMovement();
               return;

            case PEAK_GRADIENT:
               randomMovement();
               return;

            case RIGHT_UP_GRADIENT:
               response = RIGHT;
               return;

            case LEFT_UP_GRADIENT:
               response = LEFT;
               return;
            }
            break;

         case PEAK_GRADIENT:
            switch ((int)sensors[LATERAL_GRADIENT_SENSOR_INDEX])
            {
            case FLAT_GRADIENT:
               randomMovement();
               return;

            case PEAK_GRADIENT:
               randomMovement();
               return;

            case RIGHT_UP_GRADIENT:
               response = RIGHT;
               return;

            case LEFT_UP_GRADIENT:
               response = LEFT;
               return;
            }
            break;

         case FORWARD_UP_GRADIENT:
            switch ((int)sensors[LATERAL_GRADIENT_SENSOR_INDEX])
            {
            case FLAT_GRADIENT:
               response = FORWARD;
               return;

            case PEAK_GRADIENT:
               response = FORWARD;
               return;

            case RIGHT_UP_GRADIENT:
               if (random.nextBoolean())
               {
                  response = FORWARD;
               }
               else
               {
                  response = RIGHT;
               }
               return;

            case LEFT_UP_GRADIENT:
               if (random.nextBoolean())
               {
                  response = FORWARD;
               }
               else
               {
                  response = LEFT;
               }
               return;
            }
            break;

         case FORWARD_DOWN_GRADIENT:
            switch ((int)sensors[LATERAL_GRADIENT_SENSOR_INDEX])
            {
            case FLAT_GRADIENT:
               response = RIGHT;
               return;

            case PEAK_GRADIENT:
               response = RIGHT;
               return;

            case RIGHT_UP_GRADIENT:
               response = RIGHT;
               return;

            case LEFT_UP_GRADIENT:
               response = LEFT;
               return;
            }
            break;
         }
      }
      else
      {
         // Check nest?
         if ((sensors[FORWARD_GRADIENT_SENSOR_INDEX] == (int)PEAK_GRADIENT) &&
             (sensors[LATERAL_GRADIENT_SENSOR_INDEX] == (int)PEAK_GRADIENT))
         {
            if (sensors[STONE_AHEAD_SENSOR_INDEX] == (int)NestCells.STONE_CELL_VALUE)
            {
               response = RIGHT;
            }
            else
            {
               response = FORWARD;
            }
            return;
         }

         // Take stone?
         if (sensors[STONE_AHEAD_SENSOR_INDEX] == (int)NestCells.STONE_CELL_VALUE)
         {
            response = TAKE_STONE;
            return;
         }

         // Random search for stone.
         randomMovement();
         return;
      }
   }


   // Random movement.
   void randomMovement()
   {
      switch (random.nextInt(5))
      {
      case 0:
      case 1:
      case 2:
         response = FORWARD;
         return;

      case 3:
         response = LEFT;
         return;

      case 4:
         response = RIGHT;
         return;
      }
   }


   // Initialize metamorph neural network.
   public void initMetamorphNN()
   {
      metamorphNNattributeNames = new FastVector();
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               for (int d = 0; d < morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < morphognostic.numEventTypes[d]; j++)
                  {
                     metamorphNNattributeNames.addElement(new Attribute(i + "-" + x + "-" + y + "-" + d + "-" + j));
                  }
               }
            }
         }
      }
      FastVector responseVals = new FastVector();
      for (int i = 0; i < NUM_RESPONSES; i++)
      {
         responseVals.addElement(i + "");
      }
      metamorphNNattributeNames.addElement(new Attribute("type", responseVals));
      metamorphInstances = new Instances("metamorphs", metamorphNNattributeNames, 0);
      metamorphNN        = new MultilayerPerceptron();
   }


   // Create and train metamorph neural network.
   public void createMetamorphNN() throws Exception
   {
      // Create instances.
      metamorphInstances = new Instances("metamorphs", metamorphNNattributeNames, 0);
      for (Metamorph m : metamorphs)
      {
         metamorphInstances.add(createInstance(metamorphInstances, m));
      }
      metamorphInstances.setClassIndex(metamorphInstances.numAttributes() - 1);

      // Create and train the neural network.
      MultilayerPerceptron mlp = new MultilayerPerceptron();
      metamorphNN = mlp;
      mlp.setLearningRate(0.1);
      mlp.setMomentum(0.2);
      mlp.setTrainingTime(2000);
      mlp.setHiddenLayers("20");
      mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 20"));
      mlp.buildClassifier(metamorphInstances);

      // Save training instances?
      if (saveMetamorphInstances)
      {
         ArffSaver saver = new ArffSaver();
         saver.setInstances(metamorphInstances);
         saver.setFile(new File("metamorphInstances.arff"));
         saver.writeBatch();
      }

      // Save networks?
      if (saveMetamorphNN)
      {
         Debug.saveToFile("metamorphNN.dat", mlp);
      }

      // Evaluate the network.
      if (evaluateMetamorphNN)
      {
         Evaluation eval = new Evaluation(metamorphInstances);
         eval.evaluateModel(mlp, metamorphInstances);
         System.out.println("Error rate=" + eval.errorRate());
         System.out.println(eval.toSummaryString());
      }
   }


   // Create metamorph NN instance.
   Instance createInstance(Instances instances, Metamorph m)
   {
      double[]  attrValues = new double[instances.numAttributes()];
      int a = 0;
      for (int i = 0; i < morphognostic.NUM_NEIGHBORHOODS; i++)
      {
         int n = m.morphognostic.neighborhoods.get(i).sectors.length;
         for (int x = 0; x < n; x++)
         {
            for (int y = 0; y < n; y++)
            {
               Morphognostic.Neighborhood.Sector s = m.morphognostic.neighborhoods.get(i).sectors[x][y];
               for (int d = 0; d < m.morphognostic.eventDimensions; d++)
               {
                  for (int j = 0; j < s.typeDensities[d].length; j++)
                  {
                     attrValues[a] = s.typeDensities[d][j];
                     a++;
                  }
               }
            }
         }
      }
      attrValues[a] = instances.attribute(a).indexOfValue(m.response + "");
      a++;
      return(new Instance(1.0, attrValues));
   }


   // Use metamorph NN to classify morphognostic as a response.
   public int classifyMorphognostic(Morphognostic morphognostic)
   {
      Metamorph metamorph = new Metamorph(morphognostic, 0);
      int       response  = 0;

      try
      {
         // Classify.
         Instance instance        = createInstance(metamorphInstances, metamorph);
         int      predictionIndex = (int)metamorphNN.classifyInstance(instance);

         // Get the predicted class label from the predictionIndex.
         String predictedClassLabel = metamorphInstances.classAttribute().value(predictionIndex);
         response = Integer.parseInt(predictedClassLabel);

         // Get the prediction probability distribution.
         //double[] predictionDistribution = metamorphNN.distributionForInstance(instance);

         // Get morphognostic distance from prediction probability.
         //float dist = (1.0f - (float)predictionDistribution[predictionIndex]);
      }
      catch (Exception e)
      {
         System.err.println("Error classifying morphognostic:");
         e.printStackTrace();
      }
      return(response);
   }
}
