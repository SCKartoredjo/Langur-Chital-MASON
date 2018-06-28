package TestMason;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import sim.util.Bag;

public class EvolutionAlgorithm {
	// Elects the best agent(s) with a evolution thing
	private Bag bag; // Bag of Monkeys
	private int nrOfElections = 50;//50
	private double mutate = 0.15;//0.15
	Monkeys[] newSimulations = new Monkeys[nrOfElections];

	public EvolutionAlgorithm(Bag b) {
		this.bag = b;
		// Make new solutions asks for an array of monkeys(aka genes) <- this is made
		// within pairwiseElection
		makeNewSimulatonSet(pairwiseElection());
	}

	/**
	 * This will return all new solutions in a bag. The reason for the bag is
	 * because this class takes a bag in the constructor
	 * 
	 * @return
	 */
	public Bag getNewSim() {
		Bag bag = new Bag();
		bag.addAll(newSimulations);// From makeNewSimulations(...); in the constructor
		return bag;
	}

	/**
	 * This function needs a list of monkeys, as it will takes their genes. The
	 * Elite solution will be added unaltered. The other winners of the
	 * election/game will be mutated, and added in a list of new solutions
	 * 
	 * @param monkeys
	 */
	private void makeNewSimulatonSet(Monkeys[] monkeys) {

		// Start of with the Elite, this one will not be altered (as it needs to be a
		// reference for the best)
		newSimulations[0] = monkeys[0]; // 0 is the elite
		System.out.println("\nElite: ");
		printGenes(newSimulations[0]);
		// Get all the other winners and mutate them
		for (int i = 1; i < nrOfElections; i++) {
			Monkeys nm = new Monkeys(System.currentTimeMillis());

			// Mutate one gene at a time
			//System.out.println(nm.genes.size());
			for (int j = 0; j < monkeys[0].genes.size(); j++) {
				nm.genes.set(j, mutate(monkeys[0], monkeys[i].genes.get(j)));
				// System.out.println(mutate(monkeys,monkeys.test2.get(j)));
			}
			//nm.updateBehaviour();

			// Adds this Monkey to the set of new solutions
			newSimulations[i] = nm;
			//printGenes(nm);
			//System.out.println("New: " + sum(newSimulations[i].genes));

		}
	}
	
	private void printGenes(Monkeys m) {
		ScoreSheet sc = new ScoreSheet(m);
		sc.printGenes();
	}

	/**
	 * Alters the given double by adding a Gaussian distributed number
	 * 
	 * @param m
	 *            for the random
	 * @param i
	 *            the gene to mutate
	 * @return
	 */
	private double mutate(Monkeys m, Double i) {
		// Only mutates if the random is lower than the mutate chance
		if (m.random.nextDouble() <= mutate) {
			i = i+m.random.nextGaussian();//Elio said adding, but this causes small numbers to become to big too fast
			// Normalize it when needed
			if (i >= 1)
				return 1.0;
			if (i <= 0)
				return 0.0;
		}
		return i;// mutation is skipped, the original will be returned
	}

	/**
	 * If you want the sum of the array to be lower than 1
	 * 
	 * @param i
	 * @return
	 */
	private ArrayList<Double> normalise(ArrayList<Double> i) {
		double sum = 0.0;
		Iterator<Double> it = i.iterator();
		while (it.hasNext()) {
			sum += it.next();
		}

		for (int j = 0; j < i.size(); j++) {
			i.set(j, i.get(j) / sum);
		}

		return i;
	}

	/**
	 * Returns the sum of an list
	 * 
	 * @param i
	 * @return
	 */
	private double sum(ArrayList<Double> i) {
		Iterator<Double> it = i.iterator();
		double sum = 0.0;
		while (it.hasNext()) {
			sum += it.next();
		}
		return sum;
	}

	/**
	 * This function gets the bag, that was given in the constructor. From this bag
	 * 2 competitors are chosen, and the winner will be added to a list of winners.
	 * The best solution from the previous round will be the first item of the new
	 * solution list.
	 * 
	 * @return
	 */
	private Monkeys[] pairwiseElection() {// TODO: Java random is weak, need the random from a state
		Random r = new Random();
		Monkeys[] winners = new Monkeys[nrOfElections + 1];
		ArrayList<ScoreSheet> sc = new ArrayList<ScoreSheet>();

		// Make a scoresheet of all the solutions
		for (int i = 0; i < bag.size(); i++) {
			Monkeys m = (Monkeys) bag.get(i);
			if(!m.getPruning())//Failed solutions will be skipped
				sc.add(new ScoreSheet((Monkeys) bag.get(i)));
		}

		// Pick the one with the highest fitness and add it to the list of 'winner'
		// solutions
		winners[0] = getElite(sc).getMonkeys();

		// System.out.println(winners[0].getFitness());

		// nrOfElections is the number of times we will compare two solutions
		for (int i = 1; i < nrOfElections + 1; i++) {
			// copy.shuffle(r);

			// Get a random ScoreSheet
			int r1 = r.nextInt(sc.size());
			ScoreSheet s1 = sc.get(r1);

			// Get a random ScoreSheet... again
			int r2 = r.nextInt(sc.size());
			ScoreSheet s2 = sc.get(r2);

			// Now we pick the one with the highest fitness, the monkey with the best
			// solution will be added to the list of winners
			if (pickWinner(s1, s2).equals(s1)) { // Yes winners can contain duplicates. This shouldn't be a problem
				s1.addPoint();
				winners[i] = s1.getMonkeys();
			}

			else {
				s2.addPoint();
				winners[i] = s2.getMonkeys();
			}
		}
		// return getElectionWinner(winners);
		return winners;
	}

	/**
	 * Gets the best solution from the lists of solutions
	 * 
	 * @param sc
	 * @return
	 */
	private ScoreSheet getElite(ArrayList<ScoreSheet> sc) {
		ScoreSheet elite = sc.get(0);
		for (ScoreSheet s : sc) {
			if (s.getFitness() > elite.getFitness())
				elite = s;
		}
		return elite;
	}

	/**
	 * This compares the 'point' in the ScoreSheet class, and returns the (first of
	 * the) highest
	 * 
	 * @param winners
	 * @return
	 */
	private Monkeys getElectionWinner(ScoreSheet[] winners) {
		ScoreSheet leader = winners[0];
		for (ScoreSheet s : winners) {
			if (s.getPoints() > leader.getPoints())
				leader = s;
		}
		return leader.getMonkeys();

	}

	/**
	 * Compares the fitness of the solutions, returns the bigger one
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private ScoreSheet pickWinner(ScoreSheet s1, ScoreSheet s2) {
		// The election
		if (s1.getFitness() >= s2.getFitness())
			return s1;
		else
			return s2;
	}

}
