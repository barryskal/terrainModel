package ass2.spec;

import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {
	
	public static double TREE_HEIGHT = 0.25;
	public static double TREE_RADIUS = 0.05;
	public static double LEAVES_RADIUS = 0.25;
	private static int NUMBER_OF_TRUNK_STRIPS = 16;
	List<Polygon> trunkMesh;

    private Point myPos;
    
    public Tree(double x, double y, double z) {
        myPos = new Point(x, y, z);
        generateTrunkMesh();
    }
    
    public double[] getPosition() {
        return myPos.getPointAsDoubleArray();
    }
    
    public double[] getCentreOfLeaves()
    {
    	double leavesPosition[] = myPos.getPointAsDoubleArray();
    	leavesPosition[1] += TREE_HEIGHT + LEAVES_RADIUS;
    	return leavesPosition;
    }
    
    public void draw(GL2 gl, MyTexture trunkTexture, MyTexture leafTexture)
    {
    	gl.glPushMatrix();
    	
    	gl.glTranslated(myPos.x, myPos.y, myPos.z);
    	gl.glBindTexture(GL2.GL_TEXTURE_2D, trunkTexture.getTextureId());
    	drawTrunk(gl);
    	gl.glBindTexture(GL2.GL_TEXTURE_2D, leafTexture.getTextureId());
    	drawLeaves(gl);
    	gl.glPopMatrix();
    }
    
    private void drawLeaves(GL2 gl)
    {
    	
    	gl.glTranslated(0, TREE_HEIGHT + LEAVES_RADIUS, 0);
		
		GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricTexture(quadric, true);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluSphere(quadric, Tree.LEAVES_RADIUS, 32, 32);
    }
    
    private void drawTrunk(GL2 gl)
    {
    	
    	
    	double texturePosition = 0;
		double textureIncrement = (double) 1 / NUMBER_OF_TRUNK_STRIPS;
    	gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			for (Polygon polygon : trunkMesh)
			{
					Point[] pointList = polygon.getPoints();
					Point point = pointList[0];
					plotPoint(gl, point, new double[] {texturePosition, 0});
					
					point = pointList[1];
					plotPoint(gl, point, new double[] {texturePosition + textureIncrement, 0});
					
					point = pointList[2];
					plotPoint(gl, point, new double[] {texturePosition + textureIncrement, 1});
					
					point = pointList[3];
					plotPoint(gl, point, new double[] {texturePosition, 1});
					
					texturePosition += textureIncrement;
			}
		}
		gl.glEnd();
    	
    }

	private void generateTrunkMesh() {
		Polygon trunkProfile = getTrunkProfile();
    	trunkMesh = trunkProfile.extrudedPolygonMesh(new double[] {0, 1, 0}, TREE_HEIGHT);
    	
    	/* 
    	 * We don't need the top or bottom faces of the trunk, just
    	 * remove them
    	 */
    	
    	trunkMesh.remove(0);
    	trunkMesh.remove(0);
	}

	private void plotPoint(GL2 gl, Point point, double[] texturePoint) {
		gl.glNormal3dv(getNormalOfPoint(point), 0);
		gl.glTexCoord2dv(texturePoint, 0);
		gl.glVertex3dv(point.getPointAsDoubleArray(), 0);
	}

	private double[] getNormalOfPoint(Point point) 
	{
		double[] normal = 
			{
				point.x,
				0,
				point.z
			};
		return normal;
	}
    
    private Polygon getTrunkProfile()
    {
		double theta = 0;
		double angleIncrement = 2 * Math.PI / NUMBER_OF_TRUNK_STRIPS;
		Point[] trunkPoints = new Point[NUMBER_OF_TRUNK_STRIPS];
		for (int i = 0; i < NUMBER_OF_TRUNK_STRIPS; i++)
		{
			trunkPoints[i] = new Point(Tree.TREE_RADIUS * Math.cos(theta + angleIncrement), 0, Tree.TREE_RADIUS * Math.sin(theta + angleIncrement));
			theta += angleIncrement;
		}
		
		return new Polygon(trunkPoints);
    }
    
    
    

}
