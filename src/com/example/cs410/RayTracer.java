package com.example.cs410;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by Torry on 10/24/2016. PA2 CS 410 Torrybr@rams.colostate.edu
 */
class RayTracer {
	private static List<Vector3D> vertices = new ArrayList<>();
	private static List<Vector3D> faces = new ArrayList<>();
	private static final List<Double> polyTvals = new ArrayList<>();
	private static List<List<Double>> sphereSources = new ArrayList<>();
	private static final List<List<Vector3D>> sphereScene = new ArrayList<>();
	private static final List<Vector3D> polyScene = new ArrayList<>();
	private static final List<List<Double>> sphereTvals = new ArrayList<>();
	private static List<List<Double>> lightSources = new ArrayList<>();
	private static List<Double> ambientLight = new ArrayList<>();
	private static final List<Double> emptySphere = new ArrayList<>();
	private static List<List<Double>> model = new ArrayList<>();
	private static boolean polyModel = FALSE;
	private static boolean sphereModel = FALSE;
	static final List<Double> kd = new ArrayList<>();
	static final List<Double> ka = new ArrayList<>();
	static final List<Double> ks = new ArrayList<>();
	static final List<Vector3D> vn = new ArrayList<>();
	static final List<Vector3D> faceNormals = new ArrayList<>();

	

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("You need a scene, and output filename");
		} else {
			long start = System.currentTimeMillis();
			// Parse Camera Model in ParseCameraModelClass
			Path camPath = Paths.get(args[0]);
			new ParseCameraModel(camPath);

			//new ParseObj();

			if (!ParseCameraModel.getModelPathnames().isEmpty()) {
				ParseObj.setTranslate(ParseCameraModel.getModelSources().get(0));
			}

			for (String filename : ParseCameraModel.getModelPathnames()) {
				Path objPath = Paths.get(filename);
				ParseObj.parseObjFile(objPath);

				polyModel = TRUE;
				vertices = ParseObj.getVertices();
				faces = ParseObj.getFaces();
				model = ParseCameraModel.getModelSources();
				ka.addAll(ParseObj.getKa());
				kd.addAll(ParseObj.getKd());
				ks.addAll(ParseObj.getKs());
				vn.addAll(ParseObj.getVn());
				faceNormals.addAll(ParseObj.getFaceNormals());

			}

			if (!ParseCameraModel.getSphereSources().isEmpty()) {
				sphereModel = TRUE;
				sphereSources = ParseCameraModel.getSphereSources();

			}

			if (!ParseCameraModel.getAmbient().isEmpty()) {
				ambientLight = ParseCameraModel.getAmbient();
			}

			if (!ParseCameraModel.getLightSources().isEmpty()) {
				lightSources = ParseCameraModel.getLightSources();
			}

			// Parse PLY file
			// String filename = new File(args[1]).getName();
			// Path path = Paths.get(args[1]);
			// parsePLY(path, filename);

			// Output File name
			String outputFilename = new File(args[1]).getName();
			OutputPPM oPPM = new OutputPPM(outputFilename, ParseCameraModel.getRes());

			// Build camera system origin and axes in world coordinates and
			// compute ray's
			setCamCoordinates(ParseCameraModel.getEye(), ParseCameraModel.getLook(), ParseCameraModel.getUp(), ParseCameraModel.getD(), ParseCameraModel.getBounds(), ParseCameraModel.getRes(),
					oPPM);
			// colorEncodePixel(oPPM);
			long end = System.currentTimeMillis();

			NumberFormat formatter = new DecimalFormat("#0.00000");
			System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
		}
	}

	private static void setCamCoordinates(List<Double> eye, List<Double> look, List<Double> up, int d,
			List<Double> bounds, List<Integer> res, OutputPPM oPPM) {
		Vector3D EV = new Vector3D(eye.get(0), eye.get(1), eye.get(2));
		Vector3D LV = new Vector3D(look.get(0), look.get(1), look.get(2));
		Vector3D UP = new Vector3D(up.get(0), up.get(1), up.get(2));
		Vector3D WV;
		Vector3D UV;
		Vector3D VV;
		new ArrayList<Vector3D>();

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

		Ray raycompute = new Ray(vertices, faces, lightSources, ambientLight, sphereSources);

		// The 3d vector points containing all the locations of the pixels on
		// the image plane
		// List<Vector3D> pixelRays = new ArrayList<Vector3D>();
		// Verify this is correct way to traverse
		if (sphereModel && polyModel) {
			if (sphereModel) {
					mainRayThrow(eye,look,up,d,bounds,res,oPPM,EV,WV,UV,VV,raycompute);
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
				// System.out.println("Sphere tval size = " +
				// sphereTvals.get(0).size());
				// System.out.println(sphereTvals.get(0).size());
				// System.out.println("Poly tval size= " +
				// Ray.getAlltValues().size());
				// System.out.println(Ray.getAlltValues().size());
				OutputPPM.writePPM(combineScene());
			}

		} else if (sphereModel) {
			mainRayThrow(eye,look,up,d,bounds,res,oPPM,EV,WV,UV,VV,raycompute);
			if (sphereScene.size() > 1) {
				//System.out.println("RayTracer: sphereScene.size = " +sphereScene.size() + " values "+  sphereScene.toString());
				//System.out.println("RayTracer: sphereTVals.size = "+ sphereTvals.size()+ " values "+ sphereTvals.toString());
				OutputPPM.writePPM(combineScene());
			} else {
				OutputPPM.writePPM(sphereScene.get(0));
			}

		} else {
			for (int j = 0; j < res.get(1); j++) {
				for (int i = 0; i < res.get(0); i++) {
					// Throw a Ray from focal point to pixel.
					pixelRays(i, j, res, bounds, d, EV, WV, UV, VV, raycompute, emptySphere);
				}
			}

			// System.out.println("TOTAL SIZE OF PIXELS TO WRITE = " +
			// Ray.getPolyOuptut().size());

			// System.out.println("Sphere tval size = " +
			// sphereTvals.get(0).size());
			// System.out.println("Poly tval size= " +
			// Ray.getAlltValues().size());
			OutputPPM.writePPM(Ray.getPolyOuptut());
		}

	}

	private static void mainRayThrow(List<Double> eye, List<Double> look, List<Double> up, int d,
									 List<Double> bounds, List<Integer> res, OutputPPM oPPM, Vector3D EV, Vector3D WV, Vector3D UV, Vector3D VV, Ray raycompute) {
		for (List<Double> sphere : sphereSources) {
			List<Vector3D> sphMod = new ArrayList<>();
			List<Double> sphT = new ArrayList<>();
			// System.out.println("Sphere info " + sphere.get(0));
			Ray.clearSphereOutput();
			Ray.clearSpheretValues();
			for (int j = 0; j < res.get(1); j++) {
				for (int i = 0; i < res.get(0); i++) {
					// Throw a Ray from focal point to pixel.
					pixelRays(i, j, res, bounds, d, EV, WV, UV, VV, raycompute, sphere);
				}
			}

			sphMod.addAll(Ray.getSphereOutput());
			sphT.addAll(Ray.getSphereT());
			sphereScene.add(sphMod);
			sphereTvals.add(sphT);
		}
	}

	private static List<Vector3D> combineScene() {
		Vector3D zeroVec = new Vector3D(0, 0, 0);
		int x = 0;
		int y = 0;
		List<Vector3D> sphereOut = new ArrayList<>();
		for (int j = 0; j < RayTracer.sphereScene.get(0).size(); j++) {
			double min = RayTracer.sphereTvals.get(0).get(j);
			x = 0;
			y = j;
			 //System.out.println("The Minimum for this column is: " + min);
			for (int i = 1; i < RayTracer.sphereScene.size(); i++) {
				// System.out.println("Check to see if " + spheresTvalues.get(i).get(j) + " is zero" );
				if (RayTracer.sphereTvals.get(i).get(j) != Double.MAX_VALUE) {
					if (RayTracer.sphereTvals.get(i).get(j) < min) {
						// System.out.println(spheresTvalues.get(i).get(j) + " is smaller than the min");
						min = RayTracer.sphereTvals.get(i).get(j);
						// System.out.println("the new min is : " + min);
						x = i;
						y = j;
						// System.out.println("The rgb value associated with this t value: " + spheresOutput.get(i).get(j).toString());
					}
				} else {

				}
			}
			if (min == Double.MAX_VALUE) {
				// System.out.println("adding " + zeroVec.toString());
				sphereOut.add(zeroVec);
			} else {
				sphereOut.add(RayTracer.sphereScene.get(x).get(y));
				//System.out.println("adding " +
				// spheresOutput.get(x).get(y).toString());
			}

		}
		return sphereOut;
	}

	private static void pixelRays(int i, int j, List<Integer> res, List<Double> bounds, int d, Vector3D EV, Vector3D WV,
			Vector3D UV, Vector3D VV, Ray raycompute, List<Double> sphere) {
		// System.out.println(i + "/ (" + res.get(0)+ " - 1" + ") * " + "(" +
		// bounds.get(2) + " - " + bounds.get(0) + ") + " +bounds.get(0));

		Vector3D accum = new Vector3D(0,0,0);
    	Vector3D refatt = new Vector3D(1.0,1.0,1.0);
    	List <Double> sph = new ArrayList<>();
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
		
		//sph.addAll(Ray.bestSphere(ray, pixpt));
		
		//if(!sph.isEmpty()) {
		//	System.out.println("best sphere is TRUE for RAY -> " + ray.toString());
		//}
		// System.out.println("Normalized Ray = " + ray.toString());
		//if(!sphere.isEmpty() ) {	
		//		Ray.raySphereIntersection(ray, pixpt, sphere,0,accum,refatt);	
		//}
		
		// if(polyModel)Ray.rayPolygonIntersection(ray,pixpt);
		if (!sphere.isEmpty()) {
			//System.out.println("RayTracer: Origin ray " + ray.toString());
			Ray.raySphereIntersection(ray, pixpt, sphere,0,accum,refatt);
		} else {
			Ray.rayPolygonIntersection(ray, pixpt, model.get(0));
		}
		//System.out.println("Calculating intersection points for the Sphere");

	}
}
