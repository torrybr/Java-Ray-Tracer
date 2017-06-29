

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Torry on 11/1/2016.
 */
public class NormalizeModel {
    private static ArrayList<String> comments = new ArrayList<String>();
    private static int vertex;
    private static int face;
    private static List<Double> xVertice = new ArrayList<Double>();
    private static List<Double> yVertice = new ArrayList<Double>();
    private static List<Double> zVertice = new ArrayList<Double>();
    private static List<Vector3D> vertices = new ArrayList<Vector3D>();
    private static List<Vector3D> faces = new ArrayList<Vector3D>();
    private static List<String> properties = new ArrayList<String>();
    //private static List<Vector3D> pixelRays = new ArrayList<Vector3D>();
    //private static List<Vector3D> pixelPoints = new ArrayList<Vector3D>();
    private static List<Double> tValuesIntersect = new ArrayList<Double>();
    private static List<Double> alltValues = new ArrayList<Double>();
    private static int countv = 0;
    public static void parsePLY(Path path, String filename) {
        try (Stream<String> lines = Files.lines(path)) {

            lines.forEach((String s) -> {

                if (s.contains("ply")) {
                } else if (s.contains("format")) {
                } else if (s.contains("comment")) {
                    comments.add(s);
                } else if (s.contains("property")) {
                    properties.add(s);
                } else if (s.contains("element") && s.contains("vertex")) {
                    String[] parts = s.split(" ");
                    vertex = Integer.parseInt(parts[2]);
                } else if (s.contains("element") && s.contains("face")) {
                    String[] parts = s.split(" ");
                    face = Integer.parseInt(parts[2]);
                } else if (s.contains("end_header")) {

                } else {
                    if (countv < vertex) {
                        String[] coor = s.split(" ");
                        Vector3D v = new Vector3D(Double.parseDouble(coor[0]),Double.parseDouble(coor[1]),Double.parseDouble(coor[2]));
                        vertices.add(v);
                        xVertice.add(Double.parseDouble(coor[0]));
                        yVertice.add(Double.parseDouble(coor[1]));
                        zVertice.add(Double.parseDouble(coor[2]));
                        countv++;
                    } else {
                        String[] coor = s.split((" "));
                        //faces.add(s);
                        faces.add(new Vector3D(Integer.parseInt(coor[1]), Integer.parseInt(coor[2]), Integer.parseInt(coor[3])));

                    }
                }

            });
        } catch (IOException e) {
            System.out.print("Error reading file");
        }
        //List<Double> means = calculateMeans(xVertice, yVertice, zVertice);
        //List<Double> bbox = calcBoundingBox(xVertice, yVertice, zVertice);
        //List<Double> stdDev = calcStdDev(means, xVertice, yVertice, zVertice);

        //beforeCenteringPrint("=== Before Centering ", means, bbox, stdDev);

        //List<Double> center = modelCentering(bbox, means, filename);
    }
    public static List<Vector3D> getVertices() {
        return vertices;
    }

    public static List<Vector3D> getFaces() {
        return faces;
    }

    private static void beforeCenteringPrint(String stage, List<Double> means, List<Double> bbox, List<Double> stdDev) {
        //System.out.println(stage);
        //System.out.println(vertex + " vertices, " + face + " polygons");
        //System.out.println("Mean Vertex = (" + means.get(0) + ", " + means.get(1) + ", " + means.get(2) + ")");
        // System.out.println("Bounding Box: " + bbox.get(0) + " <= x <= " + bbox.get(1) + ", " + bbox.get(2) + " <= y <= " + bbox.get(3) + ", " + bbox.get(4) + " <= z <= " + bbox.get(5));
        //System.out.println("Standard Deviations: x = " + stdDev.get(0) + ", y = " + stdDev.get(1) + ", z = " + stdDev.get(2));
    }


    private static List<Double> calculateMeans(List<Double> xVert, List<Double> yVert, List<Double> zVert) {
        double xMean = 0;
        double yMean = 0;
        double zMean = 0;
        if (!xVert.isEmpty() && !yVert.isEmpty() && !zVert.isEmpty()) {
            for (Double x : xVert) {
                //System.out.println(x + "*");
                xMean += x;

            }
            for (Double y : yVert) {
                yMean += y;
            }
            for (Double z : zVert) {
                zMean += z;
            }
        }
        List<Double> means = new ArrayList<Double>();
        means.add(xMean / xVert.size());
        means.add(yMean / yVert.size());
        means.add(zMean / zVert.size());
        return means;
    }

    private static List<Double> calcBoundingBox(List<Double> xVert, List<Double> yVert, List<Double> zVert) {
        List<Double> bbox = new ArrayList<Double>();
        double xMin = 0;
        double xMax = 0;
        double yMin = 0;
        double yMax = 0;
        double zMin = 0;
        double zMax = 0;
        if (!xVert.isEmpty() && !yVert.isEmpty() && !zVert.isEmpty()) {
            for (Double x : xVert) {
                if (x < xMin) xMin = x;
                if (x > xMax) xMax = x;
            }
            for (Double y : yVert) {
                if (y < yMin) yMin = y;
                if (y > yMax) yMax = y;
            }
            for (Double z : zVert) {
                if (z < zMin) zMin = z;
                if (z > zMax) zMax = z;
            }
        }
        bbox.add(xMin);
        bbox.add(xMax);
        bbox.add(yMin);
        bbox.add(yMax);
        bbox.add(zMin);
        bbox.add(zMax);
        return bbox;
    }

    private static List<Double> calcStdDev(List<Double> means, List<Double> xVert, List<Double> yVert, List<Double> zVert) {
        List<Double> xstdDev = new ArrayList<Double>();
        List<Double> ystdDev = new ArrayList<Double>();
        List<Double> zstdDev = new ArrayList<Double>();
        List<Double> stdDev = new ArrayList<Double>();
        for (Double x : xVert) {
            double su = Math.pow((x - means.get(0)), 2);
            xstdDev.add(su);
        }
        for (Double y : yVert) {
            //System.out.println(y + " Y value");
            double su = Math.pow((y - means.get(1)), 2);
            ystdDev.add(su);
        }
        for (Double z : zVert) {
            //System.out.println(z + " Z value");
            double su = Math.pow((z - means.get(0)), 2);
            zstdDev.add(su);
        }

        List<Double> stdDevMeans = calculateMeans(xstdDev, ystdDev, zstdDev);
        stdDev.add(Math.sqrt(stdDevMeans.get(0))); //Take Square root of x mean
        stdDev.add(Math.sqrt(stdDevMeans.get(1))); //Take Square root of y mean
        stdDev.add(Math.sqrt(stdDevMeans.get(2))); //Take Square root of z mean

        return stdDev;
    }

    private static List<Double> modelCentering(List<Double> minMax, List<Double> means, String filename) {
        List<Double> translateX = new ArrayList<Double>();
        List<Double> translateY = new ArrayList<Double>();
        List<Double> translateZ = new ArrayList<Double>();

        for (Double xx : xVertice) {
            translateX.add(xx - means.get(0));
        }
        for (Double yy : yVertice) {
            //System.out.println(yy + " !!");
            translateY.add(yy - means.get(1));
        }
        for (Double zz : zVertice) {
            translateZ.add(zz - means.get(2));
        }

        List<Double> meanCentered = calculateMeans(translateX, translateY, translateZ);
        List<Double> bbox = calcBoundingBox(translateX, translateY, translateZ);
        List<Double> stdDev = calcStdDev(meanCentered, translateX, translateY, translateZ);

        beforeCenteringPrint("=== After Centering", meanCentered, bbox, stdDev);

        rounding(translateX, translateY, translateZ, stdDev, filename);

        outputFile("_centered.ply", filename, translateX, translateY, translateZ);
        return null;
    }

    private static void rounding(List<Double> translateX, List<Double> translateY, List<Double> translateZ, List<Double> stdDev, String filename) {
        List<Double> roundX = new ArrayList<Double>();
        List<Double> roundY = new ArrayList<Double>();
        List<Double> roundZ = new ArrayList<Double>();


        for (Double xx : translateX) {
            roundX.add(xx / stdDev.get(0));
        }
        for (Double yy : translateY) {
            //System.out.println(yy + " !!");
            roundY.add(yy / stdDev.get(1));
        }
        for (Double zz : translateZ) {
            roundZ.add(zz / stdDev.get(2));
        }
        List<Double> meanRounded = calculateMeans(roundX, roundY, roundZ);
        List<Double> bbox = calcBoundingBox(roundX, roundY, roundZ);
        List<Double> stdDevRounded = calcStdDev(meanRounded, roundX, roundY, roundZ);

        beforeCenteringPrint("=== After Whitening", meanRounded, bbox, stdDevRounded);

        //outputFile("_rounded.ply", filename, roundX, roundY, roundZ);
    }

    private static void outputFile(String newfilename, String oldFilename, List<Double> translateX, List<Double> translateY, List<Double> translateZ) {
        String[] sb = oldFilename.split(("\\."));
        oldFilename = sb[0];
    }
}
