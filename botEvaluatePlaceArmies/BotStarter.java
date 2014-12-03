// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package botEvaluatePlaceArmies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import view.GUI;

import main.Region;
import main.RunGame;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	private int totalEvaluatedArmyNeed = 0;
	
	
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	@Override
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut)
	{


		
		ArrayList<Region> preferredStartingRegions = new ArrayList<Region>();
	
		// // get the list of pickable regions
		ArrayList<Region> pickable = state.getPickableStartingRegions(); 
		// // sort regions numbers by ID
		ArrayList<ArrayList<Region>> byID = new ArrayList<ArrayList<Region>>();
		
		for(int i = 0; i < 6; i++){
			// 2 from each super region
			byID.add(new ArrayList<Region>());
		}

		for(int i = 0; i < pickable.size(); i++){
			byID.get(pickable.get(i).getSuperRegion().getId()-1).add(pickable.get(i));
		}

		// // our order would be: south america, north america, africa, australia, europe, asia
		// // 2-> 1 -> 4 > 6 -> 3 -> 5
		for(int i = 0; i < 2; i++){
			preferredStartingRegions.add(byID.get(1).get(i)); 
	    }

	    for(int i = 0; i < 2; i++){
	    	preferredStartingRegions.add(byID.get(0).get(i)); 
	    }

	    for(int i = 0; i < 2; i++){
	    	preferredStartingRegions.add(byID.get(3).get(i)); 
	    }
		
		return preferredStartingRegions;
	}
	

	
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{

		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();

		// number of armies left
		int numArmies = state.getStartingArmies();
		int originalNumArmies = state.getStartingArmies();

		// find all regions held by player that is bordering other regions
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions(); 

		System.out.println("\n\ngoing to get the regions needing armies!"); 
		
		PriorityQueue<RegionWrapper> gettingArmies = getRegionsNeedingArmies(myName, visibleRegions);
		PriorityQueue<RegionWrapper> givenArmies = new PriorityQueue<RegionWrapper>();
 
		System.out.println("number needing armies: " + gettingArmies.size()); 
		
		// give armies based on need 
		boolean giveByNeed = true;
		
		// need of 2
		// and 4 to place
		// want 2 per need: 4/2
		// so numArmies/totalNeed
		
		double armiesPerNeed = (numArmies+0.0)/totalEvaluatedArmyNeed; 
		int armiesPerRegion = (int)Math.ceil((double)numArmies/gettingArmies.size());
		
		
		
		if (armiesPerNeed <= 0.0){
			giveByNeed = false;
		}
		
		//GUI.makeAlert("totalEvaluatedArmyNeed: " + totalEvaluatedArmyNeed + "\narmiesPerNeed: " + armiesPerNeed + "\narmiesPerRegion: " + armiesPerRegion + "\nnumArmies: " + numArmies + "\ngiveByNeed: " + giveByNeed + "\nnumber regions to give to: " + gettingArmies.size());   
		String armiesReport = ""; 
		int i = 0;
		// give armies to each region in need until we run out. 
		while(numArmies > 0 && i < 50){
			
			if (gettingArmies.size() == 0 && givenArmies.size() > 0){
				PriorityQueue<RegionWrapper> nowEmpty = gettingArmies;
				gettingArmies = givenArmies;
				givenArmies = nowEmpty;
			}
			
			RegionWrapper current = gettingArmies.poll(); // poll is the same as pop
			 
			int armiesAdding = 0;
			if (giveByNeed){
				armiesAdding = (int) Math.round(armiesPerNeed*current.need); 
				if (armiesAdding > numArmies){
					armiesAdding = numArmies;
				}
			} else {
				armiesAdding = armiesPerRegion;
			}
			armiesReport += armiesAdding + ",";
			placeArmiesMoves.add(new PlaceArmiesMove(myName, current.region, armiesAdding));  
			numArmies -= armiesAdding; 
			i++;
		}

		GUI.makeAlert("totalEvaluatedArmyNeed: " + totalEvaluatedArmyNeed + "\narmiesPerNeed: " + armiesPerNeed + "\narmiesPerRegion: " + armiesPerRegion + "\noriginalNumArmies: " + originalNumArmies + "\ngiveByNeed: " + giveByNeed + "\nnumber regions to give to: " + gettingArmies.size() + "\n\n" + armiesReport);
		return placeArmiesMoves;
	}
	
	 
	private boolean isBorder(Region region, String myName){

			LinkedList<Region> neighbors = region.getNeighbors();

			/*int numNeighborsChecked = 0;
			while(numNeighborsChecked < neighbors.size()){
				
				if(!neighbors.get(numNeighborsChecked).ownedByPlayer(myName)){

					// add it to list then exit while loop
					return true;
				}
				numNeighborsChecked++;
			}*/
			
			for(Region neighbor : neighbors){ 
				
				if(!neighbor.ownedByPlayer(myName)){

					// add it to list then exit while loop
					return true;
				} 
			}

			return false;
		
			//return getOpponentsSurrounding(region, myName).size() > 0;
	}
	
	/**
	 * Returns a list of all the countries surrounding region that are not controlled by us. 
	 * @param region the region to retrieve surrounding regions from
	 * @param myName our player name
	 * @return the list of opponents/neutral countries surrounding region. If region is non-border, it will be empty.
	 */
	private LinkedList<Region> getOpponentsSurrounding(Region region, String myName){

		LinkedList<Region> neighbors = region.getNeighbors();
		LinkedList<Region> surrounding = new LinkedList<Region>(); 

		/*int numNeighborsChecked = 0;
		while(numNeighborsChecked < neighbors.size()){
			
			if(!neighbors.get(numNeighborsChecked).ownedByPlayer(myName)){
				surrounding.add(neighbors.get(numNeighborsChecked));
			}
			numNeighborsChecked++;
		}*/
		
		for(Region neighbor : neighbors){
			
			if(!neighbor.ownedByPlayer(myName)){
				surrounding.add(neighbor); 
			} 
		}

		return surrounding;
	}

	/**
	 * Calculate the amount of need region has for additional armies. 
	 * @param region the region to calculate need for. 
	 * @param myName our player name. 
	 * @return the need of region. 
	 */
	private int evaluateNeedArmies(Region region, String myName){ 
		LinkedList<Region> surrounding = getOpponentsSurrounding(region, myName);
		
		// region is not a border state
		if (surrounding.size() == 0){
			return 0;
		}
		
		int need = 1;
		int numNeutral = 0;
		int numOpponent = 0;
		for (Region other : surrounding){ 
			if (!other.getPlayerName().equals("neutral")){
				numOpponent++;
				need += other.getArmies();
			} else {
				numNeutral++;
			}
		}
		
		//need += numNeutral/4;
		
		//GUI.makeAlert("need of armies for " + region.getId() + ": " + need);
		return need; 
	}
	
	/**
	 * Starting from all visible regions, return a queue of our regions that have a need for armies. 
	 * Queue will not include interior (non-border) regions. 
	 * @param myName our player name
	 * @param visible the visible regions
	 * @return the queue of regions in need of armies  
	 */
	private PriorityQueue<RegionWrapper> getRegionsNeedingArmies(String myName, LinkedList<Region> visible){
		PriorityQueue<RegionWrapper> needArmies = new PriorityQueue<RegionWrapper>();
		//PriorityQueue<RegionWrapper> doNotNeedArmies = new PriorityQueue<RegionWrapper>();
		totalEvaluatedArmyNeed = 0; 
		
		for(Region current : visible){
			if (current.getPlayerName().equals(myName)){
				int need = evaluateNeedArmies(current, myName);
				totalEvaluatedArmyNeed += need;
				RegionWrapper wrapper = new RegionWrapper(current, need);
				if (need > 0){
					needArmies.add(wrapper);
				} /*else if (needArmies.size() == 0){
					doNotNeedArmies.add(wrapper);
				}*/
			}
		}
		
		/*if (needArmies.size() == 0){
			return doNotNeedArmies;
		} */
		//GUI.makeAlert("number of regions in need of armies: " + needArmies.size());
		return needArmies;
		
	}
	

	
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		
		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
		
				while(!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand*possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);

					
					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 3) //do an attack
					{
						if(toRegion.getPlayerName().equals("neutral")){
							attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, 4));
						}else{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, (int)(fromRegion.getArmies()*0.65)));
				      	}
						break;
					}
					else if(toRegion.getPlayerName().equals(myName) && isBorder(toRegion, myName) && fromRegion.getArmies() > 1) //do a transfer
					{
						if(!isBorder(fromRegion, myName)){
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, fromRegion.getArmies()-1));
					}
						break;
					 
					}
					else
						possibleToRegions.remove(toRegion);
				}

			}
		}
		
		if (attackTransferMoves.size() > 0){
			GUI.makeAlert("number of attacks/transfers: " + attackTransferMoves.size());
		}
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

	
	/**
	 * Wrap a region with its calculated need so that it can be added to the priority queue. 
	 *
	 */
	private class RegionWrapper implements Comparable<RegionWrapper>{
		protected Region region;
		protected int need;
		
		public RegionWrapper(Region region, int need){
			this.region = region;
			this.need = need;
		}
		
		
		
		
		/*@Override
		public int compareTo(Object o) {
			if (o == null || getClass() != o.getClass()){
				return -1;
			} 
			
			RegionWrapper other = (RegionWrapper) o; 
			return this.need - other.need;
		}*/
		
		@Override
		public int compareTo(RegionWrapper other) {
			if (other == null){ 
				return 1;
			} 

			return other.need - this.need;
		}
		
	}
}
