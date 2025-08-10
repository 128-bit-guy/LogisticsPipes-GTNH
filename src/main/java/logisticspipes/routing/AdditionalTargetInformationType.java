package logisticspipes.routing;

import java.util.function.Function;

import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.pipes.PipeLogisticsChassi;

public enum AdditionalTargetInformationType {

    Chassi(PipeLogisticsChassi.ChassiTargetInformation::new),
    Crafting(ModuleCrafter.CraftingChassieInformation::new),
    Supplier(ModuleActiveSupplier.SupplierTargetInformation::new),
    PatternSupplier(ModuleActiveSupplier.PatternSupplierTargetInformation::new);

    public final Function<NBTTagCompound, IAdditionalTargetInformation> loader;

    AdditionalTargetInformationType(Function<NBTTagCompound, IAdditionalTargetInformation> loader) {
        this.loader = loader;
    }

    public IAdditionalTargetInformation load(NBTTagCompound tag) {
        return loader.apply(tag);
    }
}
