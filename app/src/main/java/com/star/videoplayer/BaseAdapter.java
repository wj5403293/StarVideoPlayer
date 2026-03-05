package com.star.videoplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName BaseAdapter
 * Author Zn7
 * Date 2026/2/13
 * 注解
 */
public abstract class BaseAdapter<T, VB extends ViewBinding>
        extends RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>> {

    protected Context context;
    protected ArrayList<T> data;
    protected OnItemClickListener<T> itemClickListener;
    protected OnItemLongClickListener<T> itemLongClickListener;

    // 构造函数统一使用ArrayList
    public BaseAdapter(ArrayList<T> data) {
        this.data = data != null ? data : new ArrayList<>();
    }

    @NonNull
    @Override
    public BaseViewHolder<VB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        VB binding = createViewBinding(inflater, parent, viewType);
        return new BaseViewHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<VB> holder, int position) {
        final T item = data.get(position); // 直接使用ArrayList的get方法

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, position, item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                return itemLongClickListener.onItemLongClick(v, position, item);
            }
            return false;
        });

        bindView(holder.binding, item, position, getItemViewType(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // 核心抽象方法
    protected abstract VB createViewBinding(LayoutInflater inflater, ViewGroup parent, int viewType);
    protected abstract void bindView(VB binding, T item, int position, int viewType);

    // ArrayList操作接口
    public void addData(T item) {
        data.add(item);
        notifyItemInserted(data.size() - 1);
    }

    public void addAll(ArrayList<T> items) {
        int startPos = data.size();
        data.addAll(items);
        notifyItemRangeInserted(startPos, items.size());
    }

    public void updateData(ArrayList<T> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        if (position < data.size()) {
            notifyItemRangeChanged(position, data.size() - position);
        }
    }

    public void removeDataAll() {
        int originalSize = data.size();
        if (originalSize > 0) {
            data.clear();
            notifyItemRangeRemoved(0, originalSize);
        }
    }

    // 事件监听接口（保持ArrayList参数）
    public interface OnItemClickListener<T> {
        void onItemClick(View view, int position, T item);
    }

    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(View view, int position, T item);
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.itemLongClickListener = listener;
    }

    // ViewHolder实现
    // 新增功能：通过ViewHolder获取Binding
    public static class BaseViewHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
        private final VB binding;

        public BaseViewHolder(@NonNull VB binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // 安全获取Binding的方法
        @Nullable
        public VB getBinding() {
            // 检查视图是否仍附着在窗口上
            return itemView.isAttachedToWindow() ? binding : null;
        }
    }

    // 新增：通过RecyclerView获取指定位置的Binding
    @Nullable
    public VB getBindingByPosition(RecyclerView recyclerView, int position) {
        if (position < 0 || position >= getItemCount()) return null;

        BaseViewHolder<VB> viewHolder =
                (BaseViewHolder<VB>) recyclerView.findViewHolderForAdapterPosition(position);
        return viewHolder != null ? viewHolder.getBinding() : null;
    }

    // 新增：批量获取可见项的Binding（适用于特殊场景）
    public List<VB> getVisibleBindings(RecyclerView recyclerView) {
        List<VB> bindings = new ArrayList<>();
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder instanceof BaseViewHolder) {
                VB binding = ((BaseViewHolder<VB>) holder).getBinding();
                if (binding != null) {
                    bindings.add(binding);
                }
            }
        }
        return bindings;
    }


}
