package Signals;

/**
 * Represents a complex number.
 * @author Steve Levine
 *
 */
public class Complex {
	public double a;
	public double b;
	
	public Complex(double a, double b) {
		this.a = a;
		this.b = b;
	}
	
	public Complex(Double a) {
		this.a = a;
		this.b = 0;
	}
	
	public Complex multiply(Complex other) {
		return new Complex(this.a*other.a - this.b*other.b, this.a*other.b + this.b*other.a);
	}
	
	public Complex multiply(double scalar) {
		return new Complex(scalar * this.a, scalar * this.b);	
	}
	
	public void scale(double scalar) {
		a *= scalar;
		b *= scalar;
	}
	
	public Complex add(Complex other) {
		return new Complex(this.a + other.a, this.b + other.b);
	}
	
	public Complex subtract(Complex other) {
		return new Complex(this.a - other.a, this.b - other.b);
	}
	
	public static Complex exp(double theta) {
		return new Complex(Math.cos(theta), Math.sin(theta));
	}

	public double radius() {
		return Math.sqrt(a*a + b*b);
	}
	
	public double theta() {
		return Math.atan2(b, a);
	}
	
}
