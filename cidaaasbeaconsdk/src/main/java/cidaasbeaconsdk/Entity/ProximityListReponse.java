package cidaasbeaconsdk.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)


public class ProximityListReponse {
    private String status;
    @JsonProperty("data")
    private LOcationCordinates data;

    private String success;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LOcationCordinates getData() {
        return data;
    }

    public void setData(LOcationCordinates data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}
