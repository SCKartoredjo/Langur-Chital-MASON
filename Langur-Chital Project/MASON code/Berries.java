package TestMason;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Double2D;

public class Berries extends SimState {

	private static final long serialVersionUID = 1L;
	public int numBerries = 20;
	public Network treeNetwork = new Network(true); // relation to berries
	public Network groundNetwork = new Network(true); // relation to berries
	public Continuous2D field;

	public Berries(long seed) {
		super(seed);
	}

	public void addBerriesToField(Continuous2D field) {
		for (int i = 0; i < numBerries; i++) {
			Berry berry = new Berry();

			Double2D location = (new Double2D(field.getWidth() * 0.5 + random.nextDouble() * 15 - 5,
					field.getHeight() * 0.5 + random.nextDouble() * 15 - 5));

			berry.location = location;
			berry.start = (int) schedule.getSteps();
			field.setObjectLocation(berry, location);

			treeNetwork.addNode(berry);
			schedule.scheduleRepeating(berry);
		}
	}

	public void addToTree(Berry b) {
		treeNetwork.addNode(b);
	}

	public void addToGround(Berry b) {
		groundNetwork.addNode(b);
	}

}
