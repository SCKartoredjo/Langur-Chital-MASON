package TestMason;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class Predator implements Steppable {

	private static final double MAX_DISTANCE = 20.0;
	private static final double MAX_FORCE = 0.5;
	private static final double ENGAGE_DISTANCE = 1;
	private static final double ALARM_CALL_DISTANCE = 5;
	private Continuous2D field;
	private Double2D course = new Double2D(0.05, 0.05);
	private Bag mbag = null;
	private Bag dbag = null;
	private Double agression = 1.0;
	public boolean disinterest = false; 

	private Double2D location;
	private int nrEaten = 0;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Predator (Bag monkeys, Bag deers) {
		this.mbag = monkeys;
		this.dbag= deers;
	}

	public void step(SimState state) {
		life(state);
	}

	private void life(SimState state) {
		Monkeys monkeys = (Monkeys) state;
		Continuous2D field = monkeys.field;
		Double2D me = monkeys.field.getObjectLocation(this);
		location = me;
		
		
		MutableDouble2D sumForces = new MutableDouble2D();
		MutableDouble2D forceVector = new MutableDouble2D();
		int len = mbag.size();

		if(deerNear(field,me) && monkeyNear(field,me))
			changeDirection(state);

		if (monkeyNear(field,me) && !deerNear(field,me) && !disinterest) {
			Monkey e = closest(monkeys, me, len);
			Double2D that = field.getObjectLocation(e);
			// Within engagement distance
			try {
			forceVector.setTo((that.x - me.x), (that.y - me.y));
			} catch (NullPointerException e1) {}
			if(state.random.nextDouble() <= agression) {//racecondition, null
				
				//Attack Distance
				if (forceVector.length() < ENGAGE_DISTANCE) 
						eat(monkeys, e);			

				// Visible
				else if (forceVector.length() < MAX_DISTANCE) 
					goTo(me, that, sumForces, forceVector);
				
			
				// Nothing is visible
				else {
					fly(state, me);
				}
			}
		} else 
			fly(state, me);
		
	}

	private boolean deerNear(Continuous2D field, Double2D me) {
		Bag closeToMe = field.getNeighborsWithinDistance(me, ALARM_CALL_DISTANCE);
		for(int i=0 ; i < dbag.size() ; i++) {
			if(closeToMe.contains(dbag.get(i)))
				return true;
		}
		return false;
	}
	
	private boolean monkeyNear(Continuous2D field, Double2D me) {
		Bag closeToMe = field.getNeighborsWithinDistance(me, ENGAGE_DISTANCE);
		for(int i=0 ; i < mbag.size() ; i++) {
			if(closeToMe.contains(mbag.get(i)))
				return true;
		}
		return false;
	}

	private void fly(SimState state, Double2D me) {
		while (outOfRange(me)) {
			changeDirection(state);
			disinterest = false;
		}
		moveStraight(me);
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

	private void goTo(Double2D me, Double2D that, MutableDouble2D sumForces, MutableDouble2D forceVector) {
		forceVector.setTo((that.x - me.x), (that.y - me.y));
		//forceVector.resize(MAX_FORCE);
		sumForces.addIn(forceVector);
		sumForces.resize(MAX_FORCE);
		sumForces.addIn(me);
		field.setObjectLocation(this, new Double2D(sumForces));
	}

	private void eat(Monkeys monkeys, Monkey monkey) {
		monkey.die(monkeys);
		mbag.remove(monkey);	
		nrEaten += 1;
		disinterest = true;
	}

	private Monkey closest(Monkeys monkeys, Double2D me, int len) {
		if (len <= 0)
			return null;
		else {
			Monkey e = (Monkey) mbag.get(0);
			Double shortest = MAX_DISTANCE;
			for (int i = 0; i < len; i++) {

				Monkey foe = (Monkey) mbag.get(i);
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
	

}
