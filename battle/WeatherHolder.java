//: battle/WeatherHolder.java

package pokepon.battle;

import pokepon.enums.*;

/** A wrapper class that allows easy manipulation of Weather;
 * contains weather and weather duration, and provides getter and
 * setter methods for both.
 *
 * @author silverweed
 */

public class WeatherHolder {
	
	public WeatherHolder(Weather weather) {
		this.weather = weather;
	}

	public WeatherHolder(Weather weather,int count) {
		this.weather = weather;
		this.count = count;
	}

	public WeatherHolder(WeatherHolder wh) {
		weather = wh.get();
		count = wh.count;
	}
	
	@Override public String toString() {
		return (weather == null ? null : weather.toString());
	}

	public void set(Weather weather) {
		this.weather = weather;
	}

	public void set(WeatherHolder weather) {
		this.weather = weather.get();
		this.count = weather.count;
	}

	public void set(Weather weather,int count) {
		this.weather = weather;
		this.count = count;
	}

	public Weather get() {
		return weather;
	}

	public boolean equals(WeatherHolder w) {
		return weather != null && weather.equals(w.get()) && count == w.count;
	}

	public static final WeatherHolder getClearWeather() {
		return clearWeather;
	}

	/** Count can be manipulated directly (so you can do count++ or count--) */
	public int count;
	/** weather is accessed through get/set methods */
	private Weather weather;
	private final static WeatherHolder clearWeather = new WeatherHolder(Weather.CLEAR);
}
