package com.yfweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.yfweather.app.model.City;
import com.yfweather.app.model.County;
import com.yfweather.app.model.Province;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Sampler.Value;

public class yfWeatherDB {

	/**
	 * 数据库表名
	 * */
	public static final String DB_NAME = "yf_weather";

	/**
	 * 数据库版本
	 * */
	public static final int VERSION = 1;

	private static yfWeatherDB yfweatherdb;

	private SQLiteDatabase db;

	/**
	 * 构造方法私有化
	 * */
	private yfWeatherDB(Context context){
		yfWeatherOpenHelper dbHelper = new yfWeatherOpenHelper(context, DB_NAME, null, VERSION);

		db = dbHelper.getWritableDatabase();

	}

	/**
	 * 获取yfWeatherDB的实例
	 * */
	public synchronized static yfWeatherDB getInstance(Context context){

		if (yfweatherdb == null) {
			yfweatherdb = new yfWeatherDB(context);
		}
		return yfweatherdb;

	}

	/*************************************************************/
	/*************************************************************/

	/**
	 * 将province实例存储到数据库中
	 * */
	public void saveProvince(Province province){
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());

			db.insert("Province", null, values);
		}
	}

	/**
	 * 将city实例存储到数据库中
	 * */
	public void saveCity(City city){
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());

			values.put("province_id", city.getProvinceId());

			db.insert("City", null, values);
		}
	}

	/**
	 * 将county实例存储到数据库中
	 * */
	public void saveCounty(County county){
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountryName());
			values.put("county_code", county.getCountryCode());
			values.put("city_id", county.getCityId());

			db.insert("County", null, values);
		}
	}

	/*************************************************************/
	/*************************************************************/

	/**
	 * 
	 * 从数据库读取全国所有的省份信息
	 * */
	public List<Province> loadProvinces(){

		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));

				list.add(province);
			} while (cursor.moveToNext());
		}

		if (cursor != null) {
			cursor.close();
		}
		return list;

	}

	/**
	 * 
	 * 从数据库读取全国所有的city信息
	 * */
	public List<City> loadCities(int provinceId){

		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?", 
				new String []{String.valueOf(provinceId)}, 
				null, null, null);

		if (cursor.moveToFirst()) {
			do {
				City city = new City();

				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);

				list.add(city);

			} while (cursor.moveToNext());
		}

		if (cursor != null) {
			cursor.close();
		}
		return list;

	}



	/**
	 * 
	 * 从数据库读取全国所有的city信息
	 * */
	public List<County> loadCounty(int cityId){

		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?", 
				new String []{String.valueOf(cityId)}, 
				null, null, null);

		if (cursor.moveToFirst()) {
			do {
				County county = new County();

				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountryName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountryCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCityId(cityId);

				list.add(county);

			} while (cursor.moveToNext());
		}

		if (cursor != null) {
			cursor.close();
		}
		return list;

	}



}
