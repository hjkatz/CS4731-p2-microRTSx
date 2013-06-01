package ai.cs4730;

import java.util.ArrayList;

import rts.GameState;
import ai.AI;
import ai.general.TrafficMap;

public class AIController extends AI {

	private boolean init = false;
	private int currentTurn;
	
	public GameState state; 
	public TrafficMap trafficMap;
	public ArrayList<Integer> resources;
	
	private TownManager townManager;
	private ArmyManager armyManager;
	
	public AIController() {
		super();
		currentTurn = 0;
	}
	
	//things that need to be initialized after the object's init, many rely on state
	public void init(){
		trafficMap = new TrafficMap(state.getMap().length);
		for (int i = 0; i < state.getResourceTypes(); i++) {
			resources.add(state.getResources(i));
		}
		
		init = true;
	}

	@Override
	public void getAction(GameState gs, int time_limit) {
		state = gs;
		if(!init){init();}
		
		currentTurn++;
		trafficMap.update(currentTurn);
		
		townManager.update();
		armyManager.update();
	}

}
