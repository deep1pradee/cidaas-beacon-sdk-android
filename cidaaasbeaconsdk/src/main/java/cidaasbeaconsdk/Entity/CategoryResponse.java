package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryResponse {
    private String vendor;

    private String[] uniqueId;

    public String getVendor ()
    {
        return vendor;
    }

    public void setVendor (String vendor)
    {
        this.vendor = vendor;
    }

    public String[] getUniqueId ()
    {
        return uniqueId;
    }

    public void setUniqueId (String[] uniqueId)
    {
        this.uniqueId = uniqueId;
    }
}
