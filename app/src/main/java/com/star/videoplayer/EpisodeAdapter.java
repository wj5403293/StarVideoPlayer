package com.star.videoplayer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.star.videoplayer.databinding.ItemEpisodeBinding;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ClassName EpisodeAdapter
 * Author StarBox
 * Date 2026/3/5
 * 注解
 */
public class EpisodeAdapter extends BaseAdapter<HashMap<String, Object>, ItemEpisodeBinding> {

    public EpisodeAdapter(ArrayList<HashMap<String, Object>> data) {
        super(data);
    }

    @Override
    protected ItemEpisodeBinding createViewBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return ItemEpisodeBinding.inflate(inflater,parent,false);
    }

    @Override
    protected void bindView(ItemEpisodeBinding binding, HashMap<String, Object> item, int position, int viewType) {
        binding.time.setText(item.get("name").toString());
    }
}
