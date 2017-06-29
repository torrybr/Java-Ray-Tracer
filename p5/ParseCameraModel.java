


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Torry on 10/24/2016.
 */
public class ParseCameraModel {

    //Location of the focal point
    private static List<Double> eye = new ArrayList<Double>();
    private static List<Double> look = new ArrayList<Double>();
    private static List<Double> up = new ArrayList<Double>();

    //Focal length, i.e. the distance from the focal point to the image plane
    private static int d;

    //Bounds indicate the minimum and maximum extend of the bounded image rectangle on the infinite image plane in the camera horizontal and vertical directions respectively.
    private static List<Double> bounds = new ArrayList<Double>();

    //Resolution values separately indicate the pixel sampling resolution across the horizontal and vertical dimensions of the bounded rectangle.
    private static List<Integer> res = new ArrayList<Integer>();


    //Low level 'white' light with values of 0.1 on a zero to one scale.
    private static List<Double> ambientLight = new ArrayList<Double>();

    /**
     * zero or more light sources may be specified. The first four values given are the x, y, z and w coordinates of the light source in world coordinates. The fourth value w is
     * general one, but a zero indicates a light source at infinity in the direction specified by z, y, and z. The last three values indicate the red, green and blue levels of the
     * light source on a zero-one scale
     * <p>
     * Here a List of Lists is defined because we may have more than 1 light source.
     */
    private static List<List<Double>> lightSources = new ArrayList<List<Double>>();

    /**
     * zero or more spheres. The first three values are the x, y and z coordinates of the sphere in world coordinates. The fourth value is the radius of the sphere.
     * The last three values are the simplified material properties indicating the 'color' of the sphere in terms of red, green and blue.
     * <p>
     * Here a List of Lists is defined because we may have more than 1 sphere source.
     */
    private static List<List<Double>> sphereSources = new ArrayList<List<Double>>();


    /**
     * zero or more polygonal models may be specfied for inclusion in the scene. Note the first seven values may indicate a Model to World transformation.
     * Specifically, the first three are the x, y, z translation from model to world coordinates. The next four specify an axis-angle rotation. In this assignment
     * you may ignore these values if you wish and assume an identity transformation between the model and the world. Keep in mind this may change in the last
     * assignment. The last argument is a string indicating the name of the file containing the 3D polygonal model in OBJ format.
     * <p>
     * Here a List of Lists is defined because we may have more than 1 model source.
     */
    private static List<List<Double>> modelSources = new ArrayList<List<Double>>();
    private static List<String> modelPathnames = new ArrayList<String>();


    ParseCameraModel(Path camPath) {
        //System.out.println("Parsing Camera Model0");
        parseCameraModel(camPath);

    }


    private static void parseCameraModel(Path camPath) {
        try (Stream<String> lines = Files.lines(camPath)) {

            lines.forEach((String s) -> {
                if (s.contains("eye")) {
                    String[] coor = s.split(" ");
                    eye.add(Double.parseDouble(coor[1]));
                    eye.add(Double.parseDouble(coor[2]));
                    eye.add(Double.parseDouble(coor[3]));
                } else if (s.contains("look")) {
                    String[] coor = s.split(" ");
                    look.add(Double.parseDouble(coor[1]));
                    look.add(Double.parseDouble(coor[2]));
                    look.add(Double.parseDouble(coor[3]));
                } else if (s.contains("up")) {
                    String[] coor = s.split(" ");
                    up.add(Double.parseDouble(coor[1]));
                    up.add(Double.parseDouble(coor[2]));
                    up.add(Double.parseDouble(coor[3]));

                } else if (s.contains("bounds")) {
                    String[] coor = s.split(" ");
                    bounds.add(Double.parseDouble(coor[1]));
                    bounds.add(Double.parseDouble(coor[2]));
                    bounds.add(Double.parseDouble(coor[3]));
                    bounds.add(Double.parseDouble(coor[4]));
                } else if (s.contains("res")) {
                    String[] coor = s.split(" ");
                    res.add(Integer.parseInt(coor[1]));
                    res.add(Integer.parseInt(coor[2]));
                } else if (s.contains("ambient")) {
                    String[] coor = s.split(" ");
                    ambientLight.add(Double.parseDouble(coor[1]));
                    ambientLight.add(Double.parseDouble(coor[2]));
                    ambientLight.add(Double.parseDouble(coor[3]));
                    //System.out.println("Ambient Light: " + coor[1]);
                    //System.out.println("Ambient Light: " + coor[2]);
                    //System.out.println("Ambient Light: " + coor[3]);
                } else if (s.contains("light")) {
                    String[] coor = s.split(" ");
                    List<Double> light = new ArrayList<Double>();
                    for (int i = 1; i < coor.length; i++) {
                        //i starts at 1 so the word is not contained the array.
                        //System.out.println("Light:  " +coor[i]);
                        light.add(Double.parseDouble(coor[i]));
                    }
                    //System.out.println("**** END LIGHT SOURCE");
                    lightSources.add(light);
                } else if (s.contains("sphere")) {
                    String[] coor = s.split(" ");
                    List<Double> sphere = new ArrayList<Double>();
                    for (int i = 1; i < coor.length; i++) {
                        //System.out.println("Sphere = " + coor[i]);
                        //i starts at 1 so the word is not contained the array.
                        sphere.add(Double.parseDouble(coor[i]));
                    }
                    //System.out.println("**** END SPHERE SOURCE");
                    sphereSources.add(sphere);

                } else if (s.contains("model")) {
                    String[] coor = s.split(" ");
                    List<Double> model = new ArrayList<Double>();
                    for (int i = 1; i < coor.length - 1; i++) {
                        //i starts at 1 so the word is not contained the array, and it ends at i -1 so that the path name is not included in the List.
                        //System.out.println("Model = " + coor[i]);
                        model.add(Double.parseDouble(coor[i]));
                    }
                    //System.out.println("*** END MODEL SOURCE");
                    modelSources.add(model);
                    //This adds the pathname at the end of the line to an arraylist.
                    //System.out.println(coor[8]);
                    modelPathnames.add(coor[8]);

                } else {
                	//System.out.println(s);
                    String[] coor = s.split(" ");
                    d = Integer.parseInt(coor[1]);
                }
            });
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    public static List<Double> getEye() {
        return eye;
    }

    public static List<Double> getLook() {
        return look;
    }

    public static List<Double> getUp() {
        return up;
    }

    public static int getD() {
        return d;
    }

    public static List<Double> getBounds() {
        return bounds;
    }

    public static List<Integer> getRes() {
        return res;
    }

    public static List<Double> getAmbient() {
        return ambientLight;

    }

    public static List<List<Double>> getLightSources() {
        return lightSources;
    }

    public static List<List<Double>> getSphereSources() {
        return sphereSources;
    }

    public static List<List<Double>> getModelSources() {
        return modelSources;
    }

    public static List<String> getModelPathnames() {
        return modelPathnames;
    }

}
