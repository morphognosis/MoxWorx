// For conditions of distribution and use, see copyright notice in MoxWorx.java

// Pong playing mox: mophognosis organism.

package morphognosis.moxworx;

import java.io.*;
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

public class PongMox
{
   // Properties.
   public int       id;
   public int       x, y;
   public int       direction;
   public PongCells pongCells;
   public int       numLandmarkTypes;
   public int       x2, y2;
   public int       direction2;
   public int       driver;
   public int       driverResponse;

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
   public static final int NUM_SENSORS = 2;

   // Response types.
   public static final int WAIT          = 0;
   public static final int FORWARD       = 1;
   public static final int RIGHT         = 2;
   public static final int LEFT          = 3;
   public static final int NUM_RESPONSES = 4;

   // Navigation.
   public boolean[][] landmarkMap;
   public int         maxLandmarkEventAge;
   public class LandmarkEvent
   {
      public int value;
      public int x;
      public int y;
      public int time;
      public LandmarkEvent(int value, int x, int y, int time)
      {
         this.value = value;
         this.x     = x;
         this.y     = y;
         this.time  = time;
      }
   }
   public Vector<LandmarkEvent> landmarkEvents;
   public int eventTime;

   // Driver type.
   public enum DRIVER_TYPE
   {
      METAMORPH_DB(0),
      METAMORPH_NN(1),
      MANUAL(2);

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
   public PongMox(int id, int x, int y, int direction, int numLandmarkTypes, PongCells pongCells)
   {
      this.id               = id;
      this.pongCells        = pongCells;
      this.numLandmarkTypes = numLandmarkTypes;
      init(x, y, direction);
      int[] numEventTypes = new int[1];
      numEventTypes[0]    = numLandmarkTypes;
      morphognostic       = new Morphognostic(direction, numEventTypes);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxLandmarkEventAge = n.epoch + n.duration - 1;
      metamorphs          = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   public PongMox(int id, int x, int y, int direction, int numLandmarkTypes, PongCells pongCells,
                  int NUM_NEIGHBORHOODS,
                  int NEIGHBORHOOD_INITIAL_DIMENSION,
                  int NEIGHBORHOOD_DIMENSION_STRIDE,
                  int NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                  int EPOCH_INTERVAL_STRIDE,
                  int EPOCH_INTERVAL_MULTIPLIER)
   {
      this.id               = id;
      this.pongCells        = pongCells;
      this.numLandmarkTypes = numLandmarkTypes;
      init(x, y, direction);
      int[] numEventTypes = new int[1];
      numEventTypes[0]    = numLandmarkTypes;
      morphognostic       = new Morphognostic(direction, numEventTypes,
                                              NUM_NEIGHBORHOODS,
                                              NEIGHBORHOOD_INITIAL_DIMENSION,
                                              NEIGHBORHOOD_DIMENSION_STRIDE,
                                              NEIGHBORHOOD_DIMENSION_MULTIPLIER,
                                              EPOCH_INTERVAL_STRIDE,
                                              EPOCH_INTERVAL_MULTIPLIER);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxLandmarkEventAge = n.epoch + n.duration - 1;
      metamorphs          = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   public PongMox(PongCells pongCells)
   {
      id               = -1;
      this.pongCells   = pongCells;
      numLandmarkTypes = 1;
      init();
      int[] numEventTypes = new int[1];
      numEventTypes[0]    = numLandmarkTypes;
      morphognostic       = new Morphognostic(direction, numEventTypes);
      Morphognostic.Neighborhood n = morphognostic.neighborhoods.get(morphognostic.NUM_NEIGHBORHOODS - 1);
      maxLandmarkEventAge = n.epoch + n.duration - 1;
      metamorphs          = new ArrayList<Metamorph>();
      initMetamorphNN();
   }


   // Initialize.
   void init(int x, int y, int direction)
   {
      this.x         = x2 = x;
      this.y         = y2 = y;
      this.direction = direction2 = direction;
      sensors        = new float[NUM_SENSORS];
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driver         = DRIVER_TYPE.METAMORPH_DB.getValue();
      driverResponse = WAIT;
      landmarkMap    = new boolean[pongCells.size.width][pongCells.size.height];
      for (int i = 0; i < pongCells.size.width; i++)
      {
         for (int j = 0; j < pongCells.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      landmarkEvents = new Vector<LandmarkEvent>();
      eventTime      = 0;
   }


   void init()
   {
      init(0, 0, Orientation.NORTH);
   }


   // Reset state.
   void reset()
   {
      x         = x2;
      y         = y2;
      direction = direction2;
      for (int i = 0; i < NUM_SENSORS; i++)
      {
         sensors[i] = 0.0f;
      }
      response       = WAIT;
      driverResponse = WAIT;
      for (int i = 0; i < pongCells.size.width; i++)
      {
         for (int j = 0; j < pongCells.size.height; j++)
         {
            landmarkMap[i][j] = false;
         }
      }
      landmarkEvents.clear();
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
      Utility.saveInt(writer, numLandmarkTypes);
      Utility.saveInt(writer, x2);
      Utility.saveInt(writer, y2);
      Utility.saveInt(writer, direction2);
      morphognostic.save(writer);
      Utility.saveInt(writer, maxLandmarkEventAge);
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

      id                  = Utility.loadInt(reader);
      x                   = Utility.loadInt(reader);
      y                   = Utility.loadInt(reader);
      direction           = Utility.loadInt(reader);
      numLandmarkTypes    = Utility.loadInt(reader);
      x2                  = Utility.loadInt(reader);
      y2                  = Utility.loadInt(reader);
      direction2          = Utility.loadInt(reader);
      morphognostic       = Morphognostic.load(reader);
      maxLandmarkEventAge = Utility.loadInt(reader);
      metamorphs.clear();
      int n = Utility.loadInt(reader);
      for (int i = 0; i < n; i++)
      {
         metamorphs.add(Metamorph.load(reader));
      }
      initMetamorphNN();
   }


   // Sensor/response cycle.
   public int cycle(float[] sensors, int fx, int fy)
   {
      this.sensors = sensors;

      // Update morphognostic.
      landmarkEvents.add(new LandmarkEvent(pongCells.cells[fx][fy], fx, fy, eventTime));
      if ((eventTime - landmarkEvents.get(0).time) > maxLandmarkEventAge)
      {
         landmarkEvents.remove(0);
      }
      int w = pongCells.size.width;
      int h = pongCells.size.height;
      int a = maxLandmarkEventAge + 1;
      int landmarks[][][][] = new int[w][h][1][a];
      for (int x = 0; x < w; x++)
      {
         for (int y = 0; y < h; y++)
         {
            for (int t = 0; t < a; t++)
            {
               landmarks[x][y][0][t] = -1;
            }
         }
      }
      for (LandmarkEvent e : landmarkEvents)
      {
         if (e.value >= PongCells.LANDMARK_CELLS_BEGIN_VALUE)
         {
            landmarks[e.x][e.y][0][eventTime - e.time] = e.value -
                                                         PongCells.LANDMARK_CELLS_BEGIN_VALUE + 1;
         }
         else
         {
            landmarks[e.x][e.y][0][eventTime - e.time] = MoxWorx.EMPTY_CELL_VALUE;
         }
      }
      morphognostic.update(landmarks, x, y);

      // Respond.
      if (driver == DRIVER_TYPE.METAMORPH_DB.getValue())
      {
         metamorphDBresponse();
      }
      else if (driver == DRIVER_TYPE.METAMORPH_NN.getValue())
      {
         metamorphNNresponse();
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
