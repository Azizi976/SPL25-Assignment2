package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {

      // Check that args is valid
      if (args.length != 3 ){
        System.err.println("The input has to include 3 argumens");
        return;
      }
      // Initialiize parser
      InputParser parser = new InputParser();

      try{
        // Use parse function to get root
        ComputationNode root = parser.parse(args[1]);

        // Get num of threads and change its type to int
        int treads = Integer.parseInt(args[0]);
        LinearAlgebraEngine engine = new LinearAlgebraEngine(treads);

        // Initialize result 
        double[][] result = engine.run(root).getMatrix();
        OutputWriter.write(result, args[2]);
      }
      
      // Handle errors
      catch (ParseException e){
        OutputWriter.write(e.getMessage(), args[2]);
      }
      catch (Exception e){
        OutputWriter.write(e.getMessage(), args[2]);
      }
    }
}