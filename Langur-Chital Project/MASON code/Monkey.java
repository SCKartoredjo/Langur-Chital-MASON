package TestMason;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.field.network.*;

public class Monkey implements Steppable {

	private static final double MAX_DISTANCE = 60.0;
	private static final double MAX_FORCE = 0.05;
	private static final double ENGAGE_DISTANCE = 1;
	private static final long 	serialVersionUID = 1L;

	// Monkey data;
	private long 	start = 0;
	private double 	survivalIntake = 315; //315// How much a monkey need to eat a day
	private double 	hungrinessIncrease = (1/4304);//0.0001
	private double 	berriesWeightEaten = 0;
	private int 	nrBerriesEaten = 0;
	private int 	nrBerriesDroped = 0;

	// Monkey Behavior
	private double 	eatChance = 1; // Relevant when priority in nearBerry is false
	private double 	dropChance = 1; // Be wary dropchance is not 100% by one, since it will be normalized with ignore chance
	private double 	ignoreChance = 1;// see above
	private double 	reproduceChance = 0.005;
	private double	reproductionHungriness = 0.1;
	
	public double 	hungriness = 0.5;// 1=hungry ; 0=not hungry
	public Double2D location = new Double2D();
	public boolean 	isAlive = true;

	public Monkey(double i, double d, double e, double rc, double rh) {
		this.ignoreChance = i;
		this.dropChance = d;
		this.eatChance = e;
		this.reproduceChance = rc;
		this.reproductionHungriness = rh;
	}

	public Monkey(double e, double d, double i) {
		// For a variety of monkeys
		this.ignoreChance = i;
		this.dropChance = d;
		this.eatChance = e;
	}

	public void step(SimState state) {
		move(state);
	}

	private void move(SimState state) {
		if (isAlive) {
			Monkeys monkeys = (Monkeys) state;
			life(monkeys);
			// Monkey gets hungry
			if (hungriness - hungrinessIncrease < 1)
				hungriness += hungrinessIncrease;
			else {
				die(monkeys);
			}
		}
	}

	/**
	 * This function will print all the information of the monkeys life
	 * 
	 * @param stepsLived
	 */
	private void printInformation(long stepsLived) {
		// TODO Auto-generated method stub
		System.out.println("Monkey has died, it has lived: " + stepsLived + " steps.");
		System.out.println("This monkey had eaten: " + nrBerriesEaten + " berries, Which is a total weight of: "
				+ berriesWeightEaten + ".");
		System.out.println("It has dropped: " + nrBerriesDroped + " berries.");
	}

	/**
	 * This is how the agent behaves when alive
	 * 
	 * @param monkeys
	 */
	private void life(Monkeys monkeys) {
		Continuous2D field = monkeys.field;
		Double2D me = monkeys.field.getObjectLocation(this);
		location = me;
		MutableDouble2D sumForces = new MutableDouble2D();

		// Go through the berries and determine how much I want to have them
		MutableDouble2D forceVector = new MutableDouble2D();
		Bag out = monkeys.getTreeNetwork().getEdges(this, null);
		int len = out.size();
		if(hungriness < reproductionHungriness && monkeys.random.nextDouble() <= reproduceChance)
			reproduce(monkeys);

		if (len > 0) {
			// Get the closest Edge from this monkey
			Edge e = closest(monkeys, me, out, len);
			
			try {
				Double2D that = field.getObjectLocation(e.to());			
	
				// Monkey is within engagement distance
				forceVector.setTo((that.x - me.x), (that.y - me.y));
				if (forceVector.length() < ENGAGE_DISTANCE) {
					nearBerry(true, monkeys, out, e);
				}
	
				// A Berry is visible to Monkey
				else if (forceVector.length() < MAX_DISTANCE) {
					visibleBerry(monkeys, me, sumForces, forceVector, that);
				}
			}catch (NullPointerException e1) {}
		}

		// add in a vector to the "Tree" -- the center of the field, so we don't go too
		// far away
		toTree(monkeys, field, me, sumForces);

		// add a bit of randomness
		moveRandom(monkeys, me, sumForces);

		// Set the Result
		sumForces.addIn(me);
		monkeys.field.setObjectLocation(this, new Double2D(sumForces));
	}

	/**
	 * Move random
	 * 
	 * @param monkeys
	 * @param monkeys
	 * @param me
	 * @param sumForces
	 */
	private void moveRandom(Monkeys monkeys, Double2D me, MutableDouble2D sumForces) {
		sumForces.addIn(new Double2D(0.1 * (monkeys.random.nextDouble() * 1.0 - 0.5),
				0.1 * (monkeys.random.nextDouble() * 1.0 - 0.5)));
	}

	/**
	 * A force that binds the monkeys in place
	 * 
	 * @param monkeys
	 * @param field
	 * @param me
	 * @param sumForces
	 */
	private void toTree(Monkeys monkeys, Continuous2D field, Double2D me, MutableDouble2D sumForces) {
		sumForces.addIn(new Double2D((field.width * 0.5 - me.x) * 0.01,
				(field.height * 0.5 - me.y) * 0.01));
	}

	/**
	 * What to do when a berry is within the max_distance aka visible
	 * 
	 * @param monkeys
	 * @param me
	 * @param sumForces
	 * @param forceVector
	 * @param that
	 */
	private void visibleBerry(SimState monkeys, Double2D me, MutableDouble2D sumForces, MutableDouble2D forceVector,
			Double2D that) {
		// Monkey want to go to berry
		forceVector.setTo((that.x - me.x), (that.y - me.y));
		forceVector.resize(hungriness);
		// Only a chance of going
		if (moveSucces(forceVector, monkeys.random.nextDouble()))
			sumForces.addIn(forceVector);
	}

	/**
	 * What to do when near a berry.
	 * 
	 * @param priority
	 *            if true, the monkey will consider eating it first
	 * @param monkeys
	 * @param monkeys
	 * @param out
	 * @param e
	 *            the berry's edge
	 */
	private void nearBerry(boolean priority, Monkeys monkeys, Bag out, Edge e) {
		if (priority)
			interactWithPriority(monkeys, e);
		else
			interactWithoutPriority(monkeys, e);
	}

	/**The monkey will prioritize eating in this function.
	 * The monkey will first check if a random chance is smaller than the the minimum to reproduce.
	 * @param monkeys
	 * @param e
	 */
	private void interactWithPriority(Monkeys monkeys, Edge e) {
		double r = monkeys.random.nextDouble();
		double normalise = dropChance + ignoreChance;
		r = r * normalise;

		// Hungriness has more priority
		if (monkeys.random.nextDouble() > reproductionHungriness) {
			eat(monkeys, (Berry) e.to(), e);
		}

		// If the monkey decided it does not want to eat, it will consider
		// dropping/ignoring
		// This is done this way, because the hungriness is not manipulatable
		else if (r < dropChance) {
			drop(monkeys, (Berry) e.to());
		}
		// or ignore berry.
		monkeys.getTreeNetwork().removeEdge(e);
	}

	private void interactWithoutPriority(Monkeys monkeys, Edge e) {
		double r = monkeys.random.nextDouble();
		double normalise = dropChance + ignoreChance + eatChance;
		r = r * normalise;

		if (r < eatChance) { // eat
			eat(monkeys, (Berry) e.to(),e);
		} else if (r < dropChance) { // drop
			drop(monkeys, (Berry) e.to());
		}
		// or ignore berry.
		monkeys.getTreeNetwork().removeEdge(e);
	}

	/**
	 * Finds the closest berry to the monkey
	 * 
	 * @param monkeys
	 * @param me
	 *            me location
	 * @param out
	 *            The bag
	 * @param len
	 *            Length of the edges in the bag
	 * @return
	 */
	private Edge closest(Monkeys monkeys, Double2D me, Bag out, int len) {
		Edge e = null;
		Double shortest = MAX_DISTANCE;
		for (int monkey = 0; monkey < len; monkey++) {

			Edge foe = (Edge) (out.get(monkey));

			// I could be in the to() end or the from() end. getOtherNode is a cute function
			// which grabs the thing at the opposite end from me.
			Double2D that = monkeys.field.getObjectLocation(foe.getOtherNode(this));
			double foo = me.distance(that.x, that.y);

			if (foo < shortest) {
				shortest = foo;
				e = foe;
			}
		}
		return e;
	}

	/**
	 * Drops the berry, this removes it from the network of the monkey
	 * 
	 * @param monkeys
	 * @param berry
	 */
	private void drop(Monkeys monkeys, Berry berry) {
		berry.fall();
		nrBerriesDroped += 1;
		monkeys.getBerries().addToGround(berry);
		monkeys.getTreeNetwork().removeNode(berry);
	}

	/**
	 * Sets the location of the monkey. DOES NOT CHANCE THE LOCATION ON THE FIELD,
	 * THIS IS A REFERENCE!
	 * 
	 * @param xy
	 */
	public void setLocation(Double2D xy) {
		location = xy;
	}

	// True of the forceVector is bigger than the random generated number
	// The bigger the force, the more chance of moving
	// MAX_FORCE is 100% chance of moving
	/**
	 * The bigger the force, the more chance there is of moving
	 * 
	 * @param vector
	 * @param randomD
	 * @return
	 */

	private boolean moveSucces(MutableDouble2D vector, Double randomD) {
		return randomD * MAX_FORCE <= vector.length();
	}

	/**
	 * Eat the berry, counts it to the hungriness Removes the berry from the network
	 * 
	 * @param monkeys
	 * @param berry
	 */
	private void eat(Monkeys monkeys, Berry berry, Edge e) {
		if (hungriness - berry.weight / survivalIntake < 0)
			hungriness = 0;
		else
			hungriness -= berry.weight / survivalIntake;
		berry.consume();
		nrBerriesEaten += 1;
		berriesWeightEaten += berry.weight;
		monkeys.getTreeNetwork().removeNode(berry);
		monkeys.getTreeNetwork().removeEdge(e);
		monkeys.field.remove(berry);
	}

	/**
	 * Monkey dies
	 */
	public void die(Monkeys monkeys) {
		isAlive = false;
		monkeys.field.remove(this);
		monkeys.getTreeNetwork().removeNode(this);
		monkeys.schedule.scheduleRepeating(this).stop();
		monkeys.getMonkeys().remove(this);
		//printInformation(monkeys.schedule.getSteps() - start);
	}

	/**
	 * Set the monkey starting time in steps
	 * 
	 * @param s
	 */
	public void setStart(long s) {
		start = s;
	}
	
	private void reproduce(Monkeys monkeys) {
		monkeys.addMonkey(hungriness);
		//System.out.println("BABY BORN");
	}
	
	public double getHungriness() {return hungriness;}
	public int getBerries_Eaten() {return nrBerriesEaten;}
	public int getBerries_Dropped() {return nrBerriesDroped;}
	
}
