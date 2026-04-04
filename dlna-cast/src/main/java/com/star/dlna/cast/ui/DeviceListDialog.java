package com.star.dlna.cast.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.star.dlna.cast.R;
import com.star.dlna.cast.api.DLNACastManager;
import com.star.dlna.cast.model.CastDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备列表对话框 (Material Design 3)
 */
public class DeviceListDialog extends BottomSheetDialog {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private CircularProgressIndicator progressIndicator;
    private TextView tvEmpty;
    private MaterialButton btnRefresh;
    private OnDeviceSelectedListener listener;

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(CastDevice device);
    }

    public DeviceListDialog(@NonNull Context context) {
        super(context);
    }

    public DeviceListDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_list);

        initViews();
        setupRecyclerView();
        observeDevices();
        startDiscovery();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_devices);
        progressIndicator = findViewById(R.id.progress_indicator);
        tvEmpty = findViewById(R.id.tv_empty);
        btnRefresh = findViewById(R.id.btn_refresh);

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> startDiscovery());
        }
    }

    private void setupRecyclerView() {
        adapter = new DeviceAdapter(new ArrayList<>(), device -> {
            DLNACastManager.getInstance().selectDevice(device);
            if (listener != null) {
                listener.onDeviceSelected(device);
            }
            dismiss();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeDevices() {
        DLNACastManager.getInstance().getDevices().observeForever(devices -> {
            if (devices != null) {
                adapter.updateDevices(devices);
                updateEmptyState(devices.isEmpty());
            }
        });
    }

    private void startDiscovery() {
        showLoading(true);
        DLNACastManager.getInstance().startDiscovery(30000);

        // 3秒后隐藏加载
        recyclerView.postDelayed(() -> showLoading(false), 3000);
    }

    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState(boolean empty) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    public void setOnDeviceSelectedListener(OnDeviceSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void dismiss() {
        DLNACastManager.getInstance().stopDiscovery();
        super.dismiss();
    }

    /**
     * 设备列表适配器
     */
    private static class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

        private List<CastDevice> devices;
        private OnDeviceClickListener listener;

        interface OnDeviceClickListener {
            void onDeviceClick(CastDevice device);
        }

        DeviceAdapter(List<CastDevice> devices, OnDeviceClickListener listener) {
            this.devices = devices;
            this.listener = listener;
        }

        void updateDevices(List<CastDevice> newDevices) {
            this.devices = new ArrayList<>(newDevices);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CastDevice device = devices.get(position);
            holder.bind(device);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView ivIcon;
            private final TextView tvName;
            private final TextView tvManufacturer;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_device_icon);
                tvName = itemView.findViewById(R.id.tv_device_name);
                tvManufacturer = itemView.findViewById(R.id.tv_device_manufacturer);
            }

            void bind(CastDevice device) {
                tvName.setText(device.getName());
                tvManufacturer.setText(device.getManufacturer());
            }
        }
    }
}
