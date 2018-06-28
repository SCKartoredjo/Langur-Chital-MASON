package TestMason;

import sim.engine.SimState;
import sim.engine.*;
import sim.util.*;

public class SimulationManager extends SimState{
	
	private static final long serialVersionUID = 1L;
	private static final int evolutionRounds = 20;
	private static final int nrOfStartingSim = 50;
	private static final int nrOfResults = 1;
	private static final int steps = 4304;
	
	/**This is the class that acts like a bridge between the data and the simulation
	 * It manages the evolution of the genes and passes it to the simulation
	 * @param seed
	 */
	public SimulationManager(long seed) {
		super(seed);
	}

	public static void main(String[] args) {
		Monkeys[] results = new Monkeys[nrOfResults];
		
		for(int result = 0; result < nrOfResults ; result++) {
			
	//		//Setup with 1 monkey
	//		Monkeys m = new Monkeys(System.currentTimeMillis());
	//		
	//		Bag bag = new Bag();		
	//		bag.add(m);//First round is one agent, because I am lazy
			
			//Setup for n random monkeys
			Bag bag = new Bag();
			for (int n = 0 ; n < nrOfStartingSim ; n++) {	
				Monkeys m = new Monkeys(System.currentTimeMillis());
				m.fillGenesRandom();
				bag.add(m);
				//m.updateBehaviour();
				//System.out.println("Start Sim: " + (n+1));
				//m.startSimulation(steps);
			}
			
			Monkeys first = (Monkeys) bag.get(0);
			first.startSimulation(steps);
			
			for (int i=0 ; i < evolutionRounds ; i++) {
				System.out.println("(r:" + (result+1) +  ",e:" + (i+1) + ")");
				
				
				for(int j=1; j < bag.size() ; j++) {
					Monkeys me = (Monkeys) bag.get(j);
					//System.out.println("(r:" + (result+1) +  ",e:" + (i+1) + ",s:" + (j+1) + ")");
					me.startSimulation(steps);
				}
				
				//Make new Simulation from the current bag
				EvolutionAlgorithm ea = new EvolutionAlgorithm(bag);
				
				//Collect new solutions, overwrite the old ones, repeat loop
				bag = ea.getNewSim();
				
			}
			//Save Solution
			Monkeys solution = (Monkeys) bag.get(0);
			results[result] = solution;
		}
		
		System.out.println("DONE");
		for (int r = 0; r < nrOfResults ; r++) {
			ScoreSheet sc = new ScoreSheet (results[r]);
			sc.printGenes();
		}
		
	}

}
