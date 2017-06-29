// cs410p3 Assignment
// Author: Torry Brelsford
// Date:   Nov 26, 2016
// Class:  CS160
// Email:  torrybr@rams.colostate.edu

package com.example.cs410;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class SphereRayIntersection {

	private static List<Vector3D> sphereIntersection = new ArrayList<Vector3D>();
	private static Vector3D CV;
	private static double r;
	private static List<Double> ambientLight = new ArrayList<Double>();
	private static List<List<Double>> lightSources = new ArrayList<List<Double>>();

	SphereRayIntersection(List<Double> sphere) {
		// Sphere Radius
		r = sphere.get(3);

		// Sphere Center
		CV = new Vector3D(sphere.get(0), sphere.get(1), sphere.get(2));

	}

	public static void rayIntersectionRGB(Vector3D ray, List<Double> sphere) {
		Vector3D ptos = rayIntersection(ray, ray);
		if (ptos != null) {
			Vector3D snrm = ptos.subtract(CV);
			snrm = snrm.normalize();
		}
	}

	private static Vector3D rayIntersection(Vector3D LV, Vector3D UV) {
		Vector3D TV = CV.subtract(LV);

		double v = TV.dotProduct(UV);
		double csq = TV.dotProduct(TV);
		double disc = Math.pow(r, 2.0) - (csq - Math.pow(csq, 2.0));
		double d = 0;
		double t = 0;
		Vector3D pt;

		if (disc > 0) {
			d = Math.sqrt(disc);
			t = v - d;
			pt = UV.scalarMultiply(t);
			pt = LV.add(UV);
			return pt;

		} else {
			pt = new Vector3D(0, 0, 0);
			return null;

		}

	}

	public List<Vector3D> getSphereIntersections() {
		return sphereIntersection;
	}
}
