package bsd.stick;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by ShiDa.Bian on 2017/2/16.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.NestedHolder> {
    private String[] list;

    public RVAdapter(Context context) {
        list = context.getResources().getStringArray(R.array.data);
    }

    @Override
    public NestedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new NestedHolder(view);
    }

    @Override
    public void onBindViewHolder(NestedHolder holder, int position) {
        holder.mTextView.setText(list[position]);
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    class NestedHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public NestedHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
