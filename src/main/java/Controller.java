import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class Controller {
    private static final int BATTERYMAX = 10000;
    private static final int BATTERYLOW = 4000;
    private static final int BATTERYHIGH = 8000;
    private static final String BATTERYCHARGE = "batteryChargeLevel";
    private static final String TOGRID= "toGrid";

    private int batteryLevel;
    private int batteryLocal;
    private int consumption;
    private int ecoSources;
    private int deficit;
    private int mode;
    String key;


    //GETTERS


    public int getBatteryLocal() {
        return batteryLocal;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getDeficit() {
        return deficit;
    }

    public int getEcoSources() {
        return ecoSources;
    }


//    public int getExcess() {
//        return excess;
//    }

    public int getConsumption() {
        return consumption;
    }

    public int getMode() {
        return mode;
    }

    //SETTERS


    public void setBatteryLocal(int batteryLocal) {
        this.batteryLocal = batteryLocal;
    }

    public void setDeficit(int deficit) {
        this.deficit = deficit;
    }

    public void setMode(int mode) {
        this.mode = mode;
        System.out.println("mode is: " + this.mode);
    }

//    public void setExcess(int excess) {
//        this.excess = excess;
//    }

    public void setEcoSources(int ecoSources) {
        this.ecoSources = ecoSources;
        System.out.println("Own power sources: " + this.ecoSources);
    }

    public void setConsumption(int consumption) {
        this.consumption = consumption;
        //this.key = key;
        System.out.println("consumption set to: " + this.consumption);
        //saveDatabase("devices", this.key, this.consumption);
    }

    public void setBatteryLevel(int batteryLevel, String key) {
        this.batteryLevel = batteryLevel;
        this.key = key;
        System.out.println("\nBattery level: " + this.batteryLevel);
        saveDatabase("batteryChargeLevel", this.key, batteryLevel);
    }

    //METHODS
    protected void work() {
        DatabaseReference mode = FirebaseDatabase.getInstance()
                .getReference("normalMode");
        mode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    setMode(child.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Database error");
            }
        });

        DatabaseReference ecoSources = FirebaseDatabase.getInstance()
                .getReference("ecoSources");
        ecoSources.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    setEcoSources(children.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Database error");
            }
        });

        DatabaseReference batteryChargeLevel = FirebaseDatabase.getInstance()
                .getReference("batteryChargeLevel");
        batteryChargeLevel.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    setBatteryLocal(children.getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Database error");
            }
        });

        DatabaseReference devices = FirebaseDatabase.getInstance()
                .getReference("devices");
        devices.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int consumption = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    consumption = consumption + child.getValue(Integer.class);
                }
                setConsumption(consumption);

                if (getMode()==1) {
                    System.out.println("normal");
                    int powerBilance = getBatteryLocal() + getEcoSources() - consumption;
                    if (powerBilance > BATTERYHIGH){
                        setBatteryLevel(BATTERYHIGH, BATTERYCHARGE);
                        toGrid(powerBilance-BATTERYHIGH);
                    } else if(powerBilance > BATTERYLOW && powerBilance <= BATTERYHIGH){
                        setBatteryLocal(powerBilance);
                        setBatteryLevel(powerBilance, BATTERYCHARGE);
                        toGrid(0);
                        System.out.println("Between low i high");
                    } else if (powerBilance <= BATTERYLOW){
                        setBatteryLocal(powerBilance+consumption);
                        setBatteryLevel(powerBilance+consumption,BATTERYCHARGE);
                        toGrid(-getConsumption());
                        System.out.println("I bought: " + getConsumption());
                    }
                } else if(getMode() == 0){
                    if (getBatteryLevel() <= BATTERYHIGH){
                        setBatteryLocal(getBatteryLocal()+ getEcoSources());
                        setBatteryLevel(getBatteryLocal(),BATTERYCHARGE);
                        toGrid(-getConsumption());
                        System.out.println("Solar to battery");
                    } else if(getBatteryLevel() > BATTERYHIGH){
                        System.out.println("Battery max");
                        setBatteryLocal(BATTERYHIGH);
                        setBatteryLevel(BATTERYHIGH, BATTERYCHARGE);
                        toGrid(-getConsumption() + getEcoSources());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Database error");
            }
        });


    }
     public void saveDatabase(String path, String key, Object object) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(path);
        Map<String, Object> updates = new HashMap<>();
        updates.put(key, object);
        ref.updateChildrenAsync(updates);
        System.out.println("Updated");
    }

    public void toGrid(int excess){
        if(excess>0) {
            System.out.println("Excess sold: " + excess);
        } else if (excess == 0){
            System.out.println("Nothing sold");
        } else {
            System.out.println("Deficit bought: " + excess);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("toGrid");
        Map<String, Object> updates = new HashMap<>();
        updates.put(TOGRID, excess);
        ref.updateChildrenAsync(updates);
    }
}
