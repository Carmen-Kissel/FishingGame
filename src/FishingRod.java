
public class FishingRod //Image fuer die Angel auch in diese Klasse(als Image auf einem Hintergrund)
{
    private boolean inWater;
    
    public FishingRod()
    {
        inWater = false;
    }
    
    public void setState(boolean newState) {
        inWater = newState;
    }

    public boolean isInWater() {
        return inWater;
    }
}
