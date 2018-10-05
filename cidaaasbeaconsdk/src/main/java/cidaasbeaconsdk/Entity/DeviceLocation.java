package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLocation {
    private Geo geo;

    private String minor;

    private String major;

    private String uniqueId;

    public Geo getGeo ()
    {
        return geo;
    }

    public void setGeo (Geo geo)
    {
        this.geo = geo;
    }

    public String getMinor ()
    {
        return minor;
    }

    public void setMinor (String minor)
    {
        this.minor = minor;
    }

    public String getMajor ()
    {
        return major;
    }

    public void setMajor (String major)
    {
        this.major = major;
    }

    public String getUniqueId ()
    {
        return uniqueId;
    }

    public void setUniqueId (String uniqueId)
    {
        this.uniqueId = uniqueId;
    }

}
