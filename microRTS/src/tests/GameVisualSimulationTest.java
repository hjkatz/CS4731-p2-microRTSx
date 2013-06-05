/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import rts.Game;
import ai.cs4730.AIController;
import ai.general.GeneralAI;

/**
 * \package tests
 * \brief Provides methods for testing AIs against each other
 */

/**
 * \brief Runs a visualization of the game. This is probably the best place for
 * debugging.
 * 
 * @author santi
 */
public class GameVisualSimulationTest
{
    public static void main( String args[] ) throws Exception
    {
        int MAXCYCLES = 50000;
        int PERIOD = 20;
        
        Game game = new Game( "microRTS/maps/32x32-resources.xml", "microRTS/game/gamedef.xml", PERIOD, MAXCYCLES );
        
        game.addAgent( new AIController() );
        //    	game.addAgent(new GeneralAI(GeneralAI.LESION_ONLY_RANGE|GeneralAI.LESION_NO_DEFENSE));
        //    	game.addAgent(new GeneralAI(GeneralAI.LESION_NO_DEFENSE|GeneralAI.LESION_NO_FLYING));
        game.addAgent( new GeneralAI( GeneralAI.LESION_NONE ) );
        
        game.playVisual( 900, true, true, Game.FOLLOW_ALL_TEAMS );
    }
}
