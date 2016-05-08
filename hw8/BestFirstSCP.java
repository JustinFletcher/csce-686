/**
 * --------------------------------------------------------------------------
 * Classification: UNCLASSIFIED
 * --------------------------------------------------------------------------
 *
 * Class: BestFirstSCP
 * Program: Best First SCP program
 *
 * DESCRIPTION: This program solves the set-covering problem and the
 *   set-partitioning problem using a A* or best first search

         --*********************************************************--
         --**      copyright (C)   2016                           **--
         --**                                                     **--
         --**      Permission to use, copy and distribute this    **--
         --**      software for educational purposes without      **--
         --**      fee is hereby granted provided that the        **--
         --**      copyright notice and this permission notice    **--
         --**      appear on all copies. Permission to modify     **--
         --**      the software is granted, but not the right     **--
         --**      to distribute the modified code. All           **--
         --**      modifications are to be distributed as         **--
         --**      changes to released versions by AFIT. Use      **--
         --**      of program should be explicity referenced.     **--
         --**                                                     **--
         --********************************************************
TITLE: SCP A* program
*/
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class BestFirstSCP extends SetAlpha
{

  ArrayList<SetNode> ogPriorityQueue = new ArrayList<SetNode>();
  HashMap<String, Integer> ogVisitedMap = null;


  public BestFirstSCP(String[] args)
  {

    try
    {
      File temp = new File(args[0]);
      parseFile(temp);

      doBestFirstSearch();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  } // BestFirstSCP


  /**
   * This function follows the general outline of the Best First Search
   * It is actually an implementation of the A* algorithm, since it
   * uses the heuristic and the accumulated cost for f(x).
   * 
1  A* or Best-First-Search( m )
2  Insert( m.StartNode )
3  Until PriorityQueue is empty
4      c <- PriorityQueue.DeleteMin
5      If c is the goal
6          Exit
7      Else
8          Foreach neighbor n of c
9              If n "Unvisited"
10                  Mark n "Visited"                    
11                  Insert( n )
12          Mark c "Examined"                    
13  End procedure*/

  public void doBestFirstSearch()
  {
    ArrayList<Set> bestCover = null;
    int z = Integer.MAX_VALUE;
    
    ogVisitedMap = new HashMap<String, Integer>();
    
    // **** Initialization ****
    // Initialize the A* Algorithm
    
    ogPriorityQueue = new ArrayList<SetNode>();
    // Construct the initial candidate set array
    SetNode initialSet = new SetNode();
    initialSet.ogUSet.addAll(ogSetArray);
    initialSet.setZ();
    initialSet.sortUSet();
    initialSet.setEFromSSet();

    ogPriorityQueue.add(initialSet);
    
    while (ogPriorityQueue.size() > 0)
    {
      // **** Selection ****
      // Select from next state candidate. In this instance, we will always select the 
      // first element, because that element is the cheapest (estimated) in the frontier.
      
      SetNode c = ogPriorityQueue.get(0);
      ogPriorityQueue.remove(0);

      // **** Solution ****
      // If the set is covered and it is less than our current best solution,
      // We will set the current partial solution to the current best solution, bestCover.
      if ((c.eCoversR()) && (c.ogCost < z))
      {
        z = c.ogCost;
        bestCover = c.ogSSet;
      }
      
      // **** Next state generator ****
      // Select a node from the list of candidate solutions. Add that node to the solution
      // list, ogSSet, set all parameters (cost, Z, etc), and then add it in sorted order
      // to the candidate list, ogPriorityQueue.
      for (int i = 0; i < c.ogUSet.size(); i++)
      {
        // Get candidate set
        Set m = c.ogUSet.get(i);
        
        // Create new tree node for candidate set. The current solution set will be
        // augmented by the candidate set m.
        SetNode newSet = new SetNode();
        newSet.ogSSet.addAll(c.ogSSet);
        newSet.ogSSet.add(m);
        for (int j = 0; j < c.ogUSet.size(); j++)
        {
          if (c.ogUSet.get(j) != m)
          {
            newSet.ogUSet.add(c.ogUSet.get(j));
          }
        }
        
        // Update cost and z parameters for the new tree node
        newSet.sortUSet();
        newSet.setCost();
        newSet.setZ();
        newSet.setEFromSSet();
        
        // **** Heuristic 1 ****
        // If the current tree node already costs more than the current best solution,
        // we can abandon the current tree branch.
        // **** Heuristic 2 ****
        // We will check to make sure that this list of sets has not already been
        // visited in the past. The visited map contains a sorted string that identifies
        // the current solution set. If the solution set {3, 4, 5} has been encountered,
        // we will not consider {4, 5, 3}, or some other order of the soltuion set. All
        // of these potential solutions will have the same hash string "3,4,5".
        if ((newSet.ogCost < z) && (!ogVisitedMap.containsKey(newSet.ogID)))
        {
          // **** Feasibility *****
          // **** Next State Generator ****
          // Add the candidate tree node to the priority list in sorted order. Only
          // feasible solutions are added to the priority queue, assuming they make it
          // past the heuristics in the previous section.
          ogPriorityQueue.add(newSet);
          ogVisitedMap.put(newSet.ogID, 0);
          Collections.sort(ogPriorityQueue, new SetNode());
        }
      }
      
    }
    
    // Print solution
    System.out.println("z: " + z);
    System.out.print("SSet: ");
    for (Set r : bestCover)
    {
      System.out.print(r.ogID + " ");
    }
    System.out.println("\n");

    
  } // doBestFirstSearch


  class SetNode implements Comparator
  {
    public ArrayList<Set> ogSSet = new ArrayList<Set>();
    public ArrayList<Set> ogUSet = new ArrayList<Set>();
    public int ogZ = Integer.MAX_VALUE;
    public int[] ogE = null;
    public int ogCost = 0;
    SetNode ogParent;
    String ogID = "";
    
    /**
     * Sets the coverage bookkeeping variable E, which is used to determine
     * if the current set covers all of the elements.
     */
    public void setEFromSSet()
    {
      ogE = new int[ogElements.size()];
      for (int i = 0; i < ogSSet.size(); i++)
      {
        Set m = ogSSet.get(i);
        addCoveredElementsFromSet(m, ogE, true);
      }
    } // setEFromSSet
    

    public void addCoveredElementsFromSet(Set m, int[] e, boolean add)
    {
      for (int i = 0; i < m.ogSet.length; i++)
      {
        if (m.ogSet[i])
        {
          e[i] = e[i] + (add ? 1 : -1);
        }
      }
    } // addCoveredElementsFromSet


    /**
     * SetZ
     * 
     * Calculated the f(X) function of the A* algorithm
     * g(x) = cost of solution set, ogSSet
     * h(x) = min(cost(x)), for each x in ogUSet
     *     - The heristic function h(x), selects the minimum
     *       cost set from the set of unselected sets.
     *       We know that the cost to the solution set must
     *       be at least the cost of the minimum cost set
     *       in the unselected set of sets. This way, the
     *       heuristic is admissible and does not overestimate
     *       the cost to the goal.
     * 
     */
    public void setZ()
    {
      int z = Integer.MAX_VALUE / 2;
      for (Set m : ogUSet)
      {
        if (m.ogCost < z)
        {
          z = m.ogCost;
        }
      }
      ogZ = ogZ + ogCost;
      ogZ = z;
    } // setZ


    /**
     * Calculates cost of the current tree node, which is the summation
     * of all the cost fof the sets in the solution set, ogSSet.
     */
    public void setCost()
    {
      ogCost = 0;
      ogID = "";
      Collections.sort(ogSSet, new Set(true));

      for (int i = 0; i < ogSSet.size(); i++)
      {
        Set m = ogSSet.get(i);
        if (i != 0)
        {
          ogID += ",";
        }
        ogID += m.ogID;
        ogCost += m.ogCost;
      }
    } // setZ


    public void sortUSet()
    {
      Collections.sort(ogUSet, new Set());
    } // sortUSet


    /**
     * Determines if E covered R, or all covered
     * 
     * @return true if all items have been covered, false if not
     */
    public boolean eCoversR()
    {
      boolean result = true;

      for (int i = 0; i < ogElements.size(); i++)
      {
        if (ogE[ogElements.get(i).ogName - 1] == 0)
        {
          result = false;
          break;
        }
      }

      return result;
    } // eCoversR


    @Override
    public int compare(Object o1, Object o2)
    {
      int result = 0;
      SetNode a = (SetNode) o1;
      SetNode b = (SetNode) o2;

      if (a.ogZ < b.ogZ)
      {
        result = -1;
      }
      else if (a.ogZ > b.ogZ)
      {
        result = 1;
      }

      return result;
    }

  } // SetNode
  
  public static void main(String[] args)
  {
    BestFirstSCP m = new BestFirstSCP(args);
  }

} // BestFirstSCP
