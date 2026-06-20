package com.yn.homi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.models.Idea;

import java.util.ArrayList;
import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.IdeaViewHolder> {

    private List<Idea> ideaList = new ArrayList<>();

    public void updateData(List<Idea> newList) {
        this.ideaList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_idea, parent, false);
        return new IdeaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        Idea idea = ideaList.get(position);
        holder.tvTitle.setText(idea.getTitle());
        holder.tvDescription.setText(idea.getDescription());
        holder.tvGroup.setText(idea.getGroupLabel());

        Glide.with(holder.itemView.getContext())
                .load(idea.getThumbnailUrl())
                .placeholder(R.color.gray_light)
                .error(R.color.gray_light)
                .centerCrop()
                .into(holder.ivThumbnail);
    }

    @Override
    public int getItemCount() {
        return ideaList.size();
    }

    static class IdeaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvDescription, tvGroup;

        public IdeaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_idea_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_idea_title);
            tvDescription = itemView.findViewById(R.id.tv_idea_description);
            tvGroup = itemView.findViewById(R.id.tv_idea_group);
        }
    }
}
