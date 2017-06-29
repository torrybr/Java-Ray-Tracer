

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.lang.Math;

import org.apache.commons.math3.geometry.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by Torry on 10/24/2016. PA2 CS 410 Torrybr@rams.colostate.edu
 */
public class RayTracer {
	private static String typeFile;
	private static String version;
	private static ArrayList<String> comments = new ArrayList<String>();
	private static String eleVertex;
	private static String eleFace;
	private static int vertex;
	private static int face;
	private static List<Double> xVertice = new ArrayList<Double>();
	private static List<Double> yVertice = new ArrayList<Double>();
	private static List<Double> zVertice = new ArrayList<Double>();
	private static List<Vector3D> vertices = new ArrayList<Vector3D>();
	private static List<Vector3D> faces = new ArrayList<Vector3D>();
	private static List<String> properties = new ArrayList<String>();
	private static List<Double> polyTvals = new ArrayList<Double>();
	private static List<List<Double>> sphereSources = new ArrayList<List<Double>>();
	private static List<List<Vector3D>> sphereScene = new ArrayList<List<Vector3D>>();
	private static List<Vector3D> polyScene = new ArrayList<Vector3D>();
	private static List<List<Double>> sphereTvals = new ArrayList<List<Double>>();
	private static List<List<Double>> lightSources = new ArrayList<List<Double>>();
	private static List<Double> ambientLight = new ArrayList<Double>();
	private static List<Double> alltValues = new ArrayList<Double>();
	private static List<Double> emptySphere = new ArrayList<Double>();
	private static List<List<Double>> model = new ArrayList<List<Double>>();
	private static int countv = 0;
	private static double tmin = 0;
	private static double tmax = 0;
	private static boolean polyModel = FALSE;
	private static boolean sphereModel = FALSE;
    private static List<Double> kd = new ArrayList<Double>();
    private static List<Double> ka = new ArrayList<Double>();
    private static List<Double> ks = new ArrayList<Double>();
    private static List<Vector3D> vn = new ArrayList<Vector3D>();
    private static List<Vector3D> faceNormals = new ArrayList<Vector3D>();

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("You need a model, obj and output name");
		} else {
			long start = System.currentTimeMillis();
			// Parse Camera Model in ParseCameraModelClass
			Path camPath = Paths.get(args[0]);
			ParseCameraModel pcm = new ParseCameraModel(camPath);
			
			/**
			 * Need to see if there are any obj files returned from getModel
			 */
			ParseObj pObj = new ParseObj();
			
			
			if(!pcm.getModelPathnames().isEmpty()) {
				ParseObj.setTranslate(pcm.getModelSources().get(0));
			}
			
			for (String filename : pcm.getModelPathnames()) {
				Path objPath = Paths.get(filename);
				pObj.parseObjFile(objPath);
				
				polyModel = TRUE;
				vertices = ParseObj.getVertices();
				faces = ParseObj.getFaces();
				model = pcm.getModelSources();
				ka.addAll(ParseObj.getKa());
				kd.addAll(ParseObj.getKd());
				ks.addAll(ParseObj.getKs());
				vn.addAll(ParseObj.getVn());
				faceNormals.addAll(ParseObj.getFaceNormals());
				
			}

			if (!pcm.getSphereSources().isEmpty()) {
				sphereModel = TRUE;
				sphereSources = pcm.getSphereSources();

			}

			if (!pcm.getAmbient().isEmpty()) {
				ambientLight = pcm.getAmbient();
			}

			if (!pcm.getLightSources().isEmpty()) {
				lightSources = pcm.getLightSources();
			}

			// Parse PLY file
			// String filename = new File(args[1]).getName();
			// Path path = Paths.get(args[1]);
			// parsePLY(path, filename);

			// Output File name
			String outputFilename = new File(args[1]).getName();
			OutputPPM oPPM = new OutputPPM(outputFilename, pcm.getRes());

			// Build camera system origin and axes in world coordinates and
			// compute ray's
			setCamCoordinates(pcm.getEye(), pcm.getLook(), pcm.getUp(), pcm.getD(), pcm.getBounds(), pcm.getRes(),
					oPPM);
			// colorEncodePixel(oPPM);
			long end = System.currentTimeMillis();

			NumberFormat formatter = new DecimalFormat("#0.00000");
			System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
		}
	}

	private static void parsePLY(Path path, String filename) {
		try (Stream<String> lines = Files.lines(path)) {

			lines.forEach((String s) -> {

				if (s.contains("ply")) {
					typeFile = s;
				} else if (s.contains("format")) {
					version = s;
				} else if (s.contains("comment")) {
					comments.add(s);
				} else if (s.contains("property")) {
					properties.add(s);
				} else if (s.contains("element") && s.contains("vertex")) {
					String[] parts = s.split(" ");
					vertex = Integer.parseInt(parts[2]);
					eleVertex = s;
				} else if (s.contains("element") && s.contains("face")) {
					String[] parts = s.split(" ");
					face = Integer.parseInt(parts[2]);
					eleFace = s;
				} else if (s.contains("end_header")) {

				} else {
					if (countv < vertex) {
						String[] coor = s.split(" ");
						Vector3D v = new Vector3D(Double.parseDouble(coor[0]), Double.parseDouble(coor[1]),
								Double.parseDouble(coor[2]));
						vertices.add(v);
						xVertice.add(Double.parseDouble(coor[0]));
						yVertice.add(Double.parseDouble(coor[1]));
						zVertice.add(Double.parseDouble(coor[2]));
						countv++;
					} else {
						String[] coor = s.split((" "));
						// faces.add(s);
						faces.add(new Vector3D(Integer.parseInt(coor[1]), Integer.parseInt(coor[2]),
								Integer.parseInt(coor[3])));

					}
				}

			});
		} catch (IOException e) {
			System.out.print("Error reading file");
		}
		// List<Double> means = calculateMeans(xVertice, yVertice, zVertice);
		// List<Double> bbox = calcBoundingBox(xVertice, yVertice, zVertice);
		// List<Double> stdDev = calcStdDev(means, xVertice, yVertice,
		// zVertice);

		// beforeCenteringPrint("=== Before Centering ", means, bbox, stdDev);

		// List<Double> center = modelCentering(bbox, means, filename);
	}

	private static void beforeCenteringPrint(String stage, List<Double> means, List<Double> bbox, List<Double> stdDev) {
		// System.out.println(stage);
		// System.out.println(vertex + " vertices, " + face + " polygons");
		// System.out.println("Mean Vertex = (" + means.get(0) + ", " +
		// means.get(1) + ", " + means.get(2) + ")");
		// System.out.println("Bounding Box: " + bbox.get(0) + " <= x <= " +
		// bbox.get(1) + ", " + bbox.get(2) + " <= y <= " + bbox.get(3) + ", " +
		// bbox.get(4) + " <= z <= " + bbox.get(5));
		// System.out.println("Standard Deviations: x = " + stdDev.get(0) + ", y
		// = " + stdDev.get(1) + ", z = " + stdDev.get(2));
	}

	private static List<Double> calculateMeans(List<Double> xVert, List<Double> yVert, List<Double> zVert) {
		double xMean = 0;
		double yMean = 0;
		double zMean = 0;
		if (!xVert.isEmpty() && !yVert.isEmpty() && !zVert.isEmpty()) {
			for (Double x : xVert) {
				// System.out.println(x + "*");
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
				if (x < xMin)
					xMin = x;
				if (x > xMax)
					xMax = x;
			}
			for (Double y : yVert) {
				if (y < yMin)
					yMin = y;
				if (y > yMax)
					yMax = y;
			}
			for (Double z : zVert) {
				if (z < zMin)
					zMin = z;
				if (z > zMax)
					zMax = z;
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

	private static List<Double> calcStdDev(List<Double> means, List<Double> xVert, List<Double> yVert,
			List<Double> zVert) {
		List<Double> xstdDev = new ArrayList<Double>();
		List<Double> ystdDev = new ArrayList<Double>();
		List<Double> zstdDev = new ArrayList<Double>();
		List<Double> stdDev = new ArrayList<Double>();
		int xtotal1 = 0;
		int ytotal = 0;
		int ztotal = 0;
		for (Double x : xVert) {
			double su = Math.pow((x - means.get(0)), 2);
			xstdDev.add(su);
		}
		for (Double y : yVert) {
			// System.out.println(y + " Y value");
			double su = Math.pow((y - means.get(1)), 2);
			ystdDev.add(su);
		}
		for (Double z : zVert) {
			// System.out.println(z + " Z value");
			double su = Math.pow((z - means.get(0)), 2);
			zstdDev.add(su);
		}

		List<Double> stdDevMeans = calculateMeans(xstdDev, ystdDev, zstdDev);
		stdDev.add(Math.sqrt(stdDevMeans.get(0))); // Take Square root of x mean
		stdDev.add(Math.sqrt(stdDevMeans.get(1))); // Take Square root of y mean
		stdDev.add(Math.sqrt(stdDevMeans.get(2))); // Take Square root of z mean

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
			// System.out.println(yy + " !!");
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

	private static void rounding(List<Double> translateX, List<Double> translateY, List<Double> translateZ,
			List<Double> stdDev, String filename) {
		List<Double> roundX = new ArrayList<Double>();
		List<Double> roundY = new ArrayList<Double>();
		List<Double> roundZ = new ArrayList<Double>();

		for (Double xx : translateX) {
			roundX.add(xx / stdDev.get(0));
		}
		for (Double yy : translateY) {
			// System.out.println(yy + " !!");
			roundY.add(yy / stdDev.get(1));
		}
		for (Double zz : translateZ) {
			roundZ.add(zz / stdDev.get(2));
		}
		List<Double> meanRounded = calculateMeans(roundX, roundY, roundZ);
		List<Double> bbox = calcBoundingBox(roundX, roundY, roundZ);
		List<Double> stdDevRounded = calcStdDev(meanRounded, roundX, roundY, roundZ);

		beforeCenteringPrint("=== After Whitening", meanRounded, bbox, stdDevRounded);

		// outputFile("_rounded.ply", filename, roundX, roundY, roundZ);
	}

	private static void outputFile(String newfilename, String oldFilename, List<Double> translateX,
			List<Double> translateY, List<Double> translateZ) {
		String[] sb = oldFilename.split(("\\."));
		oldFilename = sb[0];
		PrintWriter writer = null;
		/*
		 * try { writer = new PrintWriter(oldFilename + newfilename, "UTF-8");
		 * writer.println("ply"); writer.println(version);
		 * writer.println(eleVertex); writer.println(properties.get(0));
		 * writer.println(properties.get(1)); writer.println(properties.get(2));
		 * writer.println(eleFace); writer.println(properties.get(3));
		 * writer.println("end_header"); for (int i = 0; i < translateX.size();
		 * i++) { writer.println(translateX.get(i).toString() + " " +
		 * translateY.get(i).toString() + " " + translateZ.get(i).toString()); }
		 * //for (String s : faces) { // writer.println(s); //}
		 *
		 * writer.close(); } catch (FileNotFoundException e) {
		 * e.printStackTrace(); } catch (UnsupportedEncodingException e) {
		 * e.printStackTrace(); }
		 */
	}

	private static void setCamCoordinates(List<Double> eye, List<Double> look, List<Double> up, int d,
                                          List<Double> bounds, List<Integer> res, OutputPPM oPPM) {
        Vector3D EV = new Vector3D(eye.get(0), eye.get(1), eye.get(2));
        Vector3D LV = new Vector3D(look.get(0), look.get(1), look.get(2));
        Vector3D UP = new Vector3D(up.get(0), up.get(1), up.get(2));
        Vector3D WV = new Vector3D(0, 0, 0);
        Vector3D UV = new Vector3D(0, 0, 0);
        Vector3D VV = new Vector3D(0, 0, 0);
         List<Vector3D> outPixVal = new ArrayList<Vector3D>();

        // System.out.println("EV = " + EV.toString());
        // System.out.println("LV = " + LV.toString());
        // System.out.println("UP = " + UP.toString());
        // System.out.println("WV = " + WV.toString());
        // System.out.println("UV = " + UV.toString());
        // System.out.println("VV = " + VV.toString());

        WV = EV.subtract(LV);
        // System.out.println("WV = EV-LV -> " + WV.toString());
        WV = WV.normalize();
        // System.out.println("WV Normailized = " + WV.toString());

        UV = UP.crossProduct(WV);
        // System.out.println("UV = UP Crossproduct with WV = " +
        // UV.toString());
        UV = UV.normalize();
        // System.out.println("UV Normalized = " + UV.toString());

        VV = WV.crossProduct(UV);
        // System.out.println("VV = WV crossproduct with UV = " +
        // VV.toString());

        int count = 0;

        Ray raycompute = new Ray(vertices, faces, lightSources, ambientLight,kd,ks,ka,vn,faceNormals);

        // The 3d vector points containing all the locations of the pixels on
        // the image plane
        // List<Vector3D> pixelRays = new ArrayList<Vector3D>();
        // Verify this is correct way to traverse
        if(sphereModel && polyModel) {
        	if(sphereModel) {
        		for (List<Double> sphere : sphereSources) {
   				 List<Vector3D> sphMod = new ArrayList<Vector3D>();
                   List<Double> sphT = new ArrayList<Double>();
                   //System.out.println("Sphere info " + sphere.get(0));
                   Ray.clearSphereOutput();
                   Ray.clearSpheretValues();
                   for (int j = 0; j < res.get(1); j++) {
                       for (int i = 0; i < res.get(0); i++) {
                           // Throw a Ray from focal point to pixel.
                           pixelRays(i, j, res, bounds, d, EV, WV, UV, VV, raycompute, sphere);
                       }
                   }

                   int j = 0;
                   int i = 0;
                   sphMod.addAll(Ray.getSphereOutput());
                   sphT.addAll(Ray.getSphereT());
                   count++;
                   sphereScene.add(sphMod);
                   sphereTvals.add(sphT);
                   // sphMod.clear();
               }
        	
			}
			if (polyModel) {
				for (int j = 0; j < res.get(1); j++) {
					for (int i = 0; i < res.get(0); i++) {
						// Throw a Ray from focal point to pixel.
						pixelRays(i, j, res, bounds, d, EV, WV, UV, VV, raycompute, emptySphere);
					}

				}
				polyScene.addAll(Ray.getPolyOuptut());
                polyTvals.addAll(Ray.getAlltValues());
                sphereScene.add(polyScene);
                sphereTvals.add(polyTvals);
                //System.out.println("Sphere tval size = " + sphereTvals.get(0).size());
                //System.out.println(sphereTvals.get(0).size());
            	//System.out.println("Poly tval size= " + Ray.getAlltValues().size());
            	//System.out.println(Ray.getAlltValues().size());
				oPPM.writePPM(combineScene(sphereScene,sphereTvals));
			}
		
			} else if(sphereModel) {
			for (List<Double> sphere : sphereSources) {
				 List<Vector3D> sphMod = new ArrayList<Vector3D>();
                List<Double> sphT = new ArrayList<Double>();
                //System.out.println("Sphere info " + sphere.get(0));
                Ray.clearSphereOutput();
                Ray.clearSpheretValues();
                for (int j = 0; j < res.get(1); j++) {
                    for (int i = 0; i < res.get(0); i++) {
                        // Throw a Ray from focal point to pixel.
                        pixelRays(i, j, res, bounds, d, EV, WV, UV, VV, raycompute, sphere);
                    }
                }

                int j = 0;
                int i = 0;
                sphMod.addAll(Ray.getSphereOutput());
                sphT.addAll(Ray.getSphereT());
                count++;
                sphereScene.add(sphMod);
                sphereTvals.add(sphT);
                // sphMod.clear();
            }
            if (sphereScene.size() > 1) {
    			 oPPM.writePPM(combineScene(sphereScene,sphereTvals));
    		} else {
    			oPPM.writePPM(sphereScene.get(0));
    		}

        } else {
        	for (int j = 0; j < res.get(1); j++) {
                for (int i = 0; i < res.get(0); i++) {
                    // Throw a Ray from focal point to pixel.
                    pixelRays(i, j, res, bounds, d, EV, WV, UV, VV, raycompute, emptySphere);
                }
            }
 
        	//System.out.println("TOTAL SIZE OF PIXELS TO WRITE =  " + Ray.getPolyOuptut().size());	
            
        	//System.out.println("Sphere tval size = " + sphereTvals.get(0).size());
        	//System.out.println("Poly tval size= " + Ray.getAlltValues().size());
        	oPPM.writePPM(Ray.getPolyOuptut());
        }
        
	}

	private static List<Vector3D> combineSpherePoly(List<List<Vector3D>> spheresOutput, List<List<Double>> spheresTvalues,
			List<Vector3D> polysOutput, List<Double> polyTvalues) {
		Vector3D zeroVec = new Vector3D(0, 0, 0);
		int x = 0;
		int y = 0;
        List<Vector3D> sphereOut = new ArrayList<Vector3D>();
        for (int j = 0; j < spheresOutput.get(0).size(); j++) {
            double min = spheresTvalues.get(0).get(j);
            x = 0;
            y = j;
            //System.out.println("The Minimum for this column is: " + min);
            for (int i = 1; i < spheresOutput.size(); i++) {
                //System.out.println("Check to see if " + spheresTvalues.get(i).get(j) + " is zero" );
                if (spheresTvalues.get(i).get(j) != Double.MAX_VALUE) {
                    //System.out.println(spheresTvalues.get(i).get(j)+" is not a zero");
                    //System.out.println("Comparing: " + spheresTvalues.get(i).get(j)+" To the min: "+ min);
                    if (spheresTvalues.get(i).get(j) < min) {
                        //System.out.println(spheresTvalues.get(i).get(j) + " is smaller than the min");
                        min = spheresTvalues.get(i).get(j);
                        //System.out.println("the new min is : " + min);
                        x = i;
                        y = j;
                        //System.out.println("The rgb value associated with this t value: " + spheresOutput.get(i).get(j).toString());
                    }
                } else {

                }
            }
            if (min == Double.MAX_VALUE) {
                System.out.println("adding " + zeroVec.toString());
                sphereOut.add(zeroVec);
            } else {
                sphereOut.add(spheresOutput.get(x).get(y));
                System.out.println("adding " + spheresOutput.get(x).get(y).toString());
            }

        }
        System.out.println(sphereOut.size());
        return sphereOut;
	}

	private static List<Vector3D> combineScene(List<List<Vector3D>> spheresOutput, List<List<Double>> spheresTvalues) {
		Vector3D zeroVec = new Vector3D(0, 0, 0);
		int x = 0;
		int y = 0;
        List<Vector3D> sphereOut = new ArrayList<Vector3D>();
        for (int j = 0; j < spheresOutput.get(0).size(); j++) {
            double min = spheresTvalues.get(0).get(j);
            x = 0;
            y = j;
            //System.out.println("The Minimum for this column is: " + min);
            for (int i = 1; i < spheresOutput.size(); i++) {
                //System.out.println("Check to see if " + spheresTvalues.get(i).get(j) + " is zero" );
                if (spheresTvalues.get(i).get(j) != Double.MAX_VALUE) {
                    //System.out.println(spheresTvalues.get(i).get(j)+" is not a zero");
                    //System.out.println("Comparing: " + spheresTvalues.get(i).get(j)+" To the min: "+ min);
                    if (spheresTvalues.get(i).get(j) < min) {
                        //System.out.println(spheresTvalues.get(i).get(j) + " is smaller than the min");
                        min = spheresTvalues.get(i).get(j);
                        //System.out.println("the new min is : " + min);
                        x = i;
                        y = j;
                        //System.out.println("The rgb value associated with this t value: " + spheresOutput.get(i).get(j).toString());
                    }
                } else {

                }
            }
            if (min == Double.MAX_VALUE) {
                //System.out.println("adding " + zeroVec.toString());
                sphereOut.add(zeroVec);
            } else {
                sphereOut.add(spheresOutput.get(x).get(y));
                //System.out.println("adding " + spheresOutput.get(x).get(y).toString());
            }

        }
        return sphereOut;
    }

	private static void pixelRays(int i, int j, List<Integer> res, List<Double> bounds, int d, Vector3D EV, Vector3D WV,
			Vector3D UV, Vector3D VV, Ray raycompute, List<Double> sphere) {
		// System.out.println(i + "/ (" + res.get(0)+ " - 1" + ") * " + "(" +
		// bounds.get(2) + " - " + bounds.get(0) + ") + " +bounds.get(0));

		double px = i / (double) (res.get(0) - 1) * (bounds.get(2) - bounds.get(0)) + bounds.get(0);
		double py = j / (double) (res.get(1) - 1) * (bounds.get(3) - bounds.get(1)) + bounds.get(1);

		// System.out.println(px + " " + py);

		// Near * WV
		Vector3D nWV = WV.scalarMultiply(-d);
		// System.out.println("***" + WV.toString());
		// System.out.println(d);
		// System.out.println("Near * WV = " + nWV.toString());

		// px * UV
		Vector3D pxUV = UV.scalarMultiply(px);
		// System.out.println("***" + UV.toString());
		// System.out.println(px);
		// System.out.println("px * UV = " + pxUV.toString());

		// py * VV
		Vector3D pyVV = VV.scalarMultiply(py * -1);
		// System.out.println("***" + VV.toString());
		// System.out.println(py);
		// System.out.println("py * VV = " + pyVV.toString());

		Vector3D pixpt = EV.add(nWV);
		// System.out.println("EV + (near * VW) = " + pixpt.toString());

		pixpt = pixpt.add(pxUV);
		// System.out.println("EV + (near * VW) + (px * UV) = " +
		// pixpt.toString());

		// At this spot, I have the every point in the image plane mapped to a
		// 3d vector called pixpt
		pixpt = pixpt.add(pyVV);
		// System.out.println("EV + (near * VW) + (px * UV) + (py * VV) = " +
		// pixpt.toString());

		// ADD the pixel point to array list
		// pixelPoints.add(pixpt);
		// System.out.println("Point on the Image plane --> " + pixpt);

		// Calculate the pixel ray
		Vector3D ray = pixpt.subtract(EV);
		// System.out.println("Ray = pixpt - EV = " + ray.toString());
		ray = ray.normalize();
		// System.out.println("Normalized Ray = " + ray.toString());
		
		// if(polyModel)Ray.rayPolygonIntersection(ray,pixpt);
		if (!sphere.isEmpty()) {
			Ray.raySphereIntersection(ray, pixpt, sphere);
		} else {
			Ray.rayPolygonIntersection(ray, pixpt,model.get(0));
		}

	}
}
