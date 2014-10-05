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

    private static final double PIECE_HEIGHT = 0.1;
	private List<Double> myPoints;
    private double myWidth;
    public static String TEXTURE_FILE = "roadTexture.jpg";
    public static String TEXTURE_EXTENSION = "jpg";
    
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
    			terrain.altitude(roadPoint[0], roadPoint[2]),
    			roadPoint[2]
    		};
    	return point;
    }
    
    public double[] getNormalVectorAtPoint(double t, Terrain terrain)
    {
    	double[] tangentVector = getTangentVector(t, PIECE_HEIGHT);
    	
    	return crossProduct(surfaceNormal, tangentVector);
    }
    
    private double[] getTangentVector(double t, double offset)
    {
    	double[] pointBefore;
    	if (t == 0)
    		pointBefore = point(t);
    	else
    		pointBefore = point(t - offset);
    	
    	double[] pointAfter = point(t + offset);
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
     * The width of the quads is the width provided in the json file.
     * The height of the quads is defined by the constant PIECE_HEIGHT
     * Note that the height may actually change due to the curvature of 
     * the road. To account for this, we determine the normal vector
     * to the spine at the point along the road we are currently 
     * examining. 
     * @param gl	The GL2 object being used for this scene
     */
    public void draw(GL2 gl, Terrain terrain)
    {
    	double[] tempSurfaceNormal = {0, 1, 0};
    	gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			for (double t = 0; t < size(); t += PIECE_HEIGHT)
			{
				double[] currentPoint = getPointOnTerrain(t, terrain);
				double[] normalAtCurrentPoint = getNormalVectorAtPoint(t, tempSurfaceNormal);
				double[] point0 = 
					{
						currentPoint[0] + normalAtCurrentPoint[0] * myWidth,
						currentPoint[1] + normalAtCurrentPoint[1] * myWidth,
						currentPoint[2] + normalAtCurrentPoint[2] * myWidth
					};
				
				gl.glNormal3dv(tempSurfaceNormal, 0);
				gl.glTexCoord2d(0.0, 1.0);
				gl.glVertex3dv(point0, 0);
				
				double[] point1 = 
					{
						currentPoint[0] - normalAtCurrentPoint[0] * myWidth,
						currentPoint[1] - normalAtCurrentPoint[1] * myWidth,
						currentPoint[2] - normalAtCurrentPoint[2] * myWidth
					};
				
				gl.glNormal3dv(tempSurfaceNormal, 0);
				gl.glTexCoord2d(0.0, 0.0);
				gl.glVertex3dv(point1, 0);
				
				double[] nextPoint = point(t + PIECE_HEIGHT);
				double[] normalAtNextPoint = getNormalVectorAtPoint(t + PIECE_HEIGHT, tempSurfaceNormal);
				double[] point2 = 
					{
						nextPoint[0] - normalAtNextPoint[0] * myWidth,
						nextPoint[1] - normalAtNextPoint[1] * myWidth,
						nextPoint[2] - normalAtNextPoint[2] * myWidth
					};
				
				gl.glNormal3dv(tempSurfaceNormal, 0);
				gl.glTexCoord2d(1.0, 0.0);
				gl.glVertex3dv(point2, 0);
				
				double[] point3 = 
					{
						nextPoint[0] + normalAtNextPoint[0] * myWidth,
						nextPoint[1] + normalAtNextPoint[1] * myWidth,
						nextPoint[2] + normalAtNextPoint[2] * myWidth
					};
				
				gl.glNormal3dv(tempSurfaceNormal, 0);
				gl.glTexCoord2d(1.0, 1.0);
				gl.glVertex3dv(point3, 0);
			}
		}
		gl.glEnd();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }
}
