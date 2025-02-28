package de.uni_stuttgart.informatik.canu.spatialmodel.geometry;

/**
 * <p>Title: Spatial Model</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Stuttgart</p>
 * @author Illya Stepanov
 * @version 1.1
 */

/**
 * This class implements a closed line connecting several points
 * @author Illya Stepanov 
 */
public class Polygon extends Polyline
{
  /**
   * Constructor
   */
  public Polygon()
  {
  }

  /**
   * Checks if this polygon contains another point. <br>
   * <br>
   * @param p point being checked
   */
  public boolean contains(Point p)
  {
    //boolean res = false;
    int sz = points.size();
   /* for (int i=0, j=sz-1; i<sz; j=i++)
    {
      //System.out.println("i " + i + " j " + j);
			Point point_i = (Point)points.get(i);
      Point point_j = (Point)points.get(j);
			// JHNote (11/02/2006) Corrected an important bug in this method
			//										 Considering a rectangualr boundary
			//										points on the boundaries (X = 0 Y = any) OR
			//									  (X = MAX Y = any) are considered out of the polygon.
			//										We added a "<=" instead of < in the before last
			//										equation line p.getX() " <= " (point_j.getX()-point_i.getX())
			//
			//									This is only a temporary correction. Need to contact Illya to
			//									see what he thinks of it.
			//
			// JHNote: rollbacked. Actually, this solution only solved partially the problem. So
			//										Due to the lack of time, I reinstated Illya's code.
			//										In order to again temporarily solve this issue, when a point
			//										is on the boundary or */
     	/* if ( (((point_i.getY()<=p.getY())&&
             (p.getY()<point_j.getY())) ||
            ((point_j.getY()<=p.getY())&&
             (p.getY()<point_i.getY()))) &&
           (p.getX()<=(point_j.getX()-point_i.getX())*
            (p.getY()-point_i.getY())/(point_j.getY()-point_i.getY())+point_i.getX()) )
      {
        res = !res;
      }*/
			
		/*	 if ( (((point_i.getY()<=p.getY())&&
             (p.getY()<point_j.getY())) ||
            ((point_j.getY()<=p.getY())&&
             (p.getY()<point_i.getY()))) &&
           (p.getX()<(point_j.getX()-point_i.getX())*
            (p.getY()-point_i.getY())/(point_j.getY()-point_i.getY())+point_i.getX()) )
      {
        res = !res;
      }
    }*/
		
	/*	double res = 0; 
		for (int i=0, j=sz-1; i<sz; j=i++) {
		  Point point_i = (Point)points.get(i);
      Point point_j = (Point)points.get(j);
			
			if (p.contains(point_j) || p.contains(point_i))
				return true;
			
			double tmpres = (p.getY() - point_j.getY())*(point_i.getX() - point_j.getX()) - (p.getX() - point_j.getX())*(point_i.getY() - point_j.getY());
			
			if (tmpres == 0)
				return true;
			else {
				if ((res < 0) && tmpres > 0)
					return false;
				else if ((res > 0) && tmpres < 0)
					return false;
				else {
			  // normal case for the first round;
				}
			}
		}
//  return res;
    return true;*/
		
		int i;
		double angle=0;
		Point p1 = null;
		Point p2 = null;

   for (i=0;i<points.size();i++) {
		  
		 	Point point_i = (Point)points.get(i);
      Point point_j = (Point)points.get((i+1)%points.size());
			
			Line line = new Line(point_i,point_j);
			if (line.contains(p))
				return true;
			
			p1 = new Point((point_i.getX() - p.getX()), (point_i.getY() - p.getY()));
			p2 = new Point((point_j.getX() - p.getX()), (point_j.getY() - p.getY()));
			angle += Angle2D(p1.getX(),p1.getY(),p2.getX(),p2.getY());
   }

   if (Math.abs(angle) < Math.PI)
      return(false);
   else
      return(true);
	 
  }

/*
   Return the angle between two vectors on a plane
   The angle is from vector 1 to vector 2, positive anticlockwise
   The result is between -pi -> pi
*/
	protected double Angle2D(double x1, double y1, double x2, double y2) {
		double dtheta,theta1,theta2;

		theta1 = Math.atan2(y1,x1);
		theta2 = Math.atan2(y2,x2);
		dtheta = theta2 - theta1;
		while (dtheta > Math.PI)
      dtheta -= 2*Math.PI;
		while (dtheta < -Math.PI)
      dtheta += 2*Math.PI;

   return(dtheta);
	}
}