package ass2.spec;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    public static final int STARTING_Z_DISTANCE = 2;
	private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;
    private double[] myNormalList;
    private double[] myVertexList;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) 
    {
    	
    	/*
		 * As a triangular mesh is used for the terrain, we can't just use 
	     * straight interpolation between the adjacent grid points. 
	     * We have to interpolate using only the triangle containing the point
	     * in question. 
	     * 
	     * We do this by isolating the grid "square" containing the point
	     * and calculating the angle to the point from the upper left 
	     * hand corner of that square. If the angle is <= 45 then 
	     * we are in the "bottom" triangle.
    	 */
        
    	if (isInvalidLocation(x, z))
    		return 0;
    	
    	int leftX = (int) Math.floor(x);
    	int rightX = (int) Math.ceil(x);
    	int nearZ = (int) Math.floor(z);
    	int farZ = (int) Math.ceil(z);
    	double bottomY, topY, leftY, rightY, lerpFactor;
    	
    	// Look for the easy cases first
    	if (leftX == rightX && nearZ == farZ)
    	{
    		return getGridAltitude(rightX, farZ);
    	}
    	else if (leftX == rightX)
    	{
    		bottomY = getGridAltitude(leftX, nearZ);
    		topY = getGridAltitude(leftX, farZ);
    		lerpFactor = z - nearZ;
    		return bottomY + lerpFactor * (topY - bottomY);
    	}
    	else if (nearZ == farZ)
    	{
    		leftY = getGridAltitude(leftX, farZ);
    		rightY = getGridAltitude(rightX, farZ);
    		lerpFactor = x - leftX;
    		return leftY + lerpFactor * (rightY - leftY);
    	}
    	else
    	{
    		/*
    		 * Find the angle from the top left hand corner to the point
    		 */
    		double angle = Math.atan((x - leftX) / (z - nearZ));
    		double triangleLine = Math.PI / 4;
    		
    		if (angle <= triangleLine)
    		{
    			/*
    			 * Bottom triangle
    			 * 
    			 * Interpolate the altitude on the left hand edge of the square 
    			 * then interpolate on the hypotenuse of the triangle
    			 */
    			double topLeftY = getGridAltitude(leftX, nearZ);
    			double bottomLeftY = getGridAltitude(leftX, farZ);
    			lerpFactor = z - nearZ;
    			leftY = topLeftY + lerpFactor * (bottomLeftY - topLeftY);
    			
    			// Calculate the length along the hypotenuse
    			double r = lerpFactor / Math.cos(triangleLine);
    			
    			// Interpolate the altitude along the hypotenuse
    			double bottomRightY = getGridAltitude(rightX, farZ);
    			rightY = topLeftY + (r / Math.sqrt(2)) * (bottomRightY - topLeftY);
    			
    			/*
    			 * Interpolate between the point on the left edge and the point
    			 * on the hypotenuse.
    			 */
    			
    			double xOnHypotenuse = r * Math.sin(triangleLine) + leftX;
    			double t = (x - leftX) / (xOnHypotenuse - leftX);
    			
    			return leftY + t * (rightY - leftY); 
    		}
    		else
    		{
    			// Top triangle
    			double topRightY = getGridAltitude(rightX, nearZ);
    			double bottomRightY = getGridAltitude(rightX, farZ);
    			lerpFactor = z - nearZ;
    			rightY = topRightY + lerpFactor * (bottomRightY - topRightY);
    			
    			double r = lerpFactor / Math.sin(triangleLine);
    			
    			double topLeftY = getGridAltitude(leftX, nearZ);
    			leftY = topLeftY + (r / Math.sqrt(2)) * (bottomRightY - topLeftY);
    			
    			double xOnHypotenuse = r * Math.cos(triangleLine) + leftX;
    			double t = (x - xOnHypotenuse) / (rightX - xOnHypotenuse);
    			
    			return leftY + t * (rightY - leftY);
    		}
    	}
    		
    		/*
    		double lerpFactor = x - leftX;
    		double altAtLeftX = getGridAltitude(leftX, nearZ);
    		double altAtRightX = getGridAltitude(rightX, nearZ);
    		bottomY = altAtLeftX + lerpFactor * (altAtRightX - altAtLeftX); 
    		
    		altAtLeftX = getGridAltitude(leftX, farZ);
    		altAtRightX = getGridAltitude(rightX, farZ);
    		topY = altAtLeftX + lerpFactor * (altAtRightX - altAtLeftX);
    	}
    	
    	if ((nearZ == farZ) || (bottomY == topY))
    	{
    		return bottomY;
    	}
    	else
    	{
    		double lerpFactor = z - nearZ;
    		double altAtZ = bottomY + lerpFactor*(topY - bottomY);
    		return altAtZ;
    	}*/
        
    }
    
    private boolean isInvalidLocation(double x, double z)
    {
    	return (x < 0) || (z < 0) || (x > mySize.getWidth() - 1) || (z > mySize.getHeight() - 1); 
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param x
     * @param z
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }
    
    private int getNumOfVertices()
    {
    	return (int) (mySize.getWidth() * mySize.getHeight());
    }
    
    /**
	 * Generate a list of vertices from the terrain data. The numbering scheme 
	 * will be, number the first row 1 to n, then the next row from n + 1
	 * @return
	 */
	public double[] getVertexList()
	{
		double vertexList[] = new double[getNumOfVertices() * 3];
		int vertexCounter = 0;
		for (int row = 0; row < mySize.height; row++)
		{
			for (int col = 0; col < mySize.width; col++)
			{
				vertexList[vertexCounter++] = col;
				vertexList[vertexCounter++] = getGridAltitude(col, row);
				vertexList[vertexCounter++] = row;
			}
		}
		
		myVertexList = vertexList;
		return vertexList;
	}
	
	
	/**
	 * This generates an array representing the indices of the triangle 
	 * elements making up the terrain. 
	 * 
	 * It works on the following principle:
	 * The terrain is made up of squares that are split in to two
	 * triangles as shown 
	 * 
	 * 				1		2
	 * 				.-------.
	 * 				|		|
	 * 				.-------.
	 * 				3		4
	 * 
	 * The triangles will be represented by
	 * 1->3->4
	 * and
	 * 1->4->2
	 * You then move to the right to the next square (i.e. top
	 * left coordinate will be 2 instead of 1) and do the process again.
	 * If you end up at the edge of the grid (i.e. index 2 in this example) 
	 * you move to the next row
	 */
	public int[] getTriIndexList()
	{
		int width = (int) mySize.getWidth();
		int height = (int) mySize.getHeight();
		double numberOfTris = ((width - 1) * (height - 1)) * 2;
		int[] triIndexList = new int[(int) (numberOfTris * 3)];
		int indexCounter = 0;
		
		/*
		 * Note that the loop stops at the 2nd last row as that 
		 * is the last row of triangles that we will create. 
		 */
		for (int i = 0; i < (getNumOfVertices() - width); i++)
		{
			// Make sure that we are not at the edge of the grid
			if ((i % (width)) == (width - 1))
				continue;
			// First triangle
			triIndexList[indexCounter++] = i;
			triIndexList[indexCounter++] = i + width;
			triIndexList[indexCounter++] = i + width + 1;
			// Second Triangle
			triIndexList[indexCounter++] = i;
			triIndexList[indexCounter++] = i + width + 1;
			triIndexList[indexCounter++] = i + 1;
		}
		
		return triIndexList;
	}
	
	public double[] getNormalList(int[] triIndexList, double[] vertexList)
	{
		double[] normalList = new double[triIndexList.length];
		int numberOfTriElements = triIndexList.length / 3;
		int indexCounter = 0;
		
		for (int i = 0; i < numberOfTriElements; i++)
		{
			int startIndex = i * 3;
			double[] vector1 = new double[3];
			double[] vector2 = new double[3];

			int indexOfPoint1 = (int) triIndexList[startIndex];
			int indexOfPoint2 = (int) triIndexList[startIndex + 1];
			int indexOfPoint3 = (int) triIndexList[startIndex + 2];
			double point1[] = Arrays.copyOfRange(vertexList, (indexOfPoint1 * 3), (indexOfPoint1 * 3 + 3));
			double point2[] = Arrays.copyOfRange(vertexList, (indexOfPoint2 * 3), (indexOfPoint2 * 3 + 3));
			double point3[] = Arrays.copyOfRange(vertexList, (indexOfPoint3 * 3), (indexOfPoint3 * 3 + 3));
			//double point2[] = Arrays.copyOfRange(vertexList, (startIndex * 3), (startIndex * 3 + 3));
			//double point3[] = Arrays.copyOfRange(vertexList, (startIndex + 6), (startIndex + 9));
			
			vector1[0] = point2[0] - point1[0];
			vector1[1] = point2[1] - point1[1];
			vector1[2] = point2[2] - point1[2];
			
			vector2[0] = point3[0] - point1[0];
			vector2[1] = point3[1] - point1[1];
			vector2[2] = point3[2] - point1[2];
			
			double[] normal = MathUtil.crossProduct(vector1, vector2);
			for (int j = 0; j < 3; j++)
				normalList[indexCounter++] = normal[j];
			
		}
		
		myNormalList = normalList;
		
		return normalList;
		
	}
	
	
		
	private int getTriangleNum(double x, double z) throws SharedTriangleException
	{
		/* 
		 * Find out whether it belongs to the top triangle
		 * or the bottom triangle by measuring the distance 
		 * between points 2 and 3 to the point respectively. 
		 * If the vector from 2 to P has a lower distance, then
		 * the point is in the upper triangle.
		 * Vice-versa with a lower distance to point 3.
		 * You take the normal vector from whatever triangle 
		 * the point belongs to. 
		 * If the point is in between points 2 and 3. Then it
		 * must be extacly between the triangles. In this case, 
		 * take the average of the two normal vectors.
		 */
		
		int point2X = (int) Math.ceil(x);
		if (point2X == (int) x)
		{
			// Move right if on the left edge
			if (point2X == 0)
				point2X = 1;
		}
		
		int point2Z = (int) Math.floor(z);
		if (point2Z == (int) z)
		{
			// Stay away from the bottom edge
			if (point2Z == (mySize.getHeight() - 1))
				point2Z -= 1;
		}
		
		
		int point3X = point2X - 1;
		int point3Z = point2Z + 1;
		/*System.out.printf("point2 x: %d,  z: %d%n", point2X, point2Z);
		System.out.printf("point3 x: %d,  z: %d%n", point3X, point3Z);*/
		
		
		double[] vertex2P = 
			{
				x - point2X,
				z - point2Z
			};
		
		double[] vertex3P =
			{
				x - point3X,
				z - point3Z
			};
		
		double magnitude2P = MathUtil.magnitude(vertex2P);
		double magnitude3P = MathUtil.magnitude(vertex3P);
		
		int rowNumber = point2Z;
		int columnNumber = point3X;
		int triNum = rowNumber * (2 * ((int) mySize.getWidth() - 1)) + columnNumber * 2; 
		
		if (magnitude2P == magnitude3P)
			throw new SharedTriangleException(triNum);
		
		if (magnitude2P < magnitude3P)
			triNum += 1;
		
		return triNum;
	}
	
	
	
	private class SharedTriangleException extends Exception
	{
		private int myBottomTriIndex;
		
		public SharedTriangleException() {}
		
		public SharedTriangleException(int bottomTriIndex)
		{
			myBottomTriIndex = bottomTriIndex;
		}
		
		public int getBottomTriIndex()
		{
			return myBottomTriIndex;
		}
	}
	
	private double[] getNormalForTriangle(int indexNum)
	{
		return Arrays.copyOfRange(myNormalList, (indexNum * 3), (indexNum * 3 + 3));
	}
	
	
	
	public double[] getNormalAtPoint(double x, double z)
	{
		try 
		{
			int triangleIndex = getTriangleNum(x, z);
			//System.out.println("tri index: "+ triangleIndex);
			return getNormalForTriangle(triangleIndex);
		} 
		catch (SharedTriangleException e)
		{
			int bottomIndex = e.getBottomTriIndex();
			int topIndex = bottomIndex + 1;
			
			double[] bottomNormal = getNormalForTriangle(bottomIndex);
			double[] topNormal = getNormalForTriangle(topIndex);
			double[] averageNormalVector = 
				{
					(topNormal[0] + bottomNormal[0]) / 2,
					(topNormal[1] + bottomNormal[1]) / 2,
					(topNormal[2] + bottomNormal[2]) / 2,
				};
			
			return averageNormalVector;
		}
		
	}
	
	
	public double[] getStartingTranslation()
	{
		double[] translation = new double[3];
		double halfwayAcross = (mySize.getWidth() - 1) / 2;
		translation[0] = -1 * halfwayAcross;
		translation[1] = -1 * altitude(halfwayAcross, 0) - 0.5;
		translation[2] = -STARTING_Z_DISTANCE;
		
		return translation;
	}
	

}
