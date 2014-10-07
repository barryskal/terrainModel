package ass2.spec;

public class Point 
{

	public double x;
	public double y;
	public double z;
	
	public Point(double newX, double newY, double newZ)
	{
		x = newX;
		y = newY;
		z = newZ;
	}
	
	public double[] getPointAsDoubleArray()
	{
		double[] pointArray = 
			{
				x, 
				y, 
				z
			};
		
		return pointArray;
	}
	
}
