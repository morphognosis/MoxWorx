// For conditions of distribution and use, see copyright notice in MoxWorx.java

package moxworx;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

// Metamorph.
public class Metamorph
{
   // Morphognostic.
   public Morphognostic morphognostic;

   // Mox response.
   public int response;

   // Constructors.
   public Metamorph(Morphognostic morphognostic, int response)
   {
      this.morphognostic = morphognostic;
      this.response      = response;
   }


   // Equality test.
   public boolean equals(Metamorph m)
   {
      if (response != m.response)
      {
         return(false);
      }
      if (morphognostic.compare(m.morphognostic) != 0.0f)
      {
         return(false);
      }
      return(true);
   }


   // Save.
   public void save(FileOutputStream output) throws IOException
   {
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));

      morphognostic.save(output);
      Utility.saveInt(writer, response);
      writer.flush();
   }


   // Load.
   public static Metamorph load(FileInputStream input) throws IOException
   {
      DataInputStream reader        = new DataInputStream(input);
      Morphognostic   morphognostic = Morphognostic.load(input);
      int             response      = Utility.loadInt(reader);

      return(new Metamorph(morphognostic, response));
   }


   // Print.
   public void print()
   {
      System.out.println("Morphognostic:");
      morphognostic.print();
      System.out.println("Response=" + response);
   }
}
