package ass2.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Polygon 
{

	private Point[] myPoints;
	private double[] myNormal;
	
	public Polygon(Point[] points)
	{
		myPoints = points;
		computeNormal();
	}
	
	public double[] getNormal()
	{
		return myNormal;
	}
	
	public Point[] getPoints()
	{
		return myPoints;
	}
	
	private void computeNormal() 
	{
        double[] n = new double[3];

        int size = myPoints.length;
        for (int i = 0; i < size; i++) {
            Point p0 = myPoints[i];
            Point p1 = myPoints[(i + 1) % size];

            n[0] += (p0.y - p1.y) * (p0.z + p1.z);
            n[1] += (p0.z - p1.z) * (p0.x + p1.x);
            n[2] += (p0.x - p1.x) * (p0.y + p1.y);
        }

        myNormal = n;
    }
	
	public List<Polygon> extrudedPolygonMesh(double[] normalVector, double thickness)
	{
		ArrayList<Polygon> polygonList = new ArrayList<Polygon>();
		// Add this polygon as the front face
		polygonList.add(this);
		
		int size = myPoints.length;
		Point[] backPoints = new Point[size];
		// Create the back face points
		for (int i = 0; i < size; i++)
			backPoints[i] = new Point(
					myPoints[i].x + normalVector[0] * thickness, 
					myPoints[i].y + normalVector[1] * thickness, 
					myPoints[i].z + normalVector[2] * thickness);
		
		/*
		 * Note that the array of back points are drawn in the 
		 * same order as the front face. This means that the normal 
		 * will point in the same direction as the front face. 
		 * That is not what we want, so we take a copy of the 
		 * back points array and reverse it.  
		 */
		
		Point[] reversedBackPoints = Arrays.copyOf(backPoints, size);
		reverseArray(reversedBackPoints);
		
		//reverseArray(backPoints);
		/*for (Point point : backPoints)
			System.out.printf("%.2f, %.2f, %.2f%n", point.x, point.y, point.z);
		System.out.println("----");
		for (Point point : backPoints)
			System.out.printf("%.2f, %.2f, %.2f%n", point.x, point.y, point.z);*/
		polygonList.add(new Polygon(reversedBackPoints));
		
		// Create QUAD side elements representing the thickness
		
		for (int i = 0; i < size; i++)
		{
			Point[] newSurfaceQuad = new Point[4];
			
			newSurfaceQuad[0] = myPoints[i];
			newSurfaceQuad[1] = myPoints[(i + 1) % size]; //backPoints[3 - i];
			newSurfaceQuad[2] = backPoints[(i + 1) % size]; //backPoints[(6 - i) % size];
			newSurfaceQuad[3] = backPoints[i];//myPoints[(i + 1) % size];
			
			polygonList.add(new Polygon(newSurfaceQuad));
		}
		
		return polygonList;
		
		
	}
	
	private void reverseArray(Object[] array)
	{
		Object temp;
		int endIndex = array.length - 1;
		for (int i = 0; i < (array.length / 2); i++)
		{
			temp = array[i];
			array[i] = array[endIndex - i];
			array[endIndex - i] = temp;
		}
		
	}
}
