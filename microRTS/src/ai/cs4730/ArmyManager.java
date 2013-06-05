
package ai.cs4730;

import java.util.LinkedHashMap;

public class ArmyManager extends Manager
{
    
    private enum GOAL
    {
        Explore, Attack, Defend
    };
    
    private LinkedHashMap<GOAL, Float> goals;
    
    public ArmyManager()
    {
        goals.put( GOAL.Explore, 0.8f );
        goals.put( GOAL.Attack, 0.0f );
        goals.put( GOAL.Defend, 0.2f );
    }
    
    @Override
    public void update( AIController ai )
    {
        //build new units and do militaristic activities
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
        //give it all military units including scouts
        //give it buildings that produce military units
    }
    
    @Override
    public void assignResources( AIController ai )
    {
        //give it a ceratin amount of the resources
    }
    
}
