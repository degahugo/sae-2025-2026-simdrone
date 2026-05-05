package fr.utln.jme3.math;

/**
 * This class defines a termination criteria for an optimization process. 
 * The criteria can either be based on the number of iterations or on the value of the objective function.
 * 
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 */
public class TerminationCriteria {
	
	/**
	 * The type of an epsilon based termination criteria.
	 */
	public static final int TYPE_EPSILON = 1;
	
	/**
	 * The type of an iteration count termination criteria.
	 */
	public static final int TYPE_COUNT = 2;

	/**
	 * The type of a mixed iteration count and epsilon value termination criteria.
	 */
	public static final int TYPE_COUNT_OR_EPSILON = 2;
	
	/**
	 * The type of the termination criteria (epsilon value or iteration count).
	 */
	private int type;
	
	/**
	 * The epsilon value of the termination criteria.
	 * @see #count
	 */
	private double epsilon;
	
	/**
	 * The maximum iterations count for this  termination criteria.
	 * @see #epsilon
	 */
	private int count;
	
	/**
	 * Create a new termination criteria with the given epsilon value. The type of the created criteria is {@link #TYPE_EPSILON}.
	 * @param epsilon the epsilon value of the termination criteria
	 */
	public static TerminationCriteria createEpsilonCriteria(double epsilon) {
		return new TerminationCriteria(epsilon);
	}
	
	/**
	 * Create a new termination criteria with the given maximum iterations count. The type of the created criteria is {@link #TYPE_COUNT}.
	 * @param count the maximum iterations count of the termination criteria
	 */
	public static TerminationCriteria createCountCriteria(int count) {
		return new TerminationCriteria(count);
	}
	
	/**
	 * Create a new termination criteria with the given epsilon value and maximum iterations count. The type of the created criteria is {@link #TYPE_COUNT_OR_EPSILON}.
	 * @param epsilon the epsilon value of the termination criteria
	 * @param count the maximum iterations count of the termination criteria
	 */
	public static TerminationCriteria createCountOrEpsilonCriteria(double epsilon, int count) {
		return new TerminationCriteria(epsilon, count);
	}
	
	/**
	 * Create a new termination criteria with the given type and value.
	 * @param epsilon
	 * @param count
	 */
	public TerminationCriteria(double epsilon, int count) {
		this.type = TYPE_COUNT_OR_EPSILON;
		this.epsilon = epsilon;
		this.count = count;
	}
	
	/**
	 * Create a new termination criteria with the given maximum iterations count.
	 * @param count the maximum iterations count of the termination criteria
	 */
	public TerminationCriteria(int count) {
		this.type = TYPE_COUNT;
		this.count = count;
	}
	
	/**
	 * Create a new termination criteria with the given epsilon value.
	 * @param epsilon the epsilon value of the termination criteria
	 */
	public TerminationCriteria(double epsilon) {
		this.type = TYPE_EPSILON;
		this.epsilon = epsilon;
	}
	
	/**
	 * Get the type of the termination criteria. Type can be either {@link #TYPE_EPSILON}, {@link #TYPE_COUNT} or {@link #TYPE_COUNT_OR_EPSILON}.
	 * @return the type of the termination criteria
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Get the maximal iterations count of this termination criteria. 
	 * If the termination criteria type differs from {@link #TYPE_COUNT} of {@value #TYPE_COUNT_OR_EPSILON}, <code>-1</code> is returned
	 * @return the number of iterations of this termination criteria
	 */
	public int getIterationsCount() {
		if ((type == TYPE_COUNT) || (type == TYPE_COUNT_OR_EPSILON)) {
			return count;
		} else {
			return -1;
		}
	}
	
	/**
	 * Get the epsilon value of this termination criteria. 
	 * If the termination criteria type differs from {@link #TYPE_EPSILON} of {@value #TYPE_COUNT_OR_EPSILON}, <code>Float.NaN</code> is returned
	 * @return the epsilon value of this termination criteria
	 */
	public double getEpsilonValue() {
		if ((type == TYPE_EPSILON) || (type == TYPE_COUNT_OR_EPSILON)) {
			return epsilon;
		} else {
			return Float.NaN;
		}
	}
}
