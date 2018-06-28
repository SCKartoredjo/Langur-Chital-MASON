package TestMason;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sim.util.Bag;

public class ScoreSheet {
	//Find the fitness of the monkeys to give it to the Evolution Algorithm
	//Also returns it to the simulation
	private Monkeys monkeys;
	private int point = 0;
	private double fitness = 0;
	
	public ScoreSheet(Monkeys m) {
		this.monkeys = m;
		//this.fitness = getBerriesScore();
		//this.fitness = geneScore();
		this.fitness = getInterestScore();
	}
	
	/**The amount of berries all the monkeys where able to eat in a simulation
	 * @return
	 */
	public int getBerriesScore() {
		Bag m = monkeys.getAllMonkeys();
		int score = 0;		
		
		for(int i = 0 ; i < m.size() ; i++) {
			int x = ((Monkey) m.get(i)).getBerries_Eaten();
			score += x;
			//System.out.println(x);
		}
		//System.out.println(score);
		return score;
	}
	
	public int getDroppedNumber() {
		Bag m = monkeys.getAllMonkeys();
		int score = 0;		
		
		for(int i = 0 ; i < m.size() ; i++) {
			int x = ((Monkey) m.get(i)).getBerries_Dropped();
			score += x;
			//System.out.println(x);
		}
		//System.out.println(score);
		return score;
	}
	
	private int getInterestScore() {
		Bag m = monkeys.getAllDeers();
		int score = 0;		
		
		for(int i = 0 ; i < m.size() ; i++) {
			int x = ((Deer) m.get(i)).getInterest();
			score += x;
			//System.out.println(x);
		}
		//System.out.println(score);
		return score;
	}
	
	/**A way of testing the fitness function
	 * @return
	 */
	private double geneScore() {
		return sum(monkeys.genes);
	}
	
	private double sum(List<Double> i) {
		Iterator<Double> it = i.iterator();
		double sum = 0.0;
		while(it.hasNext()) {
			sum += it.next();			
		}
		return sum;
	}
	
	/**Return the fitness, the fitness is calculate in the constructor. Don't forget to chance that when testing
	 * @return
	 */
	public double getFitness() {
		return fitness;
	}
	
	/**
	 * Adds an point to the score sheet, for whatever reason
	 */
	public void addPoint() {
		point++;
	}
	
	/**Adds i points to the score sheet, for whatever reason
	 * @param i
	 */
	public void addPoint(int i) {
		point += i;
	}
	
	/**Gets the points added though the addPoint
	 * @return
	 */
	public int getPoints() {//winner points for election of whatever really
		return point;
	}
	
	public Monkeys getMonkeys() {
		return monkeys;
	}
	
	public void printGenes() {
		String [] names = {"eat", "drop", "ignore", "reprChance", "reprHunger"};
		System.out.println("This with a score of: " + getFitness()); 
		System.out.println("berries eaten: " + getBerriesScore());
		System.out.println("berries dropped: " + getDroppedNumber());
		for(int i=0; i < names.length; i++) {
			System.out.println(names[i] + ": " + monkeys.genes.get(i));
		}
		System.out.print('\n');
	}
}
