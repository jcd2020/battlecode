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
		
		if(myTeam.equals(Team.A)){
			
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
		
		if(myTeam.equals(Team.A)){
			
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
		
		if(myTeam.equals(Team.A)){
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
	
	static int encodeTreeMessage(TreeInfo t)
	{
		int id = t.ID;
		int health = (int) t.health;
		int maxHealth = (int) t.maxHealth;
		int robotType = (int) convertEnumToInt(t.getContainedRobot());
		int x = (int)t.location.x;
		int y = (int)t.location.y;
		int bulletsContained = t.containedBullets;
		int team = t.getTeam().ordinal();
		
		String message = convertIntToBinaryString(id, 15) + convertIntToBinaryString(health, 11) + convertIntToBinaryString(maxHealth, 11)+ convertIntToBinaryString(robotType, 3) + convertIntToBinaryString(x, 7) + convertIntToBinaryString(y, 7) + convertIntToBinaryString(team, 2) + convertIntToBinaryString(bulletsContained, 10);
		
		return Integer.parseInt(message, 2);
		
	}
	
	static int[] decodeTreeMessage(int m)
	{
		String message = convertIntToBinaryString(m, 64);
		
		int id = Integer.parseInt(message.substring(0, 15), 2);
		int health = Integer.parseInt(message.substring(15, 26), 2);
		int maxHealth = Integer.parseInt(message.substring(26, 37), 2);
		int robotType = Integer.parseInt(message.substring(37, 40), 2);
		int x = Integer.parseInt(message.substring(40, 47), 2);
		int y = Integer.parseInt(message.substring(47, 54), 2);
		int team = Integer.parseInt(message.substring(54, 56), 2);
		int bulletsContained = Integer.parseInt(message.substring(56, 64), 2);
		
		return new int[]{id, health, maxHealth, robotType, x, y, team, bulletsContained};
	}
	
	static int encodeEnemyMessage(RobotInfo r)
	{
		int id = r.ID;
		int health = (int) r.health;
		int x = (int)r.location.x;
		int y = (int)r.location.y;
		int robotType = convertEnumToInt(r.type);
		
		String message = convertIntToBinaryString(id, 15) + convertIntToBinaryString(health, 11) + convertIntToBinaryString(robotType, 3) + convertIntToBinaryString(x, 7) + convertIntToBinaryString(y, 7);
		
		return Integer.parseInt(message, 2);
		
	}
	static int[] decodeEnemyMessage(int m)
	{
		String message = convertIntToBinaryString(m, 43);
		
		int id = Integer.parseInt(message.substring(0, 15), 2);
		int health = Integer.parseInt(message.substring(15, 26), 2);
		int robotType = Integer.parseInt(message.substring(26, 29), 2);
		int x = Integer.parseInt(message.substring(29, 36), 2);
		int y = Integer.parseInt(message.substring(36, 43), 2);
		
		return new int[]{id, health, robotType, x, y};
	}

	private static int convertEnumToInt(RobotType containedRobot) 
	{
		if(containedRobot == null)
		{
			return 7;
		}
		else
		{
			return containedRobot.ordinal();

		}
		
	}
	
	
	private static String convertIntToBinaryString(int number, int numBits)
	{
		String binaryString = Integer.toBinaryString(number);
		binaryString = "00000000000000000000000000000000000000000000000000000000" + binaryString;
		binaryString = binaryString.substring(binaryString.length() - numBits);
		return binaryString;
	}
}


