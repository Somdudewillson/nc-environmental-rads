package main.java.somdudewillson.ncenvironmentalrads.cycledata;

import main.java.somdudewillson.ncenvironmentalrads.EnvironmentalRads;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class RadiationStormData extends WorldSavedData {
	private static final String DATA_NAME = EnvironmentalRads.MODID + "_RadStormData";
    private static RadiationStormData instance;
    private int stormDuration = 0;
	  
	// Required constructors
	public RadiationStormData() {
		super(DATA_NAME);
	}
	public RadiationStormData(String s) {
		super(s);
	}

    public static RadiationStormData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        instance = (RadiationStormData) storage.getOrLoadData(RadiationStormData.class, DATA_NAME);

        if (instance == null) {
            instance = new RadiationStormData();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		stormDuration = nbt.getInteger("stormDuration");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("stormDuration", stormDuration);
		return compound;
	}
	
	public int getStormDuration() {
		return stormDuration;
	}
	
	public void setStormDuration(int stormDuration) {
		this.stormDuration = stormDuration;
        markDirty();
	}

}
