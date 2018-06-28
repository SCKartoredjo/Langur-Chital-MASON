package TestMason;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class Deer implements Steppable {

	private static final double MAX_DISTANCE = 10.0;
	private static final double MAX_FORCE = 0.05;
	private static final double ENGAGE_DISTANCE = 1;
	private static final double gain = 0.5;
	private Continuous2D field;
	private Double2D course = new Double2D(0.05, 0.05);
	private Double interest = 0.0;
	private int stepsInterested = 0;
	
	public boolean interested = false;
	private double berriesWeightEaten = 0;
	private int nrBerriesEaten = 0;

	private Double2D location;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void step(SimState state) {
		life(state);
		if(interest > 0)
			stepsInterested++;
	}

	private void life(SimState state) {
		Monkeys monkeys = (Monkeys) state;
		Continuous2D field = monkeys.field;
		Network network = monkeys.getGroundNetwork();
		Double2D me = monkeys.field.getObjectLocation(this);
		location = me;
		MutableDouble2D sumForces = new MutableDouble2D();
		MutableDouble2D forceVector = new MutableDouble2D();
		Bag out = monkeys.getGroundNetwork().getAllNodes();
		int len = out.size();
		
		//Interest is only under the tree
		if (me.distance(new Double2D(field.width*0.5, field.height*0.5)) > 20) {
			interest = 0.0;
			interested = false;
		}
		
		if(state.random.nextDouble() < interest) //interest for parameter and random for fixed
			stickAround(me);			

		else if (len > 0) {
			//interest = 0.0;
			// Get the closest Node
			Berry e = closest(monkeys, me, out, len);
			try {
				Double2D that = field.getObjectLocation(e);
				// Berry is within engagement distance
				forceVector.setTo((that.x - me.x), (that.y - me.y));
				if (forceVector.length() < ENGAGE_DISTANCE) {
					eat(network, e);
					//stickAround(me, sumForces);
				}
	
				// A Berry is visible
				else if (forceVector.length() < MAX_DISTANCE) {
					goTo(me, that, sumForces, forceVector);
					interested = true;
				}
				
	
				// Nothing is visible
				else {
					wander(state, me);
				}
			} catch (NullPointerException e1) {}

		} else {
			wander(state, me);
		}
	}

	private void stickAround(Double2D me) {
		//MutableDouble2D interestVector = new MutableDouble2D();
		interested = true;
		interest -= 0.01;		
	}

	private void wander(SimState state, Double2D me) {
		while (outOfRange(me))
			changeDirection(state);
		moveStraight(me);
		interested = false;
	}

	private boolean outOfRange(Double2D me) {
		return ((me.x + course.x) > field.width || (me.y + course.y) > field.height || (me.x + course.x) < 0
				|| (me.y + course.y < 0));
	}

	private void changeDirection(SimState state) {
		// TODO Auto-generated method stub
		setCourse(new Double2D(state.random.nextDouble() * 2 - 1, state.random.nextDouble() * 2 - 1));
	}

	private void moveStraight(Double2D me) {
		field.setObjectLocation(this, new Double2D((me.x + course.x), (me.y + course.y)));
	}
	
	private void moveRandom(SimState state, Double2D me, MutableDouble2D sumForces) {
		Double2D r = new Double2D(state.random.nextDouble() * (state.random.nextDouble() * 1.0 - 0.5),
				state.random.nextDouble() * (state.random.nextDouble() * 1.0 - 0.5));
		sumForces.addIn(r);
	}

	private void goTo(Double2D me, Double2D that, MutableDouble2D sumForces, MutableDouble2D forceVector) {
		forceVector.setTo((that.x - me.x), (that.y - me.y));
		//forceVector.resize(MAX_FORCE);
		sumForces.addIn(forceVector);
		sumForces.resize(MAX_FORCE);
		sumForces.addIn(me);
		field.setObjectLocation(this, new Double2D(sumForces));
	}

	private void eat(Network network, Berry berry) {
		berry.consume();
		nrBerriesEaten += 1;
		
		gainInterest();
		
		berriesWeightEaten += berry.weight;
		network.removeNode(berry);
		field.remove(berry);
	}

	private void gainInterest() {
		if(interest + gain > 1) {
			interest = 1.0;
		}
		else interest += gain;
	}

	private Berry closest(Monkeys monkeys, Double2D me, Bag out, int len) {
		if (len <= 0)
			return null;
		else {
			Berry e = (Berry) out.get(0);
			Double shortest = MAX_DISTANCE;
			for (int berry = 0; berry < len; berry++) {

				Berry foe = (Berry) out.get(berry);
				Double2D that = monkeys.field.getObjectLocation(foe);
				
				try {
					double foo = me.distance(that.x, that.y);
	
					if (foo < shortest) {
						shortest = foo;
						e = foe;
					}
				}
				catch(NullPointerException e1) {}
			}
			return e;
		}
	}

	public void setField(Continuous2D f) {
		this.field = f;
	}

	public void setCourse(Double2D c) {
		this.course = c.resize(MAX_FORCE);
	}
	
	private void printInformation(long stepsLived) {
		// TODO Auto-generated method stub
		System.out.println("Monkey has died, it has lived: " + stepsLived + " steps.");
		System.out.println("This monkey had eaten: " + nrBerriesEaten + " berries, Which is a total weight of: "
				+ berriesWeightEaten + ".");
	}
	
	public int getInterest() {
		return stepsInterested;
	}

}
