package com.pawlowski.shopisto.base;

import java.util.ArrayList;
import java.util.List;

public class BaseObservableViewMvc<ListenerType> extends BaseViewMvc{
    protected List<ListenerType> listeners = new ArrayList<>();

    public void registerListener(ListenerType listener)
    {
        listeners.add(listener);
    }

    public void unregisterListener(ListenerType listener)
    {
        listeners.remove(listener);
    }

    public void clearAllListeners()
    {
        listeners.clear();
    }
}
