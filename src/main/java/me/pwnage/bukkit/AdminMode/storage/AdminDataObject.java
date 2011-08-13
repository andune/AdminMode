/**
 * 
 */
package me.pwnage.bukkit.AdminMode.storage;

import java.io.Serializable;

/** Object that contains the raw data that we Serialize out to disk.
 * 
 * @author morganm
 *
 */
public class AdminDataObject implements Serializable {
	private static final long serialVersionUID = 1337160311439971314L;
	
	private String worldName;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
	private int health;
	
	private String items;
	
	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}
}
