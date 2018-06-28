package TestMason;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;

public class Berry implements Steppable{

	public double weight = 0.5;//5
	public Double2D location = new Double2D(); // DO NOT USE THIS LOCATION; ITS FOR TESTING
	public boolean onGround = false;
	public boolean isEaten = false;
	public Berries berries;
	public int start;
	private int decay = 50;

	/**
	 * Used by: Monkey.drop()
	 */
	public void fall() {
		onGround = true;
	}

	/**
	 * DO NOT USE THIS, ITS HERE FOR TESTING PURPOSE
	 */
	public void remove() {
		location = new Double2D(900, 900);
	}

	/**
	 * Used by: Monkey.consume() Deer.consume()
	 */
	public void consume() {
		isEaten = true;
	}

	@Override
	public void step(SimState state) {
		// TODO Auto-generated method stub
		//System.out.println(state.schedule.getSteps() - start);
		if(state.schedule.getSteps() - start > decay && onGround){
			//System.out.println("you");
			consume();
		}
	}

}
