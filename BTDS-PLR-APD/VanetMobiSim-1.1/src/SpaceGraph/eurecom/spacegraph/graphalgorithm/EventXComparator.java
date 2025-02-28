package eurecom.spacegraph.graphalgorithm;

/**
 * <p>Title: Voronoi's Points' X Coordinate Comparator</p>
 * <p>Description: In order to sort the Voronoi obstacles according to the  increased X coordinate, the Collections.sort needs a specific comparator</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Institut Eurecom</p>
 * @author Jerome Haerri
 * @version 1.0
 */
 
 /**
  * This class compare two points X coordinates
	*
  * @author Jerome Haerri
	*/
public class EventXComparator implements java.util.Comparator, java.io.Serializable {

 /**
  * Compare to Points' X coordinates.
	* @return -1 if point1.X is smaller than point2.X
	* @return +1 if point1.X is bigger than point2.X
	* @return  0 if point1.X is equals to point2.X
	*/
 public int compare(java.lang.Object point1, java.lang.Object point2) {
 
   MyPoint p1 = (MyPoint)point1; 
   MyPoint p2 = (MyPoint)point2;
   
   if (p1.x < p2.x)
     return -1;
   
   else {
     if (p1.x > p2.x)
       return 1;
     else {
       if (p1.x == p2.x) 
        return 0;
     }
   }
   return 2;
 }
 
 public boolean equals (Object obj) {
   if (this.equals(obj))
     return true;
   else
     return false;
 }
 
}
