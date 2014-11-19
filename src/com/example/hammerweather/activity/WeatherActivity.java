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
	 * ������ʾ������
	 */
	private TextView tv_cityName;
	/**
	 * ������ʾ����ʱ��
	 */
	private TextView tv_publish;
	/**
	 * ������ʾ����������Ϣ
	 */
	private TextView tv_weatherDesc;
	/**
	 * ������ʾ����1
	 */
	private TextView tv_temp1;
	/**
	 * ������ʾ����2
	 */
	private TextView tv_temp2;
	/**
	 * ������ʾ��ǰ����
	 */
	private TextView tv_currentDate;
	/**
	 * �л����а�ť
	 */
	private Button btn_switchCity;
	/**
	 * ����������ť
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
			tv_publish.setText("ͬ����...");
			ll_weatherInfo.setVisibility(View.INVISIBLE);
			tv_cityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// û���ؼ����룬ֱ����ʾ��������
			showWeather();
		}
		btn_refreshWeather.setOnClickListener(this);
		btn_switchCity.setOnClickListener(this);
	}

	/**
	 * ��Ӧ��ť�ĵ���¼�
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
			tv_publish.setText("ͬ����...");
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
	 * ��ѯ�ؼ���������Ӧ���������š�
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	
	/**
	 * ��ѯ������������Ӧ��������
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ��
	 */
	private void queryFromServer(String address, final String type) {
		// TODO Auto-generated method stub
		HttpUtil.sendHttpRequest(address, new HttpUtil.HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// �ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// ������������ص�������Ϣ
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
						tv_publish.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}
	/**
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager. getDefaultSharedPreferences(this);
		tv_cityName.setText( prefs.getString("city_name", ""));
		tv_temp1.setText(prefs.getString("temp1", ""));
		tv_temp2.setText(prefs.getString("temp2", ""));
		tv_weatherDesc.setText(prefs.getString("weather_desp", ""));
		tv_publish.setText("����" + prefs.getString("publish_time", "") + "����");
		tv_currentDate.setText(prefs.getString("current_date", ""));
		ll_weatherInfo.setVisibility(View.VISIBLE);
		tv_cityName.setVisibility(View.VISIBLE);
	}
}
