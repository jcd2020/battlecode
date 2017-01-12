package examplefuncsplayer;
import battlecode.common.*;

public class Awareness {
	
	static RobotController rc = RobotPlayer.rc;
	
	//BC 0
	static int friendlyCount(){
		return RobotPlayer.rc.getRobotCount();
	}

	//BC 1
	static RobotInfo[] enemiesInSight(){
		Team myTeam = rc.getTeam();
		
		if(myTeam == Team.A){
			
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(-1, Team.B);
			return enemyInfo;	
		}
		else{
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(-1, Team.A);
			return enemyInfo;
		}
	}
	
	//BC 2
	static TreeInfo[] neutTreesInSight(){
		TreeInfo[] neutInfo = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		return neutInfo;
	}
	
	//BC 3
	static TreeInfo[] enemyTreesInSight(){
		Team myTeam = rc.getTeam();
		
		if(myTeam == Team.A){
			
			TreeInfo[] enTreeInfo = rc.senseNearbyTrees(-1, Team.B);
			return enTreeInfo;
			
		}
		else{
			
			TreeInfo[] enTreeInfo = rc.senseNearbyTrees(-1, Team.A);
			return enTreeInfo;
		}
	}
	
	//BC 4
	static int friendlyTreeCount(){
		return rc.getTreeCount();	
	}
	
	//BC 5
	static int vpCount(){
		return rc.getTeamVictoryPoints();
	}
	
	//BC 6
	static int bulletCount(){
		return (int)rc.getTeamBullets();
	}
	
	//BC 7 ~ Only call in the runArchon Method
	static int friendlyArchonHealth(){
		return (int)rc.getHealth();
	}
	
	//BC 8
	static int enemyArchonHealth(){
		Team myTeam = rc.getTeam();
		
		if(myTeam == Team.A){
			RobotInfo[] infoArray = rc.senseNearbyRobots(-1, Team.B);
			for(RobotInfo info : infoArray){
				if(info.getType() == RobotType.ARCHON){
					return (int)info.getHealth();
				}
			}
			return -1;
		}	
		else{
			RobotInfo[] infoArray = rc.senseNearbyRobots(-1, Team.A);
			for(RobotInfo info : infoArray){
				if(info.getType() == RobotType.ARCHON){
					return (int)info.getHealth();
				}
			}
			return -1;
		}
	}
}


