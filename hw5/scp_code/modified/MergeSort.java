/**
 * --------------------------------------------------------------------------
 * Classification: UNCLASSIFIED
 * --------------------------------------------------------------------------
 *
 * Class: MergeSort
 * Package: com.san.util
 * Program: Util
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The MergeSort is handled by the Colloections.sort function. If the Collections.sort
 * function is ever changed so that it isn't stable, a stable Merge sort will have to
 * be added.
 * 
 * @author 
 */
public class MergeSort
{

  public void sort(ArrayList array, Comparator sortHandler)
  {
    Collections.sort(array, sortHandler);
  }
  
} // MergeSort
