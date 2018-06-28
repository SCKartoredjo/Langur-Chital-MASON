package TestMason;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;
import sim.engine.*;

public class Tree implements Steppable {
	private final int berries = 6;
	private final double chance = 0.05;
	private Network network = new Network(true);
	private Schedule schedule;
	private Continuous2D field;
	private Bag monkeys;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Tree(Schedule s, Continuous2D f, Bag bag) {
		this.schedule = s;
		this.field = f;
		this.monkeys = bag;
	}

	public void step(SimState state) {
		if (state.random.nextDouble() <= chance) {
			for (int i=0 ; i < berries ; i++) {
				addBerriesToField(state.random.nextDouble(), state.random.nextDouble());
			}
		}
	}

	public void setNetwork(Network n) {
		this.network = n;
	}

	/**
	 * Adds a berry to the given location, relative between (15,15) and (30,30)
	 * 
	 * @param dx
	 *            should be between [0,1]
	 * @param dy
	 *            should be between [0,1]
	 */
	private void addBerriesToField(Double dx, Double dy) {
		Berry berry = new Berry();
		Double2D location = (new Double2D(field.getWidth() * 0.5 + dx * 30 - 10, field.getHeight() * 0.5 + dy * 30 - 10));
		berry.location = location;
		berry.start = (int) schedule.getSteps();
		field.setObjectLocation(berry, location);
		network.addNode(berry);
		schedule.scheduleRepeating(berry);

		int len = monkeys.toArray().length;
		for (int i = 0; i < len; i++) {
			Monkey monkey = (Monkey) monkeys.get(i);
			double wantingThis = monkey.hungriness;
			network.addEdge(monkey, berry, wantingThis);
		}

	}

}
