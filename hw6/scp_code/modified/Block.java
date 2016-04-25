import java.util.ArrayList;
import java.util.Comparator;

public class Block
{
  public ArrayList<Set> ogSets = new ArrayList<Set>();
  int ogCurrentPos = 0;
  int ogID;


  public Block(int theID)
  {
    ogID = theID;
  } // Block


  public void sortSets(boolean useH2)
  {
    MergeSort sorter = new MergeSort();
    if (useH2)
    {
      sorter.sort(ogSets, new SetSortCoverHandler());
    }
    sorter.sort(ogSets, new SetSortHandler());
  } // sortSets


  public String toString()
  {
    return ogID + "";
  }

}

class SetSortHandler implements Comparator
{

  @Override
  public int compare(Object o1, Object o2)
  {
    int result = 0;
    Set a = (Set) o1;
    Set b = (Set) o2;
    if (a.ogCost < b.ogCost)
    {
      result = -1;
    }
    else if (a.ogCost > b.ogCost)
    {
      result = 1;
    }

    return result;
  }
}

class SetSortCoverHandler implements Comparator
{

  @Override
  public int compare(Object o1, Object o2)
  {
    int result = 0;
    Set a = (Set) o1;
    Set b = (Set) o2;
    int aN = 0;
    int bN = 0;
    for (boolean r : a.ogSet)
    {
      if (r)
      {
        aN++;
      }
    }
    for (boolean r : b.ogSet)
    {
      if (r)
      {
        bN++;
      }
    }
    if (aN < bN)
    {
      result = 1;
    }
    else if (aN > bN)
    {
      result = -1;
    }

    return result;
  }
}
