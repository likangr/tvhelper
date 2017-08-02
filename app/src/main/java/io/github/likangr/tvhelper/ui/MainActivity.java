package io.github.likangr.tvhelper.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.likangr.tvhelper.R;
import io.github.likangr.tvhelper.handler.focus.BorderView;
import io.github.likangr.tvhelper.handler.focus.ViewFocusAppearance;
import io.github.likangr.tvhelper.handler.focus.ViewFocusStrategy;

public class MainActivity extends FocusHandleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycleview);
        initViews();
    }

    private void initViews() {
        VerticalGridView recycleView = (VerticalGridView) findViewById(R.id.rv);
        recycleView.setItemSpacing(getResources().getDimensionPixelSize(R.dimen.px30));
        recycleView.setNumColumns(10);
        MyAdapter adapter = new MyAdapter();
        recycleView.setAdapter(adapter);


        getViewFocusHandler().addRecycleViewFocusAppearance(recycleView);//给recycleview 添加焦点外观；
        ViewFocusAppearance appearance = new ViewFocusAppearance()
                .setFocusStrategy(ViewFocusStrategy.STRATEGY_Y_SCALE_Y_BORDER)
                .setBorderParams(new BorderView.BorderParams()
                        .setShadowColor(Color.RED)
                        .setShadowWidth(BorderView.BorderParams.SHADOW_MAX_WIDTH));
        getViewFocusHandler().addViewFocusAppearance(findViewById(R.id.ll_btns1), appearance, true);
        getViewFocusHandler().rememberLastFocusView((ViewGroup) findViewById(R.id.ll_btns1));
        ViewFocusAppearance appearance1 = new ViewFocusAppearance()
                .setAnimTime(200)
                .setFocusStrategy(ViewFocusStrategy.STRATEGY_Y_SCALE_Y_BORDER)
                .setBorderParams(new BorderView.BorderParams()
                        .setShadowColor(Color.YELLOW))
                .setXScaleValue(1.5f)
                .setYScaleValue(1.5f);

        getViewFocusHandler().addViewFocusAppearance(findViewById(R.id.tv4), appearance1, false);

    }

    private ArrayList<String> mDatas = new ArrayList<>();

    {
        char cur = 'A';
        while (cur < 'z' + 1) {
            mDatas.add(cur + "");
            cur++;
        }
    }


    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_rv, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv.setText(mDatas.get(position));
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_letter);
        }

    }


}
