package com.example.hammerweather.activity;

import com.example.hammerweather.R;
import com.example.hammerweather.R.id;
import com.example.hammerweather.Utils.HttpUtil;
import com.example.hammerweather.Utils.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings("unused")
public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout ll_weatherInfo;
	/**
	 * 用于显示城市名
	 */
	private TextView tv_cityName;
	/**
	 * 用于显示发布时间
	 */
	private TextView tv_publish;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView tv_weatherDesc;
	/**
	 * 用于显示气温1
	 */
	private TextView tv_temp1;
	/**
	 * 用于显示气温2
	 */
	private TextView tv_temp2;
	/**
	 * 用于显示当前日期
	 */
	private TextView tv_currentDate;
	/**
	 * 切换城市按钮
	 */
	private Button btn_switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button btn_refreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);

		ll_weatherInfo = (LinearLayout) findViewById(R.id.ll_weather_info);

		tv_cityName = (TextView) findViewById(R.id.tv_city_name);
		tv_publish = (TextView) findViewById(R.id.tv_publish);
		tv_currentDate = (TextView) findViewById(R.id.tv_current_date);
		tv_temp1 = (TextView) findViewById(R.id.tv_temp1);
		tv_temp2 = (TextView) findViewById(R.id.tv_temp2);
		tv_weatherDesc = (TextView) findViewById(R.id.tv_weather_desp);

		btn_refreshWeather = (Button) findViewById(R.id.btn_refresh_weather);
		btn_switchCity = (Button) findViewById(R.id.btn_switch_city);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			tv_publish.setText("同步中...");
			ll_weatherInfo.setVisibility(View.INVISIBLE);
			tv_cityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代码，直接显示本地天气
			showWeather();
		}
		btn_refreshWeather.setOnClickListener(this);
		btn_switchCity.setOnClickListener(this);
	}

	/**
	 * 相应按钮的点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.btn_refresh_weather:
			tv_publish.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 查询县级代号所对应的天气代号。
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	
	/**
	 * 查询天气代号所对应的天气。
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
	 */
	private void queryFromServer(String address, final String type) {
		// TODO Auto-generated method stub
		HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv_publish.setText("同步失败");
					}
				});
			}
		});
	}
	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager. getDefaultSharedPreferences(this);
		tv_cityName.setText( prefs.getString("city_name", ""));
		tv_temp1.setText(prefs.getString("temp1", ""));
		tv_temp2.setText(prefs.getString("temp2", ""));
		tv_weatherDesc.setText(prefs.getString("weather_desp", ""));
		tv_publish.setText("今天" + prefs.getString("publish_time", "") + "发布");
		tv_currentDate.setText(prefs.getString("current_date", ""));
		ll_weatherInfo.setVisibility(View.VISIBLE);
		tv_cityName.setVisibility(View.VISIBLE);
	}
}
