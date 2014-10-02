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
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;



/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    private Terrain myTerrain;
    private double[] myVertices;
    private double[] myNormals;
    private int[] myTriIndices;
    private double[] myTranslation;
    private double myRotation;
    private static double MOVEMENT_AMOUNT = 0.5;
	private static double ROTATION_AMOUNT = 5;
	private final int NUM_TEXTURES = 1;
	private MyTexture[] myTextures;
	private String textureFileName1 = "groundTexture.jpg";
	private String textureExt1 = "jpg";

    public Game(Terrain terrain) {
    	super("Assignment 2");
        myTerrain = terrain;
        myVertices = myTerrain.getVertexList();
        myTriIndices = myTerrain.getTriIndexList();
        myNormals = myTerrain.getNormalList(myTriIndices, myVertices);
        myTranslation = myTerrain.getStartingTranslation();
        myRotation = 0;
   
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
    	String testFile = "test2.json";
    	Terrain terrain = LevelIO.load(new File(testFile));
        Game game = new Game(terrain);
        game.run();
    }

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        gl.glLoadIdentity();
        gl.glRotated(myRotation, 0, 1, 0);
        gl.glTranslated(myTranslation[0], myTranslation[1], myTranslation[2]);
        gl.glScaled(1, 1, -1);

        float matDiff[] = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matDiff, 0);
        
        // Set current texture
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
	
	

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
    	
    	gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    	gl.glEnable(GL2.GL_DEPTH_TEST); // Enable depth testing.

    	   // Turn on OpenGL lighting.
    	gl.glEnable(GL2.GL_LIGHTING); 
    	
    	// Turn on OpenGL texturing.
    	gl.glEnable(GL2.GL_TEXTURE_2D);
    	myTextures = new MyTexture[NUM_TEXTURES];
    	myTextures[0] = new MyTexture(gl,textureFileName1,textureExt1);
    	
    	
    	/*
    	 * The textures should not interact with the colour of the underlying
    	 * polygon, so we will set the textures to replace.
    	 */
    	gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE); 
    	
    	// Light property vectors.
    	float lightAmb[] = { 0.0f, 0.0f, 0.0f, 1.0f };
    	float lightDifAndSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    	float sunDir[] = myTerrain.getSunlight();
    	float lightPos[] = { sunDir[0], sunDir[1], sunDir[2], 0.0f };
    	//float globAmb[] = { 0.2f, 0.2f, 0.2f, 1.0f };

    	// Light properties.
    	//gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb,0);
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb,0);
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec,0);
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec,0);
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos,0);

    	gl.glEnable(GL2.GL_LIGHT0); // Enable particular light source.
    	
    			
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		 GL2 gl = drawable.getGL().getGL2();
         gl.glMatrixMode(GL2.GL_PROJECTION);
         gl.glLoadIdentity();
         
         GLU glu = new GLU();
         glu.gluPerspective(60.0, (float)width/(float)height, 1.0, 50.0);
        
         gl.glMatrixMode(GL2.GL_MODELVIEW);
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				moveForward();
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

	private void moveBackward() {
		// Change the Z translation first
		double zTranslation = MOVEMENT_AMOUNT * Math.cos(Math.toRadians(myRotation));
		
		myTranslation[2] -= zTranslation;
		
		// Shift the X value to the left or right depending on the rotation angle
		double xMovement = MOVEMENT_AMOUNT * Math.sin(Math.toRadians(myRotation));
		
		myTranslation[0] += xMovement;
		
		updateAltitudePosition();
		
	}

	private void updateAltitudePosition() {
		// Move to the correct altitude
		double[] currentMapPosition = getPositionOnMap();
		System.out.printf("x : %.2f z: %.2f%n", currentMapPosition[0], currentMapPosition[1]);
		double altitutde = myTerrain.altitude(currentMapPosition[0], currentMapPosition[1]);
		System.out.println("altitude: " + altitutde);
		myTranslation[1] = -1 * (altitutde + 0.5);
	}
	
	
	/**
	 * Generates a 2 element array containing the current x and z position on 
	 * the terrain map. This is generated from the camera position
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
		double xPosition = rightEdge + myTranslation[0];
		if (xPosition < 0)
			xPosition = 0;
		else if (xPosition > rightEdge)
			xPosition = rightEdge;
			
		position[0] = xPosition;
		
		/*
		 * Use a similar rule for the bottom and top edges of the map
		 */
		
		double zPosition = 1 + myTranslation[2];
		double topEdge =  myTerrain.size().getHeight() - 1;
		if (zPosition < 0)
			zPosition = 0;
		else if (zPosition > topEdge)
			zPosition = topEdge;
		
		position[1] = zPosition; 
		return position;
	}
	
	private void moveForward()
	{
		// Change the Z translation first
		
		double zTranslation = MOVEMENT_AMOUNT * Math.cos(Math.toRadians(myRotation));
		
		myTranslation[2] += zTranslation;
		//System.out.println("x before: " + myTranslation[0]);
		
		// Shift the X value to the left or right depending on the rotation angle
		double xMovement = MOVEMENT_AMOUNT * Math.sin(Math.toRadians(myRotation));
	
		
		myTranslation[0] -= xMovement;
		//System.out.println("x after: " + myTranslation[0]);
		
		updateAltitudePosition();
	}
	
	private void rotate(double rotateAmount)
	{
		myRotation += rotateAmount;
		if (myRotation > 180)
			myRotation = -360 + myRotation;
		else if (myRotation < -180)
			myRotation = 360 + myRotation;
	}
	
	

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
