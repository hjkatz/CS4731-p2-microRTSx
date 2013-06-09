package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import rts.GameState;
import rts.units.Unit;
import rts.units.UnitDefinition;
import ai.AI;

public class AIController extends AI{
   public final static boolean                   DEBUG         = true;
   // building types
   public static final int                       STOCKPILE     = 0;
   public static final int                       SOLDIEROFFICE = 1;
   public static final int                       AIRPORT       = 2;
   // unit types
   public static final int                       LIGHT         = 0;
   public static final int                       WORKER        = 1;
   public static final int                       HEAVY         = 2;
   public static final int                       RANGER        = 3;
   public static final int                       BIRD          = 4;
   public static final int                       SKYARCHER     = 5;
   // units and resources controlled or remembered
   public ArrayList<Integer>                     resources;
   public ArrayList<UnitController>              freeUnits;
   public ArrayList<UnitController>              notFreeUnits;
   public ArrayList<FarmUnitController>          farms;
   public Map<Integer, Boolean>                  farmOpenings;
   public ArrayList<WorkerUnitController>        workers;
   public ArrayList<WorkerUnitController>        builders;
   public ArrayList<ArmyUnitController>          groundUnits;
   public ArrayList<ArmyUnitController>          airUnits;
   public ArrayList<UnitController>              scouts;
   public ArrayList<BuildingUnitController>      stockpiles;
   public ArrayList<BuildingUnitController>      buildings;
   public ArrayList<BuildingUnitController>      enemyBuildings;
   // experts
   public WorkerManager                          workerManager;
   public ArmyManager                            armyManager;
   public BuildingManager                        buildingManager;
   public UnitAssigner                           unitAssigner;
   // game logic variable
   public GameState                              gameState;
   public MapUtil                                map;
   public int                                    currentTurn;
   private boolean                               init          = false;
   // expert's logic variables
   public STATE                                  state;
   public int                                    wantedWorkers = 2;
   public int                                    wantedScouts  = 0;
   // unit definitions
   public LinkedHashMap<Integer, UnitDefinition> unitTypes;
   public LinkedHashMap<Integer, UnitDefinition> buildingTypes;
   // keep track of how well units are doing versus other units
   // the key is the unit type, the outer array is the other unit types
   // inner arrays are the statistics, size 2 [kills vs, deaths vs]
   public LinkedHashMap<Integer, int[][]>        statistics;

   public AIController(){
      super();
      currentTurn = 0;
      freeUnits = new ArrayList<UnitController>();
      notFreeUnits = new ArrayList<UnitController>();

      resources = new ArrayList<Integer>();

      farms = new ArrayList<FarmUnitController>();
      farmOpenings = new HashMap<Integer, Boolean>();
      workers = new ArrayList<WorkerUnitController>();
      builders = new ArrayList<WorkerUnitController>();
      buildings = new ArrayList<BuildingUnitController>();
      stockpiles = new ArrayList<BuildingUnitController>();
      groundUnits = new ArrayList<ArmyUnitController>();
      airUnits = new ArrayList<ArmyUnitController>();
      scouts = new ArrayList<UnitController>();
      enemyBuildings = new ArrayList<BuildingUnitController>();

      unitTypes = new LinkedHashMap<Integer, UnitDefinition>();
      buildingTypes = new LinkedHashMap<Integer, UnitDefinition>();
      statistics = new LinkedHashMap<Integer, int[][]>();

      workerManager = new WorkerManager(this);
      armyManager = new ArmyManager(this);
      buildingManager = new BuildingManager(this);
      unitAssigner = new UnitAssigner(this);
      state = STATE.Open;
   }

   @Override public void getAction(GameState gs, int time_limit){
      gameState = gs;
      resources = gameState.getResources();
      if(!init){
         init();
      }

      for(Unit u : gameState.getMyUnits()){
         UnitController uc = new UnitController(u, this);
         if(!notFreeUnits.contains(uc)){
            if(u.isBuilding()){
               BuildingUnitController bc = new BuildingUnitController(u, this);
               freeUnits.add(bc);
            }
            else
               if(u.isWorker()){
                  WorkerUnitController wc = new WorkerUnitController(u, this);
                  freeUnits.add(wc);
               }
               else{
                  ArmyUnitController ac = new ArmyUnitController(u, this);
                  freeUnits.add(ac);
               }
         }
      }

      currentTurn++;

      MapUtil.update(gs.getMap());
      MapUtil.trafficMap.update(currentTurn);

      unitAssigner.update();
      armyManager.update();
      workerManager.update();
      buildingManager.update();
   }

   // things that need to be initialized after the object's init, many rely on state
   public void init(){
      map = new MapUtil(this);
      ArrayList<UnitDefinition> unitList = gameState.getUnitList();
      for(UnitDefinition def : unitList){
         unitTypes.put(def.type, def);
         int[][] stats = new int[unitList.size()][2];
         statistics.put(def.type, stats);
      }
      for(UnitDefinition def : gameState.getBuildingList()){
         buildingTypes.put(def.type, def);
      }
      init = true;
   }

   private enum STATE{
      Open, Midgame, Close
   }
}
