

import org.apache.commons.math3.analysis.solvers.SecantSolver;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torry on 11/26/2016.
 */
public class Ray {

    private static List<Vector3D> vertices = new ArrayList<Vector3D>();
    private static List<Vector3D> faces = new ArrayList<Vector3D>();
    private static List<Double> tValuesIntersect = new ArrayList<Double>();
    private static List<Double> alltValues = new ArrayList<Double>();
    private static List<Double> dValuesIntersect = new ArrayList<Double>();
    private static List<Double> alldValues = new ArrayList<Double>();
    private static List<List<Double>> lightSources = new ArrayList<List<Double>>();
    private static List<Double> ambientLight = new ArrayList<Double>();
    private static Vector3D ambientVec;
    private static Vector3D kVec;
    private static List<Double> sphereTvalues = new ArrayList<Double>();
    private static ArrayList<Vector3D> spherePts = new ArrayList<Vector3D>();
    private static Vector3D sphereRGB;
    private static List<Double> sphereTval = new ArrayList<Double>();;
    private static List<Vector3D> sphereOutput = new ArrayList<Vector3D>();
    private static List<Vector3D> polyOutput = new ArrayList<Vector3D>();
    private static List<Vector3D> vn = new ArrayList<Vector3D>();
    private static List<Vector3D> faceNormals = new ArrayList<Vector3D>();
    private static int count = 0;
    private static Vector3D kd;
    private static Vector3D ka;
    private static Vector3D ks;

    Ray(List<Vector3D> myVertices, List<Vector3D> myFaces, List<List<Double>> myLightSources, List<Double> myAmbientLight, List<Double> myKd, List<Double> myKa,List<Double> myKs, List<Vector3D> myVn,List<Vector3D> myFaceNormals) {
        vertices = myVertices;
        faces = myFaces;
        lightSources = myLightSources;
        ambientLight = myAmbientLight;
        ambientVec = new Vector3D(ambientLight.get(0),ambientLight.get(1),ambientLight.get(2));
        if(!myKd.isEmpty()) {
        	kd = new Vector3D(myKd.get(0),myKd.get(1),myKd.get(2));
        	 ka = new Vector3D(myKa.get(0),myKa.get(1),myKa.get(2));
             ks = new Vector3D(myKs.get(0),myKs.get(1),myKs.get(2));
             vn = myVn;
             faceNormals.addAll(myFaceNormals);
        }
        
       
       
    }

    public static void rayPolygonIntersection(Vector3D ray, Vector3D pixpt, List<Double> model) {
        //System.out.println("HERE");
        //Set up the matrix's
    	Vector3D Cv = new Vector3D(model.get(0),model.get(1),model.get(2));
    	Vector3D zeroVec = new Vector3D(0,0,0);
        RealMatrix m = new Array2DRowRealMatrix(3, 3);
        RealMatrix m1 = new Array2DRowRealMatrix(3, 3);
        RealMatrix m2 = new Array2DRowRealMatrix(3, 3);
        RealMatrix m3 = new Array2DRowRealMatrix(3, 3);
        double min = Double.MAX_VALUE;
        int faceNum = 0;
        //System.out.println(faces.size());
        for (int i = 0; i < faces.size(); i++) {
            Vector3D av = vertices.get((int) (faces.get(i).getX()));
            //System.out.println("A Vector -->" + av.toString());
            Vector3D bv = vertices.get((int) (faces.get(i).getY()));
            //System.out.println("B Vector -->" + bv.toString());
            Vector3D cv = vertices.get((int) (faces.get(i).getZ()));
           // System.out.println("C Vector -->" + cv.toString());

        	/*
            Vector3D a = new Vector3D(faces.get(i).getX(), 0, 0);
            System.out.println("A Vector -->" + a.toString());
            Vector3D b = new Vector3D(0, faces.get(i).getY(), 0);
            System.out.println("B Vector -->" + b.toString());
            Vector3D c = new Vector3D(0, 0, faces.get(i).getZ());
            System.out.println("C Vector -->" + c.toString());
			*/
            //M MATRIX SETUP
            //[ax-bx ax-cx dx]
            double[] mr1 = {av.getX() - bv.getX(), av.getX() - cv.getX(), ray.getX()};

            //[ay-by ay-cy dy]
            double[] mr2 = {av.getY() - bv.getY(), av.getY() - cv.getY(), ray.getY()};

            //[az-bz az-cz dz]
            double[] mr3 = {av.getZ() - bv.getZ(), av.getZ() - cv.getZ(), ray.getZ()};

            m.setRow(0, mr1);
            m.setRow(1, mr2);
            m.setRow(2, mr3);

            //System.out.println(m.toString());

            //M1 MATRIX SETUP
            // ax-lx ax-cx dx
            double[] m1r1 = {av.getX() - pixpt.getX(), av.getX() - cv.getX(), ray.getX()};

            // ay-ly ay-cy dy
            double[] m1r2 = {av.getY() - pixpt.getY(), av.getY() - cv.getY(), ray.getY()};

            // az-lz az-cz dz
            double[] m1r3 = {av.getZ() - pixpt.getZ(), av.getZ() - cv.getZ(), ray.getZ()};

            //SET THE MATRIX M1
            m1.setRow(0, m1r1);
            m1.setRow(1, m1r2);
            m1.setRow(2, m1r3);

            //M2 MATRIX SETUP
            // ax-bx ax-lx dx
            double[] m2r1 = {av.getX() - bv.getX(), av.getX() - pixpt.getX(), ray.getX()};

            //ay-by ay-ly dy
            double[] m2r2 = {av.getY() - bv.getY(), av.getY() - pixpt.getY(), ray.getY()};

            //az-bz az-lz dz
            double[] m2r3 = {av.getZ() - bv.getZ(), av.getZ() - pixpt.getZ(), ray.getZ()};

            //SET THE M2 MATRIX
            m2.setRow(0, m2r1);
            m2.setRow(1, m2r2);
            m2.setRow(2, m2r3);


            //M3 MATRIX SETUP
            //ax-bx ax-cx ax-lx
            double[] m3r1 = {av.getX() - bv.getX(), av.getX() - cv.getX(), av.getX() - pixpt.getX()};

            //ay-by ay-cy ay-ly
            double[] m3r2 = {av.getY() - bv.getY(), av.getY() - cv.getY(), av.getY() - pixpt.getY()};

            //az-bz az-cz az-lz
            double[] m3r3 = {av.getZ() - bv.getZ(), av.getZ() - cv.getZ(), av.getZ() - pixpt.getZ()};

            //SET THE MATRIX M3
            m3.setRow(0, m3r1);
            m3.setRow(1, m3r2);
            m3.setRow(2, m3r3);

            //CALCULATE PARAMETERS
            double t = calcT(m, m3);


            //System.out.println("HERE");

			if (t > 0) {
				double g = calcGamma(m, m2);
				double b1 = calcBeta(m, m1);
				if (g >= 0 && b1 >= 0) {
					if (g + b1 <= 1) {
						// Every valid t value for one ray
						// only keep the smallest

						if (t < min) {
							min = t;
							faceNum = i;
							//System.out.println(t);
						}
					}

				} else {

				}

			}

			// faceNum++;
			// System.out.println(faceNum);

		}

		if (min != Double.MAX_VALUE) {
			tValuesIntersect.add(min);
			Vector3D pt = ray.scalarMultiply(min).add(pixpt);
			// System.out.println(pt.toString());
			
			// System.out.println("Point "+pt2.toString());
			// System.out.println("Face is: " + faces.get(i));
			// System.out.println("Associated VN is: " +
			// vn.get((int)faceNormals.get(i).getX()));
			rayPolygonIntersectionRGB(pt, pixpt, ka, ks, kd, vn.get((int) faceNormals.get(faceNum).getX()));
			
		} else {
			//System.out.println(zeroVec.toString());
			polyOutput.add(zeroVec);
			
		}
		
		alltValues.add(min);

	}

	/*****************
	 * PARAMETER CALCULATION
	 *************************************/
	// change this to return parameter
	public static double calcT(RealMatrix m, RealMatrix m3) {
		// List<Double> tValues = new ArrayList<Double>();
		double t = 0;

		LUDecomposition mdet = new LUDecomposition(m);
		LUDecomposition m3det = new LUDecomposition(m3);
		t = m3det.getDeterminant() / mdet.getDeterminant();

		// System.out.println("M3 Matrix is ->" + m3.toString()+" M3 Determinant
		// is .. " + m3det.getDeterminant());
		// System.out.println("T VALUE IS : " + t);
		// System.out.println("M Matrix is- > " + m.toString()+" M Determinant
		// is .. " + mdet.getDeterminant());
		// alltValues.add(t);
		return t;
	}

    public static double calcGamma(RealMatrix m, RealMatrix m2) {
        double gamma = 0;

        LUDecomposition mdet = new LUDecomposition(m);
        LUDecomposition m2det = new LUDecomposition(m2);
        gamma = m2det.getDeterminant() / mdet.getDeterminant();
        //System.out.println("M2 Matrix is ->" + m2.toString()+" M2 Determinant is .. " + m2det.getDeterminant());
        //System.out.println("gamma VALUE IS : " + gamma);

        return gamma;
    }

    public static double calcBeta(RealMatrix m, RealMatrix m1) {
        double beta = 0;

        LUDecomposition mdet = new LUDecomposition(m);
        LUDecomposition m1det = new LUDecomposition(m1);
        beta = m1det.getDeterminant() / mdet.getDeterminant();
        //System.out.println("beta VALUE IS : " + mdet.getDeterminant());


        //System.out.println("M1 Matrix is ->" + m1.toString()+" M1 Determinant is .. " + m1det.getDeterminant());

        return beta;
    }

    public static void raySphereIntersection(Vector3D ray, Vector3D pixpt, List<Double> sphere) {
        //Ray = UV
        //pixpt = LV
        //System.out.println("THE SIZE OF sphereOutput is.. " + sphereOutput.size());
        double r = sphere.get(3);

        Vector3D Cv = new Vector3D(sphere.get(0), sphere.get(1), sphere.get(2));

        //System.out.println("CV = " + Cv.toString());
        //System.out.println("UV = " + ray.toString());
        //System.out.println("LV = " + pixpt.toString());

        Vector3D Tv = Cv.subtract(pixpt);
        //System.out.println("Vector Tv = " + Tv.toString());

        double v = Tv.dotProduct(ray);
        //System.out.println("v = " + v);

        double csq = Tv.dotProduct(Tv);
        //System.out.println("csq = " + csq);
        double disc = Math.pow(r, 2.0) - (csq - Math.pow(v, 2.0));
        //System.out.println("disc = " + disc);
        if (disc < 0) {
            Vector3D background = new Vector3D(0,0,0);
            sphereOutput.add(background);
            sphereTval.add(Double.MAX_VALUE);
        } else {
            double d = Math.sqrt(disc);
            //System.out.println("d = " + d);
            double t = v - d;
            sphereTval.add(t);
            //System.out.println("t = " + t);
            Vector3D pt = ray.scalarMultiply(t).add(pixpt);
            //System.out.println("pt = " + pt.toString());

            Vector3D sphereRGB = new Vector3D(sphere.get(4),sphere.get(5),sphere.get(6));

            /**
             * For every point of intersection with a sphere, calculate its RGB VALUE
             */
            raySphereIntersectionRGB(pt,Cv,pixpt,sphereRGB);
        }


    }
    private static void raySphereIntersectionRGB(Vector3D pt, Vector3D Cv, Vector3D pixpt, Vector3D sphereRGB) {
        // snrm = ptos - sph['c']
        Vector3D snrm = pt.subtract(Cv);
        //System.out.println("snrm = " + snrm);


        //snrm = snrm / snrm.norm()
        snrm = snrm.normalize();
        Vector3D color = pairwiseProduct(ambientVec,sphereRGB);
        //System.out.println("Color = " + color.toString());

        for (List<Double> lts : lightSources) {
            //Location of light in x y z world coordinate

            Vector3D ptL = new Vector3D(lts.get(0),lts.get(1),lts.get(2));

            //R G B Value of light
            Vector3D emL = new Vector3D(lts.get(4),lts.get(5),lts.get(6));
            //System.out.println("emL = " + emL.toString());


            Vector3D toL = ptL.subtract(pt);
            //System.out.println("toL = " + toL.toString());

            toL = toL.normalize();

            if(snrm.dotProduct(toL) > 0.0) {
                count++;
                //System.out.println("snrm dot tol = " + snrm.dotProduct(toL));
                color = color.add(pairwiseProduct(sphereRGB,emL).scalarMultiply(snrm.dotProduct(toL)));
                //System.out.println("Color2 = " + color.toString());

                Vector3D toC = pixpt.subtract(pt);
                //System.out.println("toC = " + toC );
                toC = toC.normalize();
                //System.out.println("toC = " + toC.toString() );
                Vector3D spR = snrm.scalarMultiply(2*snrm.dotProduct(toL)).subtract(toL);
                //System.out.println("SPR = " +spR.toString());
                color = color.add(pairwiseProduct(sphereRGB,emL).scalarMultiply(Math.pow(toC.dotProduct(spR),16)));

                //System.out.println("FINAL COLOR IS : " + color.toString());

                //
                //System.out.println("COLOR IS : " + color.toString());

            } else {

            }


        }
        sphereOutput.add(color.scalarMultiply(255));

        //System.out.println("COLOR IS : " + color.toString());

    }
    public static void rayPolygonIntersectionRGB(Vector3D pt,Vector3D  pixpt, Vector3D ka,Vector3D ks, Vector3D kd, Vector3D snrm) {
        // snrm = ptos - sph['c']
        //System.out.println(snrm.toString());
        //System.out.println("snrm = " + snrm);
        //snrm = snrm / snrm.norm()
        //snrm = snrm.normalize();
        Vector3D color = pairwiseProduct(ambientVec,ka);
        //System.out.println("Color = " + color.toString());

        for (List<Double> lts : lightSources) {
            //Location of light in x y z world coordinate

            Vector3D ptL = new Vector3D(lts.get(0),lts.get(1),lts.get(2));

            //R G B Value of light
            Vector3D emL = new Vector3D(lts.get(4),lts.get(5),lts.get(6));
            //System.out.println("emL = " + emL.toString());


            Vector3D toL = ptL.subtract(pt);
            //System.out.println("toL = " + toL.toString());

            toL = toL.normalize();
           
            if(snrm.dotProduct(toL) > 0.0) {
                count++;
                //System.out.println("snrm dot tol = " + snrm.dotProduct(toL));
                color = color.add(pairwiseProduct(kd,emL).scalarMultiply(snrm.dotProduct(toL)));
                //System.out.println("Color2 = " + color.toString());

                Vector3D toC = pixpt.subtract(pt);
                //System.out.println("toC = " + toC );
                toC = toC.normalize();
                //System.out.println("toC = " + toC.toString() );
                Vector3D spR = snrm.scalarMultiply(2*snrm.dotProduct(toL)).subtract(toL);
                //System.out.println("SPR = " +spR.toString());
                color = color.add(pairwiseProduct(ks,emL).scalarMultiply(Math.pow(toC.dotProduct(spR),16)));
                //System.out.println("FINAL COLOR IS : " + color.toString());

                //
                //System.out.println("COLOR IS : " + color.toString());

            } else {

            }


        }
        polyOutput.add(color.scalarMultiply(255));
        //System.out.println(color.toString());

    }


    private static Vector3D pairwiseProduct(Vector3D v1 , Vector3D v2) {
        Vector3D product = new Vector3D(v1.getX()*v2.getX(),v1.getY()*v2.getY(),v1.getZ()*v2.getZ());
        return product;
    }

    public static List<Double> getAlltValues() {
        return alltValues;
    }
    public static List<Double> getPolyTintersections() {
    	return tValuesIntersect;
    }

    public static List<Double> gettValuesIntersect() {
        return tValuesIntersect;
    }

    public static List<Vector3D> getFaces() {
        return faces;
    }

    public static List<Vector3D> getVertices() {
        return vertices;
    }
    public static List<Vector3D> getSphereOutput() {
        return sphereOutput;
    }
    public static void clearSphereOutput() {
        sphereOutput.clear();
    }

	public static List<Double> getSphereT() {
		return sphereTval;
	}

	public static void clearSpheretValues() {
		sphereTval.clear();

	}

	public static List<Vector3D> getPolyOuptut() {
		return polyOutput;
	}

	// public static Vector3D calcRGB(Vector3D pt,Vector3D,pixpt,Vector3D
	// ka,Vector3D ks, Vector3D kd,Vector3D snrm) {
	// return void rayPolygonIntersectionRGB1(Vector3D pt,Vector3D pixpt,
	// Vector3D ka,Vector3D ks, Vector3D kd, Vector3D snrm) {
	// }

}
