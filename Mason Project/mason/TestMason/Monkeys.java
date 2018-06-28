package TestMason;

import sim.util.*;
import sim.field.continuous.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sim.engine.*;
import sim.field.network.*;

public class Monkeys extends SimState {

	private static final long serialVersionUID = 1L;
	private static final int MAX_MONKEYS = 100;
	public Continuous2D field = new Continuous2D(1.0, 250, 250);
	private boolean pruneThis = false;
	
	//Simulation initializers: Number of agents
	public int numMonkeys = 29;//14-29
	public int numDeers = 50;
	public int numPredators = 50;
	private final int genesLenght = 100; //For testing random genes
	
	//Simulation initializers: Monkey behaviour
	private double eatChance =  0.5; // Relevant when priority in nearBerry is false
	private double dropChance = 0.5; // Be wary dropchance is not 100% by one, since it will be normalized with ignore chance
	private double ignoreChance = 0.5;// see above.
	private double 	reproduceChance = 0.0;
	private double	reproductionHungriness = 1;
	//public ArrayList<Double> genes;
	public List<Double> genes = new ArrayList<>(Arrays.asList(eatChance, dropChance, ignoreChance, reproduceChance, reproductionHungriness));
	
	private Berries berries; // All monkeys knows about all berries
	private Bag monkeys = new Bag();
	private Bag allMonkeys = new Bag();
	private Bag deers = new Bag();

	public Monkeys(long seed) {
		super(seed);
		this.berries = new Berries(System.currentTimeMillis());
		//fillGenesRandom();
	}
	
	public void updateBehaviour() {
		eatChance = genes.get(0);
		dropChance = genes.get(1);
		ignoreChance = genes.get(2);
		reproduceChance = genes.get(3);
		reproductionHungriness = genes.get(4);
	}
	
//	public void updateBehaviour() {
//		eatChance = 1.0;
//		dropChance = 1.0;
//		ignoreChance = 1.0;
//		reproduceChance = 1.0;
//		reproductionHungriness = 0.0;
//	}

	public void fillGenesRandom() {
		for(int i=0 ; i < genes.size() ; i++) {
			genes.set(i, random.nextDouble());
		}
	}

	public void start() {
		super.start();
		// clear the yard
		field.clear();
		// clear the berries
		berries.treeNetwork.clear();// berries.food is the network of all berries
		berries.addBerriesToField(field);	
		addDeers();

		// add some monkeys to the field
		for (int i = 0; i < numMonkeys; i++) {
			Monkey monkey = addMonkey();

			Bag network = berries.treeNetwork.getAllNodes();
			// Every monkey knows about all berries
			for (int j = 0; j < berries.numBerries; j++) {
				makeNetwork(monkey, network, j);
			}
		}
		//System.out.println("Initial Size " + allMonkeys.size());
		Tree tree = new Tree(schedule, field, monkeys);
		tree.setNetwork(berries.treeNetwork);
		schedule.scheduleRepeating(tree);
		
		addPredator(deers);
	}

	private void makeNetwork(Monkey monkey, Bag food, int j) {
		Object berry = food.get(j);
		double wantingThis = monkey.hungriness;
		berries.treeNetwork.addEdge(monkey, berry, wantingThis);
	}

	//For initialization of the monkeys, takes the set parameters 
	public Monkey addMonkey() {
		Monkey monkey = new Monkey(ignoreChance, dropChance, eatChance, reproduceChance, reproductionHungriness);
		monkey.setStart(schedule.getSteps());
		Double2D location = (new Double2D(field.getWidth() * 0.5 + random.nextDouble() * 15 - 5, field.getHeight() * 0.5 + random.nextDouble() * 15 - 5));
		field.setObjectLocation(monkey, location);
		schedule.scheduleRepeating(monkey);
		monkeys.add(monkey);
		allMonkeys.add(monkey);
		return monkey;
	}
	
	public Monkey addMonkey(double hungriness) {
		Monkey m = addMonkey();
		m.hungriness = hungriness;
		return m;
	}
	
//	If you want deviants in the set, not recommended for actual test. Parameters needs to be the same to generate results. 	
//	private Monkey addMonkey(double ignore, double drop, double eat) {
//		Monkey monkey = new Monkey(ignore, drop, eat);
//		monkey.setStart(schedule.getSteps());
//		monkey.hungriness = random.nextDouble();
//		// monkey.hungriness = 0.1;
//		field.setObjectLocation(monkey, new Double2D(field.getWidth() * 0.1 + random.nextDouble() * 60 - 0.1,
//				field.getHeight() * 0.1 + random.nextDouble() * 60 - 0.1));
//		schedule.scheduleRepeating(monkey);
//		return monkey;
//	}

	private void addDeers() {
		for (int i = 0; i < numDeers; i++) {
			Deer deer = new Deer();
			field.setObjectLocation(deer,
					new Double2D(field.getWidth() * random.nextDouble(), field.getHeight() * random.nextDouble()));
			deer.setField(field);
			deer.setCourse(new Double2D(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1));
			schedule.scheduleRepeating(deer);
			deers.add(deer);
		}
	}
	
	private void addPredator(Bag deers) {
		for (int i = 0; i < numPredators; i++) {
			Predator predator = new Predator(monkeys, deers);
			field.setObjectLocation(predator,
					new Double2D(field.getWidth() * random.nextDouble(), field.getHeight() * random.nextDouble()));
			predator.setField(field);
			predator.setCourse(new Double2D(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1));
			schedule.scheduleRepeating(predator);
		}
	}

	public static void main(String[] args) {
		doLoop(Monkeys.class, args);
		System.exit(0);
	}
	
	public void startSimulation(int steps) {
		SimState state = this;
		updateBehaviour();
		state.start();
		do
			if (!state.schedule.step(state)) break;
		while(state.schedule.getSteps() < steps && monkeys.size() > 0 && monkeys.size() <= MAX_MONKEYS);
		if(monkeys.size() > MAX_MONKEYS || state.schedule.getSteps() < steps || monkeys.size() <= 0)
			pruneThis = true;
		//System.out.println(monkeys.size());
		state.finish();
		//System.exit(0);
	}

	public Berries getBerries() {// All monkeys have a belief about berries
		return berries;
	}

	public Network getTreeNetwork() {// All monkeys have a belief that is the foodnetwork
		return berries.treeNetwork;
	}

	public Network getGroundNetwork() {
		return berries.groundNetwork;
	}
	
	public Bag getBag() {
		return monkeys;
	}
	
	public Bag getMonkeys() {
		return monkeys;
	}
	
	public Bag getAllMonkeys() {
		return allMonkeys;
	}
	
	public Bag getAllDeers() {
		return deers;
	}
	
	public boolean getPruning() {
		return pruneThis;
	}

}
