package com.luminous.pick;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by User on 14.12.2014.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Bitmap> arrayList;
    private int currentItem;

    public MyRecyclerViewAdapter(ArrayList<Bitmap> arr) {
        arrayList = arr;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.icon.setImageBitmap(arrayList.get(position));
        //currentItem=position;
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) holder.icon.getDrawable()).getBitmap();
                delete(bitmap);
            }
        });
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public Bitmap getItem(int i) {
        return arrayList.get(i);
    }

    public int getCurrentItem() {
        return currentItem;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private Button delete;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.image_item);
            delete = (Button) itemView.findViewById(R.id.delete_item);
        }
    }

    private void copy(Bitmap bg) {
        int position = arrayList.indexOf(bg);
        Bitmap copy = Bitmap.createBitmap(bg);
        arrayList.add(position + 1, copy);
        notifyItemInserted(position + 1);
    }

    private void delete(Bitmap bm) {
        int position = arrayList.indexOf(bm);
        arrayList.remove(position);
        notifyItemRemoved(position);
    }

    public static Bitmap RotateBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
