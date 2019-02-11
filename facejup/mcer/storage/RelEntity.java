package facejup.mcer.storage;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class RelEntity {

    public org.bukkit.entity.Entity entity;
    public Vector relativeLocation;
    public float yaw;
    public float pitch;

    public RelEntity(Entity ent, Vector relLoc, float yaw, float pitch){
        if (relLoc == null){
            relLoc = new Vector(0,0,0);
        }
        this.entity = ent;
        this.relativeLocation = relLoc;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Entity getEntity() {
    	return this.entity;
    }

}