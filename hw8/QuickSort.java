/**
 * --------------------------------------------------------------------------
 * Classification: UNCLASSIFIED
 * --------------------------------------------------------------------------
 *  Reviewed for classification  on 21 Oct 2008
 *  Reviewed for classification  on 25 Oct 2008
 *
 * Class: QuickSort
 * Package: com.san.util
 * Program: Util
 *
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;

/**
* Quicksort sorts a list of objects. It relies in a GenericSortHandler to determine object order.
* The GenericSortHandler must implement a compareObject function to determine which objects are
* greater than or less than other objects.
*
* @author  
*/
public class QuickSort
{
    /**
     * The quickSort function recursively sorts an ArrayList from lowStart to highStart
     * 
     * @param list - List to sort
     * @param lowStart - Starting low index
     * @param highStart - Starting high index
     * @param gsh - GenericSortHandler to choose between two object.
     */
  private void quickSort(ArrayList list, int lowStart, int highStart, Comparator gsh)
  {
    int low = lowStart, high = highStart;
    if (low >= high)
    {
      return;
    }
    else if (low == high - 1)
    {
      if (gsh.compare(list.get(low), list.get(high)) > 0)
      {
        Object temp = list.get(low);
        list.set(low, list.get(high));
        list.set(high, temp);
      }
      return;
    }

    Object pivot = list.get((low + high) / 2);
    list.set((low + high) / 2, list.get(high));
    list.set(high, pivot);

    while (low < high)
    {
      while ((gsh.compare(list.get(low), pivot) <= 0) &&
              (low < high))
      {
        low++;
      }
      while ((gsh.compare(list.get(high), pivot) >= 0) &&
              (low < high))
      {
        high--;
      }
      if (low < high)
      {
        Object temp = list.get(low);
        list.set(low, list.get(high));
        list.set(high, temp);
      }
    }
    list.set(highStart, list.get(high));
    list.set(high, pivot);
    quickSort(list, lowStart, low - 1, gsh);
    quickSort(list, high + 1, highStart, gsh);
  } // quickSort
  
  
  /**
     * The quickSort function recursively sorts a Vector from lowStart to highStart
     * 
     * @param list - List to sort
     * @param lowStart - Starting low index
     * @param highStart - Starting high index
     * @param gsh - GenericSortHandler to choose between two object.
     */
  private void quickSort(Vector list, int low0, int high0, Comparator gsh)
  {
    int low = low0, high = high0;
    if (low >= high)
    {
      return;
    }
    else if (low == high - 1)
    {
      if (gsh.compare(list.get(low), list.get(high)) > 0)
      {
        Object temp = list.get(low);
        list.set(low, list.get(high));
        list.set(high, temp);
      }
      return;
    }

    Object pivot = list.get((low + high) / 2);
    list.set((low + high) / 2, list.get(high));
    list.set(high, pivot);

    while (low < high)
    {
      while ((gsh.compare(list.get(low), pivot) <= 0) &&
              (low < high))
      {
        low++;
      }
      while ((gsh.compare(list.get(high), pivot) >= 0) &&
              (low < high))
      {
        high--;
      }
      if (low < high)
      {
        Object temp = list.get(low);
        list.set(low, list.get(high));
        list.set(high, temp);
      }
    }
    list.set(high0, list.get(high));
    list.set(high, pivot);
    quickSort(list, low0, low - 1, gsh);
    quickSort(list, high + 1, high0, gsh);
  } // quickSort

  
  /**
     * The sort function sorts an ArrayList
     * 
     * @param list - List to sort
     * @param gsh - GenericSortHandler to choose between two object.
     */
  public void sort(ArrayList list, Comparator gsh)
  {
    quickSort(list, 0, list.size() - 1, gsh);
  }
  
  
  /**
     * The sort function sorts a Vector
     * 
     * @param list - List to sort
     * @param gsh - GenericSortHandler to choose between two object.
     */
  public void sort(Vector list, Comparator gsh)
  {
    quickSort(list, 0, list.size() - 1, gsh);
  }

} // QuickSort
