package ai.cs4730;

import ai.AI;
import rts.GameState;
import rts.units.Unit;

import java.util.ArrayList;

public class AIController extends AI{
	public final static boolean		DEBUG				= true;
	private boolean						init				= false;

	public GameState						gameState;
	public TownManager					townManager;
	public ArmyManager					armyManager;
	public ArrayList<UnitController>	freeUnits;
	public MapUtil							map;
	public int								currentTurn;
	public STATE							state;

	public int								wantedWorkers	= 5;
	public int								wantedScouts	= 0;
	

	public AIController(){
		super();
		currentTurn = 0;
		freeUnits = new ArrayList<UnitController>();
		townManager = new TownManager();
		armyManager = new ArmyManager();

		state = STATE.Open;
	}

	@Override public void getAction(GameState gs, int time_limit){
		gameState = gs;
		if(!init){
			init();
		}

		for(Unit u : gameState.getMyUnits()){
			if(u.isWorker()){
				WorkerUnitController wc = new WorkerUnitController(u, this);
				if(!townManager.workers.contains(wc) && !armyManager.scouts.contains(wc)){
					freeUnits.add(wc);
				}
			}
			else
				if(u.isBuilding()){
					BuildingUnitController bc = new BuildingUnitController(u, this);
					if(!townManager.stockpiles.contains(bc) && !townManager.buildings.contains(bc)){
						freeUnits.add(bc);
					}
				}
				else{
					ArmyUnitController ac = new ArmyUnitController(u, this);
					if(!u.isWorker() && !armyManager.groundUnits.contains(ac) && !armyManager.groundUnits.contains(ac)){
						freeUnits.add(ac);
					}
				}
		}

		currentTurn++;

		armyManager.assignUnits(this);
		townManager.assignUnits(this);

		armyManager.update(this);
		townManager.update(this);
	}

	// things that need to be initialized after the object's init, many rely on
	// state
	public void init(){
		map = new MapUtil(this);

		init = true;
	}

	private enum STATE{
		Open, Midgame, Close
	}
}
