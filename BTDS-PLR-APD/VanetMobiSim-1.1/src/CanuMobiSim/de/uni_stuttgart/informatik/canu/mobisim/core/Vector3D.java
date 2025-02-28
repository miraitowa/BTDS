package de.uni_stuttgart.informatik.canu.mobisim.core;

/*--------------------------------------------------------------------------*/
/* CLASS Vector3D */
/********************
** @version 0.3, 1999-12-26, 2000-05-03
** @author  Gregor Schiele
** @author  Torsten Brodbeck
*/

public class Vector3D
{
   protected double x;
   protected double y;
   protected double z;


   /*--------------------------------------------------------------*/
   /* CONSTRUCTOR */
   /****************
   ** Constructs a new Vector3D. <BR>
   ** <BR>
   ** precond : x, y and z not null <BR>
   ** postcond: this.x', this.y' and this.z' set <BR>
   */
   public Vector3D(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }//endproc


   /*--------------------------------------------------------------*/
   /* CONSTRUCTOR */
   /****************
   ** Constructs a new Vector3D. <BR>
   ** <BR>
   ** precond : aVector not null <BR>
   ** postcond: this.x', this.y' and this.z' set <BR>
   */
   public Vector3D(Vector3D aVector) {
      this.x = aVector.x;
      this.y = aVector.y;
      this.z = aVector.z;
   }//endproc


   /*--------------------------------------------------------------*/
   /* CONSTRUCTOR */
   /****************
   ** Constructs a new Vector3D. <BR>
   ** <BR>
   ** precond : direction and length not null <BR>
   ** postcond: this.x', this.y' and this.z' set <BR>
   */
   public Vector3D(Vector3D direction, double length)
   {
      direction = direction.normalize();
      direction = direction.mult(length);
      this.x = direction.x;
      this.y = direction.y;
      this.z = direction.z;
   }//endproc

   /*--------------------------------------------------------------*/
   /* METHOD createNullVector */
   /**********************
   ** Creates and returns a null vector.<BR>
   ** <BR>
   ** precond : none <BR>
   ** postcond: new Vector(0,0,0)' created.<BR>
   ** @return Vector3D with value x=0, y=0 and z=0
   */
   public static Vector3D createNullVector()
   {
      return new Vector3D(0.0, 0.0, 0.0);
   }//endproc


   /*--------------------------------------------------------------*/
   /* METHOD getX */
   /**********************
   ** Returns the x value of the vector.<BR>
   ** <BR>
   ** precond : none <BR>
   ** postcond: none<BR>
   ** @return double with value of x
   */
   public double getX() {
      return this.x;
   }//endproc

   /*--------------------------------------------------------------*/
   /* METHOD getY */
   /**********************
   ** Returns the y value of the vector.<BR>
   ** <BR>
   ** precond : none <BR>
   ** postcond: none<BR>
   ** @return double with value of y
   */
   public double getY() {
      return this.y;
   }//endproc

   /*--------------------------------------------------------------*/
   /* METHOD getZ */
   /**********************
   ** Returns the z value of the vector.<BR>
   ** <BR>
   ** precond : none <BR>
   ** postcond: none<BR>
   ** @return double with value of z
   */
   public double getZ() {
      return this.z;
   }//endproc


   /*--------------------------------------------------------------*/
   /* METHOD normalize */
   /**********************
   ** Returns a normalized value of the vector.<BR>
   ** <BR>
   ** precond : none <BR>
   ** postcond: none<BR>
   ** @return Vector3D with normalized value of this.Vector3D
   */
   public Vector3D normalize()
   {
      double length = getLength();

      if(length != 0.0) {
         return this.div(length);
      }else {
         return Vector3D.createNullVector();
      }//endif

   }//endproc

   /*--------------------------------------------------------------*/
   /* METHOD getLength */
   /**********************
   ** Returns the length of the vector.<BR>
   ** <BR>
   ** precond : none <BR>
   ** postcond:none<BR>
   ** @return double with value length of this.Vector3D
   */
   public double getLength()
   {
      double tmpX, tmpY, tmpZ;
      double sum;

      tmpX = this.x * this.x;
      tmpY = this.y * this.y;
      tmpZ = this.z * this.z;

      sum = tmpX + tmpY + tmpZ;
      return Math.sqrt(sum);
   }//endproc


   /*--------------------------------------------------------------*/
   /* METHOD add */
   /**********************
   ** Returns the sum of pos an the this.Vector3D.<BR>
   ** <BR>
   ** precond : pos not null <BR>
   ** postcond: none<BR>
   ** @return Vector3D with sum of both vectors
   */
   public Vector3D add(Vector3D pos)
   {
      return new Vector3D(this.x + pos.x,
                          this.y + pos.y,
                          this.z + pos.z);
   }//endproc


    /*--------------------------------------------------------------*/
   /* METHOD sub */
   /**********************
   ** returns this - pos.<BR>
   ** <BR>
   ** precond : pos not null <BR>
   ** postcond: none<BR>
   ** @return Vector3D with value of substracted this.Vector3D and pos
   */
   public Vector3D sub(Vector3D pos)
   {
      return new Vector3D(this.x - pos.x,
                          this.y - pos.y,
                          this.z - pos.z);
   }//endproc

   /*--------------------------------------------------------------*/
   /* METHOD mult */
   /**********************
   ** Returns the value of multiplicated this.Vector3D and skalar.<BR>
   ** <BR>
   ** precond : skalar not null <BR>
   ** postcond: none<BR>
   ** @return Vector3D with mult of this.Vector3D and skalar
   */
   public Vector3D mult(double skalar)
   {
      return new Vector3D(this.x * skalar,
                          this.y * skalar,
                          this.z * skalar);
   }//endproc


   /*--------------------------------------------------------------*/
   /* METHOD div */
   /**********************
   ** Returns the value of the division of this.Vector3D and skalar.<BR>
   ** <BR>
   ** precond : skalar not null and skalar not value=0.0 <BR>
   ** postcond: none<BR>
   ** @return Vector3D with div of this.Vector3D and skalar
   */
   public Vector3D div(double skalar)
   {
      return new Vector3D(this.x / skalar,
                          this.y / skalar,
                          this.z / skalar);
   }//endproc
}//endclass
