import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SCPAlpha
{
  
  public static final String SCP_H1 = "-H1";
  public static final String SCP_H2 = "-H2";
  public static final String SCP_H3 = "-H3";
  public static final String SCP_OUT = "-out";
  
  ArrayList<Element> ogElements = null;
  ArrayList<Set> ogSetArray = null;
  ArrayList<Block> ogBlockArray = null;
  ArrayList<Integer> ogCB = null;
  ArrayList<LItem> ogL = null;
  ArrayList<Set> ogSingleSets = null;
  int ogSetID = 1;
  int[] ogE = null;
  ArrayList<Set> ogB = null;
  ArrayList<Set> ogBHat = null;
  int ogZ = 0;
  int ogZHat = 0;
  boolean ogDone = false;
  boolean ogUseH1 = true;
  boolean ogUseH2 = true;
  boolean ogUseH3 = true;
  boolean ogUseOut = false;

  int ogP = -1;

  public SCPAlpha(String[] args)
  {
    try
    {
      for (String m: args)
      {
        if (m.toUpperCase().equals(SCP_H1))
        {
          ogUseH1 = false;
        }
        else if (m.toUpperCase().equals(SCP_H2))
        {
          ogUseH2 = false;
        }
        else if (m.toUpperCase().equals(SCP_H3))
        {
          ogUseH3 = false;
        }
        else if (m.toLowerCase().equals(SCP_OUT))
        {
          ogUseOut = true;
        }
      }
      File temp = new File(args[0]);
      parseFile(temp);
            
      if (searchPossible())
      {
        // Initialization Phase
        // Heuristic 2
        // The original algorithm sorts the sets according to cost, but ties are sorted by lexicographical order. 
        // Heutistic two sorts ties using number of covered items per set. Sets with greater coverage are tried
        // first, because they eliminate a larger portion of the search space.
        
        // *** Set of candidates *** generation
        // Generates the tree in the form of a tableau.
        setupTableau(ogUseH2);
        
        if (ogUseOut)
        {
          System.out.println(getTableauString());
        }
        
        searchSCP();

        System.out.println("Best Solution:");
        printResultState();
        
      }
      else
      {
        System.out.println("Search Not Possible");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  } // SCPAlpha
  
  
  public void searchSCP()
  {
    // Initialization
    ogZ = 0;
    ogZHat = Integer.MAX_VALUE;//infinity
    
    // ogB is the partial solution Do
    ogB = new ArrayList<Set>();
    
    // ogBHat is the current best solution
    ogBHat = new ArrayList<Set>();
    ogBHat.clear();
    
    // Initialize the E variable to keep track of covered rows.
    ogE = new int[ogElements.size()];
    
    // Heuristic 3
    // If there are rows that are only covered by one set, then that set must be included.
    // Add the cover to E, and this will eliminate the block from consideration
    if ((ogUseH3) && (ogSingleSets != null) && (ogSingleSets.size() > 0))
    {
      for (Set m: ogSingleSets)
      {
        addCoveredElementsFromSet(m, ogE, true);
      }
    }
    
    // Initialize an array of current blocks. This array is used as a queue to keep
    // track of the current block and the history of selected blocks. This is also
    // an aid to backtracking.
    ogCB = new ArrayList<Integer>();
    
    // Heuristic 1
    // Initialize L Array. This will keep track of all intermediate solutions and the
    // associated values. If a cover is encountered that is within a previous cover, but
    // has a greater cost, then there is no reason to consider this branch.
    ogL = new ArrayList<LItem>();
    
    ogDone = false;

    // Check to make sure that, after adding sets from heruistic 3, we don't already have a solution.
    if (eCoversR())
    {
      ogBHat.addAll(ogSingleSets);
      ogZHat = 0;
      for (Set m: ogSingleSets)
      {
        ogZHat = ogZHat + m.ogCost;
      }
    }
    else
    {
      // Set up initial solution. Add first block to block queue.
      if (!ogUseH3)
      {
        ogCB.add(new Integer(0));
      }
      else
      {
        // Get first selectable block
        int j = 0;
        while ((j < ogBlockArray.size()) && (ogBlockArray.get(j).ogSets.size() <= 1))
        {
          j++;
        }
        ogCB.add(new Integer(j));
      }
    
      Set currentSet = ogBlockArray.get(currentBlock()).ogSets.get(ogBlockArray.get(currentBlock()).ogCurrentPos);
      addCoveredElementsFromSet(currentSet, ogE, true);
      ogB.add(currentSet);
      ogZ = ogZ + currentSet.ogCost;
      
      // *** Next state/Feasibility *** Finds the next state from the tableau. The feasibility function
      // is implied, as only valid solutions are considered.
      addMin(getMin(currentBlock()));
      
      // Check to make sure all sets have not been covered.
      if (currentBlock() == ogBlockArray.size())
      {
        ogDone = true;
      }
      
      while (!ogDone)
      {
        // Maintain partial solution
        currentSet = ogBlockArray.get(currentBlock()).ogSets.get(ogBlockArray.get(currentBlock()).ogCurrentPos);
        ogB.add(currentSet);
        ogZ = ogZ + currentSet.ogCost;
        addCoveredElementsFromSet(currentSet, ogE, true);
        //System.out.println("Test1: " + currentSet.ogID + "/" + currentSet.ogCost + ", " + 
        //        (ogZ + currentSet.ogCost) + ", " + ogZHat);
        //printCB();
        //printCP();
        //printState();
        
        // Add current Do to L
        if(ogUseH1)
        {
          ogL.add(new LItem(ogE, ogZ));
        }
        
        // Heuristic 1
        // If there are covered sets that are greater than the current cover, 
        // but have less cost, then there is no reason to search this branch.
        if ((ogUseH1) && (solutionInL(ogE, ogZ)))
        {
          //printState();
          step4();
        }
        else if (ogZ < ogZHat)
        {
          // *** Solution ***
          // Determines if a partial solution is a valid solution to the SCP.
          step5();
        }
        else
        {
          // *** Backtracking *** step
          // If we encounter a node that is higher in cost than the current
          // solution, then exploring this branch would
          // not be fruitful.
          step4();
        }
        // *** Next state/Feasibility *** Finds the next state from the tableau. The feasibility function
        // is implied, as only valid solutions are considered.
        addMin(getMin(currentBlock()));
      }
      
      // Add all sets from heuristic 3 and add to ZHat
      if ((ogSingleSets != null) && (ogSingleSets.size() > 0))
      {
        ogBHat.addAll(ogSingleSets);
        for (Set m: ogSingleSets)
        {
          ogZHat = ogZHat + m.ogCost;
        }
      }
    }
    //printState();
    
  } // searchSCP
  
   /**  *** Backtrack *** Step */
  public void step4()
  {
    if (ogB.isEmpty())
    {
      ogDone = true;
    }
    else
    {
      // For current block, increase position of current set with the block
      Block currentBlock = ogBlockArray.get(currentBlock());
      currentBlock.ogCurrentPos++;
      // Remove the current cover from our cover array
      addCoveredElementsFromSet(ogB.get(ogB.size() - 1), ogE, false);
      // Remove the most recent Z value
      ogZ = ogZ - ogB.get(ogB.size() - 1).ogCost;
      // Go back to previous block using ogCB queue
      removeLastBlock();
      // Remove latest solution from partial solution B
      removeLastB();
      //printState();
      // If we are at the end of the first block, then we are finished.
      if ((currentBlock.ogCurrentPos >= currentBlock.ogSets.size()) && (currentBlock.ogID == 0))
      {
        ogDone = true;
      }
      else if (currentBlock.ogCurrentPos >= currentBlock.ogSets.size())
      {
        // We have exhausted the block, so backtrack. Reset set position for the current block
        currentBlock.ogCurrentPos = 0;
        // Since we cannot continue in this block, we must backtrack
        step4();
      }
    }
  } // step4
  
  /** *** Solution *** step */
  public void step5()
  {
    // if all sets are covered, then we have a new best solution
    if (eCoversR())
    {
      // Set BHat latest solution
      ogBHat.clear();
      ogBHat.addAll(ogB);
      // Set Z to best solution
      ogZHat = ogZ;
      if (ogUseOut)
      {
        System.out.println("Paritial Solution:");
        printResultState();
      }
      // Backtrack from current position to continue exploring the blocks
      step4();
    }
    //printState();
  } // step5
  
  
  /**
   * Gets the nexy block from the first uncovered element in E
   * 
   * @param currentBlock - Current block position
   * @return
   */
  public int getMin(int currentBlock)
  {
    int result = -1;
    // Get next block from minimum non-covered item in E
    int i = 0;
    while ((i < ogE.length) && (result == -1))
    {
      if (ogE[i] == 0)
      {
        result = i;
      }
      else
      {
        i++;
      }
    }
    if (result == -1)
    {
      result = ogE.length;
    }
    return result;
  } // getMin
  
  
  /** Adds latest minimum block to queue of blocks. */
  public void addMin(int min)
  {
    if (ogCB.get(ogCB.size() - 1) != min)
    {
      ogCB.add(min);
    }
  } // addMin
  
  
  /**
   * Heuristic 1
   * 
   * Determines if some partial solution E exists within the the already visited solutions.
   * If so, and it has a lower Z value, then we should stop exploring this branch as a
   * better solution has already been found.
   * 
   * @param e - Current partial solution E
   * @param z - Current partial solution Z
   * @return - true if we should backtrack, false if we should not
   */
  public boolean solutionInL(int[] e, int z)
  {
    boolean result = false;
    
    int i = 0;
    while ((i < ogL.size()) && (!result))
    {
      if ((ogL.get(i).isWithin(e)) && (z > ogL.get(i).ogCost))
      {
        result = true;
      }
      i++;
    }
    
    return result;
  } // solutionInL
  

  /**
   * *** Initialization ***
   * 
   * Heuristic 2
   *   The original algorithm sorts the sets according to cost, but ties are sorted by lexicographical order. 
   *   Heuristic two sorts ties using number of covered items per set. Sets with greater coverage are tried
   *   first, because they eliminate a larger portion of the search space.
   * 
   * Sets up the Tableau
   * 
   * @param useH2 - Determines if Heuristic 2 should be used.
   */
  public void setupTableau(boolean useH2)
  {
    ogBlockArray = new ArrayList<Block>();
    for (int i = 0; i < ogElements.size(); i++)
    {
      Block b = new Block(i);
      ArrayList<Set> sets = getAllCoveringSets(ogElements.get(i).ogName);
      b.ogSets = sets;
      
      // Heuristic 2
      // The original algorithm sorts the sets according to cost, but ties are sorted by lexicographical order. 
      // Heuristic two sorts ties using number of covered items per set. Sets with greater coverage are tried
      // first, because they eliminate a larger portion of the search space.
      b.sortSets(useH2);
      ogBlockArray.add(b);
    }
  } // setupTableau

  /**
   * Gets all covering sets for the some item name
   * 
   * @param name - name of item
   * @return - All sets that cover name
   */
  public ArrayList<Set> getAllCoveringSets(int name)
  {
    ArrayList<Set> result = new ArrayList<Set>();
    
    for (Set m: ogSetArray)
    {
      if (m.ogSet[name - 1])
      {
        result.add(m);
      }
    }
    
    return result;
  } // getAllCoveringSets
  
  
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
  
  
  /**
   * *** Next State Generator ***
   * Adds the current set to E
   * 
   * E is a an integer array. If multiple sets cover a single item,
   * its coverage will be reflected as a value in E.
   * 
   * @param m Set to add to E
   * @param e Current E
   * @param add - True if the set is added, or false if the set is subtracted
   */
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
  
  
  public String getTableauString()
  {
    String result = "";
    
    System.out.print(formatStringLength(" ", 5, " ", false) + "|");
    for (Block b: ogBlockArray)
    {
      for (Set m: b.ogSets)
      {
        System.out.print(" " + formatStringLength((m.ogID) + "", 2, " ", false));
      }
      System.out.print("|");
    }
    System.out.println();

    for (int i = 0; i < ogElements.size(); i++)
    {
      System.out.print(formatStringLength(ogElements.get(i).ogName + "", 5, " ", false) + "|");
      for (Block b: ogBlockArray)
      {
        for (Set m: b.ogSets)
        {
          System.out.print(" " + print(m.ogSet[i]));
        }
        System.out.print("|");
      }
      System.out.println();
    }
    
    System.out.print(formatStringLength(" ", 5, " ", false) + "|");
    for (Block b: ogBlockArray)
    {
      for (Set m: b.ogSets)
      {
        System.out.print(" " + formatStringLength((m.ogCost) + "", 2, " ", false));
      }
      System.out.print("|");
    }
    System.out.println();
    
    return result;
  } // getTableauString
  
  
  public static String print(boolean b)
  {
    return formatStringLength((b ? "1" : "0"), 2, " ", false);
  }
  
  
  public void printState()
  {
    System.out.print("E: ");
    for (int r: ogE)
    {
      System.out.print(r+ " ");
    }
    System.out.println();
    System.out.print("B: ");
    for (Set r: ogB)
    {
      System.out.print(r.ogID + " ");
    }
    System.out.println("");
    printResultState();
    System.out.println("\n");

  } // printState
  
  
  public void printResultState()
  {
    System.out.println("ZHat: " + ogZHat);
    System.out.print("BHat: ");
    for (Set r: ogBHat)
    {
      System.out.print(r.ogID + " ");
    }
    System.out.println("\n");
  } // printState
  
  
  public void printCP()
  {
    for (int i = 0; i < ogBlockArray.size(); i++)
    {
      System.out.print(i + ": " + ogBlockArray.get(i).ogCurrentPos + ", ");
    }
    System.out.println();
  }
  
  
  public int currentBlock()
  {
    if (ogCB.size() == 0)
    {
      ogCB.add(new Integer(0));
    }
    return ogCB.get(ogCB.size() - 1);
  } // currentBlock
  
  
  public void removeLastBlock()
  {
    ogCB.remove(ogCB.size() - 1);
  } // removeLastB
  
  
  public void removeLastB()
  {
    ogB.remove(ogB.size() - 1);
  } // removeLastB
  
  
  public void printCB()
  {
    System.out.print("CB: ");
    for (int m: ogCB)
    {
      System.out.print(m + " ");
    }
    System.out.println();
  } // printCB


  public void parseFile(File theFile) throws Exception
  {
    ogElements = new ArrayList<Element>();
    ogSetArray = new ArrayList<Set>();
    String inFile = loadStringFromFile(theFile, true);
    // Get elements
    int pos1 = inFile.indexOf("{");
    int pos2 = inFile.indexOf("}");
    String temp = inFile.substring(pos1 + 1, pos2);
    String[] temp2 = temp.split(" ");
    int highest = 0;
    for (String temp3 : temp2)
    {
      if (!temp3.trim().equals(""))
      {
        ogElements.add(new Element(Integer.parseInt(temp3.trim())));
        int r = Integer.parseInt(temp3.trim());
        if (r > highest)
        {
          highest = r;
        }
      }
    }
    // Get sets
    pos1 = inFile.indexOf("{", pos2);
    pos2 = inFile.lastIndexOf("}");
    String sets = inFile.substring(pos1 + 1, pos2);
    //System.out.println("Test1: " + sets);
    String[] set = sets.split("\n");
    for (String setLine : set)
    {
      if ((!setLine.trim().equals("")) && (!setLine.equals("\n")))
      {
        //System.out.println("Test3: " + setLine);
        pos1 = setLine.indexOf("{");
        pos2 = setLine.indexOf("}");
        temp = setLine.substring(pos1 + 1, pos2);
        temp2 = temp.split(" ");
        Set newSet = new Set(highest);
        for (String temp3 : temp2)
        {
          if (!temp3.trim().equals(""))
          {
            int pos = Integer.parseInt(temp3.trim());
            newSet.ogSet[pos - 1] = true;
          }
        }
        pos1 = setLine.indexOf(",");
        pos2 = pos1;
        while ((setLine.charAt(pos2) != ')') && (pos2 < setLine.length()))
        {
          pos2++;
        }
        temp = setLine.substring(pos1 + 1, pos2);
        newSet.ogCost = Integer.parseInt(temp.trim());
        newSet.ogID = ogSetID++;
        ogSetArray.add(newSet);
      }
    }
  } // parseFile
  
  
  /**
   * Heuristic 3
   * Collects all sets that are the only cover for a single item. These
   * Must be included in the final solution set.
   * 
   * Determines if all sets can be covered. If not, then no solution is possible.
   * 
   * @return - true if a solution is possible, or false if it is not.
   */
  public boolean searchPossible()
  {
    boolean result = true;
    ogSingleSets = new ArrayList();
    for (int i = 0; i < ogSetArray.size(); i++)
    {
      for (int j = 0; j < ogSetArray.get(i).ogSet.length; j++)
      {
        if (ogSetArray.get(i).ogSet[j])
        {
          addToElement(j + 1, ogSetArray.get(i));
        }
      }
    }
    int i = 0;
    while (i < ogElements.size())
    {
      if (ogElements.get(i).ogCoveringSets == 0)
      {
        result = false;
      }
      else if ((ogElements.get(i).ogCoveringSets == 1) && (ogUseH3))
      {
        // These sets must be included in the final result
        ogSingleSets.add(ogElements.get(i).ogCoveringSetArray.get(0));
      }
      
      i++;
    }
    
    if (ogUseH3)
    {
      if (ogSingleSets.size() != 0)
      {
        // Remove all single sets from the list of sets
        for (Set singleSet: ogSingleSets)
        {
          int j = 0;
          boolean found = false;
          while ((j < ogSetArray.size()) && (!found))
          {
            if (singleSet == ogSetArray.get(j))
            {
              ogSetArray.remove(j);
              found = true;
            }
            j++;
          }
        }
      }
    }
    
    return result;
  } // searchPossible
  
  
  private void addToElement(int name, Set m)
  {
    int i = 0;
    boolean found = false;
    while ((i < ogElements.size()) && (!found))
    {
      if (ogElements.get(i).ogName == name)
      {
        ogElements.get(i).ogCoveringSets++;
        ogElements.get(i).ogCoveringSetArray.add(m);
        found = true;
      }
      i++;
    }
    
  } // addToElement
  


  /** Loads text from file.
   * 
   * @param dirName
   *          The directory of the file the text is to be written to.
   * @param fileName
   *          The name of the file.
   * @param text
   *          The text being written to a file. */
  public static String loadStringFromFile(File theFile, boolean addNewLine)
  {
    StringBuffer result = null;
    try
    {
      if ((theFile == null) || (!theFile.exists()) || (!theFile.isFile()))
      {
        return null;
      }
      BufferedReader b = new BufferedReader(new FileReader(theFile));
      String temp = b.readLine();
      while (temp != null)
      {
        if (result == null)
        {
          result = new StringBuffer();
        }
        if (addNewLine)
        {
          result.append(temp + "\n");
        }
        else
        {
          result.append(temp);
        }
        temp = b.readLine();
      }
      b.close();
    } catch (IOException io)
    {
      System.out.println("Error reading the file " + io);
    }
    return result.toString();
  } // loadStringFromFile

  
  /** Formats a String to a length, with prefix specified as a parameter
   * 
   * @param m
   *          - String to format
   * @param length
   *          - Lenght of String
   * @param theBuffer
   *          - String to append to front or back of the String
   * @return String - Formatted String */
  public static String formatStringLength(String m, int length, String theBuffer, boolean after)
  {
    StringBuffer result = new StringBuffer();

    if (m.length() == length)
    {
      result.append(m);
    }
    else if ((m.length() > length) && (length > 0))
    {
      if (after)
      {
        result.append(m.substring(0, length));
      }
      else
      {
        result.append(m.substring(m.length() - length, m.length()));
      }
    }
    else
    {
      if (after)
      {
        result.append(m);
      }
      for (int i = 0; i < (length - m.length()); i++)
      {
        result.append(theBuffer);
      }
      if (!after)
      {
        result.append(m);
      }
    }
    return result.toString();
  } // formatStringLength

  public static void main(String[] args)
  {
    SCPAlpha m = new SCPAlpha(args);
  }
}

class LItem
{
  int[] ogElements = null; 
  int ogCost = 0;
  
  public LItem(int[] elementsSource, int cost)
  {
    ogElements = new int[elementsSource.length];
    for (int i = 0; i < ogElements.length; i++)
    {
      ogElements[i] = elementsSource[i];
    }
    ogCost = cost;
  }
  
  public boolean isWithin(int[] setArray)
  {
    boolean result = true;
    
    for (int i = 0; i < ogElements.length; i++)
    {
      if ((ogElements[i] == 0) && (setArray[i] > 0))
      {
        result = false;
      }
    }
    
    return result;
  } // isWithin
  
  
  public String toString()
  {
    String result = "";
    
    for (int i = 0; i < ogElements.length; i++)
    {
      System.out.print(ogElements[i] + " ");
    }    
    System.out.println(", Cost: " + ogCost);
    return result;
  }
  
} // LItem
