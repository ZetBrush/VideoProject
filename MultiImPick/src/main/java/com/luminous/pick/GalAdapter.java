package com.luminous.pick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by intern on 12/17/14.
 */
public class GalAdapter extends RecyclerView.Adapter<GalAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater infalter;
    private ArrayList<CustomGallery> data = new ArrayList<CustomGallery>();
    ArrayList<Bitmap> arr=new ArrayList<Bitmap>();
    ImageLoader imageLoader;

    private boolean isActionMultiplePick;

    public GalAdapter(Context c,ImageLoader imageLoader, ArrayList<Bitmap> arr) {
        infalter = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = c;
        this.arr=arr;
        this.imageLoader = imageLoader;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.imgQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get(position).isSeleted) {
                    data.get(position).isSeleted = false;
                } else {
                    data.get(position).isSeleted = true;
                }

                holder.imgQueueMultiSelected.setSelected(data
                        .get(position).isSeleted);
            }
        });

        try {

            imageLoader.displayImage("file://" + data.get(position).sdcardPath,
                    holder.imgQueue, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.imgQueue
                                    .setImageResource(R.drawable.back);
                            super.onLoadingStarted(imageUri, view);
                        }
                    });
           // holder.imgQueue.setImageBitmap(arr.get(position));

                holder.imgQueueMultiSelected
                        .setSelected(data.get(position).isSeleted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgQueue;
        ImageView imgQueueMultiSelected;

        public ViewHolder(View itemView) {
            super(itemView);
            imgQueue = (ImageView) itemView.findViewById(R.id.imgQueue);
            imgQueueMultiSelected = (ImageView) itemView.findViewById(R.id.imgQueueMultiSelected);
            imgQueueMultiSelected.setVisibility(View.VISIBLE);

        }
    }

    public void addAll(ArrayList<CustomGallery> files) {

        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public ArrayList<CustomGallery> getSelected() {
        ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                dataT.add(data.get(i));
            }
        }

        return dataT;
    }

    public void setMultiplePick(boolean isMultiplePick) {
        this.isActionMultiplePick = isMultiplePick;
    }

    public CustomGallery getItem(int i) {
        return data.get(i);
    }


    public boolean isAllSelected() {
        boolean isAllSelected = true;

        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).isSeleted) {
                isAllSelected = false;
                break;
            }
        }

        return isAllSelected;
    }

    public boolean isAnySelected() {
        boolean isAnySelected = false;

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                isAnySelected = true;
                break;
            }
        }
        return isAnySelected;
    }

    public void clearCache() {
        imageLoader.clearDiscCache();
        imageLoader.clearMemoryCache();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}
