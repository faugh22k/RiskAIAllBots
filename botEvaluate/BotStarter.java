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
import java.util.HashMap;
import java.util.NoSuchElementException;

//import view.GUI;

import main.Region;
import main.SuperRegion;
//import main.RunGame;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	private int totalEvaluatedArmyNeed = 0;
	private int round = 0;
	
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
		round++;
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();

		// number of armies left
		int numArmies = state.getStartingArmies();
		int originalNumArmies = state.getStartingArmies();

		// find all regions held by player that is bordering other regions
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions(); 

		//careful! System.out.println("\n\ngoing to get the regions needing armies!");
		//System.out.println("\n\ngoing to get the regions needing armies!");
		
		PriorityQueue<RegionWrapper> gettingArmies = getRegionsNeedingArmies(myName, visibleRegions);
		PriorityQueue<RegionWrapper> givenArmies = new PriorityQueue<RegionWrapper>();
 
		//careful! System.out.println("number needing armies: " + gettingArmies.size());
		//System.out.println("number needing armies: " + gettingArmies.size()); 
		
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
		String armiesTotal = ""; 
		int i = 0;
		// give armies to each region in need until we run out. 
		while(numArmies > 0 && i < 50){
			
			if (gettingArmies.size() == 0 && givenArmies.size() > 0){
				PriorityQueue<RegionWrapper> nowEmpty = gettingArmies;
				gettingArmies = givenArmies;
				givenArmies = nowEmpty;
			}
			
			RegionWrapper current = gettingArmies.poll(); // poll is the same as pop
			givenArmies.add(current);
			
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
			armiesTotal += (armiesAdding + current.region.getArmies()) + ",";
			placeArmiesMoves.add(new PlaceArmiesMove(myName, current.region, armiesAdding));  
			numArmies -= armiesAdding; 
			i++;
		}

		// GUI.makeAlert("round " + round + "\ntotalEvaluatedArmyNeed: " + totalEvaluatedArmyNeed + "\narmiesPerNeed: " + armiesPerNeed + "\narmiesPerRegion: " + armiesPerRegion + "\noriginalNumArmies: " + originalNumArmies + "\ngiveByNeed: " + giveByNeed + "\nnumber regions to give to: " + gettingArmies.size() + "\n\n" + armiesReport + "\n" + armiesTotal);
		// RunGame.addToLog("round " + round + "\ntotalEvaluatedArmyNeed: " + totalEvaluatedArmyNeed + "\narmiesPerNeed: " + armiesPerNeed + "\narmiesPerRegion: " + armiesPerRegion + "\noriginalNumArmies: " + originalNumArmies + "\ngiveByNeed: " + giveByNeed + "\nnumber regions to give to: " + gettingArmies.size() + "\n\n" + armiesReport + "\n" + armiesTotal);
		
		// System.out.println(""); 
		
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
	private int evaluateNeedArmies(Region region, String myName, LinkedList<Region> visibleRegions){ 
		
		LinkedList<Region> surrounding = getOpponentsSurrounding(region, myName);

		// also consider which continent it is in
		int[] occupied =  continentOccupied(visibleRegions, myName);
		
		// region is not a border state
		if (surrounding.size() == 0){
			return 0;
		}
		
		int need = (int)(10/occupied[region.getSuperRegion().getId()-1]);

		int numNeutral = 0;
		int numOpponent = 0;
		
		for (Region other : surrounding){ 

			if(occupied[other.getSuperRegion().getId()-1] == (other.getSuperRegion().getSubRegions().size()+1) ){
				need += other.getSuperRegion().getArmiesReward()*2;
			}

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

    // find the number of occupied countries in each super region
	private int[] continentOccupied(LinkedList<Region> visibleRegions, String myName){

		int[] occupied = new int[6];

		for(Region region : visibleRegions){

			// if it is owned by us
			if(region.getPlayerName().equals(myName)){
				occupied[region.getSuperRegion().getId()-1]++;
			}
		}

		return occupied; 
	}

    // evalutate neighbours for attack
	private PriorityQueue<RegionWrapper> evaluateAttackTargets(Region fromRegion, String myName, BotState state){

		// considers: percentage in fulfilling continent rewards, opponent > neutral
		
		// get surrounding regions
		LinkedList<Region> surrounding = getOpponentsSurrounding(fromRegion, myName);

		PriorityQueue<RegionWrapper> orderToAttack = new PriorityQueue<RegionWrapper>();

		double reward;

		int[] continentOccupied = continentOccupied(state.getVisibleMap().getRegions(), myName);

		for(Region region: surrounding){

			reward = 0;

			// get super region 
			SuperRegion superRegion = region.getSuperRegion();

			reward += ((double)continentOccupied[superRegion.getId()-1]/(double)superRegion.getSubRegions().size()) * superRegion.getArmiesReward();

			if(!region.getPlayerName().equals("neutral")){
				reward++;
			}

			orderToAttack.add(new RegionWrapper(region, (int)Math.ceil(reward)));
		}

		return orderToAttack;

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
				int need = evaluateNeedArmies(current, myName, visible);
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
		//RunGame.addToLog("number of regions in need of armies: " + needArmies.size());
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
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions(); 
		
		
		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //could do an attack
			{
				if(isBorder(fromRegion, myName)){
				PriorityQueue<RegionWrapper> possibleToRegions = evaluateAttackTargets(fromRegion, myName, state);
		
				while(!possibleToRegions.isEmpty())
				{
					Region toRegion = possibleToRegions.poll().region;
				
					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 4) //considers an attack
					{

						// if it has only one neighbor, move all out
						LinkedList<Region> surroundings = getOpponentsSurrounding(fromRegion, myName);
						if(surroundings.size() == 1){
							attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, (fromRegion.getArmies()-1)));
						}else{

					
							attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, (int)Math.max(toRegion.getArmies()*2, fromRegion.getArmies()*0.6)));
						
						}

				  		break;
					}
					
					 
					}
                 }
				
					// considers tranfer
				else if(fromRegion.getArmies() > 1) //do a transfer
				{
					// transfer to the state we own that is the closest to a border
					Region transferTarget= getTransferTarget(fromRegion, myName, visibleRegions);

					attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, transferTarget, fromRegion.getArmies()-1));
				    
					break;
				 
				}

				}

			}
		
		return attackTransferMoves;
	}

	private Region getTransferTarget(Region fromRegion, String myName, LinkedList<Region> visibleRegions){

		HashMap<Region, Region> parents = new HashMap<Region, Region>();

		Queue<Region> queue = new Queue<Region>(); 
		queue.enqueue(fromRegion);
		Region current;

		// do a bsf to find closest move to a border state
		while(!queue.empty()){

			current = queue.dequeue();

			// check if goal state is reached 
			if(isBorder(current, myName)){

				// back track to get the target transfer region
				while(!parents.get(current).equals(fromRegion)){
					current = parents.get(current);
				}
				return current;
			}

			// else explore unvisited neighors
			LinkedList<Region> neighors = current.getNeighbors();

			for(Region neighbor: neighors){

				if(neighbor.ownedByPlayer(myName)){

				if(!parents.containsKey(neighbor)){

					queue.enqueue(neighbor);
					parents.put(neighbor, current);
				}
			  }
			}
		}

		return fromRegion;
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

	private class Queue<E> {

		// head and tail of the queue
		private Node<E> _head;
		private Node<E> _tail;

		/**
		 * Constructor of the Queue class
		 * 
		 */
		public Queue() {
			// initially the queue is empty
			_head = null;
			_tail = null;
		}

		/**
		 * Add one item to the queue
		 * 
		 * @param item
		 *            item to add
		 */
		public void enqueue(E item) {

			Node<E> node = new Node<E>(item);

			// if queue is empty
			if (_head == null) {
				_head = node;
				_tail = node;
			}

			else {
				// add one node after tail
				_tail.setNext(node);

				// set the newly added node as tail
				_tail = node;
			}

		}

		/**
		 * Remove the first node from queue
		 * 
		 * @return data to be removed
		 */
		public E dequeue() {

			// if there is no element in the queue
			if (empty()) {
				throw new NoSuchElementException();
			}

			// store the removed element temporarily
			Node<E> tmp = _head;

			// set the second node as head
			_head = _head.getNext();

			// return data stored in the node removed
			return tmp.getData();
		}

		/**
		 * Check if the queue is empty
		 * 
		 * @return true if the queue is empty, false otherwise
		 */
		public boolean empty() {
			return _head == null;
		}

		/**
		 * Look, but not remove, the first element
		 * 
		 * @return data stored in the first node
		 */
		public E peek() {

			// if the queue is empty, return null
			if (empty()) {
				return null;
			}
			// else return data stored in the first node
			else {
				return _head.getData();
			}
		}

		// private class Node used to store data and make reference
		@SuppressWarnings("hiding")
		private class Node<E> {

			// data to store in the node
			private E _data;

			// next and previous items
			
			private Node<E> _next;

			/**
			 * Construcor of Node class
			 * 
			 * @param item
			 *            item to store
			 */
			public Node(E item) {
				_data = item;
			}

			/**
			 * Get data stored in this node
			 * 
			 * @return data stored
			 */
			public E getData() {
				return _data;
			}


			/**
			 * Get next node
			 * 
			 * @return next node
			 */
			public Node<E> getNext() {
				return _next;
			}

			/**
			 * Set the next node
			 * 
			 * @param next
			 *            the next node
			 */
			public void setNext(Node<E> next) {
				_next = next;
			}

		}
	   }
}
