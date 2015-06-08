package com.beeva.beenote.viewutils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.beeva.beenote.models.Note;
import com.beeva.beenote.R;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteItemViewHolder> {

    private List<Note> items;
    private OnRecyclerItemClick onClickListener;

    public final static class NoteItemViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.noteTitle) TextView noteTitle;
        @InjectView(R.id.noteContent) TextView noteContent;
        @InjectView(R.id.rootView) View rootView;

        public NoteItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
          }
    }

    public NotesAdapter(List<Note> items, OnRecyclerItemClick<Note> onRecyclerItemClick) {
        this.items = items;
        this.onClickListener = onRecyclerItemClick;
    }

    @Override
    public NoteItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_note, viewGroup, false);
        return new NoteItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoteItemViewHolder viewHolder, final int position) {
        Note item = items.get(position);
        viewHolder.noteTitle.setText(item.getTitle());
        viewHolder.noteContent.setText(item.getContent());
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(position, items.get(position));
            }
        });
        viewHolder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickListener.onItemLongClick(position, items.get(position));
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Note note){
        this.items.add(note);
        this.notifyDataSetChanged();
    }

    public void deleteItem(Note note){
        this.items.remove(note);
        this.notifyDataSetChanged();
    }

    public void deleteItem(int position){
        this.items.remove(position);
        this.notifyDataSetChanged();
    }
}
