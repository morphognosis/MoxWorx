/*
 * Copyright (c) 2016 Tom Portegys (portegys@gmail.com). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY TOM PORTEGYS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// MoxWorx.

package morphognosis.moxworx;

import java.awt.Color;

public class MoxWorx
{
   // Usage.
   public static final String Usage = "Usage: java moxworx.MoxWorx\n" +
                                      "\t-forage <forage task options> |\n" +
                                      "\t-pong <Pong game task options> |\n" +
                                      "\t-nest <nest building task options>\n" +
                                      "Exit codes:\n" +
                                      "  0=success\n" +
                                      "  1=fail\n" +
                                      "  2=error";

   // Empty cell.
   public static final int   EMPTY_CELL_VALUE = 0;
   public static final Color EMPTY_CELL_COLOR = Color.WHITE;

   // Main.
   // Exit codes:
   // 0=success
   // 1=fail
   // 2=error
   public static void main(String[] args)
   {
      if (args.length == 0)
      {
         System.err.println(MoxWorx.Usage);
         System.exit(2);
      }
      String[] taskArgs = new String[args.length - 1];
      for (int i = 1; i < args.length; i++)
      {
         taskArgs[i - 1] = args[i];
      }

      // Run task.
      if (args[0].equals("-forage"))
      {
         Forage.main(taskArgs);
      }
      else if (args[0].equals("-pong"))
      {
         Pong.main(taskArgs);
      }
      else if (args[0].equals("-nest"))
      {
         Nest.main(taskArgs);
      }
      else
      {
         System.err.println(MoxWorx.Usage);
      }
      System.exit(0);
   }
}
