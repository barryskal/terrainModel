package ass2.spec;

public class MathUtil 
{
	public static double[] crossProduct(double[] a, double[] b)
    {
    	double[] c = new double[3];
    	c[0] = a[1] * b[2] - b[1] * a[2];
    	c[1] = a[2] * b[0] - b[2] * a[0];
    	c[2] = a[0] * b[1] - b[0] * a[1];
    	
    	// Normalise the new vector
    	double magnitude = Math.sqrt(c[0] * c[0] + c[1] * c[1] + c[2] * c[2]);
    	for (int i = 0; i < 3; i++)
    		c[i] /= magnitude;
    	
    	return c;
    }
	
	public static double magnitude(double[] vector)
	{
		double magnitude = 0;
		for (int i = 0; i < vector.length; i++)
			magnitude += vector[i] * vector[i];
		
		magnitude = Math.sqrt(magnitude);
		return magnitude;
	}
}
