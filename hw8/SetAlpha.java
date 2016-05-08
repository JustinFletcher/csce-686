/**
 * --------------------------------------------------------------------------
 * Classification: UNCLASSIFIED
 * --------------------------------------------------------------------------
 *
 * Class: SetAlpha
 * Program: Set program
 *
 * DESCRIPTION: This program solves the set-covering problem and the
 *   set-partitioning problem using the algorithm in Christofides
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SetAlpha
{
  ArrayList<Element> ogElements = null;
  ArrayList<Set> ogSetArray = null;
  int ogSetID = 1;

  public SetAlpha()
  {
    
  } // SetAlpha


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


  public void addToElement(int name, Set m)
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
  public static String formatStringLength(String m, int length,
          String theBuffer, boolean after)
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

}
