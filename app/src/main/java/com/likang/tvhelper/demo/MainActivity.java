package com.likang.tvhelper.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.likang.tvhelper.lib.focus.BaseActivity;
import com.likang.tvhelper.lib.focus.BorderView;
import com.likang.tvhelper.lib.focus.ViewFocusAppearance;
import com.likang.tvhelper.lib.focus.ViewFocusStrategy;
import com.likang.tvhelper.lib.util.ScreenUtil;

import java.util.ArrayList;

/**
 * @author likangren
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycleview);
        initViews();
    }

    private void initViews() {
        VerticalGridView recycleView = findViewById(R.id.rv);
        recycleView.setItemSpacing(ScreenUtil.dip2px(this, 30));
        recycleView.setNumColumns(10);
        MyAdapter adapter = new MyAdapter();
        recycleView.setAdapter(adapter);


        //给recycleview 添加焦点外观；
        getViewFocusHandler().addRecycleViewFocusAppearance(recycleView);
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

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private ArrayList<String> mDatas = new ArrayList<>();

        {
            char cur = 'A';
            while (cur < 'z' + 1) {
                mDatas.add(cur + "");
                cur++;
            }
        }

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

        MyViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_letter);
        }

    }


}
