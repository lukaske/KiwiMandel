package n.com.kiwimandel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.random.Random

class FlightManager(private val activityContext: Context) {

    private var api_data:JSONArray
    var listener: (()->Unit)? = null
    val todaysDate = todaysDate(3)
    val isStoredFromStart:Boolean


    init{
        Log.e("jabuk", "FlightManager initialized.")
        log("Travel date is: " + todaysDate)
        isStoredFromStart = isStoredAlready(todaysDate)
        api_data = JSONArray()
    }

    fun dataRun(){
        if(isStoredFromStart){
            api_data = JSONArray(prefsRetrieve(todaysDate))
            log("Retireved prefs: $api_data")
            listener?.invoke()
        }
        else{
            prefsClear()
            api_data = callAPI(45, todaysDate)
        }
    }

    fun log(msg:String) = Log.e("jabuk",msg)

    fun callAPI(offerNumber: Int, date: String):JSONArray {
        val queue = Volley.newRequestQueue(activityContext)
        val url = urlBuilder(date, date, offerNumber)
        var r_data = JSONArray()
        log(url)
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->

                log(response.toString())
                try {
                    val data = JSONObject(response).getJSONArray("data")
                    val knownDests = arrayOfNulls<String>(5)
                    var i = 0
                    while (i < data.length() && r_data.length()<5){
                        val rand = Random.nextInt(0,data.length()-1)
                        val item = data.get(rand)
                        val dest = JSONObject(item.toString()).getString("mapIdto")

                        if (dest !in knownDests){
                            log(data.length().toString())
                            r_data.put(item)
                            data.remove(rand)
                            log(dest)
                        }
                        i++
                    }

                    log("Selected ${r_data.length()} random flights.")
                    log(r_data.toString())
                    api_data = r_data
                    prefsStore(date, r_data.toString())
                    listener?.invoke()


                } catch (t: Throwable) {
                    log("Couldn't parse json")
                    Toast.makeText(activityContext, "Unknown error", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error -> log(error.toString())
                Toast.makeText(activityContext, "Couldn't connect. Restart to retry.", Toast.LENGTH_SHORT).show()
            })

        queue.add(stringRequest)

        return r_data
    }

    fun urlBuilder(dateFrom:String, dateTo:String, offerNumber:Int): String {
        return "https://api.skypicker.com/flights?v=2&sort=popularity&asc=0&" +
                "locale=en&daysInDestinationFrom=&daysInDestinationTo=&affilid=&children=0&infants=0&" +
                "flyFrom=49.2-16.61-250km&to=anywhere&featureName=aggregateResults&" +
                "dateFrom=${dateFrom}&dateTo=${dateTo}" + "&typeFlight=oneway&returnFrom=&" +
                "returnTo=&one_per_date=0&oneforcity=1&wait_for_refresh=0&adults=1&limit=${offerNumber}&partner=1al0JAopv00u1hGApL5wgn0mSlO3WIsd"
    }

    fun todaysDate(offset:Int = 1):String{
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offset)
        return "${calendar.get(Calendar.DATE)}/${calendar.get(Calendar.MONTH)+1}/${calendar.get(Calendar.YEAR)}"
    }

    fun prefsStore(date:String, data:String){
        val sharedPref: SharedPreferences = activityContext.getSharedPreferences("flightdata", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(date,data)
        editor.commit()

        log(sharedPref.getString(date, "stringt value")!!)
    }

    fun isStoredAlready(date:String):Boolean{
        val sharedPref: SharedPreferences = activityContext.getSharedPreferences("flightdata", Context.MODE_PRIVATE)
        return sharedPref.contains(date)
    }

    fun prefsRetrieve(date:String):String{
        val sharedPref: SharedPreferences = activityContext.getSharedPreferences("flightdata", Context.MODE_PRIVATE)
        return sharedPref.getString(date, "string value")!!
    }

    fun prefsClear(){
        val sharedPref: SharedPreferences = activityContext.getSharedPreferences("flightdata", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.commit()
    }

    fun api_data():JSONArray {
        return api_data
    }

}