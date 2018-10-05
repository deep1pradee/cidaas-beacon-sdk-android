package cidaasbeaconsdk.Entity;

import java.io.Serializable;

public class BeaconEntity implements Serializable{

    String[] uuid;
    String name;


    public BeaconEntity( String name,String[] uuid) {
        this.uuid = uuid;
        this.name = name;
    }



    public String[] getUuid() {
        return uuid;
    }

    public void setUuid(String[] uuid) {
        this.uuid = uuid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
