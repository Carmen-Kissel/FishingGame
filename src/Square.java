
public class Square
{
   private final int distanceLeft;
   private final int distanceUp;
   private final int height;
   private final int width;
   private int color;
    
   //constructor
   public Square(int distanceLeftNew, int distanceUpNew, int heightNew, int lengthNew, int colorNew) {
        distanceLeft = distanceLeftNew;
        distanceUp = distanceUpNew;
        height = heightNew;
        width = lengthNew;
        color = colorNew;
   }
   
   //public Square() {
   //     distanceLeft = 50;
   //     distanceUp = 50;
   //     height = 50;
   //     broad = 50;
   //     color = 2;
   //}
   
   //draw
   public void draw() {
        Zeichenfenster.gibFenster().zeichneRechteck(distanceLeft,distanceUp,width,height);
   }
   public void fill() {//public void fuelleRechteck(int xPos, int yPos, int breite, int hoehe, String farbe)
        Zeichenfenster.gibFenster().fuelleRechteck(distanceLeft, distanceUp, width, height, color);
   }
   
   public void setColor(int colorNew) {
       color = colorNew;
   }
}
