package ai.teamharrison;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import rts.GameState;
import rts.units.UnitDefinition;
import ai.AI;

public class HarrisonAIController extends AI{
   public final static boolean                   DEBUG          = true;
   // building types
   public static final int                       STOCKPILE      = 0;
   public static final int                       SOLDIEROFFICE  = 1;
   public static final int                       AIRPORT        = 2;
   // unit types
   public static final int                       LIGHT          = 0;
   public static final int                       WORKER         = 1;
   public static final int                       HEAVY          = 2;
   public static final int                       RANGER         = 3;
   public static final int                       BIRD           = 4;
   public static final int                       SKYARCHER      = 5;
   // units and resources controlled or remembered
   public ArrayList<Integer>                     resources;
   public ArrayList<HarrisonUnitController>              notFreeUnits;
   public ArrayList<HarrisonFarmUnitController>          farms;
   public ArrayList<HarrisonWorkerUnitController>        farmers;
   public ArrayList<HarrisonBuilderUnitController>       builders;
   public ArrayList<HarrisonArmyUnitController>          armyUnits;
   public ArrayList<HarrisonUnitController>              scouts;
   public ArrayList<HarrisonBuildingUnitController>      stockpiles;
   public ArrayList<HarrisonBuildingUnitController>      buildings;
   public ArrayList<HarrisonBuildingUnitController>      enemyBuildings;
   public ArrayList<HarrisonUnitController>              enemyUnits;
   public ArrayList<HarrisonUnitController>              deadUnits;
   // experts
   public HarrisonWorkerManager                          harrisonWorkerManager;
   public HarrisonArmyManager                            harrisonArmyManager;
   public HarrisonBuildingManager                        harrisonBuildingManager;
   public HarrisonUnitAssigner                           harrisonUnitAssigner;
   public HarrisonUnitQueue                              harrisonUnitQueue;
   // game logic variable
   public GameState                              gameState;
   public HarrisonMapUtil                                map;
   public int                                    currentTurn;
   // expert's logic variables
   public int                                    wantedWorkers  = 2;
   public int                                    wantedScouts   = 0;
   public int                                    wantedBuilders = 0;
   // unit definitions
   public LinkedHashMap<Integer, UnitDefinition> unitTypes;
   public LinkedHashMap<Integer, UnitDefinition> buildingTypes;
   // keep track of how well units are doing versus other units
   // the key is the unit type, the outer array is the other unit types
   // inner arrays are the statistics, size 2 [kills vs, deaths vs]
   public LinkedHashMap<Integer, int[][]>        statistics;
   private boolean                               init           = false;
   public boolean foundEnemyBase = false;

   public HarrisonAIController(){
      super();
      currentTurn = 0;
      notFreeUnits = new ArrayList<HarrisonUnitController>();

      resources = new ArrayList<Integer>();

      farms = new ArrayList<HarrisonFarmUnitController>();
      farmers = new ArrayList<HarrisonWorkerUnitController>();
      builders = new ArrayList<HarrisonBuilderUnitController>();
      buildings = new ArrayList<HarrisonBuildingUnitController>();
      stockpiles = new ArrayList<HarrisonBuildingUnitController>();
      armyUnits = new ArrayList<HarrisonArmyUnitController>();
      scouts = new ArrayList<HarrisonUnitController>();
      enemyBuildings = new ArrayList<HarrisonBuildingUnitController>();
      enemyUnits = new ArrayList<HarrisonUnitController>();
      deadUnits = new ArrayList<HarrisonUnitController>();

      unitTypes = new LinkedHashMap<Integer, UnitDefinition>();
      buildingTypes = new LinkedHashMap<Integer, UnitDefinition>();
      statistics = new LinkedHashMap<Integer, int[][]>();

      harrisonWorkerManager = new HarrisonWorkerManager(this);
      harrisonArmyManager = new HarrisonArmyManager(this);
      harrisonBuildingManager = new HarrisonBuildingManager(this);
      harrisonUnitAssigner = new HarrisonUnitAssigner(this);
      harrisonUnitQueue = new HarrisonUnitQueue();
   }

   @Override public void getAction(GameState gs, int time_limit){
      gameState = gs;
      resources = gameState.getResources();
      if(!init){
         init();
      }

      currentTurn++;

      HarrisonMapUtil.update();
      harrisonUnitAssigner.update();
      harrisonArmyManager.update();
      harrisonWorkerManager.update();
      harrisonBuildingManager.update();
   }

   // things that need to be initialized after the object's init, many rely on state
   public void init(){
      map = new HarrisonMapUtil(this);
      ArrayList<UnitDefinition> unitList = gameState.getUnitList();
      for(UnitDefinition def : unitList){
         unitTypes.put(def.type, def);
         int[][] stats = new int[unitList.size()][2];
         statistics.put(def.type, stats);
      }
      for(UnitDefinition def : gameState.getBuildingList()){
         buildingTypes.put(def.type, def);
      }
      harrisonUnitAssigner.update();
      harrisonArmyManager.init();
      init = true;
   }

   @Override public String getLabel(){
      return "Harrison";
   }
}
