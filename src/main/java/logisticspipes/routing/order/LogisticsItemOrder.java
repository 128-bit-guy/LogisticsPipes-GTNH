package logisticspipes.routing.order;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.resources.DictResource;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;

public class LogisticsItemOrder extends LogisticsOrder {

    public LogisticsItemOrder(DictResource item, IRequestItems destination, ResourceType type,
            IAdditionalTargetInformation info) {
        this(item, destination == null ? -1 : destination.getRouter().getSimpleID(), type, info);
        this.destination = destination;
    }

    public LogisticsItemOrder(DictResource item, int destination, ResourceType type,
            IAdditionalTargetInformation info) {
        super(type, info);
        if (item == null) {
            throw new NullPointerException();
        }
        resource = item;
        this.destinationId = destination;
    }

    @Getter
    private final DictResource resource;

    @Getter
    private IRequestItems destination;

    @Getter
    private final int destinationId;

    @Override
    public IRouter getRouter() {
        if (destination == null) {
            return null;
        }
        return destination.getRouter();
    }

    @Override
    public int getRouterId() {
        return destinationId;
    }

    @Override
    public void sendFailed() {
        if (destination == null) {
            return;
        }
        destination.itemCouldNotBeSend(getResource().stack, getInformation());
    }

    @Override
    public ItemIdentifierStack getAsDisplayItem() {
        return resource.stack;
    }

    @Override
    public int getAmount() {
        return resource.stack.getStackSize();
    }

    @Override
    public void reduceAmountBy(int amount) {
        resource.stack.setStackSize(resource.stack.getStackSize() - amount);
    }
}
