package com.pawlowski.shopisto.main.restore_fragment;

import com.pawlowski.shopisto.base.BaseObservableViewMvc;

public abstract class BaseRestoreItemViewMvc <ListenerType, ModelType> extends BaseObservableViewMvc<ListenerType> {
    abstract public void bindItem(ModelType modelType, int position);
}
