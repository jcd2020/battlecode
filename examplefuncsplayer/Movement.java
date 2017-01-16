package examplefuncsplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.*;

public class Movement {
	
	static RobotController rc = RobotPlayer.rc;
	static MapLocation myLoc = rc.getLocation();
	static final double  attackProb = .6;
	
	
	  /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }
    
    
    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */

    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    
    static boolean isFleeing(){
    	if(Math.random() > .5){
    		return true;
    	}
    	else{return false;}
    }
    
    static boolean flee(MapLocation closeEn, int bulletCt, BulletInfo[] infoArray) throws GameActionException{
    	//Tries to move away from enemies without intersecting with bullets
    	Direction fleeDir = myLoc.directionTo(closeEn).opposite();
    	
    	//Sorts to get nearest bullet
    	if(infoArray.length != 0){
    		double min = 100;
    		int index = 0;
    		for(int i = 0; i < infoArray.length; i++){
    			double dist = myLoc.distanceTo(infoArray[i].location);

    			if(dist < min){
    				min = dist;
    				index = i;
    			}
    		}
    		//Checks to see if the robot is going to hit a bullet
    		boolean notSafe = RobotPlayer.willCollideWithMe(infoArray[index]);
    		if(notSafe){
    			if(Math.random() < .5){
    				fleeDir = fleeDir.rotateLeftDegrees(10);
    			}
    			else{
    				fleeDir = fleeDir.rotateRightDegrees(10);
    			}
    		}
    	}
    	boolean fleeBool = tryMove(fleeDir, 10, 2);
    	    	
    	if(fleeBool && bulletCt > 300 && Math.random() > 1 - attackProb && rc.canFireSingleShot()){
    		rc.fireSingleShot(fleeDir.opposite());
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    static boolean scoutMotion(int scoutType){
    	//fixed pattern on 
    }
    
  
}
