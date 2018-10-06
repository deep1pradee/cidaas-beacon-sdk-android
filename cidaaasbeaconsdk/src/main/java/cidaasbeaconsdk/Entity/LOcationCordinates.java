package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class LOcationCordinates {
    private String count;

    private ProximityData[] data;

    public String getCount ()
    {
        return count;
    }

    public void setCount (String count)
    {
        this.count = count;
    }

    public ProximityData[] getData ()
    {
        return data;
    }

    public void setData (ProximityData[] data)
    {
        this.data = data;
    }
}
