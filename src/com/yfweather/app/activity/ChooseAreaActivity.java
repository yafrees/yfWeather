package com.yfweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.yfweather.app.R;
import com.yfweather.app.db.yfWeatherDB;
import com.yfweather.app.model.City;
import com.yfweather.app.model.County;
import com.yfweather.app.model.Province;
import com.yfweather.app.util.HttpCallbackListener;
import com.yfweather.app.util.HttpUtil;
import com.yfweather.app.util.Utility;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;

	private ArrayAdapter<String> adapter;
	private yfWeatherDB yfweatherDB;

	private List<String> dataList = new ArrayList<String>();

	//ʡ�б�
	private List<Province> provinceList;
	//���б�
	private List<City> cityList;
	//���б�
	private List<County> countyList;

	//ѡ�е�ʡ��
	private Province selectedProvince;
	//ѡ�еĳ���
	private City selectedCity;

	//��ǰѡ�еļ���
	private int currentLevel;

	//�Ƿ��WeatherActivity����ת����
	private boolean isFromWeatherActivity;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//�Ѿ�ѡ���˳����Ҳ��Ǵ�weatherAcrivity��ת�������Ż�ֱ����ת��weatherActivity

		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);

		yfweatherDB = yfWeatherDB.getInstance(this);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				}
				else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				}
				else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this , WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();//����ʡ������

	}

	/**************************************************************/
	/**************************************************************/

	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * */
	private void queryProvinces(){
		provinceList = yfweatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();

			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}
		else {
			queryFromServer(null , "province");
		}

	}

	/**
	 * ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * */
	private void queryCities(){

		cityList = yfweatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();

			for(City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}
		else {
			queryFromServer(selectedProvince.getProvinceCode() , "city");
		}

	}

	/**
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * */
	private void queryCounties(){

		countyList = yfweatherDB.loadCounty(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();

			for(County county : countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}
		else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}


	}


	/**************************************************************/
	/**************************************************************/

	/**
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯ��������
	 * */
	private void queryFromServer(final String code , final String type ){
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/lista/city" + code + ".xml";
			//address = "http://www.heweather.com/documents/cn-city-list" + code + ".xml";
		}
		else {
			address = "http://www.weather.com.cn/data/lista/city.xml";
			//address = "http://www.heweather.com/documents/cn-city-list.xml";
		}
		showProgressDialog();

		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;

				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(yfweatherDB, response);
				}
				else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(yfweatherDB, response, selectedProvince.getId());
				}
				else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(yfweatherDB, response, selectedCity.getId());
				}

				if (result) {
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							}
							else if ("city".equals(type)) {
								queryCities();
							}
							else if ("county".equals(type)) {
								queryCounties();
							}

						}
					});
				}

			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_LONG)
						.show();
					}
				});
			}
		});


	}




	/**************************************************************/
	/**************************************************************/

	/**
	 * ��ʾ���ȶԻ���
	 * */
	private void showProgressDialog(){
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * �رս��ȶԻ���
	 * */
	private void closeProgressDialog(){
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**************************************************************/
	/**************************************************************/


	/**
	 * ����back�������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б���ʡ�б�����ֵ����˳�
	 * */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		}
		else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		}
		else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}