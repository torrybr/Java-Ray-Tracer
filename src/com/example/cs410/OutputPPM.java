package com.example.cs410;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Created by Torry on 10/24/2016.
 */
class OutputPPM {
    private static String outputFilename;
    private static List<Integer> res;

    OutputPPM(String filename,List<Integer> res1) {
        outputFilename = filename;
        res = res1;
        // System.out.println(outputFilename);
    }

    public static void writePPM(List<Vector3D> outPixVal) {
        PrintWriter writer;
        int res1 = res.get(0) * res.get(1);
        try {
            writer = new PrintWriter(outputFilename, "UTF-8");
            writer.println("P3");
            writer.println(res.get(0)+ " " + res.get(1) + " 255");

            //THIS WORKS

            for(int j = 0; j<res1;j++) {
                //System.out.println((int)outPixVal.get(j).getX() +" " +(int)outPixVal.get(j).getY() + " " + (int)outPixVal.get(j).getZ()+ " ");
                writer.print((int)outPixVal.get(j).getX() +" " +(int)outPixVal.get(j).getY() + " " + (int)outPixVal.get(j).getZ() + " ");
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
