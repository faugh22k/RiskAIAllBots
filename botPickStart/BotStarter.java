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

package bot3;

import java.util.ArrayList;
import java.util.LinkedList;

import main.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	@Override
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
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


	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{

		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();

		// number of armies left
		int numArmies = state.getStartingArmies();

		// find all regions held by player that is bordering other regions
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		LinkedList<Region> borderingRegions = new LinkedList<Region>();

		// current region being checked
		Region current;

		for(int i = 0; i < visibleRegions.size(); i++){

			if(visibleRegions.get(i).ownedByPlayer(myName)){
				
				current = visibleRegions.get(i);

				if(isBorder(current, myName)){
	                borderingRegions.add(current);
				}

		}
		}

        int numToPlace = (int)Math.ceil((double)numArmies/borderingRegions.size());
     	
        int i = 0;
		while(numArmies > 0 && borderingRegions.size() > 0){
			placeArmiesMoves.add(new PlaceArmiesMove(myName, borderingRegions.get(i), numToPlace));
			numArmies -= numToPlace;
			i++;
		}

		return placeArmiesMoves;
	}

	private boolean isBorder(Region region, String myName){

			LinkedList<Region> neighbors = region.getNeighbors();

			int numNeighborsChecked = 0;
			while(numNeighborsChecked < neighbors.size()){
				
				if(!neighbors.get(numNeighborsChecked).ownedByPlayer(myName)){

					// add it to list then exit while loop
					return true;
				}
				numNeighborsChecked++;
			}

			return false;
	}



	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
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
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
