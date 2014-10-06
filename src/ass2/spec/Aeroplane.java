package ass2.spec;

import javax.media.opengl.GL2;

public class Aeroplane 
{
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
	
	private void drawFuselage(GL2 gl)
	{
		float[] ambAndDiffMat = {0.0f, 1.0f, 0.0f, 1.0f};
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, ambAndDiffMat, 0);
		
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			// Back Face
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
			gl.glVertex3d(0, 0, 0);
			
		}
		gl.glEnd();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	public void draw(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated(-(fuselageSideLength / 2), 0, 0);
		gl.glScaled(myScale, myScale, myScale);
		drawFuselage(gl);
		gl.glPopMatrix();
	}
	
	
}
