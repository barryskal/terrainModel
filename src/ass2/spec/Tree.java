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
	private String treeTrunkTexture = "treeTrunkTexture.jpg";
	private String treeTrunkTextureExt = "jpg";
	private String treeLeafTexture = "treeLeavesTexture.jpg";
	private String treeLeafTextureExt = "jpg";
	private MyTexture[] myTextures;

    private Point myPos;
    
    public Tree(double x, double y, double z) {
        myPos = new Point(x, y, z);
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
    
    public void draw(GL2 gl)
    {
    	gl.glPushMatrix();
    	
    	gl.glTranslated(myPos.x, myPos.y, myPos.z);
    	drawTrunk(gl);
    	drawLeaves(gl);
    	gl.glPopMatrix();
    }
    
    private void drawLeaves(GL2 gl)
    {
    	
    	gl.glTranslated(0, TREE_HEIGHT + LEAVES_RADIUS, 0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[1].getTextureId());
		
		GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricTexture(quadric, true);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluSphere(quadric, Tree.LEAVES_RADIUS, 32, 32);
    }
    
    private void drawTrunk(GL2 gl)
    {
    	Polygon trunkProfile = getTrunkProfile();
    	List<Polygon> trunkMesh = trunkProfile.extrudedPolygonMesh(new double[] {0, 1, 0}, TREE_HEIGHT);
    	
    	/* 
    	 * We don't need the top or bottom faces of the trunk, just
    	 * remove them
    	 */
    	
    	trunkMesh.remove(0);
    	trunkMesh.remove(0);
    	
    	double texturePosition = 0;
		double textureIncrement = (double) 1 / NUMBER_OF_TRUNK_STRIPS;
    	gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[1].getTextureId());
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
					
			}
		}
		gl.glEnd();
    	
    }

	private void plotPoint(GL2 gl, Point point, double[] texturePoint) {
		gl.glNormal3dv(getNormalOfPoint(point), 0);
		gl.glTexCoord2dv(texturePoint, 0);
		gl.glVertex3dv(point.getPointAsDoubleArray(), 0);
	}

	private double[] getNormalOfPoint(Point point) {
		double[] normal = 
			{
				point.x,
				0,
				point.z
			};
		return normal;
	}
    
    
    private void initialiseTextures(GL2 gl)
    {
    	myTextures = new MyTexture[2];
    	myTextures[0] = new MyTexture(gl, treeTrunkTexture, treeTrunkTextureExt);
    	myTextures[1] = new MyTexture(gl, treeLeafTexture, treeLeafTextureExt);
    }
    
    
    private Polygon getTrunkProfile()
    {
		double theta = 0;
		double angleIncrement = 2 * Math.PI / NUMBER_OF_TRUNK_STRIPS;
		Point[] trunkPoints = new Point[NUMBER_OF_TRUNK_STRIPS];
		for (int i = 0; i < NUMBER_OF_TRUNK_STRIPS; i++)
			trunkPoints[i] = new Point(Tree.TREE_RADIUS * Math.cos(theta + angleIncrement), 0, Tree.TREE_RADIUS * Math.sin(theta + angleIncrement));
		
		return new Polygon(trunkPoints);
    }
    
    
    

}
