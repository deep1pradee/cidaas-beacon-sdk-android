package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProximityData {
    private String locationId;

    private int radius;

    private String type;

    private String[] coordinates;

    public String getLocationId ()
    {
        return locationId;
    }

    public void setLocationId (String locationId)
    {
        this.locationId = locationId;
    }

    public int getRadius ()
    {
        return radius;
    }

    public void setRadius (int radius)
    {
        this.radius = radius;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String[] getCoordinates ()
    {
        return coordinates;
    }

    public void setCoordinates (String[] coordinates)
    {
        this.coordinates = coordinates;
    }
}
