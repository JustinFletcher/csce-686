import java.util.ArrayList;

public class Element
{
  public int ogName = 0;
  public int ogCoveringSets = 0;
  public ArrayList<Set> ogCoveringSetArray = new ArrayList();
  
  public Element(int name)
  {
    ogName = name;
  } // Element
  
  public String toString()
  {
    return ogName + "";
  }
} // Element
