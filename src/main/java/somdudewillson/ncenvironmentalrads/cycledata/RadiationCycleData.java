package main.java.somdudewillson.ncenvironmentalrads.cycledata;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class RadiationCycleData extends WorldSavedData {
	private static final String DATA_NAME = EnvironmentalRads.MODID + "_CycleData";
    private static RadiationCycleData instance;
    private int cyclePhase = 0;
    private int cycleTime = 0;
	  
	// Required constructors
	public RadiationCycleData() {
		super(DATA_NAME);
	}
	public RadiationCycleData(String s) {
		super(s);
	}

    public static RadiationCycleData get(World world) {
        MapStorage storage = world.getMapStorage();
        instance = (RadiationCycleData) storage.getOrLoadData(RadiationCycleData.class, DATA_NAME);

        if (instance == null) {
            instance = new RadiationCycleData();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		cyclePhase = nbt.getInteger("cyclePhase");
		cycleTime = nbt.getInteger("cycleTime");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("cyclePhase", cyclePhase);
		compound.setInteger("cycleTime", cycleTime);
		return compound;
	}
	
	//Getters and Setters
	public int getCyclePhase() {
		return cyclePhase;
	}
	public int getCycleTime() {
		return cycleTime;
	}
	
	public void setCycleTime(int cycleTime) {
		this.cycleTime = cycleTime;
        markDirty();
	}
	public void setCyclePhase(int cyclePhase) {
		this.cyclePhase = cyclePhase;
        markDirty();
	}

}
