/*
 * Genome.
 */

package morphognosis.moxworx;

import java.util.*;
import java.io.*;

public class Genome
{
   // Genes.
   Vector<Gene> genes;

   // Mutation rate.
   float mutationRate;

   // Probability of random mutation.
   float randomMutationRate;

   // Random numbers.
   int    randomSeed;
   Random randomizer;

   // Constructor.
   Genome(float mutationRate, float randomMutationRate, int randomSeed)
   {
      this.mutationRate       = mutationRate;
      this.randomMutationRate = randomMutationRate;
      this.randomSeed         = randomSeed;
      randomizer = new Random(randomSeed);
      genes      = new Vector<Gene>();
   }


   // Mutate.
   void mutate()
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).mutate();
      }
   }


   // Randomly merge genome values from given genome.
   void meldValues(Genome from1, Genome from2)
   {
      Gene gene;

      for (int i = 0; i < genes.size(); i++)
      {
         gene = genes.get(i);
         if (randomizer.nextBoolean())
         {
            gene.copyValue(from1.genes.get(i));
         }
         else
         {
            gene.copyValue(from2.genes.get(i));
         }
      }
   }


   // Copy genome values from given genome.
   void copyValues(Genome from)
   {
      Gene gene;

      for (int i = 0; i < genes.size(); i++)
      {
         gene = genes.get(i);
         gene.copyValue(from.genes.get(i));
      }
   }


   // Get genome as key-value pairs.
   HashMap<String, Object> getKeyValues()
   {
      Gene gene;

      HashMap<String, Object> map = new HashMap<String, Object>();

      for (int i = 0; i < genes.size(); i++)
      {
         gene = genes.get(i);
         switch (gene.type)
         {
         case INTEGER_VALUE:
            map.put(new String(gene.name), new Integer(gene.ivalue));
            break;

         case FLOAT_VALUE:
            map.put(new String(gene.name), new Float(gene.fvalue));
            break;

         case DOUBLE_VALUE:
            map.put(new String(gene.name), new Double(gene.dvalue));
            break;
         }
      }
      return(map);
   }


   // Load values.
   void loadValues(DataInputStream reader) throws IOException
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).loadValue(reader);
      }
   }


   // Save values.
   void saveValues(DataOutputStream writer) throws IOException
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).saveValue(writer);
      }
      writer.flush();
   }


   // Print genome.
   void print()
   {
      for (int i = 0; i < genes.size(); i++)
      {
         genes.get(i).print();
      }
   }
}
