package com.example.owner_pc.androidkanazawa2015.list;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.owner_pc.androidkanazawa2015.R;
import com.example.owner_pc.androidkanazawa2015.gnavi.ShopParameter;
import java.util.ArrayList;
/**
 * Created by atsusuke on 2016/02/01.
 */
public class List extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    View view;
    Bitmap image;
    Activity activity;
    private ArrayList<CustomData> objects;
    private ArrayList<ShopParameter> shopList = new ArrayList<ShopParameter>();
    private CustomAdapter customAdapter;
    private int size;
    private FragmentTopCallback mCallback;
    private CustomData item;
    private ListView listView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public interface FragmentTopCallback {
        void listCallback(ShopParameter shopParameter, boolean bool);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activityがコールバックを実装しているかチェック
        if (activity instanceof FragmentTopCallback == false) {
            throw new ClassCastException("activity が FragmentTopCallback を実装していません.");
        }
        //
        mCallback = (FragmentTopCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //リストの更新を定義
        createSwipeRefreshLayout();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Bundle bundle = getArguments();
        shopList = (ArrayList<ShopParameter>)bundle.getSerializable("ShopList");
        //Log.d("check", String.valueOf(shopCtrl.getShopList().get(away).shop.size()));
        listView = (ListView)view.findViewById(R.id.list);
        /* データの作成 */
        objects = new ArrayList<CustomData>();
        size = shopList.size();
        // todo 適切な画像を配置する
        image = BitmapFactory.decodeResource(getResources(), R.drawable.cir_g);
        for (int i = 0; i < size; i++){
            item = new CustomData();
            item.setImagaData(image);
            item.setTextData(shopList.get(i).getShopName());
//            shopParameter = shopList.get(i);
            objects.add(item);
            customAdapter = new CustomAdapter(activity, android.R.layout.simple_list_item_multiple_choice, objects,display);
            listView.setAdapter(customAdapter);
        }
        item = new CustomData();
        item.setImagaData(null);
        item.setTextData("Powered by ぐるなび");
        objects.add(item);
        customAdapter = new CustomAdapter(activity, android.R.layout.simple_list_item_multiple_choice, objects,display);
        listView.setAdapter(customAdapter);

        //リスト項目が選択された時のイベントを追加
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (size > position) {
                    customAdapter.notifyDataSetChanged();
                    ListView listView = (ListView) parent;
                    SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
                    //String msg = String.format("position:%d check:%b", position, checkedItemPositions.get(position));
                    //Log.d("position", String.valueOf(size));
                    //Log.d("position", msg);
                    if (checkedItemPositions.size() <=5) {
                        if (checkedItemPositions.get(position) == true) {
                            mCallback.listCallback(shopList.get(position), checkedItemPositions.get(position));
                        } else {
                            mCallback.listCallback(shopList.get(position), checkedItemPositions.get(position));
                            checkedItemPositions.delete(position);
                        }
                    }else {
                        checkedItemPositions.delete(position);
                        Toast.makeText(getActivity(), "5個以上選ぶのは贅沢だよ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 引っ張って更新するSwipeRefreshLayoutを作成
     */
    public void createSwipeRefreshLayout(){
        mSwipeRefreshLayout = (SwipeRefreshLayout)activity.findViewById(R.id.swipe_refresh_layout);
        //// TODO:適切な色を指定
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorRed,R.color.colorPrimaryDark,R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * 引っ張った時の処理
     */
    @Override
    public void onRefresh() {
        // TODO:再度位置情報を取得しリストを更新する。
        //解除
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        view = null;
        listView.setAdapter(null);
        customAdapter.clear();
        customAdapter = null;
        image = null;
        item.setImagaData(null);
        item.setTextData(null);
        objects.clear();
    }

}