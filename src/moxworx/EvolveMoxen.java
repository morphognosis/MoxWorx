/*
 * Evolve mox by mutating and recombining parameters.
 */

package moxworx;

import java.util.*;
import java.io.*;

public class EvolveMoxen
{
   // Usage.
   public static final String Usage =
      "Usage:\n" +
      "  New run:\n" +
      "    java EvolveMoxen\n" +
      "      -generations <evolution generations>\n" +
      "      -steps <moxen steps>\n" +
      "      -output <evolution output file name>\n" +
      "     [-dimensions <width> <height> (default=" + EvolveCommon.DEFAULT_WIDTH + "," + EvolveCommon.DEFAULT_HEIGHT + ")]\n" +
      "     [-numObstacleTypes <quantity> (default=" + EvolveCommon.DEFAULT_NUM_OBSTACLE_TYPES + ")]\n" +
      "     [-numObstacles <quantity> (default=" + EvolveCommon.DEFAULT_NUM_OBSTACLES + ")]\n" +
      "     [-numFoods <quantity> (default=" + EvolveCommon.DEFAULT_NUM_FOODS + ")]\n" +
      "     [-fitPopulationSize <fit population size> (default=" + EvolveCommon.DEFAULT_FIT_POPULATION_SIZE + ")]\n" +
      "     [-numMutants <number of mutants> (default=" + EvolveCommon.DEFAULT_NUM_MUTANTS + ")]\n" +
      "     [-numOffspring <number of offspring> (default=" + EvolveCommon.DEFAULT_NUM_OFFSPRING + ")]\n" +
      "     [-mutationRate <mutation rate> (default=" + EvolveCommon.DEFAULT_MUTATION_RATE + ")]\n" +
      "     [-randomMutationRate <random mutation rate> (default=" + EvolveCommon.DEFAULT_RANDOM_MUTATION_RATE + ")]\n" +
      "     [-randomSeed <random seed> (default=" + EvolveCommon.DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-logfile <log file name>]\n" +
      "  Resume run:\n" +
      "    java EvolveMoxen\n" +
      "      -generations <evolution generations>\n" +
      "      -steps <moxen steps>\n" +
      "      -input <evolution input file name>\n" +
      "      -output <evolution output file name>\n" +
      "     [-mutationRate <mutation rate> (default=" + EvolveCommon.DEFAULT_MUTATION_RATE + ")]\n" +
      "     [-randomMutationRate <random mutation rate> (default=" + EvolveCommon.DEFAULT_RANDOM_MUTATION_RATE + ")]\n" +
      "     [-randomSeed <random seed> (default=" + EvolveCommon.DEFAULT_RANDOM_SEED + ")]\n" +
      "     [-logfile <log file name>]\n" +
      "  Print population properties:\n" +
      "    java EvolveMoxen\n" +
      "      -properties\n" +
      "      -input <evolution input file name>\n" +
      "  Print evolution statistics:\n" +
      "    java EvolveMoxen\n" +
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
   double[] Fittest;
   double[] Average;

   // Population.
   EvolveCommon.Member[] Population;

   // Constructor.
   public EvolveMoxen(String[] args)
   {
      int i;

      // Get options.
      Generation    = 0;
      Generations   = -1;
      Steps         = -1;
      InputFileName = OutputFileName = LogFileName = null;
      LogWriter     = null;
      boolean gotDimensions         = false;
      boolean gotNumObstacleTypes   = false;
      boolean gotNumObstacles       = false;
      boolean gotNumFoods           = false;
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
               System.err.println(MoxWorx.Usage);
               System.exit(2);
            }
            try
            {
               EvolveCommon.WIDTH = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid width option");
               System.err.println(MoxWorx.Usage);
               System.exit(2);
            }
            if (EvolveCommon.WIDTH < 2)
            {
               System.err.println("Invalid width option");
               System.err.println(MoxWorx.Usage);
               System.exit(2);
            }
            i++;
            if (i >= args.length)
            {
               System.err.println("Invalid dimensions option");
               System.err.println(MoxWorx.Usage);
               System.exit(2);
            }
            try
            {
               EvolveCommon.HEIGHT = Integer.parseInt(args[i]);
            }
            catch (NumberFormatException e) {
               System.err.println("Invalid height option");
               System.err.println(MoxWorx.Usage);
               System.exit(2);
            }
            if (EvolveCommon.HEIGHT < 2)
            {
               System.err.println("Invalid height option");
               System.err.println(MoxWorx.Usage);
               System.exit(2);
            }
            gotDimensions = true;
            continue;
         }

         if (args[i].equals("-numObstacleTypes"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            EvolveCommon.NUM_OBSTACLE_TYPES = Integer.parseInt(args[i]);
            if (EvolveCommon.NUM_OBSTACLE_TYPES < 1)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotNumObstacleTypes = true;
            continue;
         }

         if (args[i].equals("-numObstacles"))
         {
            i++;
            if (i >= args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            EvolveCommon.NUM_OBSTACLES = Integer.parseInt(args[i]);
            if (EvolveCommon.NUM_OBSTACLES < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotNumObstacles = true;
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
            EvolveCommon.NUM_FOODS = Integer.parseInt(args[i]);
            if (EvolveCommon.NUM_FOODS < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            gotNumFoods = true;
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
            EvolveCommon.FIT_POPULATION_SIZE = Integer.parseInt(args[i]);
            if (EvolveCommon.FIT_POPULATION_SIZE < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            EvolveCommon.setPopulationSize();
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
            EvolveCommon.NUM_MUTANTS = Integer.parseInt(args[i]);
            if (EvolveCommon.NUM_MUTANTS < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            EvolveCommon.setPopulationSize();
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
            EvolveCommon.NUM_OFFSPRING = Integer.parseInt(args[i]);
            if (EvolveCommon.NUM_OFFSPRING < 0)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            EvolveCommon.setPopulationSize();
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
            EvolveCommon.MUTATION_RATE = Double.parseDouble(args[i]);
            if ((EvolveCommon.MUTATION_RATE < 0.0) || (EvolveCommon.MUTATION_RATE > 1.0))
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
            EvolveCommon.RANDOM_MUTATION_RATE = Double.parseDouble(args[i]);
            if ((EvolveCommon.RANDOM_MUTATION_RATE < 0.0) || (EvolveCommon.RANDOM_MUTATION_RATE > 1.0))
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
            EvolveCommon.RANDOM_SEED = Integer.parseInt(args[i]);
            gotRandomSeed            = true;
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
             gotDimensions || gotNumObstacleTypes || gotNumObstacles || gotNumFoods ||
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
            if (gotDimensions || gotNumObstacleTypes || gotNumObstacles || gotNumFoods ||
                gotFitPopulationSize || gotNumMutants || gotNumOffspring)
            {
               System.err.println(Usage);
               System.exit(1);
            }
         }
      }

      // Seed random numbers.
      Randomizer = new Random(EvolveCommon.RANDOM_SEED);

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


   // Start evolve.
   public void start()
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
      log("    FIT_POPULATION_SIZE=" + EvolveCommon.FIT_POPULATION_SIZE);
      log("    NUM_MUTANTS=" + EvolveCommon.NUM_MUTANTS);
      log("    NUM_OFFSPRING=" + EvolveCommon.NUM_OFFSPRING);
      log("    MUTATION_RATE=" + EvolveCommon.MUTATION_RATE);
      log("    RANDOM_MUTATION_RATE=" + EvolveCommon.RANDOM_MUTATION_RATE);
      log("    RANDOM_SEED=" + EvolveCommon.RANDOM_SEED);

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
         if ((Generation % EvolveCommon.SAVE_FREQUENCY) == 0)
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

      Population = new EvolveCommon.Member[EvolveCommon.POPULATION_SIZE];
      for (i = 0; i < EvolveCommon.POPULATION_SIZE; i++)
      {
         if (i == 0)
         {
            Population[i] =
               new EvolveCommon.Member(0, Randomizer);
         }
         else
         {
            // Mutate parameters.
            Population[i] = new EvolveCommon.Member(Population[0], 0, Randomizer);
         }
      }
      Fittest = new double[Generations + 1];
      Average = new double[Generations + 1];
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
         EvolveCommon.loadParameters(reader);

         // Load population.
         Population = new EvolveCommon.Member[EvolveCommon.POPULATION_SIZE];
         for (i = 0; i < EvolveCommon.POPULATION_SIZE; i++)
         {
            Population[i] = new EvolveCommon.Member(0, Randomizer);
            Population[i].load(input);
         }
         Fittest = new double[Generation + Generations + 1];
         Average = new double[Generation + Generations + 1];
         for (i = 0; i < Generation; i++)
         {
            Fittest[i] = Utility.loadDouble(reader);
            Average[i] = Utility.loadDouble(reader);
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
         EvolveCommon.saveParameters(writer);

         // Save population.
         for (i = 0; i < EvolveCommon.POPULATION_SIZE; i++)
         {
            Population[i].save(output);
         }
         for (i = 0, n = generation + 1; i < n; i++)
         {
            Utility.saveDouble(writer, Fittest[i]);
            Utility.saveDouble(writer, Average[i]);
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
      // Evaluate member fitness.
      evaluate(generation);

      // Prune unfit members.
      prune();

      // Create new members by mutation.
      mutate();

      // Create new members by mating.
      mate();
   }


   // Evaluate member fitness.
   void evaluate(int generation)
   {
      EvolveCommon.Member member;
      String              logEntry;

      log("Evaluate:");
      for (int i = 0; i < EvolveCommon.POPULATION_SIZE; i++)
      {
         member = Population[i];
         member.evaluate(Steps);
         logEntry = "    member=" + i + ", " + Population[i].getInfo();
         log(logEntry);
      }
   }


   // Prune unfit members.
   void prune()
   {
      double min, d;
      int    i, j, m;

      EvolveCommon.Member member;

      log("Select:");
      EvolveCommon.Member[] fitPopulation =
         new EvolveCommon.Member[EvolveCommon.FIT_POPULATION_SIZE];
      min = EvolveCommon.INVALID_FITNESS;
      for (i = 0; i < EvolveCommon.FIT_POPULATION_SIZE; i++)
      {
         m = -1;
         for (j = 0; j < EvolveCommon.POPULATION_SIZE; j++)
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
      for (i = 0; i < EvolveCommon.POPULATION_SIZE; i++)
      {
         if (Population[i] != null)
         {
            Population[i] = null;
         }
      }
      d = 0.0;
      for (i = 0; i < EvolveCommon.FIT_POPULATION_SIZE; i++)
      {
         Population[i]    = fitPopulation[i];
         fitPopulation[i] = null;
         d += Population[i].fitness;
      }
      Fittest[Generation] = Population[0].fitness;
      Average[Generation] = d / (double)EvolveCommon.FIT_POPULATION_SIZE;
   }


   // Mutate members.
   void mutate()
   {
      int i, j;

      EvolveCommon.Member member, mutant;

      log("Mutate:");
      for (i = 0; i < EvolveCommon.NUM_MUTANTS; i++)
      {
         // Select a fit member to mutate.
         j      = Randomizer.nextInt(EvolveCommon.FIT_POPULATION_SIZE);
         member = Population[j];

         // Create mutant member.
         mutant = new EvolveCommon.Member(member, member.generation + 1, Randomizer);
         Population[EvolveCommon.FIT_POPULATION_SIZE + i] = mutant;
         log("    member=" + j + ", " + member.getInfo() +
             " -> member=" + (EvolveCommon.FIT_POPULATION_SIZE + i) +
             ", " + mutant.getInfo());
      }
   }


   // Produce offspring by melding parent parameters.
   void mate()
   {
      int i, j, k;

      EvolveCommon.Member member1, member2, offspring;

      log("Mate:");
      if (EvolveCommon.FIT_POPULATION_SIZE > 1)
      {
         for (i = 0; i < EvolveCommon.NUM_OFFSPRING; i++)
         {
            // Select a pair of fit members to mate.
            j       = Randomizer.nextInt(EvolveCommon.FIT_POPULATION_SIZE);
            member1 = Population[j];
            while ((k = Randomizer.nextInt(EvolveCommon.FIT_POPULATION_SIZE)) == j) {}
            member2 = Population[k];

            // Create offspring.
            offspring = new EvolveCommon.Member(member1, member2,
                                                (member1.generation > member2.generation ?
                                                 member1.generation : member2.generation) + 1, Randomizer);
            Population[EvolveCommon.FIT_POPULATION_SIZE +
                       EvolveCommon.NUM_MUTANTS + i] = offspring;
            log("    member=" + j + ", " + member1.getInfo() + " + member=" +
                k + ", " + member2.getInfo() +
                " -> member=" + (EvolveCommon.FIT_POPULATION_SIZE +
                                 EvolveCommon.NUM_MUTANTS + i) +
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
      for (i = 0; i < EvolveCommon.POPULATION_SIZE; i++)
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


   // Main.
   public static void main(String[] args)
   {
      EvolveMoxen evolveMoxen = new EvolveMoxen(args);

      evolveMoxen.start();
      System.exit(0);
   }
}
