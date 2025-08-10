package logisticspipes.routing.order;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.resources.DictResource;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class LogisticsItemOrderManager extends LogisticsOrderManager<LogisticsItemOrder, DictResource.Identifier> {

    private static class IC
            implements LogisticsOrderLinkedList.IIdentityProvider<LogisticsItemOrder, DictResource.Identifier> {

        @Override
        public DictResource.Identifier getIdentity(LogisticsItemOrder o) {
            if (o == null || o.getResource() == null) {
                return null;
            }
            return o.getResource().getIdentifier();
        }

        @Override
        public boolean isExtra(LogisticsItemOrder o) {
            return o instanceof LogisticsItemOrderExtra;
        }
    }

    private static class LogisticsItemOrderExtra extends LogisticsItemOrder {

        public LogisticsItemOrderExtra(DictResource item, IRequestItems destination, ResourceType type,
                IAdditionalTargetInformation info) {
            super(item, destination, type, info);
        }

        public LogisticsItemOrderExtra(DictResource item, int destinationId, ResourceType type,
                IAdditionalTargetInformation info) {
            super(item, destinationId, type, info);
        }
    }

    public LogisticsItemOrderManager(ILPPositionProvider pos) {
        super(new LogisticsOrderLinkedList<>(new IC()), pos);
    }

    public LogisticsItemOrderManager(IChangeListener listener, ILPPositionProvider pos) {
        super(listener, pos, new LogisticsOrderLinkedList<>(new IC()));
    }

    @Override
    public void sendFailed() {
        _orders.getFirst().sendFailed();
        super.sendFailed();
    }

    public LogisticsItemOrder addOrder(ItemIdentifierStack stack, IRequestItems requester, ResourceType type,
            IAdditionalTargetInformation info) {
        LogisticsItemOrder order = new LogisticsItemOrder(new DictResource(stack, null), requester, type, info);
        _orders.addLast(order);
        listen();
        return order;
    }

    public LogisticsItemOrder addOrder(DictResource stack, IRequestItems requester, ResourceType type,
            IAdditionalTargetInformation info) {
        LogisticsItemOrder order = new LogisticsItemOrder(stack, requester, type, info);
        _orders.addLast(order);
        listen();
        return order;
    }

    public LogisticsItemOrderExtra addExtra(DictResource stack) {
        LogisticsItemOrderExtra order = new LogisticsItemOrderExtra(stack, null, ResourceType.EXTRA, null);
        _orders.addLast(order);
        listen();
        return order;
    }

    public void removeExtras(DictResource resource) {
        int itemsToRemove = resource.getRequestedAmount();
        DictResource.Identifier ident = resource.getIdentifier();
        Iterator<LogisticsItemOrder> iter = _orders.iterator();
        List<LogisticsItemOrder> toRemove = new LinkedList<>();
        while (iter.hasNext()) {
            LogisticsItemOrder order = iter.next();
            if (order.getType() != ResourceType.EXTRA) continue;
            if (order.getResource().getIdentifier().equals(ident)) {
                if (itemsToRemove >= order.getAmount()) {
                    itemsToRemove -= order.getAmount();
                    toRemove.add(order);
                    if (itemsToRemove == 0) {
                        _orders.removeAll(toRemove);
                        return;
                    }
                } else {
                    order.getResource().getItemStack().setStackSize(order.getAmount() - itemsToRemove);
                    break;
                }
            }
        }
        _orders.removeAll(toRemove);
    }

    public int totalItemsCountInOrders(ItemIdentifier item) {
        int itemCount = 0;
        for (LogisticsItemOrder request : _orders) {
            if (!request.getResource().getItem().equals(item)) {
                continue;
            }
            itemCount += request.getResource().stack.getStackSize();
        }
        return itemCount;
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = new NBTTagList();
        for (LogisticsItemOrder order : _orders) {
            NBTTagCompound orderTag = new NBTTagCompound();
            orderTag.setBoolean("isExtra", order instanceof LogisticsItemOrderExtra);
            NBTTagCompound resourceTag = new NBTTagCompound();
            order.getResource().writeToNBT(resourceTag);
            orderTag.setTag("resource", resourceTag);
            orderTag.setInteger("destinationId", order.getRouterId());
            orderTag.setByte("resourceType", (byte) order.getType().ordinal());
            nbttaglist.appendTag(orderTag);

        }
        nbttagcompound.setTag("orders", nbttaglist);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        while (!_orders.isEmpty()) {
            _orders.removeFirst();
        }
        NBTTagList nbttaglist = nbttagcompound.getTagList("orders", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound orderTag = nbttaglist.getCompoundTagAt(i);
            boolean isExtra = orderTag.getBoolean("isExtra");
            DictResource resource = new DictResource(orderTag.getCompoundTag("resource"));
            int destinationId = orderTag.getInteger("destinationId");
            ResourceType type = ResourceType.values()[orderTag.getByte("resourceType")];
            LogisticsItemOrder order = isExtra ? new LogisticsItemOrderExtra(resource, destinationId, type, null)
                    : new LogisticsItemOrder(resource, destinationId, type, null);
            _orders.addLastInOrder(order);
        }
    }
}
