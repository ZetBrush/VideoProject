package com.luminous.pick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by User on 14.12.2014.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>  {



    private ArrayList<Bitmap> arrayList;
    private ImageView currentItem;

    public MyRecyclerViewAdapter(ArrayList<Bitmap> arr,ImageView img) {
        arrayList = arr;
        currentItem=img;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.icon.setImageBitmap(arrayList.get(position));
        //holder.icon.setImageBitmap(Example.getBitmap(String.valueOf(position)));

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
                if(arrayList.size()>0) {
                    currentItem.setImageBitmap(arrayList.get(0));
                }else {
                    currentItem.setImageBitmap(null);
                }
            }
        });

        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentItem.setImageBitmap(arrayList.get(position));
            }
        });

        /*holder.rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) holder.icon.getDrawable()).getBitmap();
                arrayList.set(position, rotate(bitmap));
                holder.icon.setImageBitmap(rotate(bitmap));
                currentItem.setImageBitmap(rotate(bitmap));
                notifyDataSetChanged();
            }
        });*/
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public Bitmap getItem(int i) {
        return arrayList.get(i);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView icon;
        private Button delete;
        private Button rotate;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.image_item);
            delete = (Button) itemView.findViewById(R.id.delete_item);
            //rotate=(Button)itemView.findViewById(R.id.rotate_but);
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

    private Bitmap rotate(Bitmap bm){

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bg = Bitmap.createBitmap(bm , 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        return bg;
    }
}





