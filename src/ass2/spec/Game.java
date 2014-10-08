package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;



/**
 * The Game class controls the creation and movement around 
 * the terrain
 *
 * @author Barry Skalrud
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    private static final double FIELD_OF_VIEW = 60.0;
	private static final double TREE_HEIGHT = 0.5;
	private Terrain myTerrain;
    private double[] myVertices;
    private double[] myNormals;
    private int[] myTriIndices;
    private double[] myTranslation;
    private double myRotation;
    private boolean nightMode = false;
    private static double MOVEMENT_AMOUNT = 0.1;
	private static double ROTATION_AMOUNT = 5;
	private Aeroplane myAvatar;
	
	// ---- TEXTURE DATA ----
	private final int NUM_TEXTURES = 4;
	private MyTexture[] myTextures;
	private String groundTexture = "groundTexture.jpg";
	private String groundTextureExt = "jpg";
	private String treeTrunkTexture = "treeTrunkTexture.jpg";
	private String treeTrunkTextureExt = "jpg";
	private String treeLeafTexture = "treeLeavesTexture.jpg";
	private String treeLeafTextureExt = "jpg";

    public Game(Terrain terrain) {
    	super("Assignment 2");
        myTerrain = terrain;
        myVertices = myTerrain.getVertexList();
        myTriIndices = myTerrain.getTriIndexList();
        myNormals = myTerrain.getNormalList(myTriIndices, myVertices);
        myTranslation = myTerrain.getStartingTranslation();
        myRotation = 0;
        myAvatar = new Aeroplane(0.5);
    }
    
    /** 
     * Run the game.
     *
     */
    public void run() {
    	  GLProfile glp = GLProfile.getDefault();
          GLCapabilities caps = new GLCapabilities(glp);
          GLJPanel panel = new GLJPanel();
          panel.addGLEventListener(this);
 
          panel.addKeyListener(this);;
          
          // Add an animator to call 'display' at 60fps        
          FPSAnimator animator = new FPSAnimator(60);
          animator.add(panel);
          animator.start();

          getContentPane().add(panel);
          setSize(800, 600);        
          setVisible(true);
          setDefaultCloseOperation(EXIT_ON_CLOSE);        
    }
    
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        //Terrain terrain = LevelIO.load(new File(args[0]));
    	String testFile = "test4.json";
    	Terrain terrain = LevelIO.load(new File(testFile));
        Game game = new Game(terrain);
        game.run();
    }

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        gl.glLoadIdentity();
        
        drawAvatar(gl);
        
        gl.glRotated(myRotation, 0, 1, 0);
        gl.glTranslated(myTranslation[0], myTranslation[1], myTranslation[2]);
        gl.glScaled(1, 1, -1);

        setLighting(gl);
        
        float matDiff[] = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matDiff, 0);
        
        drawGround(gl);
        drawTrees(gl);
        drawRoads(gl);
        
	}

	private void drawAvatar(GL2 gl)
	{
		gl.glPushMatrix();
		double bottomOfFrame = -1 * Math.sin(Math.toRadians(FIELD_OF_VIEW / 2));
		// Move the camera back so that we can see the back of the avatar
		gl.glScaled(1, 1, -1);
        gl.glTranslated(0, bottomOfFrame , 1);

		myAvatar.draw(gl);
		
		gl.glPopMatrix();
		
	}
	
	private void drawRoads(GL2 gl)
	{
		gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[3].getTextureId());
		for (Road road : myTerrain.roads())
			road.draw(gl, myTerrain);
	}
	
	private void drawGround(GL2 gl) {
		// Set ground texture
        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[0].getTextureId());
        
        
		int numberOfTriElements = myTriIndices.length / 3; // 3 vertices to a tri element
		//gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glBegin(GL2.GL_TRIANGLES);
		{
			for (int triCounter = 0; triCounter < numberOfTriElements; triCounter++)
			{
				int startIndex = triCounter * 3;
				gl.glNormal3dv(myNormals, startIndex);
				int indexOfPoint0 = myTriIndices[startIndex];
				int indexOfPoint1 = myTriIndices[startIndex + 1];
				int indexOfPoint2 = myTriIndices[startIndex + 2];
				gl.glTexCoord2d(0, 0.0);
				gl.glVertex3dv(myVertices, indexOfPoint0 * 3);
				gl.glTexCoord2d(0.5, 1.0);
				gl.glVertex3dv(myVertices, indexOfPoint1 * 3);
				gl.glTexCoord2d(1.0, 0.0);
				gl.glVertex3dv(myVertices, indexOfPoint2 * 3);
			}
			
		}
		gl.glEnd();
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	private void drawTrees(GL2 gl)
	{
		
		for (Tree tree : myTerrain.trees())
		{
			drawTree(gl, tree);
		}
		
	}
	
	private void drawTree(GL2 gl, Tree tree)
	{
		drawTrunk(gl, tree);
		gl.glPushMatrix();
		double[] leavesPosition = tree.getCentreOfLeaves();
		gl.glTranslated(leavesPosition[0], leavesPosition[1], leavesPosition[2]);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[2].getTextureId());
		drawLeaves();
		gl.glPopMatrix();
	}
	
	
	private void drawLeaves()
	{
		
		GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricTexture(quadric, true);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluSphere(quadric, Tree.LEAVES_RADIUS, 32, 32);
	}

	private void drawTrunk(GL2 gl, Tree tree) {
		// Draw the trunk as a hollow cylinder
		gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[1].getTextureId());
		int numberOfTrunkStrips = 16;
		double theta = 0;
		double angleIncrement = 2 * Math.PI / numberOfTrunkStrips;
		double texturePosition = 0;
		double textureIncrement = (double) 1 / numberOfTrunkStrips;
		double topOfTree = tree.getPosition()[1] + TREE_HEIGHT;
		double[] treeBase = tree.getPosition();
		
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
		gl.glBegin(GL2.GL_QUADS);
		{
			for (int i = 0; i < numberOfTrunkStrips; i++)
			{
				double[] treePoint1 = 
					{
						(treeBase[0] + Tree.TREE_RADIUS * Math.cos(theta + angleIncrement)), 
						treeBase[1], 
						(treeBase[2] + Tree.TREE_RADIUS * Math.sin(theta + angleIncrement))
					};
				
				/*
				 * Adjust the height altitude of the base using the just calculated
				 * x and z coordintaes.
				 */
				
				treePoint1[1] = myTerrain.altitude(treePoint1[0], treePoint1[2]);
				
				double[] treePoint2 = 
					{
						treePoint1[0],
						topOfTree,
						treePoint1[2]
					};
				
				
				double[] treeNormal1 = 
					{
						treePoint1[0] - treeBase[0],
						0,
						treePoint1[2] - treeBase[2]
					};
				
				gl.glNormal3dv(treeNormal1, 0);
				gl.glTexCoord2d((texturePosition + textureIncrement), 0.0);
				//gl.glTexCoord2d(1.0, 0.0);
				gl.glVertex3dv(treePoint1, 0);
				gl.glTexCoord2d((texturePosition + textureIncrement), 1.0);
				//gl.glTexCoord2d(1.0, 1.0);
				gl.glVertex3dv(treePoint2, 0);
				
				double[] treePoint3 = 
					{
						(treeBase[0] + Tree.TREE_RADIUS * Math.cos(theta)), 
						topOfTree, 
						(treeBase[2] + Tree.TREE_RADIUS * Math.sin(theta))
					};
				
				double[] treePoint4 = 
					{
						(treeBase[0] + Tree.TREE_RADIUS * Math.cos(theta)), 
						treeBase[1], 
						(treeBase[2] + Tree.TREE_RADIUS * Math.sin(theta))
					};
				
				/*
				 * Adjust the height altitude of the base using the just calculated
				 * x and z coordintaes.
				 */
				
				treePoint4[1] = myTerrain.altitude(treePoint4[0], treePoint4[2]);
				
				double[] treeNormal2 = 
					{
						treePoint4[0] - treeBase[0],
						0,
						treePoint4[2] - treeBase[2]
					};
				
				gl.glNormal3dv(treeNormal2, 0);
				gl.glTexCoord2d(texturePosition, 1.0);
				//gl.glTexCoord2d(0.0, 1.0);
				gl.glVertex3dv(treePoint3, 0);
				gl.glTexCoord2d(texturePosition, 0.0);
				//gl.glTexCoord2d(0.0, 0.0);
				gl.glVertex3dv(treePoint4, 0);
				
				theta += angleIncrement;
				texturePosition += textureIncrement;
			}
		}
		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	private void setLighting(GL2 gl)
	{
		// Light property vectors.
		float lightAmb[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		float lightDifAndSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		
		if (!nightMode)
		{
			gl.glDisable(GL2.GL_LIGHT1);
			float sunDir[] = myTerrain.getSunlight();
			float lightPos[] = { sunDir[0], sunDir[1], sunDir[2], 0.0f };
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb,0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec,0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec,0);

			
			
			// Light properties.
			
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos,0);
			
			gl.glEnable(GL2.GL_LIGHT0); // Enable particular light source.			
		}
		else
		{
			gl.glDisable(GL2.GL_LIGHT0);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmb,0);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDifAndSpec,0);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightDifAndSpec,0);
			
			float globAmb[] = { 0.4f, 0.4f, 0.4f, 1.0f };
			double[] forwardVector = getForwardVector();
			float[] lightDirection = {(float) forwardVector[0], 0, (float) forwardVector[1]};
			gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb,0);
			
			
			gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_CUTOFF, 22.5f);
			gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_EXPONENT, 4);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPOT_DIRECTION, lightDirection, 0);
			
			gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 2.0f);
			gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 1.0f);
			gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.5f);
			
			gl.glEnable(GL2.GL_LIGHT1);
			
		}
	}
	
	

	
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
    	
    	gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    	gl.glEnable(GL2.GL_DEPTH_TEST); // Enable depth testing.

    	// Normalise all normals
    	 gl.glEnable(GL2.GL_NORMALIZE);
    	
    	   // Turn on OpenGL lighting.
    	gl.glEnable(GL2.GL_LIGHTING); 
    	
    	// Turn on OpenGL texturing.
    	gl.glEnable(GL2.GL_TEXTURE_2D);
    	initialiseTextures(gl);
    	
    	
    	/*
    	 * The textures should be lit, so we will set the textures to modulate.
    	 */
    	gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE); 
    	
    	
    	
    			
	}

	private void initialiseTextures(GL2 gl) {
		myTextures = new MyTexture[NUM_TEXTURES];
    	myTextures[0] = new MyTexture(gl,groundTexture,groundTextureExt);
    	myTextures[1] = new MyTexture(gl, treeTrunkTexture, treeTrunkTextureExt);
    	myTextures[2] = new MyTexture(gl, treeLeafTexture, treeLeafTextureExt);
    	myTextures[3] = new MyTexture(gl, Road.TEXTURE_FILE, Road.TEXTURE_EXTENSION);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		 GL2 gl = drawable.getGL().getGL2();
         gl.glMatrixMode(GL2.GL_PROJECTION);
         gl.glLoadIdentity();
         
         GLU glu = new GLU();
         glu.gluPerspective(FIELD_OF_VIEW, (float)width/(float)height, 1.0, 50.0);
        
         gl.glMatrixMode(GL2.GL_MODELVIEW);
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				moveForward(MOVEMENT_AMOUNT);
				break;
	            
			case KeyEvent.VK_DOWN:
				moveBackward();
				break;
				
			case KeyEvent.VK_LEFT:
				rotate(ROTATION_AMOUNT * -1);
				System.out.println("rotation: " + myRotation);
				break;
				
			case KeyEvent.VK_RIGHT:
				rotate(ROTATION_AMOUNT);
				System.out.println("rotation: " + myRotation);
				break;
		
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_N:
				setNightMode();
				break;
		}
		
		
		
	}
	
	private void setNightMode()
	{
		if (nightMode)
			nightMode = false;
		else
			nightMode = true;
	}

	private void moveBackward() {
		
		double[] movementVector = getForwardVector();
		// Change the Z translation first
		double zTranslation = MOVEMENT_AMOUNT * movementVector[1];
		
		myTranslation[2] -= zTranslation;
		
		// Shift the X value to the left or right depending on the rotation angle
		double xMovement = MOVEMENT_AMOUNT * movementVector[0];
		
		myTranslation[0] += xMovement;
		
		updateAltitudePosition();
		
	}

	private void updateAltitudePosition() {
		// Move to the correct altitude
		double[] currentMapPosition = getPositionOnMap();
		System.out.printf("Camera Position: x : %.2f z: %.2f%n", myTranslation[0], myTranslation[2]);
		System.out.printf("Map Position: x : %.2f z: %.2f%n", currentMapPosition[0], currentMapPosition[1]);
		double altitutde = myTerrain.altitude(currentMapPosition[0], currentMapPosition[1]);
		System.out.println("altitude: " + altitutde);
		myTranslation[1] = -1 * (altitutde + 0.5);
	}
	
	
	/**
	 * Generates a 2 element array containing the current x and z position on 
	 * the terrain map. This is generated from the camera position and the length of the 
	 * avatar. 
	 * For the purposes of calculating the camera position, the position on 
	 * the map corresponds to the front of the avatar. In this case, the nose 
	 * of the Aeroplane.
	 * @return	A array of doubles of size 2.
	 */
	private double[] getPositionOnMap()
	{
		double[] position = new double[2];
		/*
		 * If the horizontal position is off the map, get the position of 
		 * the closest edge point
		 */
		double rightEdge = myTerrain.size().getWidth() - 1;
		
		double backOfAeroplaneFromCamera = 1;
		double frontOFAvatarFromCamera = backOfAeroplaneFromCamera + myAvatar.getAeroplaneLength();
		double xPosition = -1 * myTranslation[0] + frontOFAvatarFromCamera * Math.sin(Math.toRadians(myRotation));
		
		//double xPosition = -1 * myTranslation[0];
		if (xPosition < 0)
			xPosition = 0;
		else if (xPosition > rightEdge)
			xPosition = rightEdge;
			
		position[0] = xPosition;
		
		/*
		 * Use a similar rule for the bottom and top edges of the map
		 */
		
		double zPosition = myTranslation[2] + frontOFAvatarFromCamera * Math.cos(Math.toRadians(myRotation));
		
		/*if (Math.abs(myRotation) < 90)
			zPosition = 1 + myTranslation[2];
		else
			zPosition = myTranslation[2] - 1;*/
		
		
		double topEdge =  myTerrain.size().getHeight() - 1;
		if (zPosition < 0)
			zPosition = 0;
		else if (zPosition > topEdge)
			zPosition = topEdge;
		
		position[1] = zPosition; 
		return position;
	}
	
	private void moveForward(double byHowMuch)
	{
		
		double[] movementVector = getForwardVector(); 
		// Change the Z translation first
		
		double zTranslation = byHowMuch * movementVector[1];
		
		myTranslation[2] += zTranslation;
		//System.out.println("x before: " + myTranslation[0]);
		
		// Shift the X value to the left or right depending on the rotation angle
		double xMovement = byHowMuch * movementVector[0];
	
		
		myTranslation[0] -= xMovement;
		//System.out.println("x after: " + myTranslation[0]);
		
		updateAltitudePosition();
	}

	/**
	 * Generates a vector representing the direction which the camera 
	 * is facing. This is used for movement.
	 */
	private double[] getForwardVector()
	{
		double[] vector = new double[2];
		vector[0] = Math.sin(Math.toRadians(myRotation));
		vector[1] = Math.cos(Math.toRadians(myRotation));
		return vector;
	}
	
	private void rotate(double rotateAmount)
	{
		myRotation += rotateAmount;
		if (myRotation > 180)
			myRotation = -360 + myRotation;
		else if (myRotation < -180)
			myRotation = 360 + myRotation;
		moveForward(0.0001);
	}
	
	

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
