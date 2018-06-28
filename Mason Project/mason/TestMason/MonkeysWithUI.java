package TestMason;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import sim.portrayal.simple.*;
import javax.swing.*;
import java.awt.Color;
import sim.portrayal.*;
import java.awt.*;

public class MonkeysWithUI extends GUIState {
	public Display2D display;
	public JFrame displayFrame;
	ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();

	public static void main(String[] args) {
		MonkeysWithUI vid = new MonkeysWithUI();
		Console c = new Console(vid);
		c.setVisible(true);
	}

	// Monkeys takes a seed,
	// Berries also takes a seed.
	// Manipulatable:
	// Monkeys:
	// nr of monkeys
	// starting hunger
	// decision parameters
	// Berries:
	// nr of berries (this is only the starting number, see Tree)
	// berry weight (but 0.5 is the "realistic" weight")
	// Trees:
	// Speed and range of spawning
	public MonkeysWithUI() {
		super(new Monkeys(System.currentTimeMillis()));
		// give deers and predators the field to set them in and the deer the berries
		// also
	}

	public MonkeysWithUI(SimState state) {
		super(state);
	}

	public static String getName() {
		return "Savanna";
	}

	public void start() {
		super.start();
		setupPortrayals();
	}

	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals() {
		Monkeys monkeys = (Monkeys) state;
		// tell the portrayals what to portray and how to portray them
		fieldPortrayal.setField(monkeys.field);
		// fieldPortrayal.setPortrayalForAll(new RectanglePortrayal2D());
		fieldPortrayal.setPortrayalForClass(Deer.class, new OvalPortrayal2D() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
				Deer deer = (Deer) object;
				if (deer.interested)
					paint = new Color(250, 0, 50, 200);
				else 
					paint = new Color(125, 125, 250, 200);
				super.draw(object, graphics, info);
			}
		});
		
		fieldPortrayal.setPortrayalForClass(Berry.class, new OvalPortrayal2D() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
				Berry berry = (Berry) object;
				if (berry.isEaten)
					paint = new Color(255, 255, 255, 0);
				else if (berry.onGround)
					paint = new Color(0, 255, 255, 100);
				else
					paint = new Color(255, 0, 255, 100);
				super.draw(object, graphics, info);
			}
		});
		fieldPortrayal.setPortrayalForClass(Monkey.class, new RectanglePortrayal2D() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
				Monkey monkey = (Monkey) object;
				if (monkey.isAlive) {
					int hungryShade = (int) (monkey.hungriness * 255);
					if (hungryShade > 255)
						hungryShade = 255;
					paint = new Color(hungryShade, 255 - hungryShade, 0, 200);
				} else
					paint = new Color(125, 125, 125, 0);
				super.draw(object, graphics, info);
			}			
		});
		
		fieldPortrayal.setPortrayalForClass(Predator.class, new RectanglePortrayal2D() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			Predator predator = (Predator) object;
			if (!predator.disinterest)
				paint = new Color(125, 125, 125);
			else
				paint = new Color(255, 100, 100);
			super.draw(object, graphics, info);
		}
	});
		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);
		// redraw the display
		display.repaint();
	}

	public void init(Controller c) {
		super.init(c);
		display = new Display2D(600, 600, this);
		display.setClipping(false);
		displayFrame = display.createFrame();
		displayFrame.setTitle("Field Display");
		c.registerFrame(displayFrame); // so the frame appears in the "Display" list
		displayFrame.setVisible(true);
		display.attach(fieldPortrayal, "Savanna");
	}

	public void quit() {
		super.quit();
		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;
	}
	
	private boolean done() {
		return state.schedule.getSteps() >= 5000;
	}

}
