package logisticspipes.interfaces.routing;

import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.routing.AdditionalTargetInformationType;

public interface IAdditionalTargetInformation {

    AdditionalTargetInformationType getType();

    void writeToNBT(NBTTagCompound tag);
}
