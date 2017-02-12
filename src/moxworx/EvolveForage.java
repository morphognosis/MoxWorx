/*
 * Evolve foraging mox by mutating and recombining parameters.
 */

package moxworx;

import java.util.*;
import java.io.*;

public class EvolveForage
{
   // Parameters.
   public static final int   DEFAULT_WIDTH               = 10;
   public int                WIDTH                       = DEFAULT_WIDTH;
   public static final int   DEFAULT_HEIGHT              = 10;
   public int                HEIGHT                      = DEFAULT_HEIGHT;
   public static final int   DEFAULT_NUM_LANDMARK_TYPES  = 1;
   public int                NUM_LANDMARK_TYPES          = DEFAULT_NUM_LANDMARK_TYPES;
   public static final int   DEFAULT_NUM_LANDMARKS       = 0;
   public int                NUM_LANDMARKS               = DEFAULT_NUM_LANDMARKS;
   public static final int   DEFAULT_NUM_FOODS           = 0;
   public int                NUM_FOODS                   = DEFAULT_NUM_FOODS;
   public static final int   DEFAULT_TRAINING_SET_SIZE   = 1;
   public int                TRAINING_SET_SIZE           = DEFAULT_TRAINING_SET_SIZE;
   public static final float DEFAULT_TRAINING_CELL_NOISE = 0.0f;
   public float              TRAINING_CELL_NOISE         = DEFAULT_TRAINING_CELL_NOISE;
   public static final ForagerMox.DRIVER_TYPE DEFAULT_MOX_TEST_DRIVER = ForagerMox.DRIVER_TYPE.METAMORPH_DB;
   public ForagerMox.DRIVER_TYPE              MOX_TEST_DRIVER         = DEFAULT_MOX_TEST_DRIVER;
   public static final int   DEFAULT_FIT_POPULATION_SIZE = 20;
   public int                FIT_POPULATION_SIZE         = DEFAULT_FIT_POPULATION_SIZE;
   public static final int   DEFAULT_NUM_MUTANTS         = 10;
   public int                NUM_MUTANTS                  = DEFAULT_NUM_MUTANTS;
   public static final int   DEFAULT_NUM_OFFSPRING        = 10;
   public int                NUM_OFFSPRING                = DEFAULT_NUM_OFFSPRING;
   public int                POPULATION_SIZE              = (FIT_POPULATION_SIZE + NUM_MUTANTS + NUM_OFFSPRING);
   public static final float DEFAULT_MUTATION_RATE        = 0.25f;
   public float              MUTATION_RATE                = DEFAULT_MUTATION_RATE;
   public static final float DEFAULT_RANDOM_MUTATION_RATE = 0.5f;
   public float              RANDOM_MUTATION_RATE         = DEFAULT_RANDOM_MUTATION_RATE;
   public static final int   DEFAULT_RANDOM_SEED          = 4517;
   public int                RANDOM_SEED                  = DEFAULT_RANDOM_SEED;
   public static final int   SAVE_FREQUENCY               = 1;
   public static final float INVALID_FITNESS              = 1000.0f;

   public void setPopulationSize()
   {
      POPULATION_SIZE = (FIT_POPULATION_SIZE + NUM_MUTANTS + NUM_OFFSPRING);
   }


   // Load parameters.
   public void loadParameters(DataInputStream reader) throws IOException
   {
      WIDTH               = Utility.loadInt(reader);
      HEIGHT              = Utility.loadInt(reader);
      NUM_LANDMARK_TYPES  = Utility.loadInt(reader);
      NUM_LANDMARKS       = Utility.loadInt(reader);
      NUM_FOODS           = Utility.loadInt(reader);
      TRAINING_SET_SIZE   = Utility.loadInt(reader);
      TRAINING_CELL_NOISE = Utility.loadFloat(reader);
      if (Utility.loadInt(reader) == ForagerMox.DRIVER_TYPE.METAMORPH_DB.getValue())
      {
         MOX_TEST_DRIVER = ForagerMox.DRIVER_TYPE.METAMORPH_DB;
      }
      else
      {
         MOX_TEST_DRIVER = ForagerMox.DRIVER_TYPE.METAMORPH_NN;
      }
      FIT_POPULATION_SIZE  = Utility.loadInt(reader);
      NUM_MUTANTS          = Utility.loadInt(reader);
      NUM_OFFSPRING        = Utility.loadInt(reader);
      MUTATION_RATE        = Utility.loadFloat(reader);
      RANDOM_MUTATION_RATE = Utility.loadFloat(reader);
      RANDOM_SEED          = Utility.loadInt(reader);
      setPopulationSize();
   }


   // Save parameters.
   public void saveParameters(PrintWriter writer) throws IOException
   {
      Utility.saveInt(writer, WIDTH);
      Utility.saveInt(writer, HEIGHT);
      Utility.saveInt(writer, NUM_LANDMARK_TYPES);
      Utility.saveInt(writer, NUM_LANDMARKS);
      Utility.saveInt(writer, NUM_FOODS);
      Utility.saveInt(writer, TRAINING_SET_SIZE);
      Utility.saveFloat(writer, TRAINING_CELL_NOISE);
      int driver = MOX_TEST_DRIVER.getValue();
      Utility.saveInt(writer, driver);
      Utility.saveInt(writer, FIT_POPULATION_SIZE);
      Utility.saveInt(writer, NUM_MUTANTS);
      Utility.saveInt(writer, NUM_OFFSPRING);
      Utility.saveFloat(writer, MUTATION_RATE);
      Utility.saveFloat(writer, RANDOM_MUTATION_RATE);
      Utility.saveInt(writer, RANDOM_SEED);
   }


   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java EvolveForage\n" +
      "      -generations <evolution generations>\n" +
      "      -steps <moxen steps>\n" +
      "      -output <evolution output file name>\n" +
      "     [-dimensions <width> <height> (default=" + DEFAULT_WIDTH + "," + DEFAULT_HEIGHT + ")]\n" +
      "     [-numLandmarkTypes <quantity> (default=" + DEFAULT_NUM_LANDMARK_TYPES + ")]\n" +
      "     [-numLandmarks <quantity> (default=" + DEFAULT_NUM_LANDMARKS + ")]\n" +
      "     [-numFoods <quantity> (default=" + DEFAULT_NUM_FOODS + ")]\n" +
      "     [-trainingSetSize <quantity> (default=1)]\n" +
      "     [-trainingCellNoise <0.0 - 1.0> (probability of changing cell value)]\n" +
      "     [-moxTestDriver <metamorphDB | metamorphNN> (default=metamorphDB)]\n" +
      "     [-fitPopulationSize <fit population size> (default=" + DEFAULT_FIT_POPULATION_SIZE + ")]\n" +
      "     [-numMutants <number of mutants> (default=" + DEFAULT_NUM_MUTANTS + ")]\n" +
      "     [-numOffspring <number of offspring> (default=" + DEFAULT_NUM_OFFSPRING + ")]\n" +
      "     [-mutationRate <mutation rate> (default=" + DEFAULT_MUTATION_RATE + ")]\n" +
      "     [-randomMutationRate <random mutation rate> (default=" + DEFAULT_RANDOM_MUTATION_RATE + ")]\n" +
      "     [-randomSeed <random seed> (default=" + DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-logfile <log file name>]\n" +
      "  Resume run:\n" +
      "    java EvolveForage\n" +
      "      -generations <evolution generations>\n" +
      "      -steps <moxen steps>\n" +
      "      -input <evolution input file name>\n" +
      "      -output <evolution output file name>\n" +
      "     [-logfile <log file name>]\n" +
      "  Print population properties:\n" +
      "    java EvolveForage\n" +
      "      -properties\n" +
      "      -input <evolution input file name>\n" +
      "  Print evolution statistics:\n" +
      "    java EvolveForage\n" +
      "      -statistics\n" +
      "      -input <evolution input file name>";

   // Generations.
   int Generation;
   int Generations;

   // Steps.
   int Steps;

   // File names.
   String      InputFileName;
   String      OutputFileName;
   String      LogFileName;
   PrintWriter LogWriter;

   // Random numbers.
   Random Randomizer;

   // Print population properties.
   boolean PrintProperties;

   // Print evolution statistics.
   boolean PrintStatistics;

   // Evolution statistics.
   float[] Fittest;
   float[] Average;

   // Population.
   Member[] Population;

   // Constructor.
   public EvolveForage(String[] args)
   {
      int i;

      // Get options.
      Generation    = 0;
      Generations   = -1;
      Steps         = -1;
      InputFileName = OutputFileName = LogFileName = null;
      LogWriter     = null;
      boolean gotDimensions         = false;
      boolean gotNumLandmarkTypes   = false;
      boolean gotNumLandmarks       = false;
      boolean gotNumFoods           = false;
      boolean gotTrainingSetSize    = false;
      boolean gotTrainingCellNoise  = false;
      boolean gotMoxTestDriver      = false;
      boolean gotFitPopulationSize  = false;
      boolean gotNumMutants         = false;
      boolean gotNumOffspring       = false;
      boolean gotMutationRate       = false;
      boolean gotRandomMutationRate = false;
      boolean gotRandomSeed         = false;
      PrintProperties = false;
      PrintStatistics = false;

      for (i = 0; i < args.length; i++)
      {
         if (args[i].equals("-generations"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            Generations = Integer.parseInt(args[i]);
            if (Generations < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }

         if (args[i].equals("-steps"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            Steps = Integer.parseInt(args[i]);
            if (Steps < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }

         if (args[i].equals("-input"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            InputFileName = new String(args[i]);
            continue;
         }

         if (args[i].equals("-output"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            OutputFileName = new String(args[i]);
            continue;
         }

         if (args[i].equals("-dimensions"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(EvolveForage.Usage);
               System.exit(2);
            }
            try
            {
               WIDTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid width option");
               System.err.println(EvolveForage.Usage);
               System.exit(2);
            }
            if (WIDTH < 2)
            {
               System.err.println("Invalid width option");
               System.err.println(EvolveForage.Usage);
               System.exit(2);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(EvolveForage.Usage);
               System.exit(2);
            }
            try
            {
               HEIGHT = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid height option");
               System.err.println(EvolveForage.Usage);
               System.exit(2);
            }
            if (HEIGHT < 2)
            {
               System.err.println("Invalid height option");
               System.err.println(EvolveForage.Usage);
               System.exit(2);
            }
            gotDimensions = true;
            continue;
         }

         if (args[i].equals("-numLandmarkTypes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_LANDMARK_TYPES = Integer.parseInt(args[i]);
            if (NUM_LANDMARK_TYPES < 1)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotNumLandmarkTypes = true;
            continue;
         }

         if (args[i].equals("-numLandmarks"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_LANDMARKS = Integer.parseInt(args[i]);
            if (NUM_LANDMARKS < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotNumLandmarks = true;
            continue;
         }

         if (args[i].equals("-numFoods"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_FOODS = Integer.parseInt(args[i]);
            if (NUM_FOODS < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotNumFoods = true;
            continue;
         }

         if (args[i].equals("-trainingSetSize"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            TRAINING_SET_SIZE = Integer.parseInt(args[i]);
            if (TRAINING_SET_SIZE < 1)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotTrainingSetSize = true;
            continue;
         }

         if (args[i].equals("-trainingCellNoise"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            TRAINING_CELL_NOISE = Float.parseFloat(args[i]);
            if ((TRAINING_CELL_NOISE < 0.0f) || (TRAINING_CELL_NOISE > 1.0f))
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotTrainingCellNoise = true;
            continue;
         }

         if (args[i].equals("-moxTestDriver"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            if (args[i].equals("metamorphDB"))
            {
               MOX_TEST_DRIVER = ForagerMox.DRIVER_TYPE.METAMORPH_DB;
            }
            else if (args[i].equals("metamorphNN"))
            {
               MOX_TEST_DRIVER = ForagerMox.DRIVER_TYPE.METAMORPH_NN;
            }
            else
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotMoxTestDriver = true;
            continue;
         }

         if (args[i].equals("-fitPopulationSize"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            FIT_POPULATION_SIZE = Integer.parseInt(args[i]);
            if (FIT_POPULATION_SIZE < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            setPopulationSize();
            gotFitPopulationSize = true;
            continue;
         }

         if (args[i].equals("-numMutants"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_MUTANTS = Integer.parseInt(args[i]);
            if (NUM_MUTANTS < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            setPopulationSize();
            gotNumMutants = true;
            continue;
         }

         if (args[i].equals("-numOffspring"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            NUM_OFFSPRING = Integer.parseInt(args[i]);
            if (NUM_OFFSPRING < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            setPopulationSize();
            gotNumOffspring = true;
            continue;
         }

         if (args[i].equals("-mutationRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            MUTATION_RATE = Float.parseFloat(args[i]);
            if ((MUTATION_RATE < 0.0f) || (MUTATION_RATE > 1.0f))
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotMutationRate = true;
            continue;
         }

         if (args[i].equals("-randomMutationRate"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            RANDOM_MUTATION_RATE = Float.parseFloat(args[i]);
            if ((RANDOM_MUTATION_RATE < 0.0f) || (RANDOM_MUTATION_RATE > 1.0f))
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotRandomMutationRate = true;
            continue;
         }

         if (args[i].equals("-randomSeed"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            RANDOM_SEED   = Integer.parseInt(args[i]);
            gotRandomSeed = true;
            continue;
         }

         if (args[i].equals("-logfile"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            LogFileName = new String(args[i]);
            continue;
         }

         if (args[i].equals("-properties"))
         {
            PrintProperties = true;
            continue;
         }

         if (args[i].equals("-statistics"))
         {
            PrintStatistics = true;
            continue;
         }

         System.err.println(Usage);
         System.exit(1);
      }

      // Print properties?
      if (PrintProperties || PrintStatistics)
      {
         if ((Generations != -1) || (Steps != -1) ||
             (InputFileName == null) ||
             (OutputFileName != null) ||
             (LogFileName != null) ||
             gotDimensions || gotNumLandmarkTypes || gotNumLandmarks || gotNumFoods ||
             gotTrainingSetSize || gotTrainingCellNoise || gotMoxTestDriver ||
             gotFitPopulationSize || gotNumMutants || gotNumOffspring ||
             gotMutationRate || gotRandomMutationRate || gotRandomSeed)
         {
            System.err.println(Usage);
            System.exit(1);
         }
      }
      else
      {
         if (Generations == -1)
         {
            System.err.println("Generations option required");
            System.err.println(Usage);
            System.exit(1);
         }

         if (Steps == -1)
         {
            System.err.println("Steps option required");
            System.err.println(Usage);
            System.exit(1);
         }

         if (OutputFileName == null)
         {
            System.err.println("Output file required");
            System.err.println(Usage);
            System.exit(1);
         }

         if (InputFileName != null)
         {
            if (gotFitPopulationSize || gotNumMutants || gotNumOffspring ||
                gotDimensions || gotNumLandmarkTypes || gotNumLandmarks ||
                gotNumFoods || gotTrainingSetSize || gotTrainingCellNoise || gotMoxTestDriver ||
                gotMutationRate || gotRandomMutationRate || gotRandomSeed)
            {
               System.err.println(Usage);
               System.exit(1);
            }
         }
      }

      // Seed random numbers.
      Randomizer = new Random(RANDOM_SEED);

      // Open log file?
      if (LogFileName != null)
      {
         try
         {
            LogWriter = new PrintWriter(new FileOutputStream(new File(LogFileName)));
         }
         catch (Exception e) {
            System.err.println("Cannot open log file " + LogFileName +
                               ":" + e.getMessage());
            System.exit(1);
         }
      }
   }


   // Run evolve.
   public void run()
   {
      // Initialize populations?
      if (InputFileName == null)
      {
         init();
      }
      else
      {
         // Load populations.
         load();
      }

      // Log run.
      log("Initializing evolve:");
      log("  Options:");
      log("    generations=" + Generations);
      log("    steps=" + Steps);
      if (InputFileName != null)
      {
         log("    input=" + InputFileName);
      }
      log("    output=" + OutputFileName);
      log("    FIT_POPULATION_SIZE=" + FIT_POPULATION_SIZE);
      log("    NUM_MUTANTS=" + NUM_MUTANTS);
      log("    NUM_OFFSPRING=" + NUM_OFFSPRING);
      log("    MUTATION_RATE=" + MUTATION_RATE);
      log("    RANDOM_MUTATION_RATE=" + RANDOM_MUTATION_RATE);
      log("    RANDOM_SEED=" + RANDOM_SEED);

      // Print population properties?
      if (PrintProperties)
      {
         printProperties();
         return;
      }

      // Print evolution statistics?
      if (PrintStatistics)
      {
         printStatistics();
         return;
      }

      // Evolution loop.
      log("Begin evolve:");
      for (Generations += Generation; Generation < Generations; Generation++)
      {
         log("Generation=" + Generation);

         evolve(Generation);

         // Save populations?
         if ((Generation % SAVE_FREQUENCY) == 0)
         {
            save(Generation);
         }
      }

      // Save populations.
      save(Generation - 1);

      log("End evolve");
   }


   // Initialize evolution.
   void init()
   {
      int i;

      Population = new Member[POPULATION_SIZE];
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         if (i == 0)
         {
            Population[i] = new Member(0, Randomizer);
            Population[i].evaluate(Steps, this);
         }
         else
         {
            // Mutate parameters.
            Population[i] = new Member(Population[0], 0, Randomizer);
            Population[i].evaluate(Steps, this);
         }
      }
      Fittest = new float[Generations + 1];
      Average = new float[Generations + 1];
   }


   // Load evolution.
   void load()
   {
      int             i;
      FileInputStream input  = null;
      DataInputStream reader = null;

      // Open the file.
      try
      {
         input  = new FileInputStream(new File(InputFileName));
         reader = new DataInputStream(input);
      }
      catch (Exception e) {
         System.err.println("Cannot open input file " + InputFileName +
                            ":" + e.getMessage());
      }

      try
      {
         Generation = Utility.loadInt(reader);
         Generation++;
      }
      catch (Exception e) {
         System.err.println("Cannot load from file " + InputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }

      try
      {
         // Load parameters.
         loadParameters(reader);

         // Load population.
         Population = new Member[POPULATION_SIZE];
         for (i = 0; i < POPULATION_SIZE; i++)
         {
            Population[i] = new Member(0, Randomizer);
            Population[i].load(input);
         }
         Fittest = new float[Generation + Generations + 1];
         Average = new float[Generation + Generations + 1];
         for (i = 0; i < Generation; i++)
         {
            Fittest[i] = Utility.loadFloat(reader);
            Average[i] = Utility.loadFloat(reader);
         }
         input.close();
      }
      catch (Exception e) {
         System.err.println("Cannot load populations from file " + InputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }
   }


   // Save evolution.
   void save(int generation)
   {
      int              i, n;
      FileOutputStream output = null;
      PrintWriter      writer = null;

      try
      {
         output = new FileOutputStream(new File(OutputFileName));
         writer = new PrintWriter(output);
      }
      catch (Exception e) {
         System.err.println("Cannot open output file " + OutputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }

      try
      {
         Utility.saveInt(writer, generation);
         writer.flush();
      }
      catch (Exception e) {
         System.err.println("Cannot save to file " + OutputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }

      try
      {
         // Save parameters.
         saveParameters(writer);

         // Save population.
         for (i = 0; i < POPULATION_SIZE; i++)
         {
            Population[i].save(output);
         }
         for (i = 0, n = generation + 1; i < n; i++)
         {
            Utility.saveFloat(writer, Fittest[i]);
            Utility.saveFloat(writer, Average[i]);
         }
         writer.flush();
         output.close();
      }
      catch (Exception e) {
         System.err.println("Cannot save populations to file " + OutputFileName +
                            ":" + e.getMessage());
         System.exit(1);
      }
   }


   // Evolution generation.
   void evolve(int generation)
   {
      String logEntry;

      log("Population:");
      for (int i = 0; i < POPULATION_SIZE; i++)
      {
         logEntry = "    member=" + i + ", " + Population[i].getInfo();
         log(logEntry);
      }

      // Prune unfit members.
      prune();

      // Create new members by mutation.
      mutate();

      // Create new members by mating.
      mate();
   }


   // Prune unfit members.
   void prune()
   {
      float min, f;
      int   i, j, m;

      Member member;

      log("Select:");
      Member[] fitPopulation = new Member[FIT_POPULATION_SIZE];
      min = INVALID_FITNESS;
      for (i = 0; i < FIT_POPULATION_SIZE; i++)
      {
         m = -1;
         for (j = 0; j < POPULATION_SIZE; j++)
         {
            member = Population[j];
            if (member == null)
            {
               continue;
            }
            if ((m == -1) || (member.fitness < min))
            {
               m   = j;
               min = member.fitness;
            }
         }
         member           = Population[m];
         Population[m]    = null;
         fitPopulation[i] = member;
         log("    " + member.getInfo());
      }
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         if (Population[i] != null)
         {
            Population[i] = null;
         }
      }
      f = 0.0f;
      for (i = 0; i < FIT_POPULATION_SIZE; i++)
      {
         Population[i]    = fitPopulation[i];
         fitPopulation[i] = null;
         f += Population[i].fitness;
      }
      Fittest[Generation] = Population[0].fitness;
      Average[Generation] = f / (float)FIT_POPULATION_SIZE;
   }


   // Mutate members.
   void mutate()
   {
      int i, j;

      Member member, mutant;

      log("Mutate:");
      for (i = 0; i < NUM_MUTANTS; i++)
      {
         // Select a fit member to mutate.
         j      = Randomizer.nextInt(FIT_POPULATION_SIZE);
         member = Population[j];

         // Create mutant member.
         mutant = new Member(member, member.generation + 1, Randomizer);
         mutant.evaluate(Steps, this);
         Population[FIT_POPULATION_SIZE + i] = mutant;
         log("    member=" + j + ", " + member.getInfo() +
             " -> member=" + (FIT_POPULATION_SIZE + i) +
             ", " + mutant.getInfo());
      }
   }


   // Produce offspring by melding parent parameters.
   void mate()
   {
      int i, j, k;

      Member member1, member2, offspring;

      log("Mate:");
      if (FIT_POPULATION_SIZE > 1)
      {
         for (i = 0; i < NUM_OFFSPRING; i++)
         {
            // Select a pair of fit members to mate.
            j       = Randomizer.nextInt(FIT_POPULATION_SIZE);
            member1 = Population[j];
            while ((k = Randomizer.nextInt(FIT_POPULATION_SIZE)) == j) {}
            member2 = Population[k];

            // Create offspring.
            offspring = new Member(member1, member2,
                                   (member1.generation > member2.generation ?
                                    member1.generation : member2.generation) + 1, Randomizer);
            offspring.evaluate(Steps, this);
            Population[FIT_POPULATION_SIZE + NUM_MUTANTS + i] = offspring;
            log("    member=" + j + ", " + member1.getInfo() + " + member=" +
                k + ", " + member2.getInfo() +
                " -> member=" + (FIT_POPULATION_SIZE +
                                 NUM_MUTANTS + i) +
                ", " + offspring.getInfo());
         }
      }
   }


   // Print population properties.
   void printProperties()
   {
      int i;

      System.out.println("Population properties:");

      System.out.println("=============================");
      for (i = 0; i < POPULATION_SIZE; i++)
      {
         System.out.println("-----------------------------");
         Population[i].printProperties();
      }
   }


   // Print evolution statistics.
   void printStatistics()
   {
      int i;

      System.out.println("Evolution statistics:");

      System.out.println("Generation\tFittest");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + Fittest[i]);
      }
      System.out.println("Generation\tAverage");
      for (i = 0; i < Generation; i++)
      {
         System.out.println(i + "\t\t" + Average[i]);
      }
   }


   // Logging.
   void log(String message)
   {
      if (LogWriter != null)
      {
         LogWriter.println(message);
         LogWriter.flush();
      }
   }


   // Mox parameter genome.
   public class MoxParmGenome extends Genome
   {
      // Constructor.
      public MoxParmGenome(Random randomizer)
      {
         super(MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt());

         // NUM_NEIGHBORHOODS.
         genes.add(
            new Gene("NUM_NEIGHBORHOODS", 2, 1, 4, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         // NEIGHBORHOOD_INITIAL_DIMENSION.
         genes.add(
            new Gene("NEIGHBORHOOD_INITIAL_DIMENSION", 3, 3, 5, 2,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         // NEIGHBORHOOD_DIMENSION_STRIDE.
         genes.add(
            new Gene("NEIGHBORHOOD_DIMENSION_STRIDE", 1, 0, 2, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         // NEIGHBORHOOD_DIMENSION_MULTIPLIER.
         genes.add(
            new Gene("NEIGHBORHOOD_DIMENSION_MULTIPLIER", 3, 1, 3, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         // EPOCH_INTERVAL_STRIDE.
         genes.add(
            new Gene("EPOCH_INTERVAL_STRIDE", 1, 1, 3, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));

         // EPOCH_INTERVAL_MULTIPLIER.
         genes.add(
            new Gene("EPOCH_INTERVAL_MULTIPLIER", 3, 1, 3, 1,
                     MUTATION_RATE, RANDOM_MUTATION_RATE, randomizer.nextInt()));
      }


      // Mutate.
      public void mutate()
      {
         super.mutate();
      }
   }

   // ID dispenser.
   public int IDdispenser = 0;

   // Population member.
   public class Member
   {
      public int    id;
      public int    generation;
      public float  fitness;
      public Random randomizer;

      // Mox parameters.
      public MoxParmGenome moxParmGenome;

      // Constructors.
      public Member(int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
      }


      // Construct mutation of given member.
      public Member(Member member, int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create and mutate parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
         moxParmGenome.copyValues(member.moxParmGenome);
         moxParmGenome.mutate();
      }


      // Construct by mating given members.
      public Member(Member member1, Member member2, int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0f;

         // Create and meld parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
         moxParmGenome.meldValues(member1.moxParmGenome, member2.moxParmGenome);
      }


      // Evaluate fitness.
      public void evaluate(int steps, EvolveForage evolver)
      {
         int t, v, x, y;

         fitness = INVALID_FITNESS;

         // Get parameters.
         Map<String, Object> parameters        = moxParmGenome.getKeyValues();
         int                 NUM_NEIGHBORHOODS = (Integer)parameters.get("NUM_NEIGHBORHOODS");
         int                 NEIGHBORHOOD_INITIAL_DIMENSION    = (Integer)parameters.get("NEIGHBORHOOD_INITIAL_DIMENSION");
         int                 NEIGHBORHOOD_DIMENSION_STRIDE     = (Integer)parameters.get("NEIGHBORHOOD_DIMENSION_STRIDE");
         int                 NEIGHBORHOOD_DIMENSION_MULTIPLIER = (Integer)parameters.get("NEIGHBORHOOD_DIMENSION_MULTIPLIER");
         int                 EPOCH_INTERVAL_STRIDE             = (Integer)parameters.get("EPOCH_INTERVAL_STRIDE");
         int                 EPOCH_INTERVAL_MULTIPLIER         = (Integer)parameters.get("EPOCH_INTERVAL_MULTIPLIER");

         // Create world.
         Forage forage = new Forage();
         forage.random = new Random(RANDOM_SEED);
         try
         {
            forage.initCells(WIDTH, HEIGHT, NUM_LANDMARK_TYPES, NUM_LANDMARKS, NUM_FOODS);
            forage.createMoxen(1, NUM_LANDMARK_TYPES + 1,
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

         // Train.
         Random      random      = evolver.Randomizer;
         ForageCells forageCells = forage.forageCells;
         int         w           = forageCells.size.width;
         int         h           = forageCells.size.height;
         for (t = 0; t < evolver.TRAINING_SET_SIZE; t++)
         {
            forage.reset();
            for (x = 0; x < w; x++)
            {
               for (y = 0; y < h; y++)
               {
                  if (random.nextFloat() < evolver.TRAINING_CELL_NOISE)
                  {
                     if ((forageCells.cells[x][y] == MoxWorx.EMPTY_CELL_VALUE) ||
                         (forageCells.cells[x][y] >= ForageCells.LANDMARK_CELLS_BEGIN_VALUE))
                     {
                        v = random.nextInt(NUM_LANDMARK_TYPES + 1);
                        if (v == 0)
                        {
                           forageCells.cells[x][y] = MoxWorx.EMPTY_CELL_VALUE;
                        }
                        else
                        {
                           forageCells.cells[x][y] = v + ForageCells.LANDMARK_CELLS_BEGIN_VALUE - 1;
                        }
                     }
                  }
               }
            }
            for (ForagerMox mox : forage.moxen)
            {
               mox.driver = ForagerMox.DRIVER_TYPE.AUTOPILOT.getValue();
            }
            try
            {
               forage.run(steps);
            }
            catch (Exception e)
            {
               System.err.println("Cannot train member " + id + ": " + e.getMessage());
               return;
            }
         }

         // Test.
         forage.reset();
         for (ForagerMox mox : forage.moxen)
         {
            mox.driver = MOX_TEST_DRIVER.getValue();
         }
         try
         {
            fitness = (float)forage.run(steps);
         }
         catch (Exception e)
         {
            System.err.println("Cannot test member " + id + ": " + e.getMessage());
            return;
         }
      }


      // Load member.
      void load(FileInputStream input) throws IOException
      {
         // DataInputStream is for unbuffered input.
         DataInputStream reader = new DataInputStream(input);

         id = Utility.loadInt(reader);
         if (id >= IDdispenser)
         {
            IDdispenser = id + 1;
         }
         generation = Utility.loadInt(reader);
         fitness    = Utility.loadFloat(reader);

         // Load parameter genome.
         moxParmGenome.loadValues(reader);
      }


      // Save member.
      void save(FileOutputStream output) throws IOException
      {
         PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));

         Utility.saveInt(writer, id);
         Utility.saveInt(writer, generation);
         Utility.saveFloat(writer, fitness);
         writer.flush();

         // Save parameter genome.
         moxParmGenome.saveValues(writer);
         writer.flush();
      }


      // Print properties.
      void printProperties()
      {
         System.out.println(getInfo());
         System.out.println("parameters:");
         moxParmGenome.print();
      }


      // Get information.
      String getInfo()
      {
         return("id=" + id +
                ", fitness=" + fitness +
                ", generation=" + generation);
      }
   }

   // Main.
   public static void main(String[] args)
   {
      EvolveForage evolveForage = new EvolveForage(args);

      evolveForage.run();
      System.exit(0);
   }
}
