/**
 * --------------------------------------------------------------------------
 * Classification: UNCLASSIFIED
 * --------------------------------------------------------------------------
 *
 * Class: Set
 * Program: SCP/SPP program
 *
 * DESCRIPTION: Implements a set
 */

import java.util.Comparator;

public class Set implements Comparator
{
  public boolean[] ogSet;
  public int ogCost;
  public int ogID = -1;
  public boolean ogSortByID = false;
  
  public Set()
  {
    
  } // Set
  
  public Set(boolean sortByID)
  {
    ogSortByID = sortByID;
  } // Set
  
  
  public Set(int size)
  {
    ogSet = new boolean[size];
  } // Set
  
  
  public String toString()
  {
    return ogID + "";
  }

  @Override
  public int compare(Object o1, Object o2)
  {
    int result = 0;
    Set a = (Set)o1;
    Set b = (Set)o2;
    
    if (ogSortByID)
    {
      if (a.ogID < b.ogID)
      {
        result = -1;
      }
      else if (a.ogID > b.ogID)
      {
        result = 1;
      }
    }
    else
    {
      if (a.ogCost < b.ogCost)
      {
        result = -1;
      }
      else if (a.ogCost > b.ogCost)
      {
        result = 1;
      }
    }
    
    return result;
  }
  
} // Set
