package test;


public class StatLib {



	// simple average
	public static float avg(float[] x){
		float sum = 0;
		for(int i=0;i<x.length;i++) {
			sum+= x[i];
		}
		return (sum/x.length);
	}

	// returns the variance of X and Y
	public static float var(float[] x){
		float Var=0;
		float u=avg(x);
		int len  = x.length;
		for(int i=0;i<x.length;i++){
			Var+=Math.pow(x[i]-u,2);
		}
		return (Var/len);
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		float uxy=0;
		float ux=avg(x);
		float uy=avg(y);
		int len  = x.length;
		for(int i=0;i<len;i++){
			uxy+=((x[i]-ux)*(y[i]-uy));
		}
		return (uxy/len);
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		return (float) (cov(x,y)/(Math.sqrt(var(x))*Math.sqrt(var(y))));
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		int size = points.length;
		float x[]= new float[size];
		float y[]= new float[size];
		float a,b;
		for(int i=0;i<size;i++){
			x[i]=points[i].x;
			y[i]=points[i].y;
		}
		a=(cov(x,y)/var(x));
		b=avg(y)-(a*avg(x));
		Line XY = new Line(a,b);
		return XY;
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){
		Line l = linear_reg(points);
		return Math.abs(l.f(p.x)-p.y);
	}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){
		return Math.abs(l.f(p.x)-p.y);
	}

}