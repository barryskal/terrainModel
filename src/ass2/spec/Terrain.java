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
     * TO BE COMPLETED
     * 
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) 
    {
        
    	if (isInvalidLocation(x, z))
    		return 0;
    	
    	int leftX = (int) Math.floor(x);
    	int rightX = (int) Math.ceil(x);
    	int nearZ = (int) Math.floor(z);
    	int farZ = (int) Math.ceil(z);
    	double bottomY, topY;
    	
    	if (leftX == rightX)
    	{
    		bottomY = getGridAltitude(leftX, nearZ);
    		topY = getGridAltitude(leftX, farZ);
    	}
    	else
    	{
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
    	}
        
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
		
		return normalList;
		
	}
	
	
	public double[] getNormalAtPoint(double x, double z)
	{
		double y = altitude(x, z);
		double x0 = Math.floor(x);
		double z0 = Math.floor(z);
		double z1 = Math.ceil(z);
		
		if (z0 == (mySize.getHeight() - 1))
		{
			z1 = z0 - 1; 
		}
		else if (z0 == z1)
		{
			z1 = z0 + 1;
		}
		
		double[] vector1 = 
			{
				x0 - x,
				myAltitude[(int) x0][(int) z0] - y,
				z0 - z
			};
		
		if (MathUtil.magnitude(vector1) == 0)
		{
			vector1[0] = -1;
			vector1[1] = myAltitude[(int) x0][(int) z0] - myAltitude[(int) (x0 + 1)][(int) z0];
			vector1[2] = 0;
		}
		
		
		double[] vector2 =
			{
				x0 - x,
				myAltitude[(int) x0][(int) z1] - y,
				z1 - z
			};
		
		System.out.printf("Vector 1: %.2f, %.2f, %.2f%n", vector1[0], vector1[1], vector1[2]);
		System.out.printf("Vector 2: %.2f, %.2f, %.2f%n", vector2[0], vector2[1], vector2[2]);
		double[] normalVector = MathUtil.crossProduct(vector1, vector2);
		return normalVector;
		
	}
	
	
	public double[] getStartingTranslation()
	{
		double[] translation = new double[3];
		double halfwayAcross = (mySize.getWidth() - 1) / 2;
		translation[0] = -1 * halfwayAcross;
		translation[1] = -1 * altitude(halfwayAcross, 0) - 0.5;
		translation[2] = -STARTING_Z_DISTANCE;//-1 * (mySize.getHeight());
		
		return translation;
	}
	

}
