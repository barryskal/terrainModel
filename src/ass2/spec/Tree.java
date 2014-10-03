package ass2.spec;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {
	
	public static double TREE_HEIGHT = 0.25;
	public static double TREE_RADIUS = 0.05;
	public static double LEAVES_RADIUS = 0.25;

    private double[] myPos;
    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
    }
    
    public double[] getPosition() {
        return myPos;
    }
    
    public double[] getCentreOfLeaves()
    {
    	double leavesPosition[] = { myPos[0], myPos[1], myPos[2] };
    	leavesPosition[1] += TREE_HEIGHT + LEAVES_RADIUS;
    	return leavesPosition;
    }
    

}
