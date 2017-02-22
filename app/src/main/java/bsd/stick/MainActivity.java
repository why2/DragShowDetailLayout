package bsd.stick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

/**
 * 这里主要是测试recyclerview，和listview
 * 两层类似与淘宝的查看商品详情效果
 */
public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    Bookends mBookends;
    RVAdapter mRVAdapter;
    private DragLayout mDragLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRVAdapter = new RVAdapter(this);
        mBookends = new Bookends(mRVAdapter);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_test);
        mDragLayout = (DragLayout) findViewById(R.id.detail_layout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBookends.addHeader(LayoutInflater.from(this).inflate(R.layout.rv_header, mRecyclerView, false));
        mRecyclerView.setAdapter(mBookends);
        mDragLayout.setOnSlideDetailsListener(new DragLayout.OnSlideFinishListener() {
            @Override
            public void onStatueChanged(DragLayout.CurrentTargetIndex status) {
                Log.i("================", "==================status=" + status);
            }
        });
    }
}
