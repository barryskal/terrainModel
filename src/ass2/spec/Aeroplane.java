package ass2.spec;

import java.util.List;

import javax.media.opengl.GL2;

public class Aeroplane 
{
	private static final double WING_THICKNESS = 0.01;
	private double myScale;
	private double myRotation;
	private boolean inFlight;
	private static double fuselageSideLength = 0.25;
	private static double fuselageLength = 0.5;
	 
	
	public Aeroplane(double scale)
	{
		myScale = scale;
		inFlight = false;
		myRotation = 0;
	}
	
	private List<Polygon> getFuselageMesh()
	{
		Point[] fuselageProfile = 
			{
				new Point(0, 0, 0),
				new Point(fuselageSideLength, 0, 0),
				new Point(fuselageSideLength, fuselageSideLength, 0),
				new Point(0, fuselageSideLength, 0)
			};
		Polygon fuselagePolygon = new Polygon(fuselageProfile);
		double[] extrusionVector = {0, 0, 1};
		List<Polygon> fuselageMesh = fuselagePolygon.extrudedPolygonMesh(extrusionVector, fuselageLength);
		return fuselageMesh;
	}
	
	private void drawFuselage(GL2 gl)
	{
		float[] ambAndDiffMat = {0.0f, 1.0f, 0.0f, 1.0f};
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, ambAndDiffMat, 0);
		
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			for (Polygon polygon : getFuselageMesh())
			{
				gl.glNormal3dv(polygon.getNormal(), 0);
				for (Point point : polygon.getPoints())
					gl.glVertex3dv(point.getPointAsDoubleArray(), 0);
			}
			
			/*// Back Face
			gl.glNormal3d(0, 0, -1);
			gl.glVertex3d(0, 0, 0);
			gl.glVertex3d(fuselageSideLength, 0, 0);
			gl.glVertex3d(fuselageSideLength, fuselageSideLength, 0);
			gl.glVertex3d(0, fuselageSideLength, 0);
			
			// Front Face
			gl.glNormal3d(0, 0, 1);
			gl.glVertex3d(0, 0, fuselageLength);
			gl.glVertex3d(fuselageSideLength, 0, fuselageLength);
			gl.glVertex3d(fuselageSideLength, fuselageSideLength, fuselageLength);
			gl.glVertex3d(0, fuselageSideLength, fuselageLength);
			
			// Bottom
			gl.glNormal3d(0, -1, 0);
			gl.glVertex3d(0, 0, 0);
			gl.glVertex3d(fuselageSideLength, 0, 0);
			gl.glVertex3d(fuselageSideLength, 0, fuselageLength);
			gl.glVertex3d(0, 0, fuselageLength);
			
			// Right
			gl.glNormal3d(1, 0, 0);
			gl.glVertex3d(fuselageSideLength, 0, 0);
			gl.glVertex3d(fuselageSideLength, 0, fuselageLength);
			gl.glVertex3d(fuselageSideLength, fuselageSideLength, fuselageLength);
			gl.glVertex3d(fuselageSideLength, fuselageSideLength, 0);
			
			// Top
			gl.glNormal3d(0, 1, 0);
			gl.glVertex3d(fuselageSideLength, fuselageSideLength, 0);
			gl.glVertex3d(fuselageSideLength, fuselageSideLength, fuselageLength);
			gl.glVertex3d(0, fuselageSideLength, fuselageLength);
			gl.glVertex3d(0, fuselageSideLength, 0);
			
			// Left
			gl.glNormal3d(-1, 0, 0);
			gl.glVertex3d(0, fuselageSideLength, 0);
			gl.glVertex3d(0, fuselageSideLength, fuselageLength);
			gl.glVertex3d(0, 0, fuselageLength);
			gl.glVertex3d(0, 0, 0);*/
			
		}
		gl.glEnd();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	private void drawWings(GL2 gl)
	{
		List<Polygon> wingMesh = getWingMesh();
		
		gl.glPushMatrix();
		gl.glTranslated(fuselageSideLength, (fuselageSideLength / 2), (fuselageLength / 4));
		drawWing(gl, wingMesh);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(0, (fuselageSideLength / 2) + WING_THICKNESS, (fuselageLength / 4));
		gl.glRotated(-180, 0, 0, 1);
		drawWing(gl, wingMesh);
		gl.glPopMatrix();
	}

	private List<Polygon> getWingMesh() {
		Point[] wingProfile = 
			{
				new Point(0, 0, 0),
				new Point(0.25, 0, 0),
				new Point(0.25, 0, 0.1),
				new Point(0, 0, 0.25),
			};
		Polygon wingPolygon = new Polygon(wingProfile);
		double[] array = wingPolygon.getNormal();
		int i = 0;
		//System.out.printf("| %.2f  %.2f  %.2f |%n", array[i], array[i + 1], array[i + 2]);
		double[] extrusionVector = {0, 1, 0};
		List<Polygon> wingMesh = wingPolygon.extrudedPolygonMesh(extrusionVector, WING_THICKNESS);
		return wingMesh;
	}
	
	private void drawTail(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated((fuselageSideLength / 2) + WING_THICKNESS/2, fuselageSideLength, 0);
		gl.glRotated(90, 0, 0, 1);
		drawWing(gl, getWingMesh());
		gl.glPopMatrix();
	}
	
	private void drawWing(GL2 gl, List<Polygon> polygonMesh)
	{
		float[] ambAndDiffMat = {0.0f, 0.0f, 1.0f, 1.0f};
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, ambAndDiffMat, 0);
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			for (Polygon polygon : polygonMesh)
			{
				gl.glNormal3dv(polygon.getNormal(), 0);
				for (Point point : polygon.getPoints())
					gl.glVertex3dv(point.getPointAsDoubleArray(), 0);
			}
			
		}
		gl.glEnd();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public void draw(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated(-(fuselageSideLength / 2), 0.1, 0);
		gl.glScaled(myScale, myScale, myScale);
		drawFuselage(gl);
		drawWings(gl);
		drawTail(gl);
		gl.glPopMatrix();
	}
	
	
	
	
	
}
