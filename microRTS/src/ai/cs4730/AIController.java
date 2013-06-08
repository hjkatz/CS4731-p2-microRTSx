package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import rts.GameState;
import rts.units.Unit;
import rts.units.UnitDefinition;
import ai.AI;

public class AIController extends AI{
   public final static boolean                   DEBUG         = true;
   private boolean                               init          = false;
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
   // Label and Priority, Bigger Priority == More likely to build
   // (Use order of 1 - 100) Every time a unit is made its priority will drop by 1
   public HashMap<Integer, Integer>              buildPriority;
   public ArrayList<FarmUnitController>          farms;
   public ArrayList<WorkerUnitController>        workers;
   public ArrayList<WorkerUnitController>        builders;
   public ArrayList<BuildingUnitController>      stockpiles;
   public ArrayList<BuildingUnitController>      buildings;
   public ArrayList<Integer>                     requestedUnits;
   public ArrayList<UnitController>              groundUnits;
   public ArrayList<UnitController>              airUnits;
   public ArrayList<UnitController>              scouts;
   public ArrayList<Integer>                     resources;
   // possible units and buildings
   public LinkedHashMap<Integer, UnitDefinition> unitTypes;
   public LinkedHashMap<Integer, UnitDefinition> buildingTypes;
   // game logic variable
   public ArrayList<BuildingUnitController>      enemyBuildings;
   public GameState                              gameState;
   public TownManager                            townManager;
   public ArmyManager                            armyManager;
   public ArrayList<UnitController>              freeUnits;
   public MapUtil                                map;
   public int                                    currentTurn;
   public STATE                                  state;
   public int                                    wantedWorkers = 5;
   public int                                    wantedScouts  = 1;

   public AIController(){
      super();
      currentTurn = 0;
      freeUnits = new ArrayList<UnitController>();

      buildPriority = new HashMap<Integer, Integer>();

      farms = new ArrayList<FarmUnitController>();
      workers = new ArrayList<WorkerUnitController>();
      builders = new ArrayList<WorkerUnitController>();
      buildings = new ArrayList<BuildingUnitController>();
      stockpiles = new ArrayList<BuildingUnitController>();
      groundUnits = new ArrayList<UnitController>();
      airUnits = new ArrayList<UnitController>();
      scouts = new ArrayList<UnitController>();
      enemyBuildings = new ArrayList<BuildingUnitController>();
      unitTypes = new LinkedHashMap<Integer, UnitDefinition>();
      buildingTypes = new LinkedHashMap<Integer, UnitDefinition>();

      requestedUnits = new ArrayList<Integer>();

      townManager = new TownManager(this);
      armyManager = new ArmyManager(this);
      state = STATE.Open;
   }

   @Override public void getAction(GameState gs, int time_limit){
      gameState = gs;
      resources = gameState.getResources();
      if(!init){
         init();
      }

      for(Unit u : gameState.getMyUnits()){
         if(u.isWorker()){
            WorkerUnitController wc = new WorkerUnitController(u, this);
            if(!workers.contains(wc) && !scouts.contains(wc)){
               freeUnits.add(wc);
            }
         }
         else
            if(u.isBuilding()){
               BuildingUnitController bc = new BuildingUnitController(u, this);
               if(!stockpiles.contains(bc) && !buildings.contains(bc)){
                  freeUnits.add(bc);
               }
            }
            else{
               ArmyUnitController ac = new ArmyUnitController(u, this);
               if(!u.isWorker() && !groundUnits.contains(ac) && !groundUnits.contains(ac)){
                  freeUnits.add(ac);
               }
            }
      }

      currentTurn++;

      MapUtil.trafficMap.update(currentTurn);
      MapUtil.update(gs.getMap());

      armyManager.assignUnits();
      townManager.assignUnits();

      armyManager.update();
      townManager.update();
   }

   // things that need to be initialized after the object's init, many rely on state
   public void init(){
      map = new MapUtil(this);
      resources = gameState.getResources();
      for(UnitDefinition def : gameState.getUnitList()){
         unitTypes.put(def.type, def);
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
