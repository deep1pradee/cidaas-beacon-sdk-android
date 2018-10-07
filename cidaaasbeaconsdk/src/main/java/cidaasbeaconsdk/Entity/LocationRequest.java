package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationRequest {
    private String sub;

    private Geo geo;

    private String status;

    private String sessionId;

   // private String[] locationIds;

    private String deviceId;

    public String getSub ()
    {
        return sub;
    }

    public void setSub (String sub)
    {
        this.sub = sub;
    }

    public Geo getGeo ()
    {
        return geo;
    }

    public void setGeo (Geo geo)
    {
        this.geo = geo;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public String getSessionId ()
    {
        return sessionId;
    }

    public void setSessionId (String sessionId)
    {
        this.sessionId = sessionId;
    }
/*
    public String[] getLocationIds ()
    {
        return locationIds;
    }

    public void setLocationIds (String[] locationIds)
    {
        this.locationIds = locationIds;
    }*/

    public String getDeviceId ()
    {
        return deviceId;
    }

    public void setDeviceId (String deviceId)
    {
        this.deviceId = deviceId;
    }
}
