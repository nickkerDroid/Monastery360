package info.fortheease.monastery360.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import info.fortheease.monastery360.R;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private final Context context;
    private final List<Integer> imageResIds; // use Integer resource ids

    public CarouselAdapter(Context context, List<Integer> imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int resId = imageResIds.get(position);

        // Glide loads images efficiently and handles downsampling
        Glide.with(context)
                .load(resId)
                .fitCenter()
                .into(holder.photoView);

        // Ensure PhotoView touch doesn't always trigger page swipe when user is panning/zooming
        holder.photoView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false; // let PhotoView still handle the event
        });
    }

    @Override
    public int getItemCount() {
        return imageResIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }
}
