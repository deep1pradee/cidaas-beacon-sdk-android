package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Geo {
    private String[] coordinates;

    public String[] getCoordinates ()
    {
        return coordinates;
    }

    public void setCoordinates (String[] coordinates)
    {
        this.coordinates = coordinates;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [coordinates = "+coordinates+"]";
    }

}
