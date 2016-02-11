package com.example.owner_pc.androidkanazawa2015;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import com.example.owner_pc.androidkanazawa2015.gnavi.AsyncTaskCallbacks;
import com.example.owner_pc.androidkanazawa2015.gnavi.GnaviCtrl;
import com.example.owner_pc.androidkanazawa2015.gnavi.Position;
import com.example.owner_pc.androidkanazawa2015.gnavi.ShopCtrl;
import com.example.owner_pc.androidkanazawa2015.gnavi.ShopParameter;
import com.example.owner_pc.androidkanazawa2015.list.List;

public class MainActivity extends AppCompatActivity implements AsyncTaskCallbacks, ViewPager.OnPageChangeListener
        , List.FragmentTopCallback, LocationListener {

    private GnaviCtrl gnaviCtrl = new GnaviCtrl(this, this);
    private SettingButton settingButton = new SettingButton(this);
    private LocationManager locationManager;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //位置情報の読み込み
        getLocation();
    }

    //ぐるナビ読み込み完了
    @Override
    public void onTaskFinished() {
        //TabとSwipeの読み込み
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainFragmentPagerAdapter(getSupportFragmentManager()
                , MainActivity.this, latitude, longitude, new ShopCtrl()));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.list_tab);
        tabLayout.getTabAt(1).setIcon(R.drawable.map_tab);
        tabLayout.getTabAt(2).setIcon(R.drawable.cir_gray);
        viewPager.addOnPageChangeListener(this);
        //ボタンの表示
        //settingButton.onDrawButton();
    }

    //ぐるナビ読み込み失敗
    @Override
    public void onTaskCancelled() {

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.v("ActivityMain", "onPause()");

        // "プログレスダイアログ表示（シンプル）" のスレッド、プログレスダイアログが存在する場合
        if (this.gnaviCtrl != null &&
                this.gnaviCtrl.progressDialog != null) {

            Log.v("DialogShowing", String.valueOf(this.gnaviCtrl.progressDialog.isShowing()));

            // プログレスダイアログ表示中の場合
            if (this.gnaviCtrl.progressDialog.isShowing()) {

                // プログレスダイアログを閉じる
                this.gnaviCtrl.progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        Log.d("MainActivity", "onPageScrolled() position="+position);
    }

    @Override
    public void onPageSelected(int position) {
        Log.d("MainActivity", "onPageSelected() position=" + position);
        ImageButton rightButton = settingButton.getRightButton();
        ImageButton leftButton = settingButton.getLeftButton();
        if (rightButton != null && leftButton != null) {
            if (position == 1) {
                //マップ画面時透過(灰色重ねる)
                rightButton.setEnabled(false);
                leftButton.setEnabled(false);
                rightButton.setAlpha(0.5f);
                leftButton.setAlpha(0.5f);
                rightButton.setColorFilter(0xaa808080);
                leftButton.setColorFilter(0xaa808080);
            } else {
                //リスト、ルーレット画面では通常表示
                rightButton.setEnabled(true);
                leftButton.setEnabled(true);
                rightButton.setAlpha(1.0f);
                leftButton.setAlpha(1.0f);
                rightButton.setColorFilter(BIND_NOT_FOREGROUND);
                leftButton.setColorFilter(BIND_NOT_FOREGROUND);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//        Log.d("MainActivity", "onPageScrollStateChanged() state="+state);
    }

    //位置情報の取得
    public void getLocation() {
        // LocationManagerを取得
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Criteriaオブジェクトを生成
        Criteria criteria = new Criteria();
        //位置情報の精度
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        //消費電力
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        //ロケーションプロバイダの取得
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 0);
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, this);
    }

    @Override
    public void listCallback(ShopParameter shopParameter, boolean bool) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("check", String.valueOf(location.getLatitude()));
        Log.d("check", String.valueOf(location.getLongitude()));
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        //位置情報取得を破棄
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        Position position = new Position(latitude, longitude);
        //ぐるナビの読み込み
        gnaviCtrl.execute(position);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
