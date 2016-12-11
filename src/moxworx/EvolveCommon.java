/*
 * Evolution common.
 */

package moxworx;

import java.util.*;
import java.io.*;

public class EvolveCommon
{
   // Parameters.
   public static final int    DEFAULT_WIDTH                = 10;
   public static int          WIDTH                        = DEFAULT_WIDTH;
   public static final int    DEFAULT_HEIGHT               = 10;
   public static int          HEIGHT                       = DEFAULT_HEIGHT;
   public static final int    DEFAULT_NUM_OBSTACLE_TYPES   = 1;
   public static int          NUM_OBSTACLE_TYPES           = DEFAULT_NUM_OBSTACLE_TYPES;
   public static final int    DEFAULT_NUM_OBSTACLES        = 0;
   public static int          NUM_OBSTACLES                = DEFAULT_NUM_OBSTACLES;
   public static final int    DEFAULT_NUM_FOODS            = 0;
   public static int          NUM_FOODS                    = DEFAULT_NUM_FOODS;
   public static final int    DEFAULT_FIT_POPULATION_SIZE  = 20;
   public static int          FIT_POPULATION_SIZE          = DEFAULT_FIT_POPULATION_SIZE;
   public static final int    DEFAULT_NUM_MUTANTS          = 10;
   public static int          NUM_MUTANTS                  = DEFAULT_NUM_MUTANTS;
   public static final int    DEFAULT_NUM_OFFSPRING        = 10;
   public static int          NUM_OFFSPRING                = DEFAULT_NUM_OFFSPRING;
   public static int          POPULATION_SIZE              = (FIT_POPULATION_SIZE + NUM_MUTANTS + NUM_OFFSPRING);
   public static final double DEFAULT_MUTATION_RATE        = 0.25;
   public static double       MUTATION_RATE                = DEFAULT_MUTATION_RATE;
   public static final double DEFAULT_RANDOM_MUTATION_RATE = 0.5;
   public static double       RANDOM_MUTATION_RATE         = DEFAULT_RANDOM_MUTATION_RATE;
   public static final int    DEFAULT_RANDOM_SEED          = 4517;
   public static int          RANDOM_SEED                  = DEFAULT_RANDOM_SEED;
   public static final int    SAVE_FREQUENCY               = 1;
   public static final float  INVALID_FITNESS              = 1000.0f;

   public static void setPopulationSize()
   {
      POPULATION_SIZE = (FIT_POPULATION_SIZE + NUM_MUTANTS + NUM_OFFSPRING);
   }


   // Mox parameter genome.
   public static class MoxParmGenome extends Genome
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
   public static int IDdispenser = 0;

   // Population member.
   public static class Member
   {
      public int    id;
      public int    generation;
      public double fitness;
      public Random randomizer;

      // Mox parameters.
      public MoxParmGenome moxParmGenome;

      // Constructors.
      public Member(int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0;

         // Create parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
      }


      // Construct mutation of given member.
      public Member(Member member, int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0;

         // Create and mutate parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
         moxParmGenome.copyValues(member.moxParmGenome);
         moxParmGenome.mutate();
      }


      // Construct by melding given members.
      public Member(Member member1, Member member2, int generation, Random randomizer)
      {
         id = IDdispenser++;
         this.generation = generation;
         this.randomizer = randomizer;
         fitness         = 0.0;

         // Create and meld parameter genome.
         moxParmGenome = new MoxParmGenome(randomizer);
         moxParmGenome.meldValues(member1.moxParmGenome, member2.moxParmGenome);
      }


      // Evaluate fitness.
      public void evaluate(int steps)
      {
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
         MoxWorx moxWorx = new MoxWorx();
         moxWorx.random = new Random(RANDOM_SEED);
         try
         {
            moxWorx.initCells(WIDTH, HEIGHT, NUM_OBSTACLE_TYPES, NUM_OBSTACLES, NUM_FOODS);
            moxWorx.createMoxen(1, NUM_OBSTACLE_TYPES + 1,
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
         for (Mox mox : moxWorx.moxen)
         {
            mox.driver = Mox.DRIVER_TYPE.AUTOPILOT.getValue();
         }
         try
         {
            moxWorx.run(steps);
         }
         catch (Exception e)
         {
            System.err.println("Cannot train member " + id + ": " + e.getMessage());
            return;
         }

         // Test.
         moxWorx.reset();
         for (Mox mox : moxWorx.moxen)
         {
            mox.driver = Mox.DRIVER_TYPE.METAMORPH_DB.getValue();
         }
         try
         {
            fitness = (float)moxWorx.run(steps);
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
         fitness    = Utility.loadDouble(reader);

         // Load parameter genome.
         moxParmGenome.loadValues(reader);
      }


      // Save member.
      void save(FileOutputStream output) throws IOException
      {
         PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));

         Utility.saveInt(writer, id);
         Utility.saveInt(writer, generation);
         Utility.saveDouble(writer, fitness);
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

   // Load evolution.
   public static void loadParameters(DataInputStream reader) throws IOException
   {
      EvolveCommon.WIDTH               = Utility.loadInt(reader);
      EvolveCommon.HEIGHT              = Utility.loadInt(reader);
      EvolveCommon.NUM_OBSTACLE_TYPES  = Utility.loadInt(reader);
      EvolveCommon.NUM_OBSTACLES       = Utility.loadInt(reader);
      EvolveCommon.NUM_FOODS           = Utility.loadInt(reader);
      EvolveCommon.FIT_POPULATION_SIZE = Utility.loadInt(reader);
      EvolveCommon.NUM_MUTANTS         = Utility.loadInt(reader);
      EvolveCommon.NUM_OFFSPRING       = Utility.loadInt(reader);
      EvolveCommon.setPopulationSize();
   }


   // Save parameters.
   public static void saveParameters(PrintWriter writer) throws IOException
   {
      Utility.saveInt(writer, EvolveCommon.WIDTH);
      Utility.saveInt(writer, EvolveCommon.HEIGHT);
      Utility.saveInt(writer, EvolveCommon.NUM_OBSTACLE_TYPES);
      Utility.saveInt(writer, EvolveCommon.NUM_OBSTACLES);
      Utility.saveInt(writer, EvolveCommon.NUM_FOODS);
      Utility.saveInt(writer, EvolveCommon.FIT_POPULATION_SIZE);
      Utility.saveInt(writer, EvolveCommon.NUM_MUTANTS);
      Utility.saveInt(writer, EvolveCommon.NUM_OFFSPRING);
   }
}
