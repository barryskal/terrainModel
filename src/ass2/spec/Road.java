package ass2.spec;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private static final double PIECE_HEIGHT = 0.01;
	private List<Double> myPoints;
    private double myWidth;
    public static String TEXTURE_FILE = "roadTexture.jpg";
    public static String TEXTURE_EXTENSION = "jpg";
    private static final double EPSILON = 0.001;
    private static final int NUM_STRIPS = 10;
    
    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int) Math.floor(t);
        t = t - i;
        
        i *= 6;
        
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        
        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;        
        
        return p;
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }
    
    /**
     * Generates a point along the spine of the road at position t
     * and at the correct altitude if the road is projected on to
     * a given terrain
     * @param t		The point along the spine at which the point is 
     * generated. Can be a value between 0 and size()
     * @param terrain	The terrain map on to which the road is being
     * projected. 
     * @return	A 3 element double array representing the x, y and z 
     * coordinates of the generated point. 
     */
    public double[] getPointOnTerrain(double t, Terrain terrain)
    {
    	double[] roadPoint = point(t);
    	double[] point = 
    		{
    			roadPoint[0],
    			terrain.altitude(roadPoint[0], roadPoint[1]),
    			roadPoint[1]
    		};
    	return point;
    }
    
    /**
     * Calculates the normal to the surface at the given point. 
     * @param t		The point at which you want the normal vector.
     * @param terrain	The terrain describing the surface being used
     * to generate the normal vector
     * @return	A double array containing the normal vector.
     */
    private double[] getNormalVectorAtPoint(double t, Terrain terrain)
    {
    	
    	double[] tangentVector = getTangentVector(t, PIECE_HEIGHT, terrain);
    	double[] point = getPointOnTerrain(t, terrain);
    	
    	return MathUtil.crossProduct(terrain.getNormalAtPoint(point[0], point[2]), tangentVector);
    }
    
    /**
     * Determine the tangent at the point given by t. 
     * This is done using the value given in offset to find the points at 
     * (t - offset) and (t + offset) which are used to calculate the tangent. 
     * The points on the curve are projected on to the terrain in order to 
     * take the road curvature effects in to account.
     * @param t		The point along the spine at which you want the tangent
     * @param offset	The offset to the point before and the point after.
     * @param terrain	The terrain on to which the spine is being projected.
     * @return		A double array containing the tangent vector.
     */
    private double[] getTangentVector(double t, double offset, Terrain terrain)
    {
    	double[] pointBefore;
    	if (t == 0)
    		pointBefore = getPointOnTerrain(t, terrain);
    	else
    		pointBefore = getPointOnTerrain(t - offset, terrain);
    	
    	
    	double[] pointAfter;
    	if ((t - size()) < EPSILON)
    		pointAfter = getPointOnTerrain(t, terrain);
    	else 
    		pointAfter = getPointOnTerrain(t + offset, terrain);
    	
    	double[] tangentVector = 
    		{
    			pointAfter[0] - pointBefore[0],
    			pointAfter[1] - pointBefore[1],
    			pointAfter[2] - pointBefore[2]
    		};
    	
    	return tangentVector;
    }
    		
    
    
    
    /**
     * Draws the road as a series of QUAD elements as shown below
     * 		0    3 4   7
     * 		 ----  ----
     * 		|	 ||	   |
     * 		|	 ||	   |   ----> Road Direction
     * 		 ----  ----
     * 		1    2 5   6
     * 
     * The width of the quads (point 0-1) is the width provided in the json file.
     * The height of the quads (point 1-2) is defined by the constant PIECE_HEIGHT
     * Note that the height may actually change due to the curvature of 
     * the road. To account for this, we determine the normal vector
     * to the spine at the point along the road we are currently 
     * examining. 
     * 
     * Also note that each of the quad elements shown above are split in 
     * to a smaller number of equal size quads, given by the constant,
     * NUM_STRIPS. 
     * @param gl	The GL2 object being used for this scene
     */
    public void draw(GL2 gl, Terrain terrain)
    {
    	int numQuads = (int) (size() / PIECE_HEIGHT) - 1;
    	double quadStripLength = myWidth / NUM_STRIPS;
    	double textureStripLength = 1 / NUM_STRIPS;
    	gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			for (double quadNum = 0; quadNum < numQuads; quadNum++)
			{
				
				double t = quadNum * PIECE_HEIGHT;
				double[] currentPoint = getPointOnTerrain(t, terrain);
				double[] normalAtCurrentPoint = getNormalVectorAtPoint(t, terrain);
				
				double[] nextPoint = getPointOnTerrain(t + PIECE_HEIGHT, terrain);
				double[] normalAtNextPoint = getNormalVectorAtPoint(t + PIECE_HEIGHT, terrain);
				
				double positionOfTopPoints = myWidth / 2;
				double positionOfBottomPoints = positionOfTopPoints - quadStripLength;
				double topTexturePoint = 1;
				double bottomTexturePoint = topTexturePoint - textureStripLength;
				
				for (int stripNum = 0; stripNum < NUM_STRIPS; stripNum++)
				{
					double[] point0 = 
						{
							currentPoint[0] + normalAtCurrentPoint[0] * positionOfTopPoints,
							0,
							currentPoint[2] + normalAtCurrentPoint[2] * positionOfTopPoints
						};
					
					point0[1] = terrain.altitude(point0[0], point0[2]);
					double[] normalAtPoint0 = terrain.getNormalAtPoint(point0[0], point0[2]);
					
					// Offset the point from the surface a little bit so it is easily visible
					offsetPointFromSurface(point0, normalAtPoint0);
					
					gl.glNormal3dv(normalAtPoint0, 0);
					gl.glTexCoord2d(0.0, topTexturePoint);
					gl.glVertex3dv(point0, 0);
					
					double[] point1 = 
						{
							currentPoint[0] + normalAtCurrentPoint[0] * positionOfBottomPoints,
							0,
							currentPoint[2] + normalAtCurrentPoint[2] * positionOfBottomPoints
						};
					
					point1[1] = terrain.altitude(point1[0], point1[2]);
					double[] normalAtPoint1 = terrain.getNormalAtPoint(point1[0], point1[2]);
					
					offsetPointFromSurface(point1, normalAtPoint1); 
					
					gl.glNormal3dv(normalAtPoint1, 0);
					gl.glTexCoord2d(0.0, bottomTexturePoint);
					gl.glVertex3dv(point1, 0);
					
					
					double[] point2 = 
						{
							nextPoint[0] + normalAtNextPoint[0] * positionOfBottomPoints,
							0,
							nextPoint[2] + normalAtNextPoint[2] * positionOfBottomPoints
						};
					
					point2[1] = terrain.altitude(point2[0], point2[2]);
					double[] normalAtPoint2 = terrain.getNormalAtPoint(point2[0], point2[2]);
					
					offsetPointFromSurface(point2, normalAtPoint2);
					
					gl.glNormal3dv(normalAtPoint2, 0);
					gl.glTexCoord2d(1.0, bottomTexturePoint);
					gl.glVertex3dv(point2, 0);
					double[] point3 = 
						{
							nextPoint[0] + normalAtNextPoint[0] * positionOfTopPoints,
							0,
							nextPoint[2] + normalAtNextPoint[2] * positionOfTopPoints
						};
					
					point3[1] = terrain.altitude(point3[0], point3[2]);
					
					double[] normalAtPoint3 = terrain.getNormalAtPoint(point3[0], point3[2]);
					offsetPointFromSurface(point3, normalAtPoint3);
					
					gl.glNormal3dv(normalAtPoint3, 0);
					gl.glTexCoord2d(1.0, topTexturePoint);
					gl.glVertex3dv(point3, 0);
				
					positionOfTopPoints = positionOfBottomPoints;
					positionOfBottomPoints -= quadStripLength;
					
					topTexturePoint = bottomTexturePoint;
					bottomTexturePoint -= textureStripLength;
					
				}
				
			}
		}
		gl.glEnd();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    /**
     * This offsets the point a slight amount from the surface using 
     * the given normal vector.
     * @param point		The point you want to offset
     * @param normalAtPoint	The normal to use to offset
     */
	private void offsetPointFromSurface(double[] point, double[] normalAtPoint) 
	{
		double offsetAmount = 0.01;
		
		for (int i = 0; i < 3; i++) {
			point[i] = point[i] + normalAtPoint[i] * offsetAmount;
		}
	}
}
