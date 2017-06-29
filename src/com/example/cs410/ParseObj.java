package com.example.cs410;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Torry on 11/17/2016.
 */
class ParseObj {

    private static final List<String> comments = new ArrayList<>();
    private static final List<String> mtllib = new ArrayList<>();
    private static final List<String> usemtl = new ArrayList<>();
    private static final List<Double> translate = new ArrayList<>();
    private static final List<Vector3D> vertices = new ArrayList<>();
    private static final List<Vector3D> faces = new ArrayList<>();
    private static final List<Vector3D> faceNormals = new ArrayList<>();
    private static final List<Vector3D> vectorNormals = new ArrayList<>();
    private static final List<Double> kd = new ArrayList<>();
    private static final List<Double> ka = new ArrayList<>();
    private static final List<Double> ks = new ArrayList<>();
    private static final List<Double> kr = new ArrayList<>(Arrays.asList(0.9,0.9,0.9));
    private static double d;
    private static int illium;
    private static double ns;

// --Commented out by Inspection START (5/10/17, 5:18 PM):
//    ParseObj() {
//
//    }
// --Commented out by Inspection STOP (5/10/17, 5:18 PM)
    public static void setTranslate(List<Double> xyz) {
    	translate.addAll(xyz);
    }

    public static void parseObjFile(Path pObj) {
        try (Stream<String> lines = Files.lines(pObj)) {

            lines.forEach((String s) -> {
                if (s.contains("#")) {
                    comments.add(s);
                } else if (s.contains("mtllib")) {
                    String coor[] = s.split(" ");
                    for (int i = 1; i < coor.length; i++) {
                        mtllib.add(coor[i]);
                        //System.out.println("material file " + coor[i]);
                        parseMtl(coor[i]);
                    }
                } else if (s.contains("usemtl")) {
                    String coor[] = s.split(" ");
                    usemtl.add(coor[1]);
                }else if (s.contains("vn")) {
                    String[] coor = s.split(" ");
                    Vector3D v = new Vector3D(Double.parseDouble(coor[1]), Double.parseDouble(coor[2]),
                            Double.parseDouble(coor[3]));
                    vectorNormals.add(v);
                }else if (s.contains("v")) {
                    String[] coor = s.split(" ");
                    Vector3D v = new Vector3D(Double.parseDouble(coor[1]) + translate.get(0), Double.parseDouble(coor[2]) + translate.get(1),
                            Double.parseDouble(coor[3])+translate.get(2));
                    vertices.add(v);

                } else if (s.contains("off")) {

                } else if (s.contains("f")) {
                    //List<Vector2D> fpair = new ArrayList<Vector2D>();
                    //List<List<Vector2D>> tot = new ArrayList<List<Vector2D>>();
                    List<Double> filler = new ArrayList<>();
                    String[] coor = s.split(" ");

                    for (int i = 1; i < coor.length; i++) {
                        String[] ss = coor[i].split("//");
                        for (String sx : ss) {
                            filler.add(Double.parseDouble(sx));
                            //System.out.println(sx);
                        }
                    }
                    //System.out.println("face " + filler.get(0) + " "+ filler.get(2) + " "+ filler.get(4));
                    Vector3D v = new Vector3D(filler.get(0)-1, filler.get(2)-1, filler.get(4)-1);
                    Vector3D v2 = new Vector3D(filler.get(1)-1, filler.get(3)-1, filler.get(5)-1);
                    faces.add(v);
                    faceNormals.add(v2);

                } else {
                		
                }
            });
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    private static void parseMtl(String fname) {

        Path p = Paths.get(fname);
        try (Stream<String> lines = Files.lines(p)) {

            lines.forEach((String s) -> {
                if (s.contains("#")) {

                } else if (s.contains("Ns")) {
                    String coor[] = s.split(" ");
                    ns = Double.parseDouble(coor[1]);
                    //System.out.println("ns " + ns);
                } else if (s.contains("Ka")) {
                    String coor[] = s.split(" ");
                    ka.add(Double.parseDouble(coor[1]));
                    ka.add(Double.parseDouble(coor[2]));
                    ka.add(Double.parseDouble(coor[3]));
                    //System.out.println("ka " + ka.get(0));
                    //System.out.println("ka " + ka.get(1));
                    //System.out.println("ka " + ka.get(2));
                } else if (s.contains("Kd")) {
                    String coor[] = s.split(" ");
                    kd.add(Double.parseDouble(coor[1]));
                    kd.add(Double.parseDouble(coor[2]));
                    kd.add(Double.parseDouble(coor[3]));
                    //System.out.println("kd " + kd.get(0));
                    //System.out.println("kd " + kd.get(1));
                    //System.out.println("kd " + kd.get(2));
                } else if (s.contains("Ks")) {
                    String coor[] = s.split(" ");
                    ks.add(Double.parseDouble(coor[1]));
                    ks.add(Double.parseDouble(coor[2]));
                    ks.add(Double.parseDouble(coor[3]));
                    //System.out.println("ks " + ks.get(0));
                    //System.out.println("ks " + ks.get(1));
                    //System.out.println("ks " + ks.get(2));
                } else if (s.contains("d")) {
                    String coor[] = s.split(" ");
                    d = Double.parseDouble(coor[1]);
                } else if (s.contains("illum")) {
                    String coor[] = s.split(" ");
                    illium = Integer.parseInt(coor[1]);
                } else if (s.contains("newmtl")) {

                }

            });
        } catch (IOException e) {
            System.out.print(e);
        }

    }

    public static List<Double> getKa() {
        return ka;
    }

    public static List<Double> getKd() {
        return kd;
    }

    public static List<Double> getKs() {
        return ks;
    }

    public static List<Vector3D> getFaces() {
        return faces;
    }

    public static List<Vector3D> getFaceNormals() {
        return faceNormals;
    }

    public static List<Vector3D> getVertices() {
        return vertices;
    }
    public static List<Vector3D> getVn() {
    	return vectorNormals;
    }
// --Commented out by Inspection START (5/10/17, 5:18 PM):
//    public static double getD() {
//    	return d;
//    }
// --Commented out by Inspection STOP (5/10/17, 5:18 PM)
    public static List<Double> getKr() {
    	return kr;
    }


}
